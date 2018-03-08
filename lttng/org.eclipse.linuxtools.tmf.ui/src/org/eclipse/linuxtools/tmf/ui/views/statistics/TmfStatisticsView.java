/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Generalized version based on LTTng
 *   Bernd Hufmann - Updated to use trace reference in TmfEvent and streaming
 *   Mathieu Denis - Generalization of the view to instantiate a viewer specific to a trace type
 *
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.viewers.ITmfViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewerFactory;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfExtraEventInfo;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfStatisticsViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeRootFactory;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.tabsview.TmfViewerFolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The generic Statistics View displays statistics for any kind of traces.
 *
 * It is implemented according to the MVC pattern. - The model is a
 * TmfStatisticsTreeNode built by the State Manager. - The view is built with a
 * TreeViewer. - The controller that keeps model and view synchronized is an
 * observer of the model.
 *
 * @version 2.0
 * @author Mathieu Denis
 */
public class TmfStatisticsView extends TmfView {

    /**
     * The ID correspond to the package in which this class is embedded
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.statistics"; //$NON-NLS-1$

    /**
     * The view name.
     */
    public static final String TMF_STATISTICS_VIEW = "StatisticsView"; //$NON-NLS-1$

    /**
     * Stores the multiple requests to the experiment
     * @since 2.0
     */
    protected final List<ITmfEventRequest> fRequests;

    /**
     * The viewer that builds the columns to show the statistics
     */
    private TmfViewerFolder fStatsViewers;

    /**
     * Stores a reference to the parent composite of this view
     */
    private Composite fParent;

    /**
     * Stores a reference to the selected experiment
     */
    private TmfExperiment fExperiment;

    /**
     * Flag to force request the data from trace
     */
    protected boolean fRequestData = false;

    /**
     * Default PAGE_SIZE for background requests
     */
    protected static final int PAGE_SIZE = 50000;

    /**
     * Update synchronization parameter (used for streaming): Update busy
     * indicator
     */
    protected boolean fStatisticsUpdateBusy = false;

    /**
     * Update synchronization parameter (used for streaming): Update pending
     * indicator
     */
    protected boolean fStatisticsUpdatePending = false;

    /**
     * Update synchronization parameter (used for streaming): Pending Update
     * time range
     */
    protected TmfTimeRange fStatisticsUpdateRange = null;

    /**
     * Update synchronization object.
     */
    protected final Object fStatisticsUpdateSyncObj = new Object();

    /**
     * Constructor of a statistics view.
     *
     * @param viewName The name to give to the view.
     */
    public TmfStatisticsView(String viewName) {
        super(viewName);
        fRequests = new LinkedList<ITmfEventRequest>();
    }

    /**
     * Default constructor.
     */
    public TmfStatisticsView() {
        this(TMF_STATISTICS_VIEW);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        fParent = parent;
        TmfExperiment currentExperiment = TmfExperiment.getCurrentExperiment();
        // Read current data if any available
        if (currentExperiment != null) {
            fRequestData = true;
            // Insert the statistics data into the tree
            TmfExperimentSelectedSignal signal = new TmfExperimentSelectedSignal(this, currentExperiment);
            experimentSelected(signal);
            return;
        }
        fStatsViewers = createStatisticsViewers();
        /*
         * Updates the experiment field only at the end because
         * experimentSelected signal verifies the old selected experiment to
         * avoid reloading the same trace.
         */
        fExperiment = currentExperiment;
    }

    /**
     * Handler called when an experiment is selected. Checks if the experiment
     * has changed and requests the selected experiment if it has not yet been
     * cached.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
        if (signal != null) {
            // Does not reload the same trace if already opened
            if (fExperiment == null
                    || signal.getExperiment().toString().compareTo(fExperiment.toString()) != 0) {
                /*
                 * Dispose the current viewer and adapt the new one to the trace
                 * type of the experiment selected
                 */
                if (fStatsViewers != null) {
                    fStatsViewers.dispose();
                }
                // Update the current experiment
                fExperiment = signal.getExperiment();
                fStatsViewers = createStatisticsViewers();
                fParent.layout();

                String experimentName;
                String treeID;
                for (ITmfViewer viewer : fStatsViewers.getViewers()) {
                    ITmfStatisticsViewer statViewer = (ITmfStatisticsViewer) viewer;
                    experimentName = fExperiment.getName();
                    treeID = statViewer.getTreeID(experimentName);

                    setInput(statViewer, treeID, fExperiment.getTraces());
                }

                if (fRequestData) {
                    requestData(fExperiment, fExperiment.getTimeRange());
                    fRequestData = false;
                }
            }
        }
    }

    /**
     * Handles the signal about disposal of the current experiment.
     *
     * @param signal
     *            The disposed signal
     */
    @TmfSignalHandler
    public void experimentDisposed(TmfExperimentDisposedSignal signal) {
        if (signal.getExperiment() != fExperiment) {
            return;
        }
        cancelOngoingRequests();
        fRequests.clear();
    }

    /**
     * Initialize the viewer with the information received.
     *
     * @param statViewer
     *            The statistics viewer for which the input will be set
     * @param treeID
     *            The unique ID of the tree that is returned by
     *            {@link TmfStatisticsViewer#getTreeID(String)}
     * @param traces
     *            The list of the traces to add in the tree.
     * @since 2.0
     */
    public void setInput(ITmfStatisticsViewer statViewer, String treeID, ITmfTrace[] traces) {
        if (TmfStatisticsTreeRootFactory.containsTreeRoot(treeID)) {
            // The experiment root is already present
            TmfStatisticsTreeNode experimentTreeNode = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);

            // check if there is partial data loaded in the experiment
            int numTraces = traces.length;
            int numNodeTraces = experimentTreeNode.getNbChildren();

            if (numTraces == numNodeTraces) {
                boolean same = true;
                /*
                 * Detect if the experiment contains the same traces as when
                 * previously selected
                 */
                for (int i = 0; i < numTraces; i++) {
                    String traceName = traces[i].getName();
                    if (!experimentTreeNode.containsChild(traceName)) {
                        same = false;
                        break;
                    }
                }

                if (same) {
                    // no need to reload data, all traces are already loaded
                    statViewer.setInput(experimentTreeNode);

                    resetUpdateSynchronization();

                    return;
                }
                experimentTreeNode.reset();
            }
        } else {
            TmfStatisticsTreeRootFactory.addStatsTreeRoot(treeID, statViewer.getStatisticData());
        }

        resetUpdateSynchronization();

        TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);

        // if the model has contents, clear to start over
        if (treeModelRoot.hasChildren()) {
            treeModelRoot.reset();
        }

        // set input to a clean data model
        statViewer.setInput(treeModelRoot);
    }

    /**
     * Refresh the view.
     *
     * @param viewer
     *            The viewer for which the content has been change
     * @param complete
     *            Should a pending update be sent afterwards or not
     * @since 2.0
     */
    public void modelInputChanged(ITmfStatisticsViewer viewer, boolean complete) {
        // Updates only the selected viewer
        if (viewer != null) {
            refreshViewer(viewer);
        }

        if (complete) {
            sendPendingUpdate();
        }
    }

    /**
     * Called when an experiment request has failed or has been cancelled.
     * Remove the data retrieved from the experiment from the statistics tree.
     *
     * @param viewer
     *            The viewer for which the content has been change
     * @since 2.0
     */
    public void modelIncomplete(ITmfStatisticsViewer viewer) {
        if (viewer == null) {
            return;
        }
        Object input = viewer.getInput();
        if (input != null && input instanceof TmfStatisticsTreeNode) {
            /*
             * The data from this experiment is invalid and shall be removed to
             * refresh upon next selection
             */
            String treeID = viewer.getTreeID(fExperiment.getName());
            TmfStatisticsTreeRootFactory.removeStatTreeRoot(treeID);

            // Reset synchronization information
            resetUpdateSynchronization();
            modelInputChanged(viewer, false);
        }
        viewer.waitCursor(false);
    }

    /**
     * Handles the signal about new experiment range.
     *
     * @param signal
     *            The experiment range updated signal
     */
    @TmfSignalHandler
    public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {
        TmfExperiment experiment = signal.getExperiment();
        // validate
        if (!experiment.equals(TmfExperiment.getCurrentExperiment())) {
            return;
        }

        requestData(experiment, signal.getRange());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (fStatsViewers != null) {
            fStatsViewers.dispose();
        }
        /*
         * Make sure there is no request running before removing the statistics
         * tree.
         */
        cancelOngoingRequests();
        // clean the model
        TmfStatisticsTreeRootFactory.removeAll();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        if (fStatsViewers != null) {
            fStatsViewers.setFocus();
        }
    }

    /**
     * Handles the experiment updated signal. This will detect new events in
     * case the indexing is not coalesced with a statistics request.
     *
     * @param signal
     *            The experiment updated signal
     *
     * @since 1.1
     */
    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        TmfExperiment experiment = signal.getExperiment();
        if (!experiment.equals(TmfExperiment.getCurrentExperiment())) {
            return;
        }

        int nbEvents = 0;
        ITmfStatisticsViewer globalViewer = (ITmfStatisticsViewer) fStatsViewers.getViewer("Global"); //$NON-NLS-1$
        TmfStatisticsTreeNode globalInput = (TmfStatisticsTreeNode) globalViewer.getInput();
        for (TmfStatisticsTreeNode node : globalInput.getChildren()) {
            nbEvents += (int) node.getValue().nbEvents;
        }

        /*
         * In the normal case, the statistics request is coalesced with indexing
         * and the number of events are the same, there is nothing to do. But if
         * it's not the case, trigger a new request to count the new events.
         */
        if (nbEvents < experiment.getNbEvents()) {
            requestData(experiment, experiment.getTimeRange());
        }
    }

    /**
     * Creates the statistics viewers for all traces in the experiment. Each
     * viewers are placed in a different tab and the first one is selected
     * automatically.
     *
     * It uses the extension point that defines the statistics viewer to build
     * from the trace type. If there is no viewer defined, it will use the
     * default viewer for the statistics. If the experiment is empty, there will
     * still be a global statistics viewer created.
     *
     * @return a folder viewer containing all the appropriate viewers
     * @since 2.0
     */
    protected TmfViewerFolder createStatisticsViewers() {
        // Default style for the tabs that will be created
        int defaultStyle = SWT.NONE;

        // Create the list of statistics viewer.
        TmfViewerFolder tabsView = new TmfViewerFolder(fParent);
        // The folder composite that will contain the tabs
        Composite folder = tabsView.getParentFolder();

        ITmfStatisticsViewer viewer = new TmfStatisticsViewer();
        if (fExperiment != null) {
            // Show the name of the experiment in the global tab
            viewer.init(folder, Messages.TmfStatisticsView_GlobalTabName + " - " + fExperiment.getName(), fExperiment); //$NON-NLS-1$
            tabsView.addTab(viewer, defaultStyle);

            String traceName;
            IResource traceResource;
            // Create a statistics viewer for each traces
            for (ITmfTrace trace : fExperiment.getTraces()) {
                traceName = trace.getName();
                traceResource = trace.getResource();
                viewer = TmfViewerFactory.getStatisticsViewer(traceResource);
                if (viewer == null) {
                    /*
                     * Creates a default statistics viewer if there is none
                     * defined for the trace type.
                     */
                    viewer = new TmfStatisticsViewer();
                }
                viewer.init(folder, traceName, trace);
                tabsView.addTab(viewer, defaultStyle);
            }
        } else {
            // Show the global tab
            viewer.init(folder, Messages.TmfStatisticsView_GlobalTabName, fExperiment);
            tabsView.addTab(viewer, defaultStyle);
        }
        // Make the global viewer visible
        tabsView.setSelection(0);
        return tabsView;
    }

    /**
     * Create a new thread to refresh the specified viewer
     *
     * @param viewer
     *            The viewer to refresh
     * @since 2.0
     */
    protected void refreshViewer(final ITmfStatisticsViewer viewer) {
        final Control viewerControl = viewer.getControl();
        // Ignore update if disposed
        if (viewerControl.isDisposed()) {
            return;
        }

        viewerControl.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!viewerControl.isDisposed()) {
                    viewer.refresh();
                }
            }
        });
    }

    /**
     * Perform the request for an experiment and populates the statistics tree
     * with events.
     *
     * @param experiment
     *            Experiment for which we need the statistics data.
     * @param timeRange
     *            to request
     */
    protected void requestData(final TmfExperiment experiment, TmfTimeRange timeRange) {
        if (experiment != null) {

            // Check if an update is already ongoing
            if (checkUpdateBusy(timeRange)) {
                return;
            }

            /*
             * Send a request for each statistics viewer to handle their data
             * differently based on their own implementation of the statistics
             * model.
             */
            int index;
            TmfEventRequest request;
            for (final ITmfViewer viewer : fStatsViewers.getViewers()) {
                // Cast with no verification, since the list of viewers is private
                final ITmfStatisticsViewer statsViewer = (ITmfStatisticsViewer) viewer;

                // Index of the first event to retrieve from the trace
                index = 0;
                for (TmfStatisticsTreeNode node : ((TmfStatisticsTreeNode) statsViewer.getInput()).getChildren()) {
                    index += (int) node.getValue().nbEvents;
                }

                // Preparation of the event request
                request = new TmfEventRequest(ITmfEvent.class, timeRange, index, TmfDataRequest.ALL_DATA, getIndexPageSize(), ExecutionType.BACKGROUND) {

                    private final AbsTmfStatisticsTree statisticsData = TmfStatisticsTreeRootFactory.getStatTree(statsViewer.getTreeID(experiment.getName()));

                    @Override
                    public void handleData(ITmfEvent data) {
                        super.handleData(data);
                        if (data != null) {
                            final String traceName = data.getTrace().getName();
                            ITmfExtraEventInfo extraInfo = new ITmfExtraEventInfo() {
                                @Override
                                public String getTraceName() {
                                    if (traceName == null) {
                                        return Messages.TmfStatisticsView_UnknownTraceName;
                                    }
                                    return traceName;
                                }
                            };
                            statisticsData.registerEvent(data, extraInfo);
                            statisticsData.increase(data, extraInfo, 1);
                            // Refresh View
                            if ((getNbRead() % statsViewer.getInputChangedRefresh()) == 0) {
                                modelInputChanged(statsViewer, false);
                            }
                        }
                    }

                    @Override
                    public void handleSuccess() {
                        super.handleSuccess();
                        modelInputChanged(statsViewer, true);
                        statsViewer.waitCursor(false);
                        super.handleCancel();
                    }

                    @Override
                    public void handleFailure() {
                        super.handleFailure();
                        modelIncomplete(statsViewer);
                    }

                    @Override
                    public void handleCancel() {
                        modelIncomplete(statsViewer);
                    }
                }; // End of the request creation

                // Sends the request through the trace/experiment concerned
                statsViewer.getTrace().sendRequest(request);
                statsViewer.waitCursor(true);
                fRequests.add(request);
            }
        }
    }

    /**
     * Return the size of the request when performing background request.
     *
     * @return the block size for background request.
     */
    protected int getIndexPageSize() {
        return PAGE_SIZE;
    }

    /**
     * Cancels every ongoing requests if there are not already completed
     * @since 2.0
     */
    protected void cancelOngoingRequests() {
        if (!fRequests.isEmpty()) {
            for (ITmfEventRequest request : fRequests) {
                if (!request.isCompleted()) {
                    request.cancel();
                }
            }
        }
    }

    /**
     * Reset update synchronization information
     */
    protected void resetUpdateSynchronization() {
        synchronized (fStatisticsUpdateSyncObj) {
            fStatisticsUpdateBusy = false;
            fStatisticsUpdatePending = false;
            fStatisticsUpdateRange = null;
        }
    }

    /**
     * Checks if statistic update is ongoing. If it is ongoing the new time
     * range is stored as pending
     *
     * @param timeRange
     *            - new time range
     * @return true if statistic update is ongoing else false
     */
    protected boolean checkUpdateBusy(TmfTimeRange timeRange) {
        synchronized (fStatisticsUpdateSyncObj) {
            if (fStatisticsUpdateBusy) {
                fStatisticsUpdatePending = true;
                if (fStatisticsUpdateRange == null
                        || timeRange.getEndTime().compareTo(fStatisticsUpdateRange.getEndTime()) > 0) {
                    fStatisticsUpdateRange = timeRange;
                }
                return true;
            }
            fStatisticsUpdateBusy = true;
            return false;
        }
    }

    /**
     * Sends pending request (if any)
     */
    protected void sendPendingUpdate() {
        synchronized (fStatisticsUpdateSyncObj) {
            fStatisticsUpdateBusy = false;
            if (fStatisticsUpdatePending) {
                fStatisticsUpdatePending = false;
                requestData(TmfExperiment.getCurrentExperiment(), fStatisticsUpdateRange);
                fStatisticsUpdateRange = null;
            }
        }
    }
}
