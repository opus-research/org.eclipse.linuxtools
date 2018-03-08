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

package org.eclipse.linuxtools.tmf.core.synchronization;

import java.io.Serializable;
import java.util.Map;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatches;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Abstract class for synchronization algorithm
 *
 * @author gbastien
 * @since 2.0
 */
public abstract class SynchronizationAlgorithm extends TmfEventMatches implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3083906749528872196L;

    /**
     * Quality of the result obtained by the synchronization algorithm
     */
    public enum syncQuality {
        /**
         * Algorithm returned a result satisfying all hypothesis for the
         * algorithm
         */
        ACCURATE,
        /**
         * Best effort of the algorithm
         */
        APPROXIMATE,
        /**
         * There is communication only in one direction
         */
        INCOMPLETE,
        /**
         * No communication between two traces
         */
        ABSENT,
        /**
         *
         */
        FAIL
    }

    /**
     * Function called when a match is found
     *
     * @param match
     *            The match to process
     */
    @Override
    public void addMatch(TmfEventDependency match) {
        super.addMatch(match);
        processMatch(match);
    }

    /**
     * Function for synchronization algorithm to do something with the received
     * match
     *
     * @param match
     *            The match of events
     */
    protected abstract void processMatch(TmfEventDependency match);

    /**
     * @return A map of statistics for this algorithm
     */
    public abstract Map<String, Object> getStats();

    /**
     * Returns a timestamp transformation algorithm
     *
     * @param trace
     *            The trace to get the transform for
     * @return The timestamp transformation formula
     */
    public abstract ITmfTimestampTransform getTimestampTransform(ITmfTrace trace);

    /**
     * Returns a timestamp transformation algorithm
     *
     * @param name
     *            The name of the trace to get the transform for
     * @return The timestamp transformation formula
     */
    public abstract ITmfTimestampTransform getTimestampTransform(String name);

    /**
     * Returns whether a given trace has a synchronization formula that is not
     * identity
     *
     * @param name
     *            The name of the trace
     * @return true if trace has formula
     */
    public abstract boolean isTraceSynced(String name);

    /**
     * Rename a trace involved in this algorithm
     *
     * @param oldname
     *            Original name of the trace
     * @param newname
     *            New name of the trace
     */
    public abstract void renameTrace(String oldname, String newname);

}
