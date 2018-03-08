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
 * Interface for matching trace events
 *
 * @since 2.0
 *
 */
public interface ITmfEventMatching {

    /**
     * Matches one event
     *
     * @param event
     *            The event to match
     * @param traceno
     *            The number of the trace this event belongs to
     *
     * @return A pair of event if a match was found, false otherwise
     */
    abstract TmfEventDependency matchEvent(ITmfEvent event, int traceno);

    /**
     * Method that start the process of matching events
     *
     * @return Whether the match was completed correctly or not
     */
    public boolean matchEvents();

    /**
     * Method that initializes any data structure for the event matching
     */
    public void initMatching();

    /**
     * TODO Was in lttv, necessary here too?
     */
    public void destroyMatching();

    /**
     * TODO Was in lttv, necessary here too? Maybe to save the matching data
     * somewhere to be reused later?
     */
    public void finalizeMatching();

    /**
     * Prints stats from the matching
     *
     * @return string of statistics
     *
     */
    public String printMatchingStats();

}
