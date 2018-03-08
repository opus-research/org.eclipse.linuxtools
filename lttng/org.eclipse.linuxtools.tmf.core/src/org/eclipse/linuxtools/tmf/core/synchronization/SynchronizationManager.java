/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.event.matching.*;

/**
 * This abstract manager class handles loading trace synchronization data or
 * otherwise their calculation.
 *
 * @author gbastien
 * @since 2.0
 */
public abstract class SynchronizationManager extends TmfComponent {

    /**
     * @param syncFile
     *            The target name of the synchronization file. If it exists, it
     *            will be opened, otherwise it will be created and data from
     *            this synchro run will be saved there
     * @param traces
     *            The list of traces to synchronize
     * @param doSync
     *            Whether to actually synchronize or just try opening a sync
     *            file
     * @return The synchronization object
     */
    public static SynchronizationAlgorithm synchronizeTraces(final File syncFile, final ITmfTrace[] traces, boolean doSync) {

        SynchronizationAlgorithm syncAlgo;
        if (doSync) {
            syncAlgo = synchronize(syncFile, traces, new SyncAlgorithmFullyIncremental());
        } else {
            syncAlgo = openExisting(syncFile);
            if (syncAlgo == null) {
                syncAlgo = new SyncAlgorithmFullyIncremental();
            }
        }
        return syncAlgo;
    }

    /**
     * @param syncFile
     *            The target name of the synchronization file. If it exists, it
     *            will be opened, otherwise it will be created and data from
     *            this synchro run will be saved there
     * @param traces
     *            The list of traces to synchronize
     * @param algo
     *            A synchronization algorithm object to determine the algorithm
     *            used to synchronization. This function makes sure the data in
     *            the file corresponds to the requested algorith. Otherwise, the
     *            synchronization is computed one more time.
     * @param doSync
     *            Whether to actually synchronize or just try opening a sync
     *            file
     * @return The synchronization object
     */
    public static SynchronizationAlgorithm synchronizeTraces(final File syncFile, final ITmfTrace[] traces, SynchronizationAlgorithm algo, boolean doSync) {

        SynchronizationAlgorithm syncAlgo;
        if (doSync) {
            syncAlgo = synchronize(syncFile, traces, algo);
        } else {
            syncAlgo = openExisting(syncFile);
            if (syncAlgo == null || (syncAlgo.getClass() != algo.getClass())) {
                if (algo != null) {
                    syncAlgo = algo;
                } else {
                    syncAlgo = new SyncAlgorithmFullyIncremental();
                }
            }
        }

        return syncAlgo;
    }

    private static SynchronizationAlgorithm openExisting(final File syncFile) {
        if ( (syncFile != null) && syncFile.exists()) {
            /* Load an existing history */
            try {
                SynchronizationBackend syncBackend = new SynchronizationBackend(syncFile);
                SynchronizationAlgorithm algo = syncBackend.openExistingSync();
                return algo;
            } catch (IOException e) {
                /*
                 * There was an error opening the existing file. Perhaps it was
                 * corrupted, perhaps it's an old version? We'll just
                 * fall-through and try to build a new one from scratch instead.
                 */
                e.printStackTrace();
            }
        }
        return null;
    }

    private static SynchronizationAlgorithm synchronize(final File syncFile, final ITmfTrace[] traces, SynchronizationAlgorithm syncAlgo) {
        ITmfEventMatching matching = traces[0].getMatchingClass(TmfEventMatching.MatchingType.NETWORK);
        matching.setTraces(traces);
        matching.setProcessingUnit(syncAlgo);
        matching.matchEvents();

        SynchronizationBackend syncBackend;
        try {
            syncBackend = new SynchronizationBackend(syncFile, false);
            syncBackend.saveSync(syncAlgo);
        } catch (IOException e) {
            /*
             * The file may not exist. Doesn't matter at this point we'll just
             * create it upon saving
             */
        }

        return syncAlgo;
    }

}
