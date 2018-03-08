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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * An array of checkpoints stored on disk. It is very efficient
 * for searching checkpoints by rank (O(1))
 */
public class FlatArray {
    /**
     * Typical FlatArray file name
     */
    public final static String INDEX_FILE_NAME = "checkpoint_flatarray.idx"; //$NON-NLS-1$
    private static final int INT_SIZE = 4;
    private static final int LONG_SIZE = 8;

    ITmfTrace fTrace;

    private static final int VERSION = 1;
    private int cacheMisses = 0;
    private boolean fCreatedFromScratch;

    private RandomAccessFile fRandomAccessFile;
    private File fFile;

    // Cached values
    private int fCheckpointSize = 0;
    private FileChannel fFileChannel;
    private ByteBuffer fByteBuffer;

    private FlatArrayHeader fFlatArrayHeader;
    private TmfTimeRange fTimeRange;

    class FlatArrayHeader {
        int fVersion;
        int fSize = 0;
        long fTimeRangeOffset;
        long fNbEvents;

        private final static int SIZE = INT_SIZE +
                INT_SIZE +
                LONG_SIZE +
                LONG_SIZE;

        void serializeIn() throws IOException {
            fVersion = fRandomAccessFile.readInt();
            fSize = fRandomAccessFile.readInt();
            fNbEvents = fRandomAccessFile.readLong();
            fTimeRangeOffset = fRandomAccessFile.readLong();
        }

        void serializeOut() throws IOException {
            fRandomAccessFile.seek(0);
            fRandomAccessFile.writeInt(VERSION);
            fRandomAccessFile.writeInt(fSize);
            fRandomAccessFile.writeLong(fNbEvents);
            fRandomAccessFile.writeLong(fTimeRangeOffset);
        }
    }

    /**
     * Constructs a FlatArray for a given trace from scratch or from an existing file.
     * When the FlatArray is created from scratch, it is populated
     * by subsequent calls to {@link #insert}.
     *
     * @param file the file to use as the persistent storage
     * @param trace the trace
     */
    public FlatArray(File file, ITmfTrace trace) {
        fTrace = trace;
        fFile = file;
        fCreatedFromScratch = !fFile.exists();
        fCheckpointSize = fTrace.getCheckpointSize();
        fByteBuffer = ByteBuffer.allocate(fCheckpointSize);

        if (!fCreatedFromScratch) {
            if (!tryRestore()) {
                fFile.delete();
                dispose();
            }
        }

        if (fCreatedFromScratch) {
            initialize();
        }
    }

    private void initialize() {
        try {
            fRandomAccessFile = new RandomAccessFile(fFile, "rw"); //$NON-NLS-1$
            fFileChannel = fRandomAccessFile.getChannel();
            fFlatArrayHeader = new FlatArrayHeader();

            // Reserve space for header
            fRandomAccessFile.setLength(FlatArrayHeader.SIZE);

            fTimeRange = new TmfTimeRange(new TmfTimestamp(0), new TmfTimestamp(0));
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.ErrorOpeningIndex, fFile), e);
        }
    }

    /**
     * @return true if the FlatArray could be restored from disk, false
     *         otherwise
     */
    private boolean tryRestore() {
        try {
            fRandomAccessFile = new RandomAccessFile(fFile, "r"); //$NON-NLS-1$
            fFileChannel = fRandomAccessFile.getChannel();
        } catch (FileNotFoundException e) {
            Activator.logError(MessageFormat.format(Messages.ErrorOpeningIndex, fFile), e);
            return false;
        }

        try {
            fFlatArrayHeader = new FlatArrayHeader();
            fFlatArrayHeader.serializeIn();
            if (fFlatArrayHeader.fVersion != VERSION) {
                return false;
            }
            serializeInTimeRange();
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorReadingHeader, fFile), e);
            return false;
        }

        return true;
    }

    /**
     * Dispose and delete the FlatArray
     */
    public void delete() {
        dispose();
        if (fFile.exists()) {
            fFile.delete();
        }
    }

    /**
     * Insert a checkpoint into the file-backed array
     *
     * @param checkpoint the checkpoint to insert
     */
    public void insert(ITmfCheckpoint checkpoint) {
        try {
            ++fFlatArrayHeader.fSize;
            fRandomAccessFile.seek(fRandomAccessFile.length());
            fByteBuffer.clear();
            checkpoint.serializeOut(fByteBuffer);
            fRandomAccessFile.write(fByteBuffer.array());
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.FlatArray_IOErrorWriting, fFile), e);
        }
    }

    /**
     * Get a checkpoint from a rank
     *
     * @param rank the rank to search
     * @return the checkpoint that has been found or null if not found
     */
    public ITmfCheckpoint get(int rank) {
        ITmfCheckpoint checkpoint = null;
        try {
            int pos = FlatArrayHeader.SIZE + fCheckpointSize * rank;
            fRandomAccessFile.seek(pos);
            fByteBuffer.clear();
            fRandomAccessFile.read(fByteBuffer.array());
            ITmfLocation location = fTrace.restoreLocation(fByteBuffer);
            ITmfTimestamp timeStamp = TmfTimestamp.newAndSerialize(fByteBuffer);
            checkpoint = new TmfCheckpoint(timeStamp, location);
            checkpoint.serializeIn(fByteBuffer);
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.FlatArray_IOErrorReading, fFile), e);
        }
        return checkpoint;
    }

    private void serializeInTimeRange() throws IOException {
        ByteBuffer b = ByteBuffer.allocate(1024);
        fFileChannel.read(b, fFlatArrayHeader.fTimeRangeOffset);
        b.flip();
        fTimeRange = new TmfTimeRange(TmfTimestamp.newAndSerialize(b), TmfTimestamp.newAndSerialize(b));
    }

    private void serializeOutTimeRange() throws IOException {
        fFlatArrayHeader.fTimeRangeOffset = fRandomAccessFile.length();
        ByteBuffer b = ByteBuffer.allocate(1024);
        new TmfTimestamp(fTimeRange.getStartTime()).serializeOut(b);
        new TmfTimestamp(fTimeRange.getEndTime()).serializeOut(b);
        b.flip();
        fFileChannel.write(b, fFlatArrayHeader.fTimeRangeOffset);
    }

    /**
     * Dispose the structure and its resources
     */
    public void dispose() {
        try {
            if (fRandomAccessFile != null) {
                fRandomAccessFile.close();
            }
            fCreatedFromScratch = true;
            fFlatArrayHeader = null;
            fRandomAccessFile = null;
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.BTree_IOErrorClosingIndex, fFile), e);
        }
    }

    /**
     * Search for a checkpoint and return the rank.
     *
     * @param checkpoint the checkpoint to search
     * @return the rank
     */
    public int binarySearch(ITmfCheckpoint checkpoint) {
        if (fFlatArrayHeader.fSize == 1) {
            return 0;
        }

        int lower = 0;
        int upper = fFlatArrayHeader.fSize - 1;
        int lastMiddle = -1;
        int middle = 0;
        while (lower <= upper && lastMiddle != middle) {
            lastMiddle = middle;
            middle = (lower + upper) / 2;
            ITmfCheckpoint found = get(middle);
            ++cacheMisses;
            int compare = checkpoint.compareTo(found);
            if (compare == 0) {
                return middle;
            }

            if (compare < 0) {
                upper= middle;
            } else {
                lower= middle + 1;
            }
        }
        return lower -1;
    }

    /**
    *
    * @return true if the FlatArray was created from scratch, false otherwise
    */
   public boolean isCreatedFromScratch() {
       return fCreatedFromScratch;
   }

    /**
     * @return the number of cache misses.
     */
    public long getCacheMisses() {
        return cacheMisses;
    }

    /**
     * Returns the size of the FlatArray expressed as a number of checkpoints.
     *
     * @return the size of the FlatArray
     */
    public int size() {
        return fFlatArrayHeader.fSize;
    }

    /**
     * Set the trace time range
     *
     * @param timeRange the trace time range
     */
    public void setTimeRange(TmfTimeRange timeRange) {
        fTimeRange = timeRange;
    }

    /**
     * Get the trace time range
     *
     * @return the trace time range
     */
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    /**
     * Set the number of events in the trace
     *
     * @param nbEvents the number of events in the trace
     */
    public void setNbEvents(long nbEvents) {
        fFlatArrayHeader.fNbEvents = nbEvents;
    }

    /**
     * Get the number of events in the trace
     *
     * @return the number of events in the trace
     */
    public long getNbEvents() {
        return fFlatArrayHeader.fNbEvents;
    }

    /**
     * Set the index as complete. No more checkpoints will be inserted.
     */
    public void setIndexComplete() {
        try {
            serializeOutTimeRange();

            fFlatArrayHeader.serializeOut();
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorWritingHeader, fFile), e);
        }
    }
}
