/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CtfKernelStateInput;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystemManager;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * State system tests using a partial history.
 *
 * @author Alexandre Montplaisir
 */
public class PartialStateSystemTest extends StateSystemTest {

    /**
     * Initialization
     */
    @BeforeClass
    public static void initialize() {
        assumeTrue(CtfTmfTestTraces.tracesExist());
        File stateFile = null;
        try {
            stateFile = File.createTempFile("test-partial", ".ht"); //$NON-NLS-1$ //$NON-NLS-2$
            stateFile.deleteOnExit();

            input = new CtfKernelStateInput(CtfTmfTestTraces.getTestTrace(TRACE_INDEX));
            ssq = StateSystemManager.newPartialHistory(stateFile, input, true);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TmfTraceException e) {
            e.printStackTrace();
        }
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
}
