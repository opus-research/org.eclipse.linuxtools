/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Bernd Hufmann - Use state system analysis module instead of factory
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * State system tests using a partial history.
 *
 * @author Alexandre Montplaisir
 */
@Ignore
public class PartialStateSystemTest extends StateSystemTest {

    private static final @NonNull String TEST_FILE_NAME = "test-partial";

    private static File stateFile;
    private static TestLttngKernelAnalysisModule module;


    /**
     * Initialization
     */
    @BeforeClass
    public static void initialize() {
        assumeTrue(testTrace.exists());
        stateFile = new File(TmfTraceManager.getSupplementaryFileDir(testTrace.getTrace()) + TEST_FILE_NAME);
        if (stateFile.exists()) {
            stateFile.delete();
        }

        module = new TestLttngKernelAnalysisModule(TEST_FILE_NAME);
        try {
            module.setTrace(testTrace.getTrace());
        } catch (TmfAnalysisException e) {
            fail();
        }
        module.schedule();
        assertTrue(module.waitForCompletion());
        ssq = module.getStateSystem();

        assertNotNull(ssq);
    }

    /**
     * Class clean-up
     */
    @AfterClass
    public static void tearDownClass() {
        module.close();
        stateFile.delete();
    }

    /**
     * Partial histories cannot get the intervals' end times. The fake value that
     * is returned is equal to the query's timestamp. So override this here
     * so that {@link #testFullQueryThorough} keeps working.
     */
    @Override
    protected long getEndTimes(int idx) {
        return interestingTimestamp1;
    }

    // ------------------------------------------------------------------------
    // Skip tests using single-queries (unsupported in partial history)
    // ------------------------------------------------------------------------

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testSingleQuery1() {
        super.testSingleQuery1();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQuery1() {
        super.testRangeQuery1();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQuery2() {
        super.testRangeQuery2();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQuery3() {
        super.testRangeQuery3();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testSingleQueryInvalidTime1() throws TimeRangeException {
        super.testSingleQueryInvalidTime1();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testSingleQueryInvalidTime2() throws TimeRangeException {
        super.testSingleQueryInvalidTime2();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQueryInvalidTime1() throws TimeRangeException {
        super.testRangeQueryInvalidTime1();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testRangeQueryInvalidTime2() throws TimeRangeException {
        super.testRangeQueryInvalidTime2();
    }

    @NonNullByDefault
    private static class TestLttngKernelAnalysisModule extends TmfStateSystemAnalysisModule {

        private final String htFileName;

        /**
         * Constructor adding the views to the analysis
         * @param htFileName
         *      The History File Name
         */
        public TestLttngKernelAnalysisModule(String htFileName) {
            super();
            this.htFileName = htFileName;
        }

        @Override
        public void setTrace(@Nullable ITmfTrace trace) throws TmfAnalysisException {
            if (!(trace instanceof CtfTmfTrace)) {
                throw new IllegalStateException("TestLttngKernelAnalysisModule: trace should be of type CtfTmfTrace"); //$NON-NLS-1$
            }
            super.setTrace(trace);
        }

        @Override
        protected ITmfStateProvider createStateProvider() {
            return new LttngKernelStateProvider(getTrace());
        }

        @Override
        protected StateSystemBackendType getBackendType() {
            return StateSystemBackendType.PARTIAL;
        }

        @Override
        protected String getSsFileName() {
            return htFileName;
        }

    }
}
