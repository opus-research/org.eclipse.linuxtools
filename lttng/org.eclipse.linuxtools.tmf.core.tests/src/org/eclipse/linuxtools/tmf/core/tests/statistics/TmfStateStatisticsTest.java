/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statistics;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStateStatistics;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.junit.BeforeClass;

/**
 * Unit tests for the {@link TmfStateStatistics}
 *
 * @author Alexandre Montplaisir
 */
public class TmfStateStatisticsTest extends TmfStatisticsTest {

    /**
     * Set up the fixture (build the state history, etc.) once for all tests.
     */
    @BeforeClass
    public static void setUpClass() {
        assumeTrue(CtfTmfTestTraces.tracesExist());
        try {
            File fullFile = File.createTempFile("stats-test-full", ".ht");
            File partialFile = File.createTempFile("stats-tests-partial", ".ht");
            fullFile.deleteOnExit();
            partialFile.deleteOnExit();
            CtfTmfTrace trace = CtfTmfTestTraces.getTestTrace(TRACE_INDEX);
            backend = new TmfStateStatistics(trace, fullFile, partialFile);
        } catch (TmfTraceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
