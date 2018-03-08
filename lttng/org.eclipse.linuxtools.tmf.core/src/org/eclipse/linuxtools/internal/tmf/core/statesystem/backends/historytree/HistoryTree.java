/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;

/**
 * Meta-container for the History Tree. This structure contains all the
 * high-level data relevant to the tree.
 *
 * @author alexmont
 *
 */
class HistoryTree {

    private static final int HISTORY_FILE_MAGIC_NUMBER = 0x05FFA900;

    /**
     * File format version. Increment minor on backwards-compatible changes.
     * Increment major + set minor back to 0 when breaking compatibility.
     */
    private static final int MAJOR_VERSION = 3;
    private static final byte MINOR_VERSION = 0;

    /**
     * Tree-specific configuration
     */
    /* Container for all the configuration constants */
    protected final HTConfig config;

    /* Reader/writer object */
    private final HT_IO treeIO;

    /**
     * Variable Fields (will change throughout the existance of the SHT)
     */
    /* Latest timestamp found in the tree (at any given moment) */
    private long treeEnd;

    /* How many nodes exist in this tree, total */
    private int nodeCount;

    /* "Cache" to keep the active nodes in memory */
    protected Vector<CoreNode> latestBranch;

    /**
     * Create a new State History from scratch, using a SHTConfig object for
     * configuration
     *
     * @param conf
     * @throws IOException
     */
    private HistoryTree(HTConfig conf) throws IOException {
        /*
         * Simple assertion to make sure we have enough place in the 0th block
         * for the tree configuration
         */
        assert (conf.blockSize >= getTreeHeaderSize());

        config = conf;
        treeEnd = conf.treeStart;
        nodeCount = 0;
        latestBranch = new Vector<CoreNode>();

        /* Prepare the IO object */
        treeIO = new HT_IO(this, true);

        /* Add the first node to the tree */
        CoreNode firstNode = initNewCoreNode(-1, conf.treeStart);
        latestBranch.add(firstNode);
    }

    /**
     * "New State History" constructor, which doesn't use SHTConfig but the
     * individual values separately. Kept for now for backwards compatibility,
     * but you should definitely consider using SHTConfig instead (since its
     * contents can then change without directly affecting SHT's API).
     */
    HistoryTree(File newStateFile, int blockSize, int maxChildren,
            long startTime) throws IOException {
        this(new HTConfig(newStateFile, blockSize, maxChildren, startTime));
    }

    /**
     * "Reader" constructor : instantiate a SHTree from an existing tree file on
     * disk
     *
     * @param existingFileName
     *            Path/filename of the history-file we are to open
     * @throws IOException
     */
    HistoryTree(File existingStateFile) throws IOException {
        /*
         * Open the file ourselves, get the tree header information we need,
         * then pass on the descriptor to the TreeIO object.
         */
        int rootNodeSeqNb, res;
        int bs, maxc;
        long startTime;

        /* Java I/O mumbo jumbo... */
        if (!existingStateFile.exists()) {
            throw new IOException("Selected state file does not exist"); //$NON-NLS-1$
        }
        if (existingStateFile.length() <= 0) {
            throw new IOException("Invalid state file selected, " + //$NON-NLS-1$
                    "target file is empty"); //$NON-NLS-1$
        }

        FileInputStream fis = new FileInputStream(existingStateFile);
        ByteBuffer buffer = ByteBuffer.allocate(getTreeHeaderSize());
        FileChannel fc = fis.getChannel();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        fc.read(buffer);
        buffer.flip();

        /*
         * Check the magic number,to make sure we're opening the right type of
         * file
         */
        res = buffer.getInt();
        if (res != HISTORY_FILE_MAGIC_NUMBER) {
            fc.close();
            fis.close();
            throw new IOException("Selected file does not" + //$NON-NLS-1$
                    "look like a History Tree file"); //$NON-NLS-1$
        }

        res = buffer.getInt(); /* Major version number */
        if (res != MAJOR_VERSION) {
            fc.close();
            fis.close();
            throw new IOException("Select History Tree file is of an older " //$NON-NLS-1$
                    + "format. Please use a previous version of " //$NON-NLS-1$
                    + "the parser to open it."); //$NON-NLS-1$
        }

        res = buffer.getInt(); /* Minor version number */

        bs = buffer.getInt(); /* Block Size */
        maxc = buffer.getInt(); /* Max nb of children per node */

        this.nodeCount = buffer.getInt();
        rootNodeSeqNb = buffer.getInt();
        startTime = buffer.getLong();

        this.config = new HTConfig(existingStateFile, bs, maxc, startTime);
        fc.close();
        fis.close();
        /*
         * FIXME We close fis here and the TreeIO will then reopen the same
         * file, not extremely elegant. But how to pass the information here to
         * the SHT otherwise?
         */
        this.treeIO = new HT_IO(this, false);

        rebuildLatestBranch(rootNodeSeqNb);
        this.treeEnd = latestBranch.firstElement().getNodeEnd();

        /*
         * Make sure the history start time we read previously is consistent
         * with was is actually in the root node.
         */
        if (startTime != latestBranch.firstElement().getNodeStart()) {
            fc.close();
            fis.close();
            throw new IOException("Inconsistent start times in the" + //$NON-NLS-1$
                    "history file, it might be corrupted."); //$NON-NLS-1$
        }
    }

    /**
     * "Save" the tree to disk. This method will cause the treeIO object to
     * commit all nodes to disk and then return the RandomAccessFile descriptor
     * so the Tree object can save its configuration into the header of the
     * file.
     *
     * @param requestedEndTime
     */
    void closeTree(long requestedEndTime) {
        FileChannel fc;
        ByteBuffer buffer;
        int i, res;

        /*
         * Work-around the "empty branches" that get created when the root node
         * becomes full. Overwrite the tree's end time with the original wanted
         * end-time, to ensure no queries are sent into those empty nodes.
         *
         * This won't be needed once extended nodes are implemented.
         */
        this.treeEnd = requestedEndTime;

        /* Close off the latest branch of the tree */
        for (i = 0; i < latestBranch.size(); i++) {
            latestBranch.get(i).closeThisNode(treeEnd);
            treeIO.writeNode(latestBranch.get(i));
        }

        /* Only use this for debugging purposes, it's VERY slow! */
        // this.checkIntegrity();

        fc = treeIO.getFcOut();
        buffer = ByteBuffer.allocate(getTreeHeaderSize());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.clear();

        /* Save the config of the tree to the header of the file */
        try {
            fc.position(0);

            buffer.putInt(HISTORY_FILE_MAGIC_NUMBER);

            buffer.putInt(MAJOR_VERSION);
            buffer.putInt(MINOR_VERSION);

            buffer.putInt(config.blockSize);
            buffer.putInt(config.maxChildren);

            buffer.putInt(nodeCount);

            /* root node seq. nb */
            buffer.putInt(latestBranch.firstElement().getSequenceNumber());

            /* start time of this history */
            buffer.putLong(latestBranch.firstElement().getNodeStart());

            buffer.flip();
            res = fc.write(buffer);
            assert (res <= getTreeHeaderSize());
            /* done writing the file header */

        } catch (IOException e) {
            /* We should not have any problems at this point... */
            e.printStackTrace();
        } finally {
            try {
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * @name Accessors
     */

    long getTreeStart() {
        return config.treeStart;
    }

    long getTreeEnd() {
        return treeEnd;
    }

    int getNodeCount() {
        return nodeCount;
    }

    HT_IO getTreeIO() {
        return treeIO;
    }

    /**
     * Rebuild the latestBranch "cache" object by reading the nodes from disk
     * (When we are opening an existing file on disk and want to append to it,
     * for example).
     *
     * @param rootNodeSeqNb
     *            The sequence number of the root node, so we know where to
     *            start
     * @throws ClosedChannelException
     */
    private void rebuildLatestBranch(int rootNodeSeqNb) throws ClosedChannelException {
        HTNode nextChildNode;

        this.latestBranch = new Vector<CoreNode>();

        nextChildNode = treeIO.readNodeFromDisk(rootNodeSeqNb);
        latestBranch.add((CoreNode) nextChildNode);
        while (latestBranch.lastElement().getNbChildren() > 0) {
            nextChildNode = treeIO.readNodeFromDisk(latestBranch.lastElement().getLatestChild());
            latestBranch.add((CoreNode) nextChildNode);
        }
    }

    /**
     * Insert an interval in the tree
     *
     * @param interval
     */
    void insertInterval(HTInterval interval) throws TimeRangeException {
        if (interval.getStartTime() < config.treeStart) {
            throw new TimeRangeException();
        }
        tryInsertAtNode(interval, latestBranch.size() - 1);
    }

    /**
     * Inner method to find in which node we should add the interval.
     *
     * @param interval
     *            The interval to add to the tree
     * @param indexOfNode
     *            The index *in the latestBranch* where we are trying the
     *            insertion
     */
    private void tryInsertAtNode(HTInterval interval, int indexOfNode) {
        HTNode targetNode = latestBranch.get(indexOfNode);

        /* Verify if there is enough room in this node to store this interval */
        if (interval.getIntervalSize() > targetNode.getNodeFreeSpace()) {
            /* Nope, not enough room. Insert in a new sibling instead. */
            addSiblingNode(indexOfNode);
            tryInsertAtNode(interval, latestBranch.size() - 1);
            return;
        }

        /* Make sure the interval time range fits this node */
        if (interval.getStartTime() < targetNode.getNodeStart()) {
            /*
             * No, this interval starts before the startTime of this node. We
             * need to check recursively in parents if it can fit.
             */
            assert (indexOfNode >= 1);
            tryInsertAtNode(interval, indexOfNode - 1);
            return;
        }

        /*
         * Ok, there is room, and the interval fits in this time slot. Let's add
         * it.
         */
        targetNode.addInterval(interval);

        /* Update treeEnd if needed */
        if (interval.getEndTime() > this.treeEnd) {
            this.treeEnd = interval.getEndTime();
        }
        return;
    }

    /**
     * Method to add a sibling to any node in the latest branch. This will add
     * children back down to the leaf level, if needed.
     *
     * @param indexOfNode
     *            The index in latestBranch where we start adding
     */
    private void addSiblingNode(int indexOfNode) {
        int i;
        CoreNode newNode, prevNode;
        long splitTime = treeEnd;

        assert (indexOfNode < latestBranch.size());

        /* Check if we need to add a new root node */
        if (indexOfNode == 0) {
            addNewRootNode();
            return;
        }

        /* Check if we can indeed add a child to the target parent */
        if (latestBranch.get(indexOfNode - 1).getNbChildren() == config.maxChildren) {
            /* If not, add a branch starting one level higher instead */
            addSiblingNode(indexOfNode - 1);
            return;
        }

        /* Split off the new branch from the old one */
        for (i = indexOfNode; i < latestBranch.size(); i++) {
            latestBranch.get(i).closeThisNode(splitTime);
            treeIO.writeNode(latestBranch.get(i));

            prevNode = latestBranch.get(i - 1);
            newNode = initNewCoreNode(prevNode.getSequenceNumber(),
                    splitTime + 1);
            prevNode.linkNewChild(newNode);

            latestBranch.set(i, newNode);
        }
        return;
    }

    /**
     * Similar to the previous method, except here we rebuild a completely new
     * latestBranch
     */
    private void addNewRootNode() {
        int i, depth;
        CoreNode oldRootNode, newRootNode, newNode, prevNode;
        long splitTime = this.treeEnd;

        oldRootNode = latestBranch.firstElement();
        newRootNode = initNewCoreNode(-1, config.treeStart);

        /* Tell the old root node that it isn't root anymore */
        oldRootNode.setParentSequenceNumber(newRootNode.getSequenceNumber());

        /* Close off the whole current latestBranch */
        for (i = 0; i < latestBranch.size(); i++) {
            latestBranch.get(i).closeThisNode(splitTime);
            treeIO.writeNode(latestBranch.get(i));
        }

        /* Link the new root to its first child (the previous root node) */
        newRootNode.linkNewChild(oldRootNode);

        /* Rebuild a new latestBranch */
        depth = latestBranch.size();
        latestBranch = new Vector<CoreNode>();
        latestBranch.add(newRootNode);
        for (i = 1; i < depth + 1; i++) {
            prevNode = latestBranch.get(i - 1);
            newNode = initNewCoreNode(prevNode.getParentSequenceNumber(),
                    splitTime + 1);
            prevNode.linkNewChild(newNode);
            latestBranch.add(newNode);
        }
    }

    /**
     * Add a new empty node to the tree.
     *
     * @param parentSeqNumber
     *            Sequence number of this node's parent
     * @param startTime
     *            Start time of the new node
     * @return The newly created node
     */
    private CoreNode initNewCoreNode(int parentSeqNumber, long startTime) {
        CoreNode newNode = new CoreNode(this, this.nodeCount, parentSeqNumber,
                startTime);
        this.nodeCount++;

        /* Update the treeEnd if needed */
        if (startTime >= this.treeEnd) {
            this.treeEnd = startTime + 1;
        }
        return newNode;
    }

    /**
     * Inner method to select the next child of the current node intersecting
     * the given timestamp. Useful for moving down the tree following one
     * branch.
     *
     * @param currentNode
     * @param t
     * @return The child node intersecting t
     * @throws ClosedChannelException
     *             If the file channel was closed while we were reading the tree
     */
    HTNode selectNextChild(CoreNode currentNode, long t) throws ClosedChannelException {
        assert (currentNode.getNbChildren() > 0);
        int potentialNextSeqNb = currentNode.getSequenceNumber();

        for (int i = 0; i < currentNode.getNbChildren(); i++) {
            if (t >= currentNode.getChildStart(i)) {
                potentialNextSeqNb = currentNode.getChild(i);
            } else {
                break;
            }
        }
        /*
         * Once we exit this loop, we should have found a children to follow. If
         * we didn't, there's a problem.
         */
        assert (potentialNextSeqNb != currentNode.getSequenceNumber());

        /*
         * Since this code path is quite performance-critical, avoid iterating
         * through the whole latestBranch array if we know for sure the next
         * node has to be on disk
         */
        if (currentNode.isDone()) {
            return treeIO.readNodeFromDisk(potentialNextSeqNb);
        }
        return treeIO.readNode(potentialNextSeqNb);
    }

    /**
     * Helper function to get the size of the "tree header" in the tree-file The
     * nodes will use this offset to know where they should be in the file. This
     * should always be a multiple of 4K.
     */
    static int getTreeHeaderSize() {
        return 4096;
    }

    long getFileSize() {
        return config.stateFile.length();
    }

    // ------------------------------------------------------------------------
    // Test/debugging methods
    // ------------------------------------------------------------------------

    /* Only used for debugging, shouldn't be externalized */
    @SuppressWarnings("nls")
    boolean checkNodeIntegrity(HTNode zenode) {

        HTNode otherNode;
        CoreNode node;
        StringBuffer buf = new StringBuffer();
        boolean ret = true;

        // FIXME /* Only testing Core Nodes for now */
        if (!(zenode instanceof CoreNode)) {
            return true;
        }

        node = (CoreNode) zenode;

        try {
            /*
             * Test that this node's start and end times match the start of the
             * first child and the end of the last child, respectively
             */
            if (node.getNbChildren() > 0) {
                otherNode = treeIO.readNode(node.getChild(0));
                if (node.getNodeStart() != otherNode.getNodeStart()) {
                    buf.append("Start time of node (" + node.getNodeStart() + ") "
                            + "does not match start time of first child " + "("
                            + otherNode.getNodeStart() + "), " + "node #"
                            + otherNode.getSequenceNumber() + ")\n");
                    ret = false;
                }
                if (node.isDone()) {
                    otherNode = treeIO.readNode(node.getLatestChild());
                    if (node.getNodeEnd() != otherNode.getNodeEnd()) {
                        buf.append("End time of node (" + node.getNodeEnd()
                                + ") does not match end time of last child ("
                                + otherNode.getNodeEnd() + ", node #"
                                + otherNode.getSequenceNumber() + ")\n");
                        ret = false;
                    }
                }
            }

            /*
             * Test that the childStartTimes[] array matches the real nodes' start
             * times
             */
            for (int i = 0; i < node.getNbChildren(); i++) {
                otherNode = treeIO.readNode(node.getChild(i));
                if (otherNode.getNodeStart() != node.getChildStart(i)) {
                    buf.append("  Expected start time of child node #"
                            + node.getChild(i) + ": " + node.getChildStart(i)
                            + "\n" + "  Actual start time of node #"
                            + otherNode.getSequenceNumber() + ": "
                            + otherNode.getNodeStart() + "\n");
                    ret = false;
                }
            }

        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }

        if (!ret) {
            System.out.println("");
            System.out.println("SHT: Integrity check failed for node #"
                    + node.getSequenceNumber() + ":");
            System.out.println(buf.toString());
        }
        return ret;
    }

    void checkIntegrity() {
        try {
            for (int i = 0; i < nodeCount; i++) {
                checkNodeIntegrity(treeIO.readNode(i));
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    /* Only used for debugging, shouldn't be externalized */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "Information on the current tree:\n\n" + "Blocksize: "
                + config.blockSize + "\n" + "Max nb. of children per node: "
                + config.maxChildren + "\n" + "Number of nodes: " + nodeCount
                + "\n" + "Depth of the tree: " + latestBranch.size() + "\n"
                + "Size of the treefile: " + this.getFileSize() + "\n"
                + "Root node has sequence number: "
                + latestBranch.firstElement().getSequenceNumber() + "\n"
                + "'Latest leaf' has sequence number: "
                + latestBranch.lastElement().getSequenceNumber();
    }

    private int curDepth;

    /**
     * Start at currentNode and print the contents of all its children, in
     * pre-order. Give the root node in parameter to visit the whole tree, and
     * have a nice overview.
     */
    @SuppressWarnings("nls")
    private void preOrderPrint(PrintWriter writer, boolean printIntervals,
            CoreNode currentNode) {
        /* Only used for debugging, shouldn't be externalized */
        int i, j;
        HTNode nextNode;

        writer.println(currentNode.toString());
        if (printIntervals) {
            currentNode.debugPrintIntervals(writer);
        }
        curDepth++;

        try {
            for (i = 0; i < currentNode.getNbChildren(); i++) {
                nextNode = treeIO.readNode(currentNode.getChild(i));
                assert (nextNode instanceof CoreNode); // TODO temporary
                for (j = 0; j < curDepth - 1; j++) {
                    writer.print("  ");
                }
                writer.print("+-");
                preOrderPrint(writer, printIntervals, (CoreNode) nextNode);
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
        curDepth--;
        return;
    }

    /**
     * Print out the full tree for debugging purposes
     *
     * @param writer
     *            PrintWriter in which to write the output
     * @param printIntervals
     *            Says if you want to output the full interval information
     */
    void debugPrintFullTree(PrintWriter writer, boolean printIntervals) {
        /* Only used for debugging, shouldn't be externalized */
        curDepth = 0;
        this.preOrderPrint(writer, false, latestBranch.firstElement());

        if (printIntervals) {
            writer.println("\nDetails of intervals:"); //$NON-NLS-1$
            curDepth = 0;
            this.preOrderPrint(writer, true, latestBranch.firstElement());
        }
        writer.println('\n');
    }

}
