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

package org.eclipse.linuxtools.lttng2.kernel.core.tests.event.matchandsync;

import static org.junit.Assert.*;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.junit.Before;
import org.junit.Test;



/**
 * Tests for experiment syncing
 *
 * @author gbastien
 */
@SuppressWarnings("nls")
public class ExperimentSyncTest {

    private static final String EXPERIMENT   = "MyExperiment";
    private static int          BLOCK_SIZE   = 1000;

    private static ITmfTrace[] fTraces;
    private static TmfExperiment fExperiment;

    /**
     * Testing the constructors
     */
    @Before
    public void setUp() {
        try {
            CtfTmfTrace trace1 = MatchAndSyncTestFiles.getTestTrace(1);
            CtfTmfTrace trace2 = MatchAndSyncTestFiles.getTestTrace(2);

            fTraces = new CtfTmfTrace[2];
            fTraces[0] = trace1;
            fTraces[1] = trace2;

            fExperiment = new TmfExperiment(trace1.getEventType(), EXPERIMENT, fTraces, BLOCK_SIZE);

        } catch (TmfTraceException e) {
            e.printStackTrace();
            fail ("Cannot generate traces");
        }
    }

    /**
     * Testing experiment synchronization
     */
    @Test
    public void testExperimentSync() {
        try {
            SynchronizationAlgorithm syncAlgo = fExperiment.synchronize(true);

            ITmfTimestampTransform tt1, tt2;

            tt1 = syncAlgo.getTimestampTransform(fTraces[0]);
            tt2 = syncAlgo.getTimestampTransform(fTraces[1]);

            fTraces[0].setTimestampTransform(tt1);
            fTraces[1].setTimestampTransform(tt2);

            assertEquals("TmfTimestampTransform [ IDENTITY ]", tt2.toString());
            assertEquals("TmfTimestampLinear [ alpha = 0.9999413783703139011056845831168394, beta = 79796507913179.33347660124688298171 ]", tt1.toString());

            assertEquals(syncAlgo.getTimestampTransform(fTraces[0].getName()),fTraces[0].getTimestampTransform());
            assertEquals(syncAlgo.getTimestampTransform(fTraces[1].getName()),fTraces[1].getTimestampTransform());

        } catch (TmfTraceException e) {
            fail("Exception thrown in experiment synchronization " + e.getMessage());
        }
    }
}
