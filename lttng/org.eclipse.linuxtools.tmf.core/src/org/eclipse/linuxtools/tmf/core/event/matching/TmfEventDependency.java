/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event.matching;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;

/**
 * @author gbastien
 *
 */
public class TmfEventDependency {
    private TmfEvent fSourceEvent;
    private TmfEvent fDestEvent;

    /**
     * @param source  The source event of this dependency
     * @param destination  The destination event of this dependency
     */
    public TmfEventDependency(final TmfEvent source, final TmfEvent destination) {
        fSourceEvent = source;
        fDestEvent = destination;
    }

    /**
     * @return The source event
     */
    public TmfEvent getfSourceEvent() {
        return fSourceEvent;
    }
    /**
     * @param fSourceEvent The source event to set
     */
    public void setfSourceEvent(final TmfEvent fSourceEvent) {
        this.fSourceEvent = fSourceEvent;
    }
    /**
     * @return the Destination event
     */
    public TmfEvent getfDestinationEvent() {
        return fDestEvent;
    }
    /**
     * @param fDestEvent the destination event to set
     */
    public void setfDestinationEvent(final TmfEvent fDestEvent) {
        this.fDestEvent = fDestEvent;
    }
}
