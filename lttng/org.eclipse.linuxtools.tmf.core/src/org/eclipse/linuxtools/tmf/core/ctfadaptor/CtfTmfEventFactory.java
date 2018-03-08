/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.ctf.core.CTFStrings;
import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfLostEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfLostEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * Factory for CtfTmfEvent's.
 *
 * This code was moved out of CtfTmfEvent to provide better separation between
 * the parsing/instantiation of events, and the usual TMF API implementations.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public final class CtfTmfEventFactory {

    /**
     * Don't let anyone instantiate this class.
     */
    private CtfTmfEventFactory() {}

    /**
     * Factory method to instantiate new {@link CtfTmfEvent}'s.
     *
     * @param eventDef
     *            CTF EventDefinition object corresponding to this trace event
     * @param fileName
     *            The path to the trace file
     * @param originTrace
     *            The trace from which this event originates
     * @return The newly-built CtfTmfEvent
     * @since 3.0
     */
    public static ITmfEvent createEvent(EventDefinition eventDef,
            String fileName, CtfTmfTrace originTrace) {

        /* Prepare what to pass to CtfTmfEvent's constructor */
        long ts = eventDef.getTimestamp();
        CtfTmfTimestamp timestamp = new CtfTmfTimestamp(
                originTrace.getCTFTrace().timestampCyclesToNanos(ts));

        int sourceCPU = eventDef.getCPU();

        ITmfEventField content = new TmfEventField(
                ITmfEventField.ROOT_FIELD_ID, null, parseFields(eventDef));

        String reference = fileName == null ? CtfTmfEvent.NO_STREAM : fileName;

        final String LOST_EVENT = "Lost event";//$NON-NLS-1$
        if (eventDef.getDeclaration().getName().equals(LOST_EVENT)) {
            IntegerDefinition nbLostEvents = (IntegerDefinition) eventDef.getFields().getDefinitions().get(CTFStrings.LOST_EVENTS_FIELD);
            IntegerDefinition duration = (IntegerDefinition) eventDef.getFields().getDefinitions().get(CTFStrings.LOST_EVENTS_DURATION);
            CtfTmfTimestamp timestampEnd = new CtfTmfTimestamp(
                    originTrace.getCTFTrace().timestampCyclesToNanos(ts) + duration.getValue());
            TmfEventType et = new TmfEventType(LOST_EVENT, LOST_EVENT, content);
            ITmfLostEvent lostEvent = new TmfLostEvent(originTrace, ITmfContext.UNKNOWN_RANK, timestamp, "", et, "", content, new TmfTimeRange(timestamp, timestampEnd), nbLostEvents.getValue()); //$NON-NLS-1$ //$NON-NLS-2$

            return lostEvent;
        }

        /* Construct and return the object */
        CtfTmfEvent event = new CtfTmfEvent(
                originTrace,
                ITmfContext.UNKNOWN_RANK,
                timestamp,
                content,
                reference,
                sourceCPU,
                eventDef.getDeclaration()
                );
        return event;
    }

    /* Singleton instance of a null event */
    private static CtfTmfEvent nullEvent = null;

    /**
     * Get an instance of a null event.
     *
     * @return An empty event
     */
    public static CtfTmfEvent getNullEvent() {
        if (nullEvent == null) {
            nullEvent = new CtfTmfEvent();
        }
        return nullEvent;
    }

    /**
     * Extract the field information from the structDefinition haze-inducing
     * mess, and put them into something ITmfEventField can cope with.
     */
    private static CtfTmfEventField[] parseFields(EventDefinition eventDef) {
        List<CtfTmfEventField> fields = new ArrayList<CtfTmfEventField>();

        StructDefinition structFields = eventDef.getFields();
        for (Map.Entry<String, Definition> entry : structFields.getDefinitions().entrySet()) {
            String curFieldName = entry.getKey();
            Definition curFieldDef = entry.getValue();
            CtfTmfEventField curField = CtfTmfEventField.parseField(curFieldDef, curFieldName);
            fields.add(curField);
        }

        /* Add context information as CtfTmfEventField */
        StructDefinition structContext = eventDef.getContext();
        if (structContext != null) {
            for (Map.Entry<String, Definition> entry : structContext.getDefinitions().entrySet()) {
                /* Prefix field name */
                String curContextName = CtfConstants.CONTEXT_FIELD_PREFIX + entry.getKey();
                Definition curContextDef = entry.getValue();
                CtfTmfEventField curContext = CtfTmfEventField.parseField(curContextDef, curContextName);
                fields.add(curContext);
            }
        }

        return fields.toArray(new CtfTmfEventField[fields.size()]);
    }
}
