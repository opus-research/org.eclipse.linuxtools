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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.viewers.statistics;

import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.ui.Activator;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfStateSystemBuildCompleted;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfExtraEventInfo;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeRootFactory;
import org.eclipse.linuxtools.tmf.ui.views.statistics.Messages;

/**
 * Uses the state system to populate the statistics tree
 *
 * @author Mathieu Denis
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
        if (isListeningTo(signal.getTrace().getName())) {
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
                if (isListeningTo(trace.getName()) && trace instanceof CtfTmfTrace) {
                    /* Retrieves the state system querier */
                    final CtfTmfTrace ctfTrace = (CtfTmfTrace) trace;
                    final IStateSystemQuerier ssq = ctfTrace.getStateSystem();

                    if (ssq == null) {
                        /*
                         * The state system is not accessible yet for this
                         * trace. Try the next one.
                         */
                        continue;
                    }

                    /*
                     * Requests the state preceding the one beginning at the start
                     * time to include the event(s) that may have happened at the
                     * start time. The statistics states calculated in the state
                     * system exclude the events happening at the start time, but
                     * include those happening at the end time.
                     */
                    final long startTime = Math.max(ssq.getStartTime(), timeRange.getStartTime().getValue() - 1);
                    final long endTime;
                    if (isGlobal) {
                        /*
                         * Always use the state system end time for the global
                         * request, since the state system may already be built.
                         */
                        endTime = ssq.getCurrentEndTime();
                    } else {
                        endTime = Math.min(ssq.getCurrentEndTime(), timeRange.getEndTime().getValue());
                    }

                    /* Gets the full state at the requested start and end time. */
                    final List<ITmfStateInterval> startState, endState;
                    try {
                        startState = ssq.queryFullState(startTime);
                        endState = ssq.queryFullState(endTime);
                    } catch (TimeRangeException e) {
                        handleFailure(e, isGlobal);
                        return;
                    }

                    final ITmfExtraEventInfo extraInfo = new ITmfExtraEventInfo() {
                        @Override
                        public String getTraceName() {
                            if (trace.getName() == null) {
                                return Messages.TmfStatisticsView_UnknownTraceName;
                            }
                            return trace.getName();
                        }
                    };

                    int startNbEvents, endNbEvents, nbEvents;
                    ITmfEvent event;
                    ITmfEventType eventTypeObj;
                    /* Is this the beginning of the trace? */
                    final boolean isSSQStartTime = (startTime == ssq.getStartTime())? true : false;
                    /* Requests all event type quarks in the statistics branch. */
                    List<Integer> eventTypeQuarks = ssq.getQuarks(Attributes.STATISTICS, Attributes.EVENT_TYPES, "*"); //$NON-NLS-1$
                    for (int eventTypeQuark : eventTypeQuarks) {
                        try {
                            /* Gets start and end event type count. */
                            startNbEvents = startState.get(eventTypeQuark).getStateValue().unboxInt();
                            endNbEvents = endState.get(eventTypeQuark).getStateValue().unboxInt();
                            /*
                             * The default value for the statistics is 0, rather
                             * than the value -1 used by the state system for
                             * non-initialized state.
                             */
                            if (isSSQStartTime || startNbEvents == -1) {
                                startNbEvents = 0;
                            }
                            /*
                             * TODO remove temporary fix to work around
                             * a bug in the state system occurring for
                             * the last event at the trace end time.
                             */
                            if (endNbEvents < 0) {
                                /*
                                 * Request 1ns before the end of the trace and add
                                 * the last event to the count.
                                 */
                                endNbEvents = ssq.querySingleState(endTime - 1, eventTypeQuark).getStateValue().unboxInt() + 1;
                            } /* TODO end of work around */

                            if (endNbEvents < 0) {
                                /*
                                 * No event of this type happened during the requested
                                 * time range.
                                 */
                                endNbEvents = 0;
                            }
                            nbEvents = endNbEvents - startNbEvents;
                            /*
                             * Registers the event type and increment the value by
                             * the number of events.
                             */
                            eventTypeObj = new TmfEventType("Building the statistics view", ssq.getAttributeName(eventTypeQuark), null); //$NON-NLS-1$
                            event = new TmfEvent(trace, null, null, eventTypeObj, null, null);

                            if (isGlobal) {
                                statsData.registerEvent(event, extraInfo, nbEvents);
                            } else {
                                statsData.registerEventInTimeRange(event, extraInfo, nbEvents);
                            }
                        } catch (TimeRangeException e) {
                            handleFailure(e, isGlobal);
                            return;
                        } catch (StateValueTypeException e) {
                            handleFailure(e, isGlobal);
                            return;
                        } catch (AttributeNotFoundException e) {
                            handleFailure(e, isGlobal);
                            return;
                        }
                    }
                    super.modelComplete(isGlobal);
                }
            }
        }
    }

    private void handleFailure(Exception e, boolean isGlobal) {
        Activator.getDefault().logError("Request failed for StateSystemStatisticsViewer", e); //$NON-NLS-1$
        super.modelIncomplete(isGlobal);
    }
}
