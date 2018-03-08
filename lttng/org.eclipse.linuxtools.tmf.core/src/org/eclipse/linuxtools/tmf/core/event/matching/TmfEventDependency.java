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

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;

/**
 * @author gbastien
 * @since 2.0
 *
 */
public class TmfEventDependency {
    private ITmfEvent fSourceEvent;
    private ITmfEvent fDestEvent;

    /**
     * @param source  The source event of this dependency
     * @param destination  The destination event of this dependency
     */
    public TmfEventDependency(final ITmfEvent source, final ITmfEvent destination) {
        fSourceEvent = source;
        fDestEvent = destination;
    }

    /**
     * @return The source event
     */
    public ITmfEvent getSourceEvent() {
        return fSourceEvent;
    }
    /**
     * @param sourceEvent The source event to set
     */
    public void setSourceEvent(final ITmfEvent sourceEvent) {
        this.fSourceEvent = sourceEvent;
    }
    /**
     * @return the Destination event
     */
    public ITmfEvent getDestinationEvent() {
        return fDestEvent;
    }
    /**
     * @param destEvent the destination event to set
     */
    public void setDestinationEvent(final ITmfEvent destEvent) {
        this.fDestEvent = destEvent;
    }
}
