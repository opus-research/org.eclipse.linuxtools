/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * Immutable implementation of ITmfEvent.
 *
 * All the fields should be set at the constructor and not be touched again. If
 * you need to modify the object's fields after the constructor call, look at
 * extending {@link TmfMutableEvent} instead.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TmfEvent implements ITmfEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ITmfTrace fTrace;
    private final long fRank;
    private final ITmfTimestamp fTimestamp;
    private final String fSource;
    private final ITmfEventType fType;
    private final ITmfEventField fContent;
    private final String fReference;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Full constructor
     *
     * @param trace
     *            The parent trace
     * @param rank
     *            The event rank (in the trace)
     * @param timestamp
     *            The event timestamp
     * @param source
     *            The event source
     * @param type
     *            The event type
     * @param content
     *            The event content (payload)
     * @param reference
     *            The event reference
     */
    public TmfEvent(final ITmfTrace trace, final long rank,
            final ITmfTimestamp timestamp, final String source,
            final ITmfEventType type, final ITmfEventField content,
            final String reference) {
        fTrace = trace;
        fRank = rank;
        fTimestamp = timestamp;
        fSource = source;
        fType = type;
        fContent = content;
        fReference = reference;
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
        if (!(obj instanceof TmfEvent)) {
            return false;
        }
        final TmfEvent other = (TmfEvent) obj;
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
        return "TmfEvent [fTimestamp=" + fTimestamp + ", fTrace=" + fTrace + ", fRank=" + fRank
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
