/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.event;

import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * @author alvaro
 *
 */
public class LttngSyntheticEvent extends LttngEvent {

	public static final LttngSyntheticEvent NullEvent = new LttngSyntheticEvent();

	// ======================================================================+
	// Data
	// =======================================================================
	/**
	 * <p>
	 * BEFORE: Before the update to the state system
	 * </p>
	 * <p>
	 * UPDATE: Proceed to update the state system
	 * </p>
	 * <p>
	 * AFTER: After the update of the state system
	 * </p>
	 * <p>
	 * ACK: Acknowledge indicator for any of the previous sequences
	 * </p>
	 */
	public enum SequenceInd {
		STARTREQ, BEFORE, UPDATE, AFTER, ENDREQ
	};

	private SequenceInd sequence = SequenceInd.BEFORE;
	private LttngEvent baseEvent = null;
	private LttngTraceState fTraceModel = null;
	// ======================================================================+
	// Constructors
	// =======================================================================
	/**
	 * @param baseEvent
	 */
	public LttngSyntheticEvent(ITmfEvent baseEvent) {
		super(baseEvent);
		this.baseEvent = (LttngEvent) baseEvent;
	}

	/**
	 * @param parent
	 * @param timestamp
	 * @param source
	 * @param type
	 * @param content
	 * @param reference
	 * @param lttEvent
	 */
	public LttngSyntheticEvent(TmfTrace parent,
			LttngTimestamp timestamp, String source,
			LttngEventType type, LttngEventContent content,
			String reference, JniEvent lttEvent) {
		super(parent, timestamp, source, type, content, reference, lttEvent);
	}

	private LttngSyntheticEvent() {
		this(null, null, null, null, null, null, null);
	}

	// ======================================================================+
	// Methods
	// =======================================================================

	/**
	 * @return the sequence indicator
	 */
	public SequenceInd getSynType() {
		return sequence;
	}

	/**
	 * @param type
	 *            the sequence indicator to set
	 */
	public void setSequenceInd(SequenceInd sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the baseEvent
	 */
	public LttngEvent getBaseEvent() {
		return baseEvent;
	}

	/**
	 * @param traceModel
	 *            the trace state-data-model associated to this event
	 */
	public void setTraceModel(LttngTraceState traceModel) {
		this.fTraceModel = traceModel;
	}

	/**
	 * @return the traceModel
	 */
	public LttngTraceState getTraceModel() {
		return fTraceModel;
	}

	/**
	 * /* (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent#getTrace()
	 */
    @Override
   public TmfTrace getTrace() {
		if (baseEvent != null) {
			return (TmfTrace) baseEvent.getTrace();
		} else {
			return (TmfTrace) super.getTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.event.LttngEvent#setParentTrace(org.eclipse
	 * .linuxtools.tmf.trace.TmfTrace)
	 */
	@Override
	public void setParentTrace(TmfTrace parentTrace) {
		if (baseEvent != null) {
			baseEvent.setParentTrace(parentTrace);
		} else {
			super.setParentTrace(parentTrace);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.event.LttngEvent#getChannelName()
	 */
	@Override
	public String getChannelName() {
		if (baseEvent != null) {
			return baseEvent.getChannelName();
		} else {
			return super.getChannelName();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.event.LttngEvent#getCpuId()
	 */
	@Override
	public long getCpuId() {
		if (baseEvent != null) {
			return baseEvent.getCpuId();
		} else {
			return super.getCpuId();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.event.LttngEvent#getMarkerName()
	 */
	@Override
	public String getMarkerName() {
		if (baseEvent != null) {
			return baseEvent.getMarkerName();
		} else {
			return super.getMarkerName();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.event.LttngEvent#getContent()
	 */
	@Override
	public LttngEventContent getContent() {
		if (baseEvent != null) {
			return baseEvent.getContent();
		} else {
			return super.getContent();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.event.LttngEvent#setContent(org.eclipse.
	 * linuxtools.lttng.event.LttngEventContent)
	 */
	@Override
	public void setContent(LttngEventContent newContent) {
		if (baseEvent != null) {
			baseEvent.setContent(newContent);
		} else {
			super.setContent(newContent);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.event.LttngEvent#getType()
	 */
	@Override
	public LttngEventType getType() {
		if (baseEvent != null) {
			return baseEvent.getType();
		} else {
			return super.getType();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.event.LttngEvent#setType(org.eclipse.linuxtools
	 * .lttng.event.LttngEventType)
	 */
	@Override
	public void setType(LttngEventType newType) {
		if (baseEvent != null) {
			baseEvent.setType(newType);
		} else {
			super.setType(newType);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.linuxtools.lttng.event.LttngEvent#updateJniEventReference
	 * (org.eclipse.linuxtools.lttng.jni.JniEvent)
	 */
	@Override
	public void updateJniEventReference(JniEvent newJniEventReference) {
		if (baseEvent != null) {
			baseEvent.updateJniEventReference(newJniEventReference);
		} else {
			super.updateJniEventReference(newJniEventReference);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.event.LttngEvent#convertEventTmfToJni()
	 */
	@Override
	public JniEvent convertEventTmfToJni() {
		if (baseEvent != null) {
			return baseEvent.convertEventTmfToJni();
		} else {
			return super.convertEventTmfToJni();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.linuxtools.lttng.event.LttngEvent#toString()
	 */
	@Override
	public String toString() {
		if (baseEvent != null) {
			return baseEvent.toString();
		} else {
			return super.toString();
		}
	}

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((baseEvent == null) ? 0 : baseEvent.hashCode());
        result = prime * result + ((fTraceModel == null) ? 0 : fTraceModel.hashCode());
        result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
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
        if (!(obj instanceof LttngSyntheticEvent)) {
            return false;
        }
        LttngSyntheticEvent other = (LttngSyntheticEvent) obj;
        if (baseEvent == null) {
            if (other.baseEvent != null) {
                return false;
            }
        } else if (!baseEvent.equals(other.baseEvent)) {
            return false;
        }
        if (fTraceModel == null) {
            if (other.fTraceModel != null) {
                return false;
            }
        } else if (!fTraceModel.equals(other.fTraceModel)) {
            return false;
        }
        if (sequence != other.sequence) {
            return false;
        }
        return true;
    }

}
