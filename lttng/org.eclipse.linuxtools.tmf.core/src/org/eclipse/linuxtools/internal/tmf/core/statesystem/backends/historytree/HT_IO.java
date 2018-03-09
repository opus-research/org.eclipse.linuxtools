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
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;

/**
 * This class abstract inputs/outputs of the HistoryTree nodes.
 *
 * It contains all the methods and descriptors to handle reading/writing nodes
 * to the tree-file on disk and all the caching mechanisms.
 *
 * This abstraction is mainly for code isolation/clarification purposes.
 * Every HistoryTree must contain 1 and only 1 HT_IO element.
 *
 * @author Alexandre Montplaisir
 *
 */
class HT_IO {
    /* Configuration of the History Tree */
    HTConfig fConfig;

    /* Fields related to the file I/O */
    private final FileInputStream fis;
    private final FileOutputStream fos;
    private final FileChannel fcIn;
    private final FileChannel fcOut;

    // TODO test/benchmark optimal cache size
    private final int CACHE_SIZE = 256;
    private final HTNode fNodeCache[] = new HTNode[CACHE_SIZE];

    /**
     * Standard constructor
     *
     * @param config
     *             The configuration object for the StateHistoryTree
     * @param newFile
     *            Flag indicating that the file must be created from scratch

     * @throws IOException
     *             An exception can be thrown when file cannot be accessed
     */
    HT_IO(HTConfig config, boolean newFile) throws IOException {
        fConfig = config;

        File historyTreeFile = config.getStateFile();
        if (newFile) {
            boolean success1 = true;
            /* Create a new empty History Tree file */
            if (historyTreeFile.exists()) {
                success1 = historyTreeFile.delete();
            }
            boolean success2 = historyTreeFile.createNewFile();
            if (!(success1 && success2)) {
                /* It seems we do not have permission to create the new file */
                throw new IOException("Cannot create new file at " + //$NON-NLS-1$
                        historyTreeFile.getName());
            }
            fis = new FileInputStream(historyTreeFile);
            fos = new FileOutputStream(historyTreeFile, false);
        } else {
            /*
             * We want to open an existing file, make sure we don't squash the
             * existing content when opening the fos!
             */
            this.fis = new FileInputStream(historyTreeFile);
            this.fos = new FileOutputStream(historyTreeFile, true);
        }
        this.fcIn = fis.getChannel();
        this.fcOut = fos.getChannel();
    }

    /**
     * Generic "read node" method, which checks if the node is in memory first,
     * and if it's not it goes to disk to retrieve it.
     *
     * @param seqNumber
     *            Sequence number of the node we want
     * @return The wanted node in object form
     * @throws ClosedChannelException
     *             If the channel was closed before we could read
     */
    HTNode readNode(int seqNumber) throws ClosedChannelException {
        //HTNode node = readNodeFromMemory(seqNumber);
        //if (node == null)
        {
            return readNodeFromDisk(seqNumber);
        }
        //return node;
    }

    /*
    private HTNode readNodeFromMemory(int seqNumber) {

        for (HTNode node : tree.getLatestBranch()) {
            if (node.getSequenceNumber() == seqNumber) {
                return node;
            }
        }
        return null;
    }
    */

    /**
     * This method here isn't private, if we know for sure the node cannot be in
     * memory it's a bit faster to use this directly (when opening a file from
     * disk for example)
     *
     * @throws ClosedChannelException
     *             Usually happens because the file was closed while we were
     *             reading. Instead of using a big reader-writer lock, we'll
     *             just catch this exception.
     */
    synchronized HTNode readNodeFromDisk(int seqNumber) throws ClosedChannelException {
        /* Do a cache lookup */
        int offset = seqNumber & (CACHE_SIZE - 1);
        HTNode readNode = fNodeCache[offset];
        if (readNode != null && readNode.getSequenceNumber() == seqNumber) {
          return readNode;
        }

        /* Lookup on disk */
        try {
            seekFCToNodePos(fcIn, seqNumber);
            readNode = HTNode.readNode(fConfig, fcIn);

            /* Put the node in the cache. */
            fNodeCache[offset] = readNode;
            return readNode;
        } catch (ClosedChannelException e) {
            throw e;
        } catch (IOException e) {
            /* Other types of IOExceptions shouldn't happen at this point though */
            e.printStackTrace();
            return null;
        }
    }

    void writeNode(HTNode node) {
        try {
            /* Insert the node into the cache. */
            int seqNumber = node.getSequenceNumber();
            int offset = seqNumber & (CACHE_SIZE - 1);
            fNodeCache[offset] = node;

            /* Position ourselves at the start of the node and write it */
            seekFCToNodePos(fcOut, seqNumber);
            node.writeSelf(fcOut);
        } catch (IOException e) {
            /* If we were able to open the file, we should be fine now... */
            e.printStackTrace();
        }
    }

    FileChannel getFcOut() {
        return this.fcOut;
    }

    FileInputStream supplyATReader(int node_offset) {
        try {
            /*
             * Position ourselves at the start of the Mapping section in the
             * file (which is right after the Blocks)
             */
            seekFCToNodePos(fcIn, node_offset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fis;
    }

    synchronized void closeFile() {
        try {
            fis.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized void deleteFile() {
        closeFile();

        File historyTreeFile = fConfig.getStateFile();
        if (!historyTreeFile.delete()) {
            /* We didn't succeed in deleting the file */
            //TODO log it?
        }
    }

    /**
     * Seek the given FileChannel to the position corresponding to the node that
     * has seqNumber
     *
     * @param fc
     * @param seqNumber
     * @throws IOException
     */
    private void seekFCToNodePos(FileChannel fc, int seqNumber)
            throws IOException {
        /*
         * Cast to (long) is needed to make sure the result is a long too and
         * doesn't get truncated
         */
        fc.position(HistoryTree.TREE_HEADER_SIZE
                + ((long) seqNumber)  * fConfig.getBlockSize());
    }

}
