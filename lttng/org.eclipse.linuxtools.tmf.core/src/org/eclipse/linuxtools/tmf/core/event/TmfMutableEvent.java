/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *   Alexandre Montplaisir - Renamed from the old TmfEvent
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * A basic implementation of ITmfEvent.
 *
 * As its name implies, TmfMutableEvent is mutable, so should be used for event
 * types where the object instantiation and the population of the fields have to
 * be done in separate steps.
 *
 * Still, the separate setters should only be used for the "preparation" of the
 * event, and should not be called afterwards. Randomly re-assigning fields
 * after the event has been sent to the framework is asking for trouble!
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfTimestamp
 * @see ITmfEventType
 * @see ITmfEventField
 * @see ITmfTrace
 * @since 2.0
 */
public abstract class TmfMutableEvent implements ITmfEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private ITmfTrace fTrace;
    private long fRank;
    private ITmfTimestamp fTimestamp;
    private String fSource;
    private ITmfEventType fType;
    private ITmfEventField fContent;
    private String fReference;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor. All fields have their default value (null) and the
     * event rank is set to TmfContext.UNKNOWN_RANK.
     */
    public TmfMutableEvent() {
        this(null, ITmfContext.UNKNOWN_RANK, null, null, null, null, null);
    }

    /**
     * Standard constructor. The event rank will be set to TmfContext.UNKNOWN_RANK.
     *
     * @param trace the parent trace
     * @param timestamp the event timestamp
     * @param source the event source
     * @param type the event type
     * @param content the event content (payload)
     * @param reference the event reference

     */
    public TmfMutableEvent(final ITmfTrace trace, final ITmfTimestamp timestamp, final String source,
            final ITmfEventType type, final ITmfEventField content, final String reference)
    {
        this(trace, ITmfContext.UNKNOWN_RANK, timestamp, source, type, content, reference);
    }

    /**
     * Full constructor
     *
     * @param trace the parent trace
     * @param rank the event rank (in the trace)
     * @param timestamp the event timestamp
     * @param source the event source
     * @param type the event type
     * @param content the event content (payload)
     * @param reference the event reference
     */
    public TmfMutableEvent(final ITmfTrace trace, final long rank, final ITmfTimestamp timestamp, final String source,
            final ITmfEventType type, final ITmfEventField content, final String reference)
    {
        fTrace = trace;
        fRank = rank;
        fTimestamp = timestamp;
        fSource = source;
        fType = type;
        fContent = content;
        fReference = reference;
    }

    /**
     * Copy constructor
     *
     * @param event the original event
     */
    public TmfMutableEvent(final ITmfEvent event) {
        if (event == null) {
            throw new IllegalArgumentException();
        }
        /*
         * The fields are shallow-copied, but fTrace is a singleton, and all the
         * others are immutable, so it's safe to do so.
         */
        fTrace = event.getTrace();
        fRank = event.getRank();
        fTimestamp = event.getTimestamp();
        fSource = event.getSource();
        fType = event.getType();
        fContent = event.getContent();
        fReference = event.getReference();
    }

    // ------------------------------------------------------------------------
    // ITmfEvent
    // ------------------------------------------------------------------------

    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public long getRank() {
        return fRank;
    }

    @Override
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
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
    // Convenience setters
    // ------------------------------------------------------------------------

    /**
     * @param trace the new event trace
     */
    protected void setTrace(final ITmfTrace trace) {
        fTrace = trace;
    }

    /**
     * @param rank the new event rank
     */
    protected void setRank(final long rank) {
        fRank = rank;
    }

    /**
     * @param timestamp the new event timestamp
     */
    protected void setTimestamp(final ITmfTimestamp timestamp) {
        fTimestamp = timestamp;
    }

    /**
     * @param source the new event source
     */
    protected void setSource(final String source) {
        fSource = source;
    }

    /**
     * @param type the new event type
     */
    protected void setType(final ITmfEventType type) {
        fType = type;
    }

    /**
     * @param content the event new content
     */
    protected void setContent(final ITmfEventField content) {
        fContent = content;
    }

    /**
     * @param reference the new event reference
     */
    protected void setReference(final String reference) {
        fReference = reference;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fTrace == null) ? 0 : fTrace.hashCode());
        result = prime * result + (int) (fRank ^ (fRank >>> 32));
        result = prime * result + ((fTimestamp == null) ? 0 : fTimestamp.hashCode());
        result = prime * result + ((fSource == null) ? 0 : fSource.hashCode());
        result = prime * result + ((fType == null) ? 0 : fType.hashCode());
        result = prime * result + ((fContent == null) ? 0 : fContent.hashCode());
        result = prime * result + ((fReference == null) ? 0 : fReference.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfMutableEvent)) {
            return false;
        }
        final TmfMutableEvent other = (TmfMutableEvent) obj;
        if (fTrace == null) {
            if (other.fTrace != null) {
                return false;
            }
        } else if (!fTrace.equals(other.fTrace)) {
            return false;
        }
        if (fRank != other.fRank) {
            return false;
        }
        if (fTimestamp == null) {
            if (other.fTimestamp != null) {
                return false;
            }
        } else if (!fTimestamp.equals(other.fTimestamp)) {
            return false;
        }
        if (fSource == null) {
            if (other.fSource != null) {
                return false;
            }
        } else if (!fSource.equals(other.fSource)) {
            return false;
        }
        if (fType == null) {
            if (other.fType != null) {
                return false;
            }
        } else if (!fType.equals(other.fType)) {
            return false;
        }
        if (fContent == null) {
            if (other.fContent != null) {
                return false;
            }
        } else if (!fContent.equals(other.fContent)) {
            return false;
        }
        if (fReference == null) {
            if (other.fReference != null) {
                return false;
            }
        } else if (!fReference.equals(other.fReference)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfMutableEvent [fTimestamp=" + fTimestamp + ", fTrace=" + fTrace + ", fRank=" + fRank
                + ", fSource=" + fSource + ", fType=" + fType + ", fContent=" + fContent
                + ", fReference=" + fReference + "]";
    }

    // ------------------------------------------------------------------------
    // IAdaptable
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new TmfEventPropertySource(this);
        }
        return null;
    }
}
