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
 *   Alexandre Montplaisir - Ported to the TmfStatistics provider
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics;

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfStateSystemBuildCompleted;
import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStatistics;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeRootFactory;

/**
 * Uses the state system to populate the statistics tree
 *
 * @author Mathieu Denis
 * @since 2.0
 */
public class StateSystemStatisticsViewer extends TmfStatisticsViewer {

    /**
     * Stores the requested time range.
     */
    protected TmfTimeRange fRequestedTimerange;

    /**
     * Handler for the state system build completed signal
     *
     * @param signal The signal that's received
     */
    @TmfSignalHandler
    public void stateSystemBuildCompleted(final TmfStateSystemBuildCompleted signal) {
        if (isListeningTo(signal.getTrace().getName()) && signal.getID().equals(TmfStatistics.STATE_ID)) {
            TmfExperiment experiment = TmfExperiment.getCurrentExperiment();
            requestData(experiment, experiment.getTimeRange());
            requestTimeRangeData(experiment, fRequestedTimerange);
        }
    }

    @Override
    protected void requestData(final TmfExperiment experiment, final TmfTimeRange timeRange) {
        final Thread thread = new Thread("Statistics view build") { //$NON-NLS-1$
            @Override
            public void run() {
                buildStatisticsTree(experiment, timeRange, true);
            }
        };
        thread.start();
    }

    @Override
    protected void requestTimeRangeData(final TmfExperiment experiment, final TmfTimeRange timeRange) {
        fRequestedTimerange = timeRange;

        final Thread thread = new Thread("Statistics view build") { //$NON-NLS-1$
            @Override
            public void run() {
                buildStatisticsTree(experiment, timeRange, false);
            }
        };
        thread.start();
    }

    /**
     * Requests all the data of the experiment to the state system which
     * contains information about the statistics.
     *
     * Since the viewer may be listening to multiple traces, it have to receive
     * the experiment rather than a single trace. The filtering is done with the
     * method {@link #isListeningTo(String trace)}.
     *
     * @param experiment
     *            The experiment for which a request must be done
     * @param timeRange
     *            The time range that will be requested to the state system
     * @param isGlobal
     *            Tells if the request is for the global event count or the
     *            partial one.
     */
    private void buildStatisticsTree(final TmfExperiment experiment, TmfTimeRange timeRange, boolean isGlobal) {
        final TmfStatisticsTreeNode statTree = TmfStatisticsTreeRootFactory.getStatTreeRoot(getTreeID());
        final AbsTmfStatisticsTree statsData = TmfStatisticsTreeRootFactory.getStatTree(getTreeID());
        if (statsData == null) {
            return;
        }

        synchronized (statsData) {
            if (isGlobal) {
                statTree.resetGlobalValue();
            } else {
                statTree.resetTimeRangeValue();
            }

            /*
             * Checks each trace in the experiment, since the viewer may be
             * listening to multiple traces.
             */
            for (final ITmfTrace trace : experiment.getTraces()) {
                if (!isListeningTo(trace.getName())) {
                    continue;
                }

                /* Retrieves the statistics object */
                final ITmfStatistics stats = trace.getStatistics();
                if (stats == null) {
                    /*
                     * The state system is not accessible yet for this trace.
                     * Try the next one.
                     */
                    continue;
                }

                /*
                 * Prepare the extraInfo object, which is needed by
                 * registerEvent() below
                 */
                final ITmfExtraEventInfo extraInfo = new ITmfExtraEventInfo() {
                    @Override
                    public String getTraceName() {
                        if (trace.getName() == null) {
                            return Messages.TmfStatisticsView_UnknownTraceName;
                        }
                        return trace.getName();
                    }
                };

                /*
                 * Do the query on the statistics backend, then put the
                 * information appropriately in the statsData.
                 */
                Map<String, Long> map = stats.getEventTypesInRange(timeRange.getStartTime(), timeRange.getEndTime());
                ITmfEvent event;
                ITmfEventType eventTypeObj;
                for (Map.Entry<String, Long> entry : map.entrySet()) {
                    eventTypeObj = new TmfEventType("Building the statistics view", entry.getKey(), null); //$NON-NLS-1$
                    event = new TmfEvent(trace, null, null, eventTypeObj, null, null);

                    if (isGlobal) {
                        statsData.registerEvent(event, extraInfo, entry.getValue().intValue());
                    } else {
                        statsData.registerEventInTimeRange(event, extraInfo, entry.getValue().intValue());
                    }
                }
                super.modelComplete(isGlobal);
            }
        }
    }
}
