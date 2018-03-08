/**********************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.loader;

import org.eclipse.linuxtools.tmf.core.uml2sd.ITmfAsyncSequenceDiagramEvent;
import org.eclipse.linuxtools.tmf.ui.views.uml2sd.core.AsyncMessage;

/**
 * <p>
 * Extends AsyncMessage class to provide additional information about the trace event.
 * </p>
 * 
 * @version 1.0
 * @author Bernd Hufmann
 */
public class TmfAsyncMessage extends AsyncMessage implements ITmfAsyncSequenceDiagramEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * A asynchronous sequence diagram event implementation
     */
    protected ITmfAsyncSequenceDiagramEvent fSdEvent;
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Standard constructor
     * 
     * @param sdEvent The asynchronous sequence diagram event implementation
     * @param eventOccurrence The event index
     */
    public TmfAsyncMessage(ITmfAsyncSequenceDiagramEvent sdEvent, int eventOccurrence) {
        this.fSdEvent = sdEvent;
        setEventOccurrence(eventOccurrence);
        setName(sdEvent.getName());
        setStartTime(sdEvent.getStartTime());
        setEndTime(sdEvent.getEndTime());
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.ust.examples.event.ISequenceDiagramEvent#getSender()
     */
    @Override
    public String getSender() {
        return fSdEvent.getSender();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.ust.examples.event.ISequenceDiagramEvent#getReceiver()
     */
    @Override
    public String getReceiver() {
        return fSdEvent.getReceiver();
    }
}