/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Alexandre Montplaisir - Initial API and implementation
 *  Bernd Hufmann - Refactored for new builder class
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.HistoryBuilder;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.StateSystem;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree.ThreadedHistoryTreeBackend;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.builder.FullHistoryBuilder;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.builder.FullHistoryOpener;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.builder.InMemoryHistoryBuilder;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.builder.NullHistoryBuilder;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.builder.PartialStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;

/**
 * This class handles loading or creating state history files for use in TMF's
 * generic state system.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public final class TmfStateSystemFactory extends TmfComponent {

    /** Size of the blocking queue to use when building a state history */
    private static final int QUEUE_SIZE = 10000;

    /** "static" class */
    private TmfStateSystemFactory() {}


    /**
     * Load the history file matching the target trace. If the file already
     * exists, it will be opened directly. If not, it will be created from
     * scratch. In the case the history has to be built, it's possible to block
     * the calling thread until construction is complete.
     *
     * @param htFile
     *            The target name of the history file we want to use. If it
     *            exists it will be opened. If it doesn't, a new file will be
     *            created with this name/path.
     * @param stateProvider
     *            The {@link ITmfStateProvider} to use for building the history
     *            file. It may be required even if we are opening an
     *            already-existing history (ie, for partial histories).
     * @param buildManually
     *            If false, the construction will wait for a signal before
     *            starting. If true, it will build everything right now and
     *            block the caller. It has no effect if the file already exists.
     * @return A IStateSystemQuerier handler to the state system, with which you
     *         can then run queries on the history.
     * @throws TmfTraceException
     *             If there was a problem reading or writing one of the files.
     *             See the contents of this exception for more info.
     * @since 2.0
     */
    @Deprecated
    public static ITmfStateSystem newFullHistory(File htFile,
            ITmfStateProvider stateProvider, boolean buildManually)
            throws TmfTraceException {
        IStateHistoryBackend htBackend;

        /* If the target file already exists, do not rebuild it uselessly */
        // TODO for now we assume it's complete. Might be a good idea to check
        // at least if its range matches the trace's range.
        if (htFile.exists()) {
           /* Load an existing history */
            final int version = (stateProvider == null) ?
                    ITmfStateProvider.IGNORE_PROVIDER_VERSION :
                    stateProvider.getVersion();
            try {
                htBackend = new HistoryTreeBackend(htFile, version);
                return HistoryBuilder.openExistingHistory(htBackend);
            } catch (IOException e) {
                /*
                 * There was an error opening the existing file. Perhaps it was
                 * corrupted, perhaps it's an old version? We'll just
                 * fall-through and try to build a new one from scratch instead.
                 */
            }
        }

        /* Create a new state history from scratch */
        HistoryBuilder builder;

        if (stateProvider == null) {
            return null;
        }

        try {
            htBackend = new ThreadedHistoryTreeBackend(htFile,
                    stateProvider.getStartTime(), stateProvider.getVersion(), QUEUE_SIZE);
            StateSystem ss = new StateSystem(htBackend);
            stateProvider.assignTargetStateSystem(ss);
            builder = new HistoryBuilder(stateProvider, ss, htBackend, buildManually);
        } catch (IOException e) {

            /*
             * If it fails here however, it means there was a problem writing to
             * the disk, so throw a real exception this time.
             */
            throw new TmfTraceException(e.toString(), e);
        }
        return builder.getStateSystemQuerier();
    }

    /**
     * Load the history file matching the target trace. If the file already
     * exists, it will be opened directly. If not, it will be created from
     * scratch. In the case the history has to be built, it's possible to block
     * the calling thread until construction is complete.
     *
     * @param htFile
     *            The target history file we want to use. If it
     *            exists it will be opened. If it doesn't, a new file will be
     *            created with this name/path.
     * @param stateProvider
     *            The {@link ITmfStateProvider} to use for building the history
     *            file. It may be required even if we are opening an
     *            already-existing history (ie, for partial histories).
     * @return A IStateSystemBuilder handler to the state system, with which you
     *         can then run queries on the history.
     * @throws TmfTraceException
     *             If there was a problem reading or writing one of the files.
     *             See the contents of this exception for more info.
     * @since 3.0
     */
    public static ITmfHistoryBuilder newFullHistory(File htFile,
            ITmfStateProvider stateProvider)
            throws TmfTraceException {

        ITmfHistoryBuilder builder = null;

        /* If the target file already exists, do not rebuild it uselessly */
        // TODO for now we assume it's complete. Might be a good idea to check
        // at least if its range matches the trace's range.
        if (htFile.exists()) {
           /* Load an existing history */
            final int version = (stateProvider == null) ?
                    ITmfStateProvider.IGNORE_PROVIDER_VERSION :
                    stateProvider.getVersion();
            try {
                builder = new FullHistoryOpener(htFile, version);
                return builder;
            } catch (IOException e) {
                /*
                 * There was an error opening the existing file. Perhaps it was
                 * corrupted, perhaps it's an old version? We'll just
                 * fall-through and try to build a new one from scratch instead.
                 */
            }
        }

        /* Create a new state history from scratch */
        if (stateProvider == null) {
            return null;
        }

        try {
            builder = new FullHistoryBuilder(stateProvider, htFile);
        } catch (IOException e) {
            /*
             * If it fails here however, it means there was a problem writing to
             * the disk, so throw a real exception this time.
             */
            throw new TmfTraceException(e.toString(), e);
        }
        return builder;
    }

    /**
     * Create a new state system using a null history back-end. This means that
     * no history intervals will be saved anywhere, and as such only
     * {@link ITmfStateSystem#queryOngoingState} will be available.
     *
     * This has to be built "manually" (which means you should call
     * stateProvider.processEvent() to update the ongoing state of the state system).
     *
     * @param stateProvider
     *            The state provider plugin to build the history
     * @return Reference to the history-less state system that got built
     * @since 2.0
     */
    public static ITmfHistoryBuilder newNullHistory(ITmfStateProvider stateProvider) {
        return new NullHistoryBuilder(stateProvider);
    }

    /**
     * Create a new state system using in-memory interval storage. This should
     * only be done for very small state system, and will be naturally limited
     * to 2^31 intervals.
     *
     * This will block the caller while the construction is ongoing.
     *
     * @param stateProvider
     *            The sstateProvider to use
     * @return Reference to the state system that just got built
     * @since 2.0
     */
    public static ITmfHistoryBuilder newInMemHistory(ITmfStateProvider stateProvider) {
        return new InMemoryHistoryBuilder(stateProvider);
    }

    /**
     * Create a new state system backed with a partial history. A partial
     * history is similar to a "full" one (which you get with
     * {@link #newFullHistory}), except that the file on disk is much smaller,
     * but queries are a bit slower.
     *
     * Also note that single-queries are implemented using a full-query
     * underneath, (which are much slower), so this might not be a good fit for
     * a use case where you have to do lots of single queries.
     *
     * @param htPartialFile
     *            The target file of the history. Since they are usually quick
     *            to build, it will overwrite any existing file, without trying
     *            to re-open it.
     * @param realStateProvider
     *            The state provider to use to build this history.
     * @return Reference to the newly constructed state system
     * @throws TmfTraceException
     *             If the history file could not be created
     * @since 2.0
     */
    public static ITmfHistoryBuilder newPartialHistory(File htPartialFile,
            ITmfStateProvider realStateProvider)
                    throws TmfTraceException {

        return new PartialStateSystemBuilder(realStateProvider, htPartialFile);
    }
}
