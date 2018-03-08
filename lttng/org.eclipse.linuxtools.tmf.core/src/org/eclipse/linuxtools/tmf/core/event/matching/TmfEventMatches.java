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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class that does something with a match This default implementation of the
 * class just adds it to a list of matches
 *
 * @author gbastien
 * @since 2.0
 */
public class TmfEventMatches implements IMatchProcessingUnit {

    /**
     * The list of matches found
     */
    protected final List<TmfEventDependency> fMatches;

    /**
     * Constructor
     */
    public TmfEventMatches() {
        fMatches = new ArrayList<TmfEventDependency>();

    }

    /**
     * Adds and processes a match
     *
     * @param match
     *            The new match
     */
    @Override
    public void addMatch(TmfEventDependency match) {
        fMatches.add(match);
    }

    /**
     * Returns the match at the specified index
     *
     * @param index
     *            The index of the match to get
     * @return The match at index or null or not present
     */
    public TmfEventDependency getMatch(int index) {
        return fMatches.get(index);
    }

    /**
     * Counts the matches
     *
     * @return The number of matches
     */
    @Override
    public int countMatches() {
        return fMatches.size();
    }

    /**
     * Function called after all matching has been done, to do any post-match
     * treatment
     */
    @Override
    public void matchingEnded() {

    }

    /**
     * Converts to string
     * @return String representing this object
     */
    @Override
    public String toString() {
        return "TmfEventMatches [ Number of matches found: " + fMatches.size() + " ]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void init(ITmfTrace[] fTraces) {

    }

}
