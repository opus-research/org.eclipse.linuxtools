/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis (mathieu.denis@polymtl.ca) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.statistics;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.util.TmfFixedArray;
import org.eclipse.linuxtools.tmf.ui.views.statistics.ITmfExtraEventInfo;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseStatisticsTree;

/**
 * Extends the {@link TmfBaseStatisticsTree} to adapt the statistics view to the state system.
 *
 * @author Mathieu Denis
 * @see TmfBaseStatisticsTree
 */
public class StateSystemBaseStatisticsTree extends TmfBaseStatisticsTree {

    /**
     * Default constructor
     */
    public StateSystemBaseStatisticsTree() {
        super();
    }

    /**
     * Create the nodes in the statistics tree related to the event
     */
    @Override
    public void registerEvent(ITmfEvent event, ITmfExtraEventInfo extraInfo) {
        TmfFixedArray<String>[] paths = getNormalPaths(event, extraInfo);
        for (TmfFixedArray<String> path : paths) {
            getOrCreate(path);
        }

        paths = getTypePaths(event, extraInfo);
        for (TmfFixedArray<String> path : paths) {
            getOrCreate(path);
        }
    }

    /**
     * Increment <i>values</i> time the counter for the specified event
     */
    @Override
    public void increase(ITmfEvent event, ITmfExtraEventInfo extraInfo, int values) {
        TmfFixedArray<String>[] paths = getNormalPaths(event, extraInfo);
        for (TmfFixedArray<String> path : paths) {
            getOrCreate(path).getValue().nbEvents += values;
        }

        paths = getTypePaths(event, extraInfo);
        for (TmfFixedArray<String> path : paths) {
            getOrCreate(path).getValue().nbEvents += values;
        }
    }
}
