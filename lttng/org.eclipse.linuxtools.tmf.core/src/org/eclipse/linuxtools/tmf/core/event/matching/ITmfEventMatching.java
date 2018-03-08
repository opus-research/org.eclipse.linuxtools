/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event.matching;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Interface for matching trace events
 *
 * @since 2.0
 * @author gbastien
 */
public interface ITmfEventMatching {

    /**
     * Method that start the process of matching events
     *
     * @return Whether the match was completed correctly or not
     */
    public boolean matchEvents();

    /**
     * Public setter to initialize the traces
     *
     * @param trace
     *            one trace
     */
    public void setTraces(ITmfTrace trace);

    /**
     * Public setter to initialize the traces
     *
     * @param traces
     *            The array of traces
     */
    public void setTraces(ITmfTrace[] traces);

    /**
     * Public setter to set the match processing unit
     *
     * @param tmfEventMatches
     *            the processing unit object
     */
    public void setProcessingUnit(IMatchProcessingUnit tmfEventMatches);

}
