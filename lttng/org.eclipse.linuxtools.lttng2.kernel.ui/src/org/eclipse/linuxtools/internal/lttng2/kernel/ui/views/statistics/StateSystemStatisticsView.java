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

import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfStateSystemBuildCompleted;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemQuerier;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.views.statistics.ITmfExtraEventInfo;
import org.eclipse.linuxtools.tmf.ui.views.statistics.Messages;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfStatisticsTreeRootFactory;

/**
 * Uses the state system to populate the statistics tree
 *
 * @author Mathieu Denis
 */
public class StateSystemStatisticsView extends TmfStatisticsView {

    /**
     * The ID correspond to the package in which this class is embedded
     */
    @SuppressWarnings("hiding")
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.ui.views.statistics"; //$NON-NLS-1$
    /**
     * Reference to the experiment currently selected.
     */
    private TmfExperiment<ITmfEvent> fExperimentSelected;

    /**
     * Default Constructor
     */
    public StateSystemStatisticsView() {
        super(ID);
    }

    @Override
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<? extends ITmfEvent> signal) {
        super.experimentSelected(signal);
        if (signal != null && signal.getExperiment() != null) {
            fExperimentSelected = (TmfExperiment<ITmfEvent>) signal.getExperiment();
            requestData(fExperimentSelected, fExperimentSelected.getTimeRange());
        }
    }

    /**
     * Handler for the state system build completed signal
     *
     * @param signal The signal that's received
     */
    @TmfSignalHandler
    public void stateSystemBuildCompleted(final TmfStateSystemBuildCompleted signal) {
        final TmfExperiment<ITmfEvent> selectedExperiment = fExperimentSelected;
        if (selectedExperiment == null) {
            return;
        }

        for (ITmfTrace<?> trace : selectedExperiment.getTraces()) {
            if (trace == signal.getTrace() && trace instanceof CtfKernelTrace) {
                requestData(selectedExperiment, selectedExperiment.getTimeRange());
            }
        }
    }

    @Override
    protected AbsTmfStatisticsTree getStatisticData() {
        return new StateSystemBaseStatisticsTree();
    }

    /**
     * Create a thread to send a request to populate the statistics model
     *
     * @param experiment The experiment for which a request must be done
     * @param timeRange The time range that will be requested to the state system
     */
    @Override
    protected void requestData(final TmfExperiment<?> experiment, final TmfTimeRange timeRange) {
        final Thread thread = new Thread("Statistics view build") { //$NON-NLS-1$
            @Override
            public void run() {
                buildStatisticsTree(experiment, timeRange);
            }
        };
        thread.start();
    }

    /**
     * Request all the data of the experiment to the state system which contains information about the statistics.
     *
     * This method is a re-implementation of the {@link TmfStatisticsView#requestData} to support the new statistics
     * state system.
     *
     * @param experiment The experiment for which a request must be done
     * @param timeRange The time range that will be requested to the state system
     */
    private void buildStatisticsTree(TmfExperiment<?> experiment, TmfTimeRange timeRange) {
        if (experiment != null) {
            TmfStatisticsTreeNode statTree = TmfStatisticsTreeRootFactory.getStatTreeRoot(getTreeID(experiment.getName()));
            AbsTmfStatisticsTree statsData = TmfStatisticsTreeRootFactory.getStatTree(getTreeID(experiment.getName()));
            synchronized (statsData) {
                statTree.reset();

                // For all the traces in the experiment, request their state system querier
                for (ITmfTrace<?> trace : experiment.getTraces()) {

                    if (trace instanceof CtfTmfTrace) {

                        CtfTmfTrace ctfTrace = (CtfTmfTrace) trace;
                        IStateSystemQuerier ssq = ctfTrace.getStateSystem();

                        long startTime = Math.max(ssq.getStartTime(), timeRange.getStartTime().getValue());
                        long endTime = ssq.getCurrentEndTime();

                        // Request all event type quarks in the statistics branch
                        List<Integer> eventTypeQuarks = ssq.getQuarks(Attributes.STATISTICS, Attributes.EVENT_TYPES,
                                "*"); //$NON-NLS-1$
                        String eventType;
                        int startNbEvents, endNbEvents, nbEvents;
                        for (int eventTypeQuark : eventTypeQuarks) {
                            // Get the name of the event to register it in the statistics model tree
                            eventType = ssq.getAttributeName(eventTypeQuark);
                            try {
                                // Get the number of events at the start and the end for the event type
                                startNbEvents = ssq.querySingleState(startTime, eventTypeQuark).getStateValue().unboxInt();
                                if (startNbEvents == -1) {
                                    startNbEvents = 0;
                                } else {
                                    // Fix to count the event occurring at the start time
                                    if (startTime == ssq.getStartTime()) {
                                        // The first event of the trace shall to be counted
                                        startNbEvents--;
                                    } else {
                                        // Request the nb of events 1ns before the start time to see if the event
                                        // occurred at the start time or not
                                        startNbEvents = ssq.querySingleState(startTime - 1, eventTypeQuark).getStateValue().unboxInt();
                                    }
                                }
                                endNbEvents = ssq.querySingleState(endTime, eventTypeQuark).getStateValue().unboxInt();
                                // TODO remove temporary fix to work around a bug in the state system occurring for
                                // the
                                // last event
                                if (endNbEvents < 0) {
                                    // Request 1ns before the end of the trace
                                    endNbEvents = ssq.querySingleState(endTime - 1, eventTypeQuark).getStateValue().unboxInt() + 1;
                                } // TODO end of work around
                                if (endNbEvents < 0) {
                                    endNbEvents = 0;
                                }
                                nbEvents = endNbEvents - startNbEvents;
                                // Register the event type and increment the value by the number of events
                                final String traceName = trace.getName();
                                ITmfExtraEventInfo extraInfo = new ITmfExtraEventInfo() {
                                    @Override
                                    public String getTraceName() {
                                        if (traceName == null) {
                                            return Messages.TmfStatisticsView_UnknownTraceName;
                                        }
                                        return traceName;
                                    }
                                };
                                ITmfEventType eventTypeObj = new TmfEventType(
                                        "Building the statistics view", eventType, null); //$NON-NLS-1$
                                ITmfEvent event = new TmfEvent(trace, null, null, eventTypeObj, null, null);
                                statsData.registerEvent(event, extraInfo);
                                statsData.increase(event, extraInfo, nbEvents);
                            } catch (AttributeNotFoundException e) {
                                e.printStackTrace();
                                return;
                            } catch (TimeRangeException e) {
                                e.printStackTrace();
                                return;
                            } catch (StateValueTypeException e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                }
                super.modelInputChanged(true);
            }
        }
    }
}
