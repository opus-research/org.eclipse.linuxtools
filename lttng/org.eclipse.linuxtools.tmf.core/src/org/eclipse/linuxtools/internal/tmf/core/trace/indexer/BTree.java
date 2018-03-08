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
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;

/**
 * A BTree made of BTreeNodes representing a series of ITmfCheckpoints
 * ordered by time stamps. @link{ BTreeNodeCache } is used to improve performance by caching some nodes
 * in memory and the other nodes are kept on disk.
 */
public class BTree {

    /**
     * Typical BTree file name
     */
    public final static String INDEX_FILE_NAME = "checkpoint_btree.idx"; //$NON-NLS-1$
    private static final int VERSION = 4;
    private static final int INT_SIZE = 4;
    private static final int LONG_SIZE = 8;
    private static final boolean ALWAYS_CACHE_ROOT = true;

    ITmfTrace fTrace;
    ByteBuffer fNodeByteBuffer;

    private final int fMaxNumEntries;
    private final int fMaxNumChildren;
    private final int fMedianEntry;
    private final File fFile;
    private RandomAccessFile fRandomAccessFile;
    private boolean fCreatedFromScratch;

    private BTreeHeader fBTreeHeader = null;

    // Cached values
    private int nodeSize = - 1;
    private FileChannel fFileChannel;
    private BTreeNodeCache fNodeCache;

    private TmfTimeRange fTimeRange;

    class BTreeHeader {
        int fVersion = 0;
        int fSize = 0;
        long fRoot;
        long fNbEvents = 0;
        long fTimeRangeOffset = 0;

        int SIZE = INT_SIZE +
                INT_SIZE +
                LONG_SIZE +
                LONG_SIZE +
                LONG_SIZE;

        void serializeIn() throws IOException {
            ByteBuffer bb = ByteBuffer.allocate(SIZE);
            fFileChannel.read(bb);
            bb.flip();

            fVersion = bb.getInt();
            fSize = bb.getInt();
            fRoot = bb.getLong();
            fNbEvents = bb.getLong();
            fTimeRangeOffset = bb.getLong();
        }

        void serializeOut() throws IOException {
            fRandomAccessFile.seek(0);
            ByteBuffer bb = ByteBuffer.allocate(SIZE);
            bb.putInt(VERSION);
            bb.putInt(fSize);
            bb.putLong(fRoot);
            bb.putLong(fNbEvents);
            bb.putLong(fTimeRangeOffset);
            bb.flip();
            fFileChannel.write(bb);
        }
    }

    /**
     * Constructs a BTree for a given trace from scratch or from an existing file.
     * The degree is used to calibrate the number of entries in each node which
     * can affect performance. When the BTree is created from scratch, it is populated
     * by subsequent calls to {@link #insert}.
     *
     * @param degree the degree to use in the tree
     * @param file the file to use as the persistent storage
     * @param trace the trace
     */
    public BTree(int degree, File file, ITmfTrace trace) {
        fFile = file;
        fTrace = trace;

        fMaxNumEntries = 2 * degree - 1;
        fMaxNumChildren = 2 * degree;
        fMedianEntry = degree - 1;

        fNodeByteBuffer = ByteBuffer.allocate(getNodeSize());

        fCreatedFromScratch = !fFile.exists();

        fNodeCache = new BTreeNodeCache(this);

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

            // Reserve space for header
            fBTreeHeader = new BTreeHeader();
            fRandomAccessFile.setLength(fBTreeHeader.SIZE);

            BTreeNode rootNode = allocateNode();
            setRootNode(rootNode);

            fTimeRange = new TmfTimeRange(new TmfTimestamp(0), new TmfTimestamp(0));
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.ErrorOpeningIndex, fFile), e);
        }
    }

    /**
     * @return true if the BTree could be restored from disk, false otherwise
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
            fBTreeHeader = new BTreeHeader();
            fBTreeHeader.serializeIn();
            if (fBTreeHeader.fVersion != VERSION) {
                return false;
            }

            serializeInTimeRange();
            BTreeNode rootNode = fNodeCache.getNode(fBTreeHeader.fRoot);

            setRootNode(rootNode);
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorReadingHeader, fFile), e);
            return false;
        }

        return true;
    }

    /**
     * Dispose and delete the BTree
     */
    public void delete() {
        dispose();
        if (fFile.exists()) {
            fFile.delete();
        }
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
            fBTreeHeader = null;
            fRandomAccessFile = null;
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.BTree_IOErrorClosingIndex, fFile), e);
        }
    }

    private void serializeInTimeRange() throws IOException {
        ByteBuffer b = ByteBuffer.allocate(1024);
        fFileChannel.read(b, fBTreeHeader.fTimeRangeOffset);
        b.flip();
        fTimeRange = new TmfTimeRange(TmfTimestamp.newAndSerialize(b), TmfTimestamp.newAndSerialize(b));
    }

    private void serializeOutTimeRange() throws IOException {
        fBTreeHeader.fTimeRangeOffset = fRandomAccessFile.length();
        ByteBuffer b = ByteBuffer.allocate(1024);
        fTimeRange.getStartTime().serializeOut(b);
        fTimeRange.getEndTime().serializeOut(b);
        b.flip();
        fFileChannel.write(b, fBTreeHeader.fTimeRangeOffset);
    }

    /**
     *
     * @return true if BTree was created from scratch, false otherwise
     */
    public boolean isCreatedFromScratch() {
        return fCreatedFromScratch;
    }

    /**
     * Insert a checkpoint into the file-backed BTree
     *
     * @param checkpoint the checkpoint to insert
     */
    public void insert(ITmfCheckpoint checkpoint) {
        insert(checkpoint, fBTreeHeader.fRoot, null, 0);
    }

    private void setRootNode(BTreeNode newRootNode) {
        fBTreeHeader.fRoot = newRootNode.getOffset();
        if (ALWAYS_CACHE_ROOT) {
            fNodeCache.setRootNode(newRootNode);
        } else {
            fNodeCache.addNode(newRootNode);
        }
    }

    private void insert(ITmfCheckpoint checkpoint, long nodeOffset, BTreeNode pParent, int iParent) {
        BTreeNode parent = pParent;
        BTreeNode node = fNodeCache.getNode(nodeOffset);

        // If this node is full (last entry isn't null), split it
        if (node.getEntry(fMaxNumEntries - 1) != null) {

            ITmfCheckpoint median = node.getEntry(fMedianEntry);
            if (median.compareTo(checkpoint) == 0) {
                // Found it
                return;
            }

            // Split it.
            // Create the new node and move the larger entries over.
            BTreeNode newnode = allocateNode();
            fNodeCache.addNode(newnode);
            long newNodeOffset = newnode.getOffset();
            for (int i = 0; i < fMedianEntry; ++i) {
                newnode.setEntry(i, node.getEntry(fMedianEntry + 1 + i));
                node.setEntry(fMedianEntry + 1 + i, null);
                newnode.setChild(i, node.getChild(fMedianEntry + 1 + i));
                node.setChild(fMedianEntry + 1 + i, BTreeNode.NULL_CHILD);
            }
            newnode.setChild(fMedianEntry, node.getChild(fMaxNumEntries));
            node.setChild(fMaxNumEntries, BTreeNode.NULL_CHILD);

            if (parent == null) {
                parent = allocateNode();
                setRootNode(parent);
                parent.setChild(0, nodeOffset);
            } else {
                // Insert the median into the parent.
                for (int i = fMaxNumEntries - 2; i >= iParent; --i) {
                    ITmfCheckpoint r = parent.getEntry(i);
                    if (r != null) {
                        parent.setEntry(i + 1, r);
                        parent.setChild(i + 2, parent.getChild(i + 1));
                    }
                }
            }

            fNodeCache.getNode(parent.getOffset());

            parent.setEntry(iParent, median);
            parent.setChild(iParent + 1, newNodeOffset);

            node.setEntry(fMedianEntry, null);

            // Set the node to the correct one to follow.
            if (checkpoint.compareTo(median) > 0) {
                node = newnode;
            }
        }

        // Binary search to find the insert point.
        int lower= 0;
        int upper= fMaxNumEntries - 1;
        while (lower < upper && node.getEntry(upper - 1) == null) {
            upper--;
        }

        while (lower < upper) {
            int middle = (lower + upper) / 2;
            ITmfCheckpoint check = node.getEntry(middle);
            if (check == null) {
                upper = middle;
            } else {
                int compare = check.compareTo(checkpoint);
                if (compare > 0) {
                    upper = middle;
                } else if (compare < 0) {
                    lower = middle + 1;
                } else {
                    // Found it, no insert
                    return;
                }
            }
        }
        final int i= lower;
        long child = node.getChild(i);
        if (child != BTreeNode.NULL_CHILD) {
            // Visit the children.
            insert(checkpoint, child, node, i);
        } else {
            // We are at the leaf, add us in.
            // First copy everything after over one.
            for (int j = fMaxNumEntries - 2; j >= i; --j) {
                ITmfCheckpoint r = node.getEntry(j);
                if (r != null) {
                    node.setEntry(j + 1, r);
                }
            }
            node.setEntry(i, checkpoint);
            return;
        }
    }

    int getNodeSize() {
        if (nodeSize == -1) {
            nodeSize = INT_SIZE; // num entries
            nodeSize += fTrace.getCheckpointSize() * fMaxNumEntries;
            nodeSize += LONG_SIZE * fMaxNumChildren;
        }

        return nodeSize;
    }

    private BTreeNode allocateNode() {
        try {
            long offset = fRandomAccessFile.length();
            fRandomAccessFile.setLength(offset + getNodeSize());
            BTreeNode node = new BTreeNode(this, offset);
            return node;
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.BTree_IOErrorAllocatingNode, fFile), e);
        }
        return null;
    }

    /**
     * Accept a visitor. This visitor is used to search through the whole
     * tree.
     *
     * @param treeVisitor the visitor to accept
     */
    public void accept(IBTreeVisitor treeVisitor) {
        accept(fBTreeHeader.fRoot, treeVisitor);
    }

    private void accept(long nodeOffset, IBTreeVisitor visitor) {

        if (nodeOffset == BTreeNode.NULL_CHILD) {
            return;
        }

        BTreeNode node = fNodeCache.getNode(nodeOffset);

        // Binary search to find first entry greater or equal.
        int lower = 0;
        int upper = fMaxNumEntries - 1;
        while (lower < upper && node.getEntry(upper - 1) == null) {
            upper--;
        }
        while (lower < upper) {
            int middle = (lower + upper) / 2;
            ITmfCheckpoint middleCheckpoint = node.getEntry(middle);
            if (middleCheckpoint == null) {
                upper = middle;
            } else {
                int compare = visitor.compare(middleCheckpoint);
                if (compare == 0) {
                    return;
                } else if (compare > 0) {
                    upper = middle;
                } else {
                    lower = middle + 1;
                }
            }
        }

        // Start with first record greater or equal, reuse comparison
        // results.
        int i = lower;
        for (; i < fMaxNumEntries; ++i) {
            ITmfCheckpoint record = node.getEntry(i);
            if (record == null) {
                break;
            }

            int compare = visitor.compare(record);
            if (compare > 0) {
                // Start point is to the left.
                accept(node.getChild(i), visitor);
                return;
            } else if (compare == 0) {
                return;
            }
        }
        accept(node.getChild(i), visitor);
        return;
    }

    /**
     * Returns the size of the BTree expressed as a number of checkpoints.
     *
     * @return the size of the BTree
     */
    public int size() {
        return fBTreeHeader.fSize;
    }

    /**
     * Set the size of the BTree, expressed as a number of checkpoints
     *
     * @param size the size of the BTree
     */
    public void setSize(int size) {
        fBTreeHeader.fSize = size;
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
        fBTreeHeader.fNbEvents = nbEvents;
    }

    /**
     * Get the number of events in the trace
     *
     * @return the number of events in the trace
     */
    public long getNbEvents() {
        return fBTreeHeader.fNbEvents;
    }

    /**
     * @return the number of cache misses. @see BTreeNodeCache
     */
    public long getCacheMisses() {
        return fNodeCache.getCacheMisses();
    }

    /**
     * Set the index as complete. No more checkpoints will be inserted.
     */
    public void setIndexComplete() {
        try {
            serializeOutTimeRange();

            fBTreeHeader.serializeOut();
            fNodeCache.serializeOut();
        } catch (IOException e) {
            Activator.logError(MessageFormat.format(Messages.IOErrorWritingHeader, fFile), e);
        }
    }

    /**
     * Get the maximum number of entries in a node
     *
     * @return the maximum number of entries in a node
     */
    int getMaxNumEntries() {
        return fMaxNumEntries;
    }

    /**
     * Get the maximum number of children in a node
     *
     * @return the maximum number of children in a node
     */
    int getMaxNumChildren() {
        return fMaxNumChildren;
    }

    RandomAccessFile getFile() {
        return fRandomAccessFile;
    }

    FileChannel getFileChannel() {
        return fRandomAccessFile.getChannel();
    }
}
