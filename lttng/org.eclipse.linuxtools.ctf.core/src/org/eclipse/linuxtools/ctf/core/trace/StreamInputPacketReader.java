/*******************************************************************************
 * Copyright (c) 2011-2012 Ericsson, Ecole Polytechnique de Montreal and others
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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.linuxtools.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.SimpleDatatypeDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDefinition;
import org.eclipse.linuxtools.internal.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.internal.ctf.core.trace.Stream;
import org.eclipse.linuxtools.internal.ctf.core.trace.StreamInputPacketIndexEntry;

/**
 * CTF trace packet reader. Reads the events of a packet of a trace file.
 *
 * @version 1.0
 * @author Matthew Khouzam
 * @author Simon Marchi
 */
public class StreamInputPacketReader implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Reference to the index entry of the current packet.
     */
    private StreamInputPacketIndexEntry currentPacket = null;

    /**
     * BitBuffer used to read the trace file.
     */
    private final BitBuffer bitBuffer = new BitBuffer();

    /**
     * StreamInputReader that uses this StreamInputPacketReader.
     */
    private final StreamInputReader streamInputReader;

    /**
     * Last timestamp recorded.
     *
     * Needed to calculate the complete timestamp values for the events with
     * compact headers.
     */
    private long lastTimestamp = 0;

    /**
     * Trace packet header.
     */
    private StructDefinition tracePacketHeaderDef = null;

    /**
     * Stream packet context definition.
     */
    private StructDefinition streamPacketContextDef = null;

    /**
     * Stream event header definition.
     */
    private StructDefinition streamEventHeaderDef = null;

    /**
     * Stream event context definition.
     */
    private StructDefinition streamEventContextDef = null;

    /**
     * Maps event ID to event definitions.
     */
    private final HashMap<Long, EventDefinition> events;

    /**
     * CPU id of current packet.
     */
    private int currentCpu = 0;

    /**
     * number of lost events in this packet
     */
    private int lostEvents;

    private int lostSoFar;

    private int lostEventsInThisPacket;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInputPacketReader.
     *
     * @param streamInputReader
     *            The StreamInputReader to which this packet reader belongs to.
     */
    public StreamInputPacketReader(StreamInputReader streamInputReader) {
        this.streamInputReader = streamInputReader;

        /*
         * Set the BitBuffer's byte order.
         */
        getBitBuffer().setByteOrder(streamInputReader.getByteOrder());

        events = streamInputReader.getStreamInput().getStream().getTrace()
                .getEventDefs(streamInputReader.getStreamInput());
        /*
         * Create definitions needed to read the events.
         */
        createDefinitions();

        lostEvents = 0;
        lostSoFar = 0;
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * Gets the current packet
     *
     * @return the current packet
     */
    public StreamInputPacketIndexEntry getCurrentPacket() {
        return this.currentPacket;
    }

    /**
     * Gets the steamPacketContext Definition
     *
     * @return steamPacketContext Definition
     */
    public StructDefinition getStreamPacketContextDef() {
        return this.streamPacketContextDef;
    }

    /**
     * Gets the CPU (core) number
     *
     * @return the CPU (core) number
     */
    public int getCPU() {
        return this.currentCpu;
    }

    @Override
    public String getPath() {
        return ""; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Creates definitions needed to read events (stream-defined and
     * event-defined).
     */
    private void createDefinitions() {
        /*
         * Create trace packet header definition.
         */
        final Stream currentStream = getStreamInputReader().getStreamInput()
                .getStream();
        StructDeclaration tracePacketHeaderDecl = currentStream.getTrace()
                .getPacketHeader();
        if (tracePacketHeaderDecl != null) {
            setTracePacketHeaderDef(tracePacketHeaderDecl.createDefinition(
                    this, "trace.packet.header")); //$NON-NLS-1$
        }

        /*
         * Create stream packet context definition.
         */
        StructDeclaration streamPacketContextDecl = currentStream
                .getPacketContextDecl();
        if (streamPacketContextDecl != null) {
            setStreamPacketContextDef(streamPacketContextDecl.createDefinition(
                    this, "stream.packet.context")); //$NON-NLS-1$
        }

        /*
         * Create stream event header definition.
         */
        StructDeclaration streamEventHeaderDecl = currentStream
                .getEventHeaderDecl();
        if (streamEventHeaderDecl != null) {
            setStreamEventHeaderDef(streamEventHeaderDecl.createDefinition(
                    this, "stream.event.header")); //$NON-NLS-1$
        }

        /*
         * Create stream event context definition.
         */
        StructDeclaration streamEventContextDecl = currentStream
                .getEventContextDecl();
        if (streamEventContextDecl != null) {
            setStreamEventContextDef(streamEventContextDecl.createDefinition(
                    this, "stream.event.context")); //$NON-NLS-1$
        }

        createEventDefinitions();
    }

    /**
     * Creates definitions needed to read the event. (event-defined).
     */
    private void createEventDefinitions() {
        Collection<EventDeclaration> eventDecls = getStreamInputReader()
                .getStreamInput().getStream().getEvents().values();

        /*
         * Create definitions for each event.
         */
        for (EventDeclaration event : eventDecls) {
            if (!events.containsKey(event.getId())) {
                EventDefinition eventDef = event
                        .createDefinition(getStreamInputReader());
                events.put(event.getId(), eventDef);
            }
        }
    }

    /**
     * Changes the current packet to the given one.
     *
     * @param currentPacket
     *            The index entry of the packet to switch to.
     */
    public void setCurrentPacket(StreamInputPacketIndexEntry currentPacket) {
        this.currentPacket = currentPacket;

        if (this.currentPacket != null) {
            /*
             * Change the map of the BitBuffer.
             */
            MappedByteBuffer bb = null;
            try {
                bb = getStreamInputReader()
                        .getStreamInput()
                        .getFileChannel()
                        .map(MapMode.READ_ONLY,
                                this.currentPacket.getOffsetBytes(),
                                (this.currentPacket.getPacketSizeBits() + 7) / 8);
            } catch (IOException e) {
                /*
                 * The streamInputReader object is already allocated, so this
                 * shouldn't fail bar some very bad kernel or RAM errors...
                 */
                e.printStackTrace();
            }

            getBitBuffer().setByteBuffer(bb);

            /*
             * Read trace packet header.
             */
            if (getTracePacketHeaderDef() != null) {
                getTracePacketHeaderDef().read(getBitBuffer());
            }

            /*
             * Read stream packet context.
             */
            if (getStreamPacketContextDef() != null) {
                getStreamPacketContextDef().read(getBitBuffer());
                /*
                 * Read CPU ID
                 */

                if (this.getCurrentPacket().getTarget() != null) {
                    this.currentCpu = (int) this.getCurrentPacket()
                            .getTargetId();
                }
                /*
                 * Read number of lost events
                 */

                int totalLostEvents = (int) this.getCurrentPacket()
                        .getLostEvents();
                lostEventsInThisPacket = totalLostEvents - lostEvents;
                lostEvents = totalLostEvents;
                currentPacket.setLostEvents(lostEventsInThisPacket);
                lostSoFar = 0;

            }

            /*
             * Use the timestamp begin of the packet as the reference for the
             * timestamp reconstitution.
             */
            lastTimestamp = currentPacket.getTimestampBegin();
        } else {
            getBitBuffer().setByteBuffer(null);

            lastTimestamp = 0;
        }
    }

    /**
     * Returns whether it is possible to read any more events from this packet.
     *
     * @return True if it is possible to read any more events from this packet.
     */
    public boolean hasMoreEvents() {
        if (currentPacket != null) {
            return getBitBuffer().position() < currentPacket
                    .getContentSizeBits();
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
        /* WARNING: This is still LTTng-specific. */
        Long eventID = null;
        long timestamp = 0;

        if (lostEventsInThisPacket > lostSoFar) {
            EventDefinition eventDef = EventDeclaration
                    .getLostEventDeclaration().createDefinition(
                            streamInputReader);
            eventDef.setTimestamp(this.lastTimestamp);
            ++lostSoFar;
            return eventDef;
        }
        StructDefinition sehd = getStreamEventHeaderDef(); // acronym for a long
                                                           // variable name
        BitBuffer currentBitBuffer = getBitBuffer();
        /*
         * Read the stream event header.
         */

        if (sehd != null) {
            sehd.read(currentBitBuffer);

            /*
             * Check for an event id.
             */
            SimpleDatatypeDefinition idDef = (SimpleDatatypeDefinition) sehd
                    .lookupDefinition("id"); //$NON-NLS-1$
            IntegerDefinition timestampDef = sehd.lookupInteger("timestamp"); //$NON-NLS-1$
            eventID = idDef.getIntegerValue();

            /*
             * Check for the variant v.
             */
            VariantDefinition variantDef = (VariantDefinition) sehd
                    .lookupDefinition("v"); //$NON-NLS-1$
            if (variantDef != null) {

                /*
                 * Get the variant current field
                 */
                StructDefinition variantCurrentField = (StructDefinition) variantDef
                        .getCurrentField();

                /*
                 * Try to get the id field in the current field of the variant.
                 * If it is present, it overrides the previously read event id.
                 */
                IntegerDefinition idIntegerDef = (IntegerDefinition) variantCurrentField
                        .lookupDefinition("id"); //$NON-NLS-1$
                if (idIntegerDef != null) {
                    eventID = idIntegerDef.getValue();
                }
                /*
                 * Get the timestamp.
                 */
                timestampDef = (IntegerDefinition) variantCurrentField
                        .lookupDefinition("timestamp"); //$NON-NLS-1$

            }

            /*
             * Calculate the event timestamp.
             */
            timestamp = calculateTimestamp(timestampDef);
        }

        /*
         * Read the stream event context.
         */
        if (getStreamEventContextDef() != null) {
            getStreamEventContextDef().read(currentBitBuffer);
        }

        /*
         * Get the right event definition using the event id.
         */
        EventDefinition eventDef = events.get(eventID);
        if (eventDef == null) {
            throw new CTFReaderException("Incorrect event id : " + eventID); //$NON-NLS-1$
        }

        /*
         * Read the event context.
         */
        if (eventDef.getContext() != null) {
            eventDef.getContext().read(currentBitBuffer);
        }

        /*
         * Read the event fields.
         */
        if (eventDef.getFields() != null) {
            eventDef.getFields().read(currentBitBuffer);
        }

        /*
         * Set the event timestamp using the timestamp calculated by
         * updateTimestamp.
         */
        eventDef.setTimestamp(timestamp);

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
            lastTimestamp = timestampDef.getValue();
            return lastTimestamp;
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
        if (newval < (lastTimestamp & majorasbitmask)) {
            newval = newval + (1L << len);
        }

        /* Keep only the high bits of the old value */
        lastTimestamp = lastTimestamp & ~majorasbitmask;

        /* Then add the low bits of the new value */
        lastTimestamp = lastTimestamp + newval;

        return lastTimestamp;
    }

    @Override
    public Definition lookupDefinition(String lookupPath) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Gets the stream event context definition (see CTF specs)
     *
     * @return the definition of the stream event context (the form not the
     *         content)
     */
    public StructDefinition getStreamEventContextDef() {
        return this.streamEventContextDef;
    }

    /**
     * Sets the stream event context definition
     *
     * @param streamEventContextDef
     *            The stream event context definition
     */
    public void setStreamEventContextDef(StructDefinition streamEventContextDef) {
        this.streamEventContextDef = streamEventContextDef;
    }

    /**
     * Gets the stream event header definition
     *
     * @return the stream event header definition
     */
    public StructDefinition getStreamEventHeaderDef() {
        return this.streamEventHeaderDef;
    }

    /**
     * Sets the stream event header definition
     *
     * @param streamEventHeaderDef
     *            the stream event header definition
     */
    public void setStreamEventHeaderDef(StructDefinition streamEventHeaderDef) {
        this.streamEventHeaderDef = streamEventHeaderDef;
    }

    /**
     * Sets the stream packet context definition
     *
     * @param streamPacketContextDef
     *            the stream packet context definition
     */
    public void setStreamPacketContextDef(
            StructDefinition streamPacketContextDef) {
        this.streamPacketContextDef = streamPacketContextDef;
    }

    /**
     * Gets the trace packet header definition
     *
     * @return the trace packet header definition
     */
    public StructDefinition getTracePacketHeaderDef() {
        return this.tracePacketHeaderDef;
    }

    /**
     * Sets the trace packet header definition
     *
     * @param tracePacketHeaderDef
     *            the trace packet header definition
     */
    public void setTracePacketHeaderDef(StructDefinition tracePacketHeaderDef) {
        this.tracePacketHeaderDef = tracePacketHeaderDef;
    }

    /**
     * @return the parent stream input reader
     */
    public StreamInputReader getStreamInputReader() {
        return this.streamInputReader;
    }

    /**
     *
     * @return THe bit buffer that reads the file.
     */
    public BitBuffer getBitBuffer() {
        return bitBuffer;
    }
}
