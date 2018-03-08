/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.indexer;

import java.io.File;

import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.FlatArray;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceIndex;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;

/**
 * A checkpoint index that uses a FlatArray to store and search checkpoints by time stamps and by checkpoint rank.
 * Note: This index alone will not work for traces that have events with time stamps that are out of order.
 *
 * @since 3.0
 */
public class TmfFlatArrayTraceIndex implements ITmfTraceIndex {

    private final FlatArray fCheckpoints;

    private static final String INDEX_FILE_NAME = FlatArray.INDEX_FILE_NAME;

    /**
     * Creates an index for the given trace
     *
     * @param trace the trace
     */
    public TmfFlatArrayTraceIndex(ITmfTrace trace) {
        fCheckpoints = new FlatArray(getIndexFile(trace, INDEX_FILE_NAME), (ITmfPersistentlyIndexable)trace);
    }

    private static File getIndexFile(ITmfTrace trace, String fileName) {
        String directory = TmfTraceManager.getSupplementaryFileDir(trace);
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdir();
        }
        return new File(directory + fileName);
    }

    @Override
    public void dispose() {
        fCheckpoints.dispose();
    }

    @Override
    public void insert(ITmfCheckpoint checkpoint) {
        fCheckpoints.insert(checkpoint);
    }

    @Override
    public ITmfCheckpoint get(long checkpoint) {
        return fCheckpoints.get(checkpoint);
    }

    @Override
    public long binarySearch(ITmfCheckpoint checkpoint) {
        return fCheckpoints.binarySearch(checkpoint);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return fCheckpoints.size();
    }

    @Override
    public boolean isCreatedFromScratch() {
        return fCheckpoints.isCreatedFromScratch();
    }

    @Override
    public void setTimeRange(TmfTimeRange timeRange) {
        fCheckpoints.setTimeRange(timeRange);
    }

    @Override
    public void setNbEvents(long nbEvents) {
        fCheckpoints.setNbEvents(nbEvents);
    }

    @Override
    public TmfTimeRange getTimeRange() {
        return fCheckpoints.getTimeRange();
    }

    @Override
    public long getNbEvents() {
        return fCheckpoints.getNbEvents();
    }

    @Override
    public void setIndexComplete() {
        fCheckpoints.setIndexComplete();
    }

}
