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

import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.BTree;
import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.BTreeCheckpointVisitor;
import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.FlatArray;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceIndex;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;

/**
 * A checkpoint index that uses a BTree to store and search checkpoints by time stamps.
 * It's possible to have the checkpoints time stamps in a different order than their checkpoint ranks.
 * Because of that, we use a separate structure FlatArray that is better suited for searching
 * by checkpoint rank (O(1)).
 *
 * @since 3.0
 */
public class TmfBTreeTraceIndex implements ITmfTraceIndex {

    private BTree fCheckpoints;
    private FlatArray fCheckpointRanks;

    private final int BTREE_DEGREE = 15;

    /**
     * Creates an index for the given trace
     *
     * @param trace the trace
     */
    public TmfBTreeTraceIndex(ITmfTrace trace) {
        fCheckpoints = createBTree(trace);
        fCheckpointRanks = createFlatArray(trace);

        // If one of the files is created from scratch, make sure we rebuild the other one too
        if (fCheckpoints.isCreatedFromScratch() != fCheckpointRanks.isCreatedFromScratch()) {
            fCheckpointRanks.delete();
            fCheckpoints.delete();
            fCheckpoints = createBTree(trace);
            fCheckpointRanks = createFlatArray(trace);
        }
    }

    private static FlatArray createFlatArray(ITmfTrace trace) {
        return new FlatArray(getIndexFile(trace, FlatArray.INDEX_FILE_NAME), trace);
    }

    private BTree createBTree(ITmfTrace trace) {
        return new BTree(BTREE_DEGREE, getIndexFile(trace, BTree.INDEX_FILE_NAME), trace);
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
        fCheckpointRanks.dispose();
    }

    @Override
    public void add(ITmfCheckpoint checkpoint) {
        checkpoint.setCheckpointRank(fCheckpoints.size());
        fCheckpoints.insert(checkpoint);
        fCheckpointRanks.insert(checkpoint);
        fCheckpoints.setSize(fCheckpoints.size() + 1);
    }

    @Override
    public ITmfCheckpoint get(int checkpoint) {
        return fCheckpointRanks.get(checkpoint);
    }

    @Override
    public int binarySearch(ITmfCheckpoint checkpoint) {
        BTreeCheckpointVisitor v = new BTreeCheckpointVisitor(checkpoint);
        fCheckpoints.accept(v);
        ITmfCheckpoint found = v.getCheckpoint();
        if (found == null) {
            return -1;
        }
        return found.getCheckpointRank();
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
        fCheckpointRanks.setIndexComplete();
    }

}
