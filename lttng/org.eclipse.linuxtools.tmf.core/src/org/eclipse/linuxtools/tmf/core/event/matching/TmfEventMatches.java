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

import java.util.ArrayList;
import java.util.List;

/**
 * Class that does something with a match This default implementation of the
 * class just adds it to a list of matches
 *
 * TODO - Really implement this. It might be useful to split the matching pairs
 * per trace number? Or use something else than a list to be able to filter the
 * matches per trace, per time, etc? By default, include a 'match analysis' unit
 * in this class to process the match, or leave it for children classes who need
 * it?
 *
 * @author gbastien
 * @since 2.0
 */
public class TmfEventMatches {

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
     * Adds a match
     *
     * @param match
     *            The new match
     */
    public void addMatch(TmfEventDependency match) {
        fMatches.add(match);
    }

    /**
     * Returns the match at the specified index
     *
     * @param index
     *            The index of the match to get
     * @return The matches list
     */
    public TmfEventDependency getMatch(int index) {
        return fMatches.get(index);
    }

    /**
     * Counts the matches
     *
     * @return The number of matches found
     */
    public int countMatches() {
        return fMatches.size();
    }

}
