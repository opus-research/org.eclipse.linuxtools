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

package org.eclipse.linuxtools.internal.tmf.core.trace.indexer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * An array of checkpoints stored on disk. It is very efficient for searching
 * checkpoints by rank (O(1))
 */
public class FlatArray extends AbstractFileCheckpointCollection {
    /**
     * Typical FlatArray file name
     */
    public final static String INDEX_FILE_NAME = "checkpoint_flatarray.idx"; //$NON-NLS-1$
    private static final int VERSION = 1;

    // Cached values
    private int fCheckpointSize = 0;
    private ByteBuffer fByteBuffer;

    /**
     * Constructs a FlatArray for a given trace from scratch or from an existing
     * file. When the FlatArray is created from scratch, it is populated by
     * subsequent calls to {@link #insert}.
     *
     * @param file
     *            the file to use as the persistent storage
     * @param trace
     *            the trace
     */
    public FlatArray(File file, ITmfPersistentlyIndexable trace) {
        super(file, trace);

        fCheckpointSize = fTrace.getCheckpointSize();
        fByteBuffer = ByteBuffer.allocate(fCheckpointSize);
        fByteBuffer.clear();
    }

    /**
     * Insert a checkpoint into the file-backed array
     *
     * @param checkpoint
     *            the checkpoint to insert
     */
    @Override
    public void insert(ITmfCheckpoint checkpoint) {
        try {
            ++fHeader.fSize;
            fRandomAccessFile.seek(fRandomAccessFile.length());
            fByteBuffer.clear();
            checkpoint.serialize(fByteBuffer);
            fRandomAccessFile.write(fByteBuffer.array());
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.FlatArray_IOErrorWriting, fFile), e);
        }
    }

    /**
     * Get a checkpoint from a rank
     *
     * @param rank
     *            the rank to search
     * @return the checkpoint that has been found or null if not found
     */
    public ITmfCheckpoint get(long rank) {
        ITmfCheckpoint checkpoint = null;
        try {
            long pos = fHeader.getSize() + fCheckpointSize * rank;
            fRandomAccessFile.seek(pos);
            fByteBuffer.clear();
            fRandomAccessFile.read(fByteBuffer.array());
            ITmfLocation location = fTrace.restoreLocation(fByteBuffer);
            ITmfTimestamp timeStamp = new TmfTimestamp(fByteBuffer);
            checkpoint = new TmfCheckpoint(timeStamp, location, fByteBuffer);
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.FlatArray_IOErrorReading, fFile), e);
        }
        return checkpoint;
    }

    /**
     * Search for a checkpoint and return the rank.
     *
     * @param checkpoint
     *            the checkpoint to search
     * @return the checkpoint rank of the searched checkpoint, if it is
     *         contained in the index; otherwise, (-(insertion point) - 1).
     */
    @Override
    public long binarySearch(ITmfCheckpoint checkpoint) {
        if (fHeader.fSize == 1) {
            return 0;
        }

        long lower = 0;
        long upper = fHeader.fSize - 1;
        long lastMiddle = -1;
        long middle = 0;
        while (lower <= upper && lastMiddle != middle) {
            lastMiddle = middle;
            middle = (lower + upper) / 2;
            ITmfCheckpoint found = get(middle);
            setCacheMisses(getCacheMisses() + 1);
            int compare = checkpoint.compareTo(found);
            if (compare == 0) {
                return middle;
            }

            if (compare < 0) {
                upper = middle;
            } else {
                lower = middle + 1;
            }
        }
        long insertionPoint = lower;
        return -(insertionPoint) - 1;
    }

    @Override
    protected int getVersion() {
        return VERSION + super.getVersion();
    }
}
