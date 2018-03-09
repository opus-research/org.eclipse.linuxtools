/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.ctf.core.trace;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;

import org.eclipse.linuxtools.ctf.core.CTFStrings;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SimpleDatatypeDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * CTF trace packet reader. Reads the events of a packet of a trace file.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StreamInputPacketReader implements IDefinitionScope, AutoCloseable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** BitBuffer used to read the trace file. */
    private final BitBuffer fBitBuffer;

    /** StreamInputReader that uses this StreamInputPacketReader. */
    private final StreamInputReader fStreamInputReader;

    /** Trace packet header. */
    private final StructDefinition fTracePacketHeaderDef;

    /** Stream packet context definition. */
    private final StructDefinition fStreamPacketContextDef;

    /** Stream event header definition. */
    private final StructDefinition fStreamEventHeaderDef;

    /** Stream event context definition. */
    private final StructDefinition fStreamEventContextDef;

    /** Reference to the index entry of the current packet. */
    private StreamInputPacketIndexEntry fCurrentPacket = null;

    /**
     * Last timestamp recorded.
     *
     * Needed to calculate the complete timestamp values for the events with
     * compact headers.
     */
    private long fLastTimestamp = 0;

    /** CPU id of current packet. */
    private int fCurrentCpu = 0;

    private int fLostEventsInThisPacket;

    private long fLostEventsDuration;

    private boolean fHasLost = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInputPacketReader.
     *
     * @param streamInputReader
     *            The StreamInputReader to which this packet reader belongs to.
     */
    public StreamInputPacketReader(StreamInputReader streamInputReader) {
        fStreamInputReader = streamInputReader;

        /* Set the BitBuffer's byte order. */
        fBitBuffer = new BitBuffer();
        fBitBuffer.setByteOrder(streamInputReader.getByteOrder());

        /* Create trace packet header definition. */
        final Stream currentStream = streamInputReader.getStreamInput().getStream();
        StructDeclaration tracePacketHeaderDecl = currentStream.getTrace().getPacketHeader();
        if (tracePacketHeaderDecl != null) {
            fTracePacketHeaderDef = tracePacketHeaderDecl.createDefinition(this, "trace.packet.header"); //$NON-NLS-1$
        } else {
            fTracePacketHeaderDef = null;
        }

        /* Create stream packet context definition. */
        StructDeclaration streamPacketContextDecl = currentStream.getPacketContextDecl();
        if (streamPacketContextDecl != null) {
            fStreamPacketContextDef = streamPacketContextDecl.createDefinition(this, "stream.packet.context"); //$NON-NLS-1$
        } else {
            fStreamPacketContextDef = null;
        }

        /* Create stream event header definition. */
        StructDeclaration streamEventHeaderDecl = currentStream.getEventHeaderDecl();
        if (streamEventHeaderDecl != null) {
            fStreamEventHeaderDef = streamEventHeaderDecl.createDefinition(this, "stream.event.header"); //$NON-NLS-1$
        } else {
            fStreamEventHeaderDef = null;
        }

        /* Create stream event context definition. */
        StructDeclaration streamEventContextDecl = currentStream.getEventContextDecl();
        if (streamEventContextDecl != null) {
            fStreamEventContextDef = streamEventContextDecl.createDefinition(this, "stream.event.context"); //$NON-NLS-1$
        } else {
            fStreamEventContextDef = null;
        }

        /* Create event definitions */
        Collection<IEventDeclaration> eventDecls = streamInputReader.getStreamInput().getStream().getEvents().values();

        for (IEventDeclaration event : eventDecls) {
            if (!streamInputReader.getEventDefinitions().containsKey(event.getId())) {
                EventDefinition eventDef = event.createDefinition(streamInputReader);
                streamInputReader.addEventDefinition(event.getId(), eventDef);
            }
        }
    }

    /**
     * Dispose the StreamInputPacketReader
     *
     * @since 3.0
     */
    @Override
    public void close() {
        fBitBuffer.setByteBuffer(null);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the current packet
     *
     * @return the current packet
     */
    StreamInputPacketIndexEntry getCurrentPacket() {
        return fCurrentPacket;
    }

    /**
     * Gets the steamPacketContext Definition
     *
     * @return steamPacketContext Definition
     */
    public StructDefinition getStreamPacketContextDef() {
        return fStreamPacketContextDef;
    }

    /**
     * Gets the stream's event context definition.
     *
     * @return The streamEventContext definition
     */
    public StructDefinition getStreamEventContextDef() {
        return fStreamEventContextDef;
    }

    /**
     * Gets the CPU (core) number
     *
     * @return the CPU (core) number
     */
    public int getCPU() {
        return fCurrentCpu;
    }

    @Override
    public String getPath() {
        return ""; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Changes the current packet to the given one.
     *
     * @param currentPacket
     *            The index entry of the packet to switch to.
     * @throws CTFReaderException
     *             If we get an error reading the packet
     */
    void setCurrentPacket(StreamInputPacketIndexEntry currentPacket) throws CTFReaderException {
        StreamInputPacketIndexEntry prevPacket = null;
        fCurrentPacket = currentPacket;

        if (fCurrentPacket != null) {
            /*
             * Change the map of the BitBuffer.
             */
            ByteBuffer bb = null;
            try {
                bb = fStreamInputReader.getStreamInput().getByteBufferAt(
                                fCurrentPacket.getOffsetBytes(),
                                (fCurrentPacket.getPacketSizeBits() + 7) / 8);
            } catch (IOException e) {
                throw new CTFReaderException(e.getMessage(), e);
            }

            fBitBuffer.setByteBuffer(bb);

            /*
             * Read trace packet header.
             */
            if (fTracePacketHeaderDef != null) {
                fTracePacketHeaderDef.read(fBitBuffer);
            }

            /*
             * Read stream packet context.
             */
            if (getStreamPacketContextDef() != null) {
                getStreamPacketContextDef().read(fBitBuffer);

                /* Read CPU ID */
                if (getCurrentPacket().getTarget() != null) {
                    fCurrentCpu = (int) getCurrentPacket().getTargetId();
                }

                /* Read number of lost events */
                fLostEventsInThisPacket = (int) getCurrentPacket().getLostEvents();
                if (fLostEventsInThisPacket != 0) {
                    fHasLost = true;
                    /*
                     * Compute the duration of the lost event time range. If the
                     * current packet is the first packet, duration will be set
                     * to 1.
                     */
                    long lostEventsStartTime;
                    int index = fStreamInputReader.getStreamInput().getIndex().getEntries().indexOf(currentPacket);
                    if (index == 0) {
                        lostEventsStartTime = currentPacket.getTimestampBegin() + 1;
                    } else {
                        prevPacket = fStreamInputReader.getStreamInput().getIndex().getEntries().get(index - 1);
                        lostEventsStartTime = prevPacket.getTimestampEnd();
                    }
                    fLostEventsDuration = Math.abs(lostEventsStartTime - currentPacket.getTimestampBegin());
                }
            }

            /*
             * Use the timestamp begin of the packet as the reference for the
             * timestamp reconstitution.
             */
            fLastTimestamp = currentPacket.getTimestampBegin();
        } else {
            fBitBuffer.setByteBuffer(null);

            fLastTimestamp = 0;
        }
    }

    /**
     * Returns whether it is possible to read any more events from this packet.
     *
     * @return True if it is possible to read any more events from this packet.
     */
    public boolean hasMoreEvents() {
        if (fCurrentPacket != null) {
            return fHasLost || (fBitBuffer.position() < fCurrentPacket.getContentSizeBits());
        }
        return false;
    }

    /**
     * Reads the next event of the packet into the right event definition.
     *
     * @return The event definition containing the event data that was just
     *         read.
     * @throws CTFReaderException
     *             If there was a problem reading the trace
     */
    public EventDefinition readNextEvent() throws CTFReaderException {
        /* Default values for those fields */
        long eventID = EventDeclaration.UNSET_EVENT_ID;
        long timestamp = 0;
        if (fHasLost) {
            fHasLost = false;
            EventDefinition eventDef = EventDeclaration.getLostEventDeclaration().createDefinition(fStreamInputReader);
            ((IntegerDefinition) eventDef.getFields().getDefinitions().get(CTFStrings.LOST_EVENTS_FIELD)).setValue(fLostEventsInThisPacket);
            ((IntegerDefinition) eventDef.getFields().getDefinitions().get(CTFStrings.LOST_EVENTS_DURATION)).setValue(fLostEventsDuration);
            eventDef.setTimestamp(fLastTimestamp);
            return eventDef;
        }

        final StructDefinition sehd = fStreamEventHeaderDef;
        final BitBuffer currentBitBuffer = fBitBuffer;
        final long posStart = currentBitBuffer.position();
        /* Read the stream event header. */
        if (sehd != null) {
            sehd.read(currentBitBuffer);

            /* Check for the event id. */
            Definition idDef = sehd.lookupDefinition("id"); //$NON-NLS-1$
            if (idDef instanceof SimpleDatatypeDefinition) {
                eventID = ((SimpleDatatypeDefinition) idDef).getIntegerValue();
            } else if (idDef != null) {
                throw new CTFReaderException("Incorrect event id : " + eventID); //$NON-NLS-1$
            }

            /*
             * Get the timestamp from the event header (may be overridden later
             * on)
             */
            IntegerDefinition timestampDef = sehd.lookupInteger("timestamp"); //$NON-NLS-1$
            if (timestampDef != null) {
                timestamp = calculateTimestamp(timestampDef);
            } // else timestamp remains 0

            /* Check for the variant v. */
            Definition variantDef = sehd.lookupDefinition("v"); //$NON-NLS-1$
            if (variantDef instanceof VariantDefinition) {

                /* Get the variant current field */
                StructDefinition variantCurrentField = (StructDefinition) ((VariantDefinition) variantDef).getCurrentField();

                /*
                 * Try to get the id field in the current field of the variant.
                 * If it is present, it overrides the previously read event id.
                 */
                Definition idIntegerDef = variantCurrentField.lookupDefinition("id"); //$NON-NLS-1$
                if (idIntegerDef instanceof IntegerDefinition) {
                    eventID = ((IntegerDefinition) idIntegerDef).getValue();
                }

                /*
                 * Get the timestamp. This would overwrite any previous
                 * timestamp definition
                 */
                Definition def = variantCurrentField.lookupDefinition("timestamp"); //$NON-NLS-1$
                if (def instanceof IntegerDefinition) {
                    timestamp = calculateTimestamp((IntegerDefinition) def);
                }
            }
        }

        /* Read the stream event context. */
        if (fStreamEventContextDef != null) {
            fStreamEventContextDef.read(currentBitBuffer);
        }

        /* Get the right event definition using the event id. */
        EventDefinition eventDef = fStreamInputReader.getEventDefinitions().get(eventID);
        if (eventDef == null) {
            throw new CTFReaderException("Incorrect event id : " + eventID); //$NON-NLS-1$
        }

        /* Read the event context. */
        if (eventDef.getEventContext() != null) {
            eventDef.getEventContext().read(currentBitBuffer);
        }

        /* Read the event fields. */
        if (eventDef.getFields() != null) {
            eventDef.getFields().read(currentBitBuffer);
        }

        /*
         * Set the event timestamp using the timestamp calculated by
         * updateTimestamp.
         */
        eventDef.setTimestamp(timestamp);

        if (posStart == currentBitBuffer.position()) {
            throw new CTFReaderException("Empty event not allowed, event: " + eventDef.getDeclaration().getName()); //$NON-NLS-1$
        }

        return eventDef;
    }

    /**
     * Calculates the timestamp value of the event, possibly using the timestamp
     * from the last event.
     *
     * @param timestampDef
     *            Integer definition of the timestamp.
     * @return The calculated timestamp value.
     */
    private long calculateTimestamp(IntegerDefinition timestampDef) {
        long newval;
        long majorasbitmask;
        int len = timestampDef.getDeclaration().getLength();

        /*
         * If the timestamp length is 64 bits, it is a full timestamp.
         */
        if (timestampDef.getDeclaration().getLength() == 64) {
            fLastTimestamp = timestampDef.getValue();
            return fLastTimestamp;
        }

        /*
         * Bit mask to keep / remove all old / new bits.
         */
        majorasbitmask = (1L << len) - 1;

        /*
         * If the new value is smaller than the corresponding bits of the last
         * timestamp, we assume an overflow of the compact representation.
         */
        newval = timestampDef.getValue();
        if (newval < (fLastTimestamp & majorasbitmask)) {
            newval = newval + (1L << len);
        }

        /* Keep only the high bits of the old value */
        fLastTimestamp = fLastTimestamp & ~majorasbitmask;

        /* Then add the low bits of the new value */
        fLastTimestamp = fLastTimestamp + newval;

        return fLastTimestamp;
    }

    @Override
    public Definition lookupDefinition(String lookupPath) {
        return null;
    }
}
