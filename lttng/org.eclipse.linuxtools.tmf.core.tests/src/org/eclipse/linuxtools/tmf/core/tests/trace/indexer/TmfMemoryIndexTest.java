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

package org.eclipse.linuxtools.tmf.core.tests.trace.indexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Random;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.indexer.TmfMemoryIndex;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for the TmfMemoryIndex class
 */
public class TmfMemoryIndexTest {
    private static final int CHECKPOINTS_INSERT_NUM = 50000;
    private TmfTraceStub fTrace;
    TmfMemoryIndex fMemoryIndex = null;

    /**
     * Setup the test. Make sure the index is deleted.
     */
    @Before
    public void setUp() {
        fTrace = new TmfTraceStub();
    }

    /**
     * Tear down the test. Make sure the index is deleted.
     */
    @After
    public void tearDown() {
        fTrace.dispose();
        fTrace = null;
        if (fMemoryIndex != null) {
            fMemoryIndex.dispose();
        }
    }

    /**
     * Test constructing a new TmfMemoryIndex
     */
    @Test
    public void testConstructor() {
        fMemoryIndex = createMemoryIndex();
        assertTrue(fMemoryIndex.isCreatedFromScratch());
        fMemoryIndex.dispose();
    }

    /**
     * Test a new TmfMemoryIndex is considered created from scratch
     */
    @Test
    public void testIsCreatedFromScratch() {
        fMemoryIndex = createMemoryIndex();
        assertTrue(fMemoryIndex.isCreatedFromScratch());
        fMemoryIndex.dispose();

        fMemoryIndex = createMemoryIndex();
        assertTrue(fMemoryIndex.isCreatedFromScratch());
        fMemoryIndex.dispose();
    }

    /**
     * Test a single insertion
     */
    @Test
    public void testInsert() {
        fMemoryIndex = createMemoryIndex();
        TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345), new TmfLongLocation(123456L));
        fMemoryIndex.add(checkpoint);

        ITmfCheckpoint indexCheckpoint = fMemoryIndex.get(0);
        assertEquals(checkpoint, indexCheckpoint);

        int found = fMemoryIndex.binarySearch(checkpoint);
        assertEquals(0, found);

        fMemoryIndex.dispose();
    }

    /**
     * Test many checkpoint insertions.
     * Make sure they can be found after inserting
     */
    @Test
    public void testInsertAlot() {
        fMemoryIndex = createMemoryIndex();
        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345 + i), new TmfLongLocation(123456L + i));
            checkpoint.setCheckpointRank(i);
            fMemoryIndex.add(checkpoint);
        }

        boolean random = false;
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            if (random) {
                Random rand = new Random();
                list.add(rand.nextInt(CHECKPOINTS_INSERT_NUM));
            } else {
                list.add(i);
            }
        }

        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            Integer randomCheckpoint = list.get(i);
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345 + randomCheckpoint), new TmfLongLocation(123456L + randomCheckpoint));

            ITmfCheckpoint indexCheckpoint = fMemoryIndex.get(randomCheckpoint);
            assertEquals(checkpoint, indexCheckpoint);
        }

        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            Integer randomCheckpoint = list.get(i);
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345 + randomCheckpoint), new TmfLongLocation(123456L + randomCheckpoint));

            int found = fMemoryIndex.binarySearch(checkpoint);
            assertEquals(randomCheckpoint.intValue(), found);
        }

        fMemoryIndex.dispose();
    }

    /**
     * Test many checkpoint insertions using the same timestamp.
     */
    @Test
    public void testInsertSameTimestamp() {
        fMemoryIndex = createMemoryIndex();
        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345), new TmfLongLocation(123456L + i));
            checkpoint.setCheckpointRank(i);
            fMemoryIndex.add(checkpoint);
        }

        boolean random = false;
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            if (random) {
                Random rand = new Random();
                list.add(rand.nextInt(CHECKPOINTS_INSERT_NUM));
            } else {
                list.add(i);
            }
        }

        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            Integer randomCheckpoint = list.get(i);
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345), new TmfLongLocation(123456L + randomCheckpoint));

            ITmfCheckpoint indexCheckpoint = fMemoryIndex.get(randomCheckpoint);
            assertEquals(checkpoint, indexCheckpoint);
        }

        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            Integer randomCheckpoint = list.get(i);
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345), new TmfLongLocation(123456L + randomCheckpoint));

            int found = fMemoryIndex.binarySearch(checkpoint);
            assertEquals(randomCheckpoint.intValue(), found);
        }

        fMemoryIndex.dispose();
    }

    /**
     * Tests that binarySearch find the correct checkpoint and ends with a perfect match
     */
    @Test
    public void testBinarySearch() {
        fMemoryIndex = createMemoryIndex();
        for (long i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(i), new TmfLongLocation(i));
            fMemoryIndex.add(checkpoint);
        }

        TmfCheckpoint expectedCheckpoint = new TmfCheckpoint(new TmfTimestamp(122), new TmfLongLocation(122L));
        int expectedRank = 122;

        int rank = fMemoryIndex.binarySearch(expectedCheckpoint);
        ITmfCheckpoint found = fMemoryIndex.get(rank);

        assertEquals(expectedRank, rank);
        assertEquals(found, expectedCheckpoint);
        fMemoryIndex.dispose();
    }

    /**
     * Tests that binarySearch find the correct checkpoint when the time stamp is between checkpoints
     */
    @Test
    public void testBinarySearchFindInBetween() {
        fMemoryIndex = createMemoryIndex();
        for (long i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(2 * i), new TmfLongLocation(2 * i));
            fMemoryIndex.add(checkpoint);
        }

        TmfCheckpoint searchedCheckpoint = new TmfCheckpoint(new TmfTimestamp(123), new TmfLongLocation(123L));
        TmfCheckpoint expectedCheckpoint = new TmfCheckpoint(new TmfTimestamp(122), new TmfLongLocation(122L));
        int expectedInsertionPoint = 61;
        int expectedRank = -(expectedInsertionPoint + 2);

        int rank = fMemoryIndex.binarySearch(searchedCheckpoint);
        assertEquals(expectedRank, rank);

        ITmfCheckpoint found = fMemoryIndex.get(expectedInsertionPoint);

        assertEquals(found, expectedCheckpoint);
        fMemoryIndex.dispose();
    }

    /**
     * Tests that binarySearch finds the correct checkpoint when searching for a checkpoint with a null location.
     * It should return the previous checkpoint from the first checkpoint that matches the timestamp.
     */
    @Test
    public void testFindInBetweenSameTimestamp() {
        fMemoryIndex = createMemoryIndex();
        int checkpointNum = 0;
        for (; checkpointNum < 100; checkpointNum++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(0), new TmfLongLocation((long)checkpointNum));
            checkpoint.setCheckpointRank(checkpointNum);
            fMemoryIndex.add(checkpoint);
        }

        for (; checkpointNum < 200; checkpointNum++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(1), new TmfLongLocation((long)checkpointNum));
            checkpoint.setCheckpointRank(checkpointNum);
            fMemoryIndex.add(checkpoint);
        }

        final TmfCheckpoint searchedCheckpoint = new TmfCheckpoint(new TmfTimestamp(1), null);
        final TmfCheckpoint expectedCheckpoint = new TmfCheckpoint(new TmfTimestamp(0), new TmfLongLocation(99L));

        int expectedInsertionPoint = 99;
        int expectedRank = -(expectedInsertionPoint + 2);

        int rank = fMemoryIndex.binarySearch(searchedCheckpoint);
        assertEquals(expectedRank, rank);
        ITmfCheckpoint found = fMemoryIndex.get(expectedInsertionPoint);

        assertEquals(expectedRank, rank);
        assertEquals(found, expectedCheckpoint);
        fMemoryIndex.dispose();
    }

    private TmfMemoryIndex createMemoryIndex() {
        return new TmfMemoryIndex(fTrace);
    }

    /**
     * Test setSize, size
     */
    @Test
    public void testSetGetSize() {
        fMemoryIndex = createMemoryIndex();
        assertEquals(0, fMemoryIndex.size());
        int expected = CHECKPOINTS_INSERT_NUM;
        for (int i = 0; i < expected; ++i) {
            fMemoryIndex.add(new TmfCheckpoint(new TmfTimestamp(0), new TmfLongLocation(0L)));
        }
        assertEquals(expected, fMemoryIndex.size());
        fMemoryIndex.dispose();
    }

    /**
     * Test dispose
     */
    @Test
    public void testDispose() {
        fMemoryIndex = createMemoryIndex();
        fMemoryIndex.dispose();
        assertTrue(fMemoryIndex.isEmpty());
    }
}
