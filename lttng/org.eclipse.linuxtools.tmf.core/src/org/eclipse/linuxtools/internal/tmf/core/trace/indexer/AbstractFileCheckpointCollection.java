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
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;

/**
 * Common implementation of file-based checkpoint collection
 */
public abstract class AbstractFileCheckpointCollection implements ICheckpointCollection {

    private static final int VERSION = 1;

    /**
     * The base file header, can be extended
     */
    protected class CheckpointCollectionFileHeader {
        private final static int SIZE = INT_SIZE +
                INT_SIZE +
                LONG_SIZE +
                LONG_SIZE;

        /**
         * Get the size of the header in bytes. This should be overridden if the
         * header is augmented with more data
         *
         * @return the size of the header in bytes
         */
        protected int getSize() {
            return SIZE;
        }

        /**
         * Constructs a new file header for an existing file
         *
         * @param randomAccessFile
         *            the existing file
         * @throws IOException
         *             if an I/O error occurs reading from the file
         */
        protected CheckpointCollectionFileHeader(RandomAccessFile randomAccessFile) throws IOException {
            fVersion = randomAccessFile.readInt();
            fSize = randomAccessFile.readInt();
            fNbEvents = randomAccessFile.readLong();
            fTimeRangeOffset = randomAccessFile.readLong();
        }

        /**
         * Constructs a new file header for the given version
         *
         * @param version
         *            the version
         */
        protected CheckpointCollectionFileHeader(int version) {
            fVersion = version;
        }

        /**
         * Serialize the header to a file
         *
         * @param randomAccessFile
         *            the existing file
         * @throws IOException
         *             if an I/O error occurs writing to the file
         */
        protected void serialize(RandomAccessFile randomAccessFile) throws IOException {
            randomAccessFile.seek(0);
            randomAccessFile.writeInt(getVersion());
            randomAccessFile.writeInt(fSize);
            randomAccessFile.writeLong(fNbEvents);
            randomAccessFile.writeLong(fTimeRangeOffset);
        }

        /**
         * The version of the collection. Should be incremented if a binary
         * incompatible change occurs.
         */
        protected final int fVersion;
        /**
         * The size of the collection expressed in a number of checkpoints.
         */
        protected int fSize = 0;
        /**
         * Offset in bytes where the time range is store
         */
        protected long fTimeRangeOffset;
        /**
         * The total number of events in the trace
         */
        protected long fNbEvents;
    }

    /**
     * The size of an int in bytes
     */
    protected static final int INT_SIZE = 4;
    /**
     * The size of a long in bytes
     */
    protected static final int LONG_SIZE = 8;

    /**
     * The maximum size of the serialize buffer when writing the time range
     */
    protected static final int MAX_TIME_RANGE_SERIALIZE_SIZE = 1024;

    /**
     * The originating trace
     */
    protected ITmfPersistentlyIndexable fTrace;

    private long fCacheMisses = 0;
    private boolean fCreatedFromScratch;

    /**
     * File handle for the file being read/written
     */
    protected RandomAccessFile fRandomAccessFile;
    /**
     * File handle for the file being read/written
     */
    protected File fFile;

    /**
     * The base file header
     */
    protected CheckpointCollectionFileHeader fHeader;

    // Cached values
    private FileChannel fFileChannel;
    private TmfTimeRange fTimeRange;

    /**
     * Constructs a checkpoint collection for a given trace from scratch or from
     * an existing file. When the checkpoint collection is created from scratch,
     * it is populated by subsequent calls to {@link #insert}.
     *
     * @param file
     *            the file to use as the persistent storage
     * @param trace
     *            the trace
     */
    public AbstractFileCheckpointCollection(File file, ITmfPersistentlyIndexable trace) {
        fTrace = trace;
        fFile = file;
        setCreatedFromScratch(!fFile.exists());

        if (!isCreatedFromScratch()) {
            if (!tryRestore()) {
                fFile.delete();
                dispose();
            }
        }

        if (isCreatedFromScratch()) {
            initialize();
        }
    }

    /**
     * Creates a new basic file header with the version field initialized. This
     * should be overridden if the file header is extended
     *
     * @return the created file header
     */
    protected CheckpointCollectionFileHeader createHeader() {
        return new CheckpointCollectionFileHeader(VERSION);
    }

    /**
     * Creates a new basic file header for an existing file. This should be
     * overridden if the file header is extended
     *
     * @param randomAccessFile
     *            the existing file
     * @return the created file header
     * @throws IOException
     *             if an I/O error occurs reading from the file
     */
    protected CheckpointCollectionFileHeader createHeader(RandomAccessFile randomAccessFile) throws IOException {
        return new CheckpointCollectionFileHeader(randomAccessFile);
    }

    /**
     * Get the version of the collection.
     *
     * @return the version of the collection.
     */
    protected int getVersion() {
        return VERSION;
    }

    private void initialize() {
        try {
            fRandomAccessFile = new RandomAccessFile(fFile, "rw"); //$NON-NLS-1$
            fFileChannel = fRandomAccessFile.getChannel();
            fHeader = createHeader();

            // Reserve space for header
            fRandomAccessFile.setLength(fHeader.getSize());

            fTimeRange = new TmfTimeRange(new TmfTimestamp(0), new TmfTimestamp(0));
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.ErrorOpeningIndex, fFile), e);
        }
    }

    /**
     * Try to restore the index from disk. Try to open the file and check the
     * version
     * 
     * @return true if the collection could be restored from disk, false
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
            fHeader = createHeader(fRandomAccessFile);
            if (fHeader.fVersion != getVersion()) {
                return false;
            }
            serializeInTimeRange();
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorReadingHeader, fFile), e);
            return false;
        }

        return true;
    }

    private void serializeInTimeRange() throws IOException {
        ByteBuffer b = ByteBuffer.allocate(MAX_TIME_RANGE_SERIALIZE_SIZE);
        fFileChannel.read(b, fHeader.fTimeRangeOffset);
        b.flip();
        fTimeRange = new TmfTimeRange(new TmfTimestamp(b), new TmfTimestamp(b));
    }

    private void serializeOutTimeRange() throws IOException {
        fHeader.fTimeRangeOffset = fRandomAccessFile.length();
        ByteBuffer b = ByteBuffer.allocate(MAX_TIME_RANGE_SERIALIZE_SIZE);
        new TmfTimestamp(fTimeRange.getStartTime()).serialize(b);
        new TmfTimestamp(fTimeRange.getEndTime()).serialize(b);
        b.flip();
        fFileChannel.write(b, fHeader.fTimeRangeOffset);
    }

    /**
     * Set the index as complete. No more checkpoints will be inserted.
     */
    @Override
    public void setIndexComplete() {
        try {
            serializeOutTimeRange();

            fHeader.serialize(fRandomAccessFile);
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorWritingHeader, fFile), e);
        }
    }

    /**
     *
     * @return true if the checkpoint collection was created from scratch, false
     *         otherwise
     */
    @Override
    public boolean isCreatedFromScratch() {
        return fCreatedFromScratch;
    }

    /**
     * Set whether or not the collection is created from scratch
     *
     * @param isCreatedFromScratch
     *            whether or not the collection is created from scratch
     */
    protected void setCreatedFromScratch(boolean isCreatedFromScratch) {
        fCreatedFromScratch = isCreatedFromScratch;
    }

    /**
     * @return the number of cache misses.
     */
    @Override
    public long getCacheMisses() {
        return fCacheMisses;
    }

    /**
     * Set the number of cache misses.
     *
     * @param cacheMisses
     *            the number of cache misses
     */
    protected void setCacheMisses(long cacheMisses) {
        this.fCacheMisses = cacheMisses;
    }

    /**
     * Returns the size of the checkpoint collection expressed as a number of
     * checkpoints.
     *
     * @return the size of the checkpoint collection
     */
    @Override
    public int size() {
        return fHeader.fSize;
    }

    /**
     * Set the trace time range
     *
     * @param timeRange
     *            the trace time range
     */
    @Override
    public void setTimeRange(TmfTimeRange timeRange) {
        fTimeRange = timeRange;
    }

    /**
     * Get the trace time range
     *
     * @return the trace time range
     */
    @Override
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    /**
     * Set the number of events in the trace
     *
     * @param nbEvents
     *            the number of events in the trace
     */
    @Override
    public void setNbEvents(long nbEvents) {
        fHeader.fNbEvents = nbEvents;
    }

    /**
     * Get the number of events in the trace
     *
     * @return the number of events in the trace
     */
    @Override
    public long getNbEvents() {
        return fHeader.fNbEvents;
    }

    /**
     * Dispose and delete the checkpoint collection
     */
    @Override
    public void delete() {
        dispose();
        if (fFile.exists()) {
            fFile.delete();
        }
    }

    /**
     * Dispose the collection and its resources
     */
    @Override
    public void dispose() {
        try {
            if (fRandomAccessFile != null) {
                fRandomAccessFile.close();
            }
            setCreatedFromScratch(true);
            fHeader = null;
            fRandomAccessFile = null;
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorClosingIndex, fFile), e);
        }
    }
}
