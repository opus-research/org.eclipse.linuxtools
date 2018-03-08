/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.parsers.custom;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTraceDefinition.OutputColumn;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Base event for custom text parsers.
 *
 * @author Patrick Tassé
 */
public class CustomEvent implements ITmfEvent {

    /** Default timestamp scale for text-parser events */
    public static final byte TIMESTAMP_SCALE = -3;

    /** Input format key */
    protected static final String TIMESTAMP_INPUT_FORMAT_KEY = "CE_TS_I_F"; //$NON-NLS-1$

    /** Empty message */
    protected static final String NO_MESSAGE = ""; //$NON-NLS-1$

    /** The trace to which this event belongs */
    protected CustomTraceDefinition fDefinition;

    /** The payload data of this event, <field name, value> */
    protected Map<String, String> fData;

    private TmfEventField[] fColumnData;

    // ------------------------------------------------------------------------
    // Fields for ITmfEvent
    // ------------------------------------------------------------------------

    private ITmfTimestamp fTimestamp;
    private ITmfTrace fTrace;
    private long fRank;
    private String fSource;
    private ITmfEventType fType;
    private ITmfEventField fContent;
    private String fReference;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Basic constructor.
     *
     * @param definition
     *            The trace definition to which this event belongs
     */
    public CustomEvent(CustomTraceDefinition definition) {
        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    /**
     * Build a new CustomEvent from an existing TmfEvent.
     *
     * @param definition
     *            The trace definition to which this event belongs
     * @param other
     *            The TmfEvent to copy
     */
    public CustomEvent(CustomTraceDefinition definition, ITmfEvent other) {
        this(definition,
             other.getTrace(),
             other.getTimestamp(),
             other.getSource(),
             other.getType(),
             other.getReference());

        fDefinition = definition;
        fData = new HashMap<String, String>();
    }

    /**
     * Full constructor
     *
     * @param definition
     *            Trace definition of this event
     * @param parentTrace
     *            Parent trace object
     * @param timestamp
     *            Timestamp of this event
     * @param source
     *            Source of the event
     * @param type
     *            Event type
     * @param reference
     *            Event reference
     */
    public CustomEvent(CustomTraceDefinition definition, ITmfTrace parentTrace,
            ITmfTimestamp timestamp, String source, ITmfEventType type,
            String reference) {
        fDefinition = definition;
        fTrace = parentTrace;
        fTimestamp = timestamp;
        fSource = source;
        fType = type;
        fReference = reference;
        fData = new HashMap<String, String>();
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    @Override
    public ITmfTimestamp getTimestamp() {
        if (fData != null) {
            processData();
        }
        return fTimestamp;
    }

    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public long getRank() {
        return fRank;
    }

    @Override
    public String getSource() {
        return fSource;
    }

    @Override
    public ITmfEventType getType() {
        return fType;
    }

    @Override
    public ITmfEventField getContent() {
        return fContent;
    }

    @Override
    public String getReference() {
        return fReference;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * Modify this event's timestamp.
     *
     * @param timestamp
     *            The new timestamp
     */
    public void setTimestamp(ITmfTimestamp timestamp) {
        fTimestamp = timestamp;
    }

    /**
     * Modify this event's content (fields).
     *
     * @param content
     *            The new event content
     */
    public void setContent(ITmfEventField content) {
        fContent = content;
    }

    /**
     * Modify this event's type
     *
     * @param type
     *            The new event type
     */
    public void setType(ITmfEventType type) {
        fType = type;
    }

    /**
     * @return The event fields
     */
    public TmfEventField[] extractItemFields() {
        if (fData != null) {
            processData();
        }
        return Arrays.copyOf(fColumnData, fColumnData.length);
    }

    private void processData() {
        String timeStampString = fData.get(CustomTraceDefinition.TAG_TIMESTAMP);
        String timeStampInputFormat = fData.get(TIMESTAMP_INPUT_FORMAT_KEY);
        Date date = null;
        if (timeStampInputFormat != null && timeStampString != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(timeStampInputFormat);
            try {
                date = dateFormat.parse(timeStampString);
                setTimestamp(new TmfTimestamp(date.getTime(), TIMESTAMP_SCALE));
            } catch (ParseException e) {
                setTimestamp(TmfTimestamp.ZERO);
            }
        } else {
            setTimestamp(TmfTimestamp.ZERO);
        }

        int i = 0;
        fColumnData = new TmfEventField[fDefinition.outputs.size()];
        for (OutputColumn outputColumn : fDefinition.outputs) {
            String value = fData.get(outputColumn.name);
            if (outputColumn.name.equals(CustomTraceDefinition.TAG_TIMESTAMP) && date != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(fDefinition.timeStampOutputFormat);
                fColumnData[i++] = new TmfEventField(outputColumn.name, dateFormat.format(date));
            } else {
                fColumnData[i++] = new TmfEventField(outputColumn.name, (value != null ? value : "")); //$NON-NLS-1$
            }
        }
        CustomEventContent content = (CustomEventContent) getContent();
        content.setFields(fColumnData);
        fData = null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fDefinition == null) ? 0 : fDefinition.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CustomEvent)) {
            return false;
        }
        CustomEvent other = (CustomEvent) obj;
        if (fDefinition == null) {
            if (other.fDefinition != null) {
                return false;
            }
        } else if (!fDefinition.equals(other.fDefinition)) {
            return false;
        }
        return true;
    }

}
