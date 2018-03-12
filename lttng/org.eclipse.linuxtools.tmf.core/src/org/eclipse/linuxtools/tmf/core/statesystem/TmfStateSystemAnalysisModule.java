/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Bernd Hufmann - Integrated history builder functionality
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.partial.PartialHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.partial.PartialStateSystem;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.statesystem.core.StateSystemFactory;
import org.eclipse.linuxtools.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.linuxtools.statesystem.core.backend.InMemoryBackend;
import org.eclipse.linuxtools.statesystem.core.backend.NullBackend;
import org.eclipse.linuxtools.statesystem.core.backend.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.statesystem.core.backend.historytree.ThreadedHistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;

/**
 * Abstract analysis module to generate a state system. It is a base class that
 * can be used as a shortcut by analysis who just need to build a single state
 * system with a state provider.
 *
 * Analysis implementing this class should only need to provide a state system
 * and optionally a backend (default to NULL) and, if required, a filename
 * (defaults to the analysis'ID)
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
@NonNullByDefault
public abstract class TmfStateSystemAnalysisModule extends TmfAbstractAnalysisModule
        implements ITmfAnalysisModuleWithStateSystems {

    private static final String EXTENSION = ".ht"; //$NON-NLS-1$

    private final CountDownLatch fInitialized = new CountDownLatch(1);

    @Nullable private ITmfStateSystemBuilder fStateSystem;
    @Nullable private ITmfStateProvider fStateProvider;
    @Nullable private IStateHistoryBackend fHtBackend;
    @Nullable private ITmfEventRequest fRequest;

    /**
     * State system backend types
     *
     * @author Geneviève Bastien
     */
    protected enum StateSystemBackendType {
        /** Full history in file */
        FULL,
        /** In memory state system */
        INMEM,
        /** Null history */
        NULL,
        /** State system backed with partial history */
        PARTIAL
    }


    /**
     * Retrieve a state system belonging to trace, by passing the ID of the
     * relevant analysis module.
     *
     * This will start the execution of the analysis module, and start the
     * construction of the state system, if needed.
     *
     * @param trace
     *            The trace for which you want the state system
     * @param moduleId
     *            The ID of the state system analysis module
     * @return The state system, or null if there was no match
     * @since 3.1
     */
    public static @Nullable ITmfStateSystem getStateSystem(ITmfTrace trace, String moduleId) {
        TmfStateSystemAnalysisModule module =
                trace.getAnalysisModuleOfClass(TmfStateSystemAnalysisModule.class, moduleId);
        if (module != null) {
            module.schedule();
            module.waitForInitialization();
            /*
             * FIXME If we keep a reference to "module", the compiler expects us to
             * close it. The Analysis Module's API should be reworked to not expose
             * these objects directly (utility classes instead?)
             */
            return module.getStateSystem();
        }
        return null;
    }

    /**
     * Get the state provider for this analysis module
     *
     * @return the state provider
     */
    protected abstract ITmfStateProvider createStateProvider();

    /**
     * Get the state system backend type used by this module
     *
     * @return The {@link StateSystemBackendType}
     */
    protected StateSystemBackendType getBackendType() {
        /* Using full history by default, sub-classes can override */
        return StateSystemBackendType.FULL;
    }

    /**
     * Get the supplementary file name where to save this state system. The
     * default is the ID of the analysis followed by the extension.
     *
     * @return The supplementary file name
     */
    protected String getSsFileName() {
        return getId() + EXTENSION;
    }

    /**
     * Get the state system generated by this analysis, or null if it is not yet
     * created.
     *
     * @return The state system
     */
    @Nullable
    public ITmfStateSystem getStateSystem() {
        return fStateSystem;
    }

    /**
     * Block the calling thread until the analysis module has been initialized.
     * After this method returns, {@link #getStateSystem()} should not return
     * null anymore.
     */
    public void waitForInitialization() {
        try {
            fInitialized.await();
        } catch (InterruptedException e) {}
    }

    // ------------------------------------------------------------------------
    // TmfAbstractAnalysisModule
    // ------------------------------------------------------------------------

    @Override
    protected boolean executeAnalysis(@Nullable final  IProgressMonitor monitor) {
        IProgressMonitor mon = (monitor == null ? new NullProgressMonitor() : monitor);
        final ITmfStateProvider provider = createStateProvider();

        String id = getId();

        /* FIXME: State systems should make use of the monitor, to be cancelled */
        try {
            /* Get the state system according to backend */
            StateSystemBackendType backend = getBackendType();
            String directory;
            File htFile;
            switch (backend) {
            case FULL:
                directory = TmfTraceManager.getSupplementaryFileDir(getTrace());
                htFile = new File(directory + getSsFileName());
                createFullHistory(id, provider, htFile);
                break;
            case PARTIAL:
                directory = TmfTraceManager.getSupplementaryFileDir(getTrace());
                htFile = new File(directory + getSsFileName());
                createPartialHistory(id, provider, htFile);
                break;
            case INMEM:
                createInMemoryHistory(id, provider);
                break;
            case NULL:
                createNullHistory(id, provider);
                break;
            default:
                break;
            }
        } catch (TmfTraceException e) {
            return false;
        }
        return !mon.isCanceled();
    }

    @Override
    protected void canceling() {
        ITmfEventRequest req = fRequest;
        if ((req != null) && (!req.isCompleted())) {
            req.cancel();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fStateSystem != null) {
            fStateSystem.dispose();
        }
    }

    // ------------------------------------------------------------------------
    // History creation methods
    // ------------------------------------------------------------------------

    /*
     * Load the history file matching the target trace. If the file already
     * exists, it will be opened directly. If not, it will be created from
     * scratch.
     */
    private void createFullHistory(String id, ITmfStateProvider provider, File htFile) throws TmfTraceException {

        /* If the target file already exists, do not rebuild it uselessly */
        // TODO for now we assume it's complete. Might be a good idea to check
        // at least if its range matches the trace's range.

        if (htFile.exists()) {
           /* Load an existing history */
            final int version = provider.getVersion();
            try {
                IStateHistoryBackend backend = new HistoryTreeBackend(htFile, version);
                fHtBackend = backend;
                fStateSystem = StateSystemFactory.newStateSystem(id, backend, false);
                fInitialized.countDown();
                return;
            } catch (IOException e) {
                /*
                 * There was an error opening the existing file. Perhaps it was
                 * corrupted, perhaps it's an old version? We'll just
                 * fall-through and try to build a new one from scratch instead.
                 */
            }
        }

        /* Size of the blocking queue to use when building a state history */
        final int QUEUE_SIZE = 10000;

        try {
            IStateHistoryBackend backend = new ThreadedHistoryTreeBackend(htFile,
                    provider.getStartTime(), provider.getVersion(), QUEUE_SIZE);
            fHtBackend = backend;
            fStateSystem = StateSystemFactory.newStateSystem(id, backend);
            provider.assignTargetStateSystem(fStateSystem);
            build(provider);
        } catch (IOException e) {
            /*
             * If it fails here however, it means there was a problem writing to
             * the disk, so throw a real exception this time.
             */
            throw new TmfTraceException(e.toString(), e);
        }
    }

    /*
     * Create a new state system backed with a partial history. A partial
     * history is similar to a "full" one (which you get with
     * {@link #newFullHistory}), except that the file on disk is much smaller,
     * but queries are a bit slower.
     *
     * Also note that single-queries are implemented using a full-query
     * underneath, (which are much slower), so this might not be a good fit for
     * a use case where you have to do lots of single queries.
     */
    private void createPartialHistory(String id, ITmfStateProvider provider, File htPartialFile)
            throws TmfTraceException {
        /*
         * The order of initializations is very tricky (but very important!)
         * here. We need to follow this pattern:
         * (1 is done before the call to this method)
         *
         * 1- Instantiate realStateProvider
         * 2- Instantiate realBackend
         * 3- Instantiate partialBackend, with prereqs:
         *  3a- Instantiate partialProvider, via realProvider.getNew()
         *  3b- Instantiate nullBackend (partialSS's backend)
         *  3c- Instantiate partialSS
         *  3d- partialProvider.assignSS(partialSS)
         * 4- Instantiate realSS
         * 5- partialSS.assignUpstream(realSS)
         * 6- realProvider.assignSS(realSS)
         * 7- Call HistoryBuilder(realProvider, realSS, partialBackend) to build the thing.
         */

        /* Size of the blocking queue to use when building a state history */
        final int QUEUE_SIZE = 10000;

        final long granularity = 50000;

        /* 2 */
        IStateHistoryBackend realBackend = null;
        try {
            realBackend = new ThreadedHistoryTreeBackend(htPartialFile,
                    provider.getStartTime(), provider.getVersion(), QUEUE_SIZE);
        } catch (IOException e) {
            throw new TmfTraceException(e.toString(), e);
        }

        /* 3a */
        ITmfStateProvider partialProvider = provider.getNewInstance();

        /* 3b-3c, constructor automatically uses a NullBackend */
        PartialStateSystem pss = new PartialStateSystem();

        /* 3d */
        partialProvider.assignTargetStateSystem(pss);

        /* 3 */
        IStateHistoryBackend partialBackend =
                new PartialHistoryBackend(partialProvider, pss, realBackend, granularity);

        /* 4 */
        @SuppressWarnings("restriction")
        org.eclipse.linuxtools.internal.statesystem.core.StateSystem realSS =
        (org.eclipse.linuxtools.internal.statesystem.core.StateSystem) StateSystemFactory.newStateSystem(id, partialBackend);

        /* 5 */
        pss.assignUpstream(realSS);

        /* 6 */
        provider.assignTargetStateSystem(realSS);

        /* 7 */
        fHtBackend = partialBackend;
        fStateSystem = realSS;

        build(provider);
    }

    /*
     * Create a new state system using a null history back-end. This means that
     * no history intervals will be saved anywhere, and as such only
     * {@link ITmfStateSystem#queryOngoingState} will be available.
     */
    private void createNullHistory(String id, ITmfStateProvider provider) {
        IStateHistoryBackend backend = new NullBackend();
        fHtBackend = backend;
        fStateSystem = StateSystemFactory.newStateSystem(id, backend);
        provider.assignTargetStateSystem(fStateSystem);
        build(provider);
    }

    /*
     * Create a new state system using in-memory interval storage. This should
     * only be done for very small state system, and will be naturally limited
     * to 2^31 intervals.
     */
    private void createInMemoryHistory(String id, ITmfStateProvider provider) {
        IStateHistoryBackend backend = new InMemoryBackend(provider.getStartTime());
        fHtBackend = backend;
        fStateSystem = StateSystemFactory.newStateSystem(id, backend);
        provider.assignTargetStateSystem(fStateSystem);
        build(provider);
    }

    private void disposeProvider(boolean deleteFiles) {
        ITmfStateProvider provider = fStateProvider;
        if (provider != null) {
            provider.dispose();
        }
        if (deleteFiles && (fHtBackend != null)) {
            fHtBackend.removeFiles();
        }
    }

    private void build(ITmfStateProvider provider) {
        if ((fStateSystem == null) || (fHtBackend == null)) {
            throw new IllegalArgumentException();
        }

        ITmfEventRequest request = fRequest;
        if ((request != null) && (!request.isCompleted())) {
            request.cancel();
        }

        request = new StateSystemEventRequest(provider);
        provider.getTrace().sendRequest(request);

        /*
         * Only now that we've actually started the build, we'll update the
         * class fields, so that they become visible for other callers.
         */
        fStateProvider = provider;
        fRequest = request;

        /*
         * The state system object is now created, we can consider this module
         * "initialized" (components can retrieve it and start doing queries).
         */
        fInitialized.countDown();

        /*
         * Block the executeAnalysis() construction is complete (so that the
         * progress monitor displays that it is running).
         */
        try {
             request.waitForCompletion();
        } catch (InterruptedException e) {
             e.printStackTrace();
        }
    }

    private class StateSystemEventRequest extends TmfEventRequest {
        private final ITmfStateProvider sci;
        private final ITmfTrace trace;

        public StateSystemEventRequest(ITmfStateProvider sp) {
            super(sp.getExpectedEventType(),
                    TmfTimeRange.ETERNITY,
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            this.sci = sp;

            // sci.getTrace() will eventually return a @NonNull
            @SuppressWarnings("null")
            @NonNull ITmfTrace tr = sci.getTrace();
            trace = tr;

        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == trace) {
                sci.processEvent(event);
            } else if (trace instanceof TmfExperiment) {
                /*
                 * If the request is for an experiment, check if the event is
                 * from one of the child trace
                 */
                for (ITmfTrace childTrace : ((TmfExperiment) trace).getTraces()) {
                    if (childTrace == event.getTrace()) {
                        sci.processEvent(event);
                    }
                }
            }
        }

        @Override
        public void handleSuccess() {
            super.handleSuccess();
            disposeProvider(false);
        }

        @Override
        public void handleCancel() {
            super.handleCancel();
            disposeProvider(true);
        }

        @Override
        public void handleFailure() {
            super.handleFailure();
            disposeProvider(true);
        }
    }

    // ------------------------------------------------------------------------
    // ITmfAnalysisModuleWithStateSystems
    // ------------------------------------------------------------------------

    @Override
    @Nullable
    public ITmfStateSystem getStateSystem(String id) {
        if (id.equals(getId())) {
            return fStateSystem;
        }
        return null;
    }

    @Override
    public Iterable<ITmfStateSystem> getStateSystems() {
        @SuppressWarnings("null")
        @NonNull Iterable<ITmfStateSystem> ret = Collections.singleton((ITmfStateSystem) fStateSystem);
        return ret;
    }
}
