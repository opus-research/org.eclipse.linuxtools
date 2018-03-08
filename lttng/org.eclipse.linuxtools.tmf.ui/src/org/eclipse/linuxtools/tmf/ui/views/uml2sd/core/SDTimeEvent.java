/**********************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * Copyright (c) 2011, 2012 Ericsson.
 * 
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 * Bernd Hufmann - Updated for TMF
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.views.uml2sd.core;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * Class implementation of a sequence diagram time event. 
 * 
 * @version 1.0
 * @author sveyrier
 * 
 */
public class SDTimeEvent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The time stamp of the event
     */
    protected ITmfTimestamp fTimestamp;
    /**
     * The event index.
     */
    protected int fEvent;
    /**
     * The time range implementing node.
     */
    protected ITimeRange fNode;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * The default constructor.
     * 
     * @param time The time stamp of the event.
     * @param event The event index.
     * @param node The time range implementing node.
     */
    public SDTimeEvent(ITmfTimestamp time, int event, ITimeRange node) {
        fTimestamp = time;
        fEvent = event;
        fNode = node;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Returns the timestamp of the event.
     * 
     * @return the timestamp of the event.
     */
    public ITmfTimestamp getTime() {
        return fTimestamp;
    }

    /**
     * Returns the event index.
     * 
     * @return the event index.
     */
    public int getEvent() {
        return fEvent;
    }

    /**
     * Returns the time range implementing node.
     * 
     * @return the time range implementing node.
     */
    public ITimeRange getGraphNode() {
        return fNode;
    }

}
