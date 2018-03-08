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

import java.util.List;

/**
 * Class that does something with a match
 * This default implementation of the class just adds it to a list of matches
 *
 * TODO - Really implement this. It might be useful to split the matching pairs per trace number?
 *        Or use something else than a list to be able to filter the matches per trace, per time, etc?
 *        By default, include a 'match analysis' unit in this class to process the match, or leave it for children classes who need it?
 * @author gbastien
 */
public class TmfEventMatches {

    /**
     * The list of matches found
     */
    protected List<TmfEventDependency> fMatches;

    /**
     * Adds a match
     *
     * @param match The new match
     */
    public void addMatch(TmfEventDependency match) {
        fMatches.add(match);
    }

    /**
     * @return The matches list
     */
    public List<TmfEventDependency> getMatches() {
        return fMatches;
    }

}
