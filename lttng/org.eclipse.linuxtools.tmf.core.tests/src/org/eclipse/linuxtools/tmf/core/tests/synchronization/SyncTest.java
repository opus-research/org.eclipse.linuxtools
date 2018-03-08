/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.synchronization;

import static org.junit.Assert.*;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.Messages;
import org.eclipse.linuxtools.tmf.core.synchronization.SyncAlgorithmFullyIncremental;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.linuxtools.tmf.core.synchronization.TmfTimestampTransform;
import org.eclipse.linuxtools.tmf.tests.stubs.event.TmfSyncEventStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link SynchronizationAlgorithm} and its descendants
 *
 * @author gbastien
 */
@SuppressWarnings("nls")
public class SyncTest {

    private TmfTraceStub t1, t2 ;

    /**
     * Initializing the traces
     */
    @Before
    public void init() {
        t1 = new TmfTraceStub();
        t1.init("t1");
        t2 = new TmfTraceStub();
        t2.init("t2");
    }

    /**
     * Testing fully incremental algorithm
     */
    @Test
    public void testFullyIncremental() {

        SynchronizationAlgorithm syncAlgo = new SyncAlgorithmFullyIncremental();

        assertEquals(Messages.SyncAlgorithmFullyIncremental_absent, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));
        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(1)),
              new TmfSyncEventStub(t1, new TmfTimestamp(1))
        ));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 1 ] " +
          "alpha 1 beta 0 ]", syncAlgo.toString());
        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(1)),
              new TmfSyncEventStub(t2, new TmfTimestamp(3))
        ));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 2 ] " +
          "alpha 1 beta 0 ]", syncAlgo.toString());
        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(2)),
              new TmfSyncEventStub(t1, new TmfTimestamp(3))
        ));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 3 ] " +
          "alpha 1 beta 0.5 ]", syncAlgo.toString());
        assertEquals(Messages.SyncAlgorithmFullyIncremental_approx, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(3)),
              new TmfSyncEventStub(t2, new TmfTimestamp(5))
        ));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 4 ] " +
          "alpha 0.75 beta 1.25 ]", syncAlgo.toString());
        assertEquals(Messages.SyncAlgorithmFullyIncremental_accurate, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(4)),
              new TmfSyncEventStub(t2, new TmfTimestamp(8))
        ));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 5 ] " +
          "alpha 0.75 beta 1.25 ]", syncAlgo.toString());
        assertEquals(Messages.SyncAlgorithmFullyIncremental_accurate, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(4)),
              new TmfSyncEventStub(t1, new TmfTimestamp(5))
        ));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 6 ] " +
          "alpha 1.125 beta 0.875 ]", syncAlgo.toString());
        assertEquals(Messages.SyncAlgorithmFullyIncremental_accurate, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(4)),
              new TmfSyncEventStub(t1, new TmfTimestamp(6))
        ));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 7 ] " +
          "alpha 1.125 beta 0.875 ]", syncAlgo.toString());
        assertEquals(Messages.SyncAlgorithmFullyIncremental_accurate, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(6)),
              new TmfSyncEventStub(t2, new TmfTimestamp(7))
        ));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 8 ] " +
          "alpha 0.725 beta 1.275 ]", syncAlgo.toString());
        assertEquals(Messages.SyncAlgorithmFullyIncremental_accurate, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        ITmfTimestampTransform tt2 = syncAlgo.getTimestampTransform(t2);
        ITmfTimestampTransform tt1 = syncAlgo.getTimestampTransform(t1);

        assertEquals(syncAlgo.getTimestampTransform("t1"), tt1);
        assertEquals(TmfTimestampTransform.IDENTITY, tt1);
        assertEquals(syncAlgo.getTimestampTransform("t2"), tt2);

        /* Make the two hulls intersect */
        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(7)),
              new TmfSyncEventStub(t2, new TmfTimestamp(4))
        ));
        syncAlgo.addMatch(
              new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(7)),
              new TmfSyncEventStub(t1, new TmfTimestamp(3))
        ));
        assertEquals(Messages.SyncAlgorithmFullyIncremental_fail, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));
    }

    /**
     * Testing the packet matching
     */
    @Test
    public void testOneHull() {

        SynchronizationAlgorithm syncAlgo = new SyncAlgorithmFullyIncremental();

        assertEquals(Messages.SyncAlgorithmFullyIncremental_absent, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(1)),
                new TmfSyncEventStub(t2, new TmfTimestamp(3)))
                );
        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(2)),
                new TmfSyncEventStub(t2, new TmfTimestamp(5)))
                );

        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(3)),
                new TmfSyncEventStub(t2, new TmfTimestamp(5)))
                );
        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(4)),
                new TmfSyncEventStub(t2, new TmfTimestamp(7)))
                );
        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 4 ] " +
                "alpha 1 beta 0 ]", syncAlgo.toString());

    }

    /**
     * Testing the packet matching
     */
    @Test
    public void testDisjoint() {

        SynchronizationAlgorithm syncAlgo = new SyncAlgorithmFullyIncremental();

        assertEquals(Messages.SyncAlgorithmFullyIncremental_absent, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(1)),
                new TmfSyncEventStub(t2, new TmfTimestamp(3)))
                );
        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(2)),
                new TmfSyncEventStub(t2, new TmfTimestamp(5)))
                );

        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(3)),
                new TmfSyncEventStub(t2, new TmfTimestamp(5)))
                );
        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t1, new TmfTimestamp(4)),
                new TmfSyncEventStub(t2, new TmfTimestamp(7)))
                );
        assertEquals(Messages.SyncAlgorithmFullyIncremental_incomplete, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 4 ] " +
                "alpha 1 beta 0 ]", syncAlgo.toString());

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(7)),
                new TmfSyncEventStub(t1, new TmfTimestamp(6)))
                );
        assertEquals(Messages.SyncAlgorithmFullyIncremental_approx, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(8)),
                new TmfSyncEventStub(t1, new TmfTimestamp(6)))
                );
        assertEquals(Messages.SyncAlgorithmFullyIncremental_approx, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));

        syncAlgo.addMatch(
                new TmfEventDependency(new TmfSyncEventStub(t2, new TmfTimestamp(10)),
                new TmfSyncEventStub(t1, new TmfTimestamp(8)))
                );
        assertEquals(Messages.SyncAlgorithmFullyIncremental_approx, syncAlgo.getStats().get(Messages.SyncAlgorithmFullyIncremental_quality));
        assertEquals("SyncAlgorithmFullyIncremental [ parent: TmfEventMatches [ Number of matches found: 7 ] " +
                "alpha 1 beta 2.5 ]", syncAlgo.toString());
    }
}
