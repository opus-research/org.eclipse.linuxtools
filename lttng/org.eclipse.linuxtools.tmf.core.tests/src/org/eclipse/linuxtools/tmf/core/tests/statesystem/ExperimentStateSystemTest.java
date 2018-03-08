/*******************************************************************************
 * Copyright (c) 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir and Geneviève Bastien - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statesystem;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Experiments can have state systems of their own, and so can the traces
 * underneath. These tests are there to make sure that both state systems are
 * built correctly, and only when needed.
 *
 * Note: each test need to be run in a thread otherwise we get illegal thread
 * access from the signal manager.
 *
 * @author Alexandre Montplaisir
 */
public class ExperimentStateSystemTest {

    private ExperimentStub fExp;
    private CtfTmfTraceStub fTrace1;
    private CtfTmfTraceStub fTrace2;

    /**
     * Class set-up. Making sure the test traces are present.
     *
     * To reduce execution time, the smallest ctf traces are taken for the tests
     */
    @BeforeClass
    public static void classSetUp() {
        assumeTrue(CtfTmfTestTrace.CYG_PROFILE.exists());
        assumeTrue(CtfTmfTestTrace.CYG_PROFILE_FAST.exists());
    }

    /**
     * Test set-up
     */
    @Before
    public void setUp() {
        /* Set up the traces */
        fTrace1 = new CtfTmfTraceStub();
        fTrace2 = new CtfTmfTraceStub();
        try {
            fTrace1.initTrace(null, CtfTmfTestTrace.CYG_PROFILE.getPath(), ITmfEvent.class);
            fTrace2.initTrace(null, CtfTmfTestTrace.CYG_PROFILE_FAST.getPath(), ITmfEvent.class);
        } catch (TmfTraceException e) {
            fail();
        }
        fExp = new ExperimentStub(ITmfEvent.class, "exp", new ITmfTrace[] { fTrace1, fTrace2 });

        /* Ensure that nothing is started at the moment */
        assertFalse(fExp.getStateProvider().isStarted());
        assertFalse(fTrace1.getStateProvider().isStarted());
        assertFalse(fTrace2.getStateProvider().isStarted());
    }

    /**
     * Test opening only one of the traces. The experiment should not build its
     * state system.
     */
    @Test
    public void testOnlyOneTrace() {
        final Thread thread = new Thread("Test Experiment State System") { //$NON-NLS-1$

            @Override
            public void run() {
                openTrace(fTrace1);
                fTrace1.getStateSystem().waitUntilBuilt();
            }
        };
        thread.start();

        /* Wait for thread to finish and do the asserts */
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }
        assertFalse(fExp.getStateProvider().isStarted());
        assertTrue(fTrace1.getStateProvider().isStarted());
        assertFalse(fTrace2.getStateProvider().isStarted());
    }

    /**
     * Test opening the experiment only. The experiment and both its traces
     * should buy their state system.
     */
    @Test
    public void testOnlyExperiment() {
        final Thread thread = new Thread("Test Experiment State System") { //$NON-NLS-1$

            @Override
            public void run() {
                openTrace(fExp);
                fExp.getStateSystem().waitUntilBuilt();
            }
        };
        thread.start();

        /* Wait for thread to finish and do the asserts */
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }

        assertTrue(fExp.getStateProvider().isStarted());
        assertTrue(fTrace1.getStateProvider().isStarted());
        assertTrue(fTrace2.getStateProvider().isStarted());
    }

    /**
     * Test opening one trace, and the experiment. The second trace should also
     * built its state system because of the experiment.
     */
    @Test
    public void testOneTraceAndExperiment() {
        final Thread thread = new Thread("Test Experiment State System") { //$NON-NLS-1$

            @Override
            public void run() {
                try {
                    System.out.println("One trace an experiment");
                    openTrace(fTrace1);
                    openTrace(fExp); // Let's test the concurrent requests too!
                    fTrace1.getStateSystem().waitUntilBuilt();
                    fExp.getStateSystem().waitUntilBuilt();
                } catch (Exception e) {
                    fail(e.getMessage());
                }
            }
        };
        thread.start();

        /* Wait for thread to finish and do the asserts */
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }

        assertTrue(fExp.getStateProvider().isStarted());
        assertTrue(fTrace1.getStateProvider().isStarted());
        assertTrue(fTrace1.getStateProvider().isStarted());
    }

    /**
     * Test opening both traces. The experiment should not built its state
     * system.
     */
    @Test
    public void testTwoTraces() {
        final Thread thread = new Thread("Test Experiment State System") { //$NON-NLS-1$

            @Override
            public void run() {
                openTrace(fTrace1);
                openTrace(fTrace2);
                fTrace1.getStateSystem().waitUntilBuilt();
                fTrace2.getStateSystem().waitUntilBuilt();
            }
        };
        thread.start();

        /* Wait for thread to finish and do the asserts */
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }

        assertFalse(fExp.getStateProvider().isStarted());
        assertTrue(fTrace1.getStateProvider().isStarted());
        assertTrue(fTrace2.getStateProvider().isStarted());
    }

    /**
     * Build ALL THE THINGS!
     */
    @Test
    public void testEverything() {
        final Thread thread = new Thread("Test Experiment State System") { //$NON-NLS-1$

            @Override
            public void run() {
                openTrace(fTrace1);
                openTrace(fTrace2);
                openTrace(fExp);
                fExp.getStateSystem().waitUntilBuilt();
                fTrace1.getStateSystem().waitUntilBuilt();
                fTrace2.getStateSystem().waitUntilBuilt();
            }
        };
        thread.start();

        /* Wait for thread to finish and do the asserts */
        try {
            thread.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }

        assertTrue(fExp.getStateProvider().isStarted());
        assertTrue(fTrace1.getStateProvider().isStarted());
        assertTrue(fTrace2.getStateProvider().isStarted());
    }

    // ------------------------------------------------------------------------
    // Helper methods and classes
    // ------------------------------------------------------------------------

    private void openTrace(final ITmfTrace trace) {
        /* This should launch the construction of the trace's state system(s) */
        TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, trace, null));
    }

    /**
     * Trace type stub, building a fake state provider.
     */
    private class CtfTmfTraceStub extends CtfTmfTrace {

        private static final String STATE_ID = "org.eclipse.linuxtools.tmf.core.tests.statesystem.ExperimentStateSystemTest.CtfTmfTraceStub";

        private final StateProviderStub stateProvider = new StateProviderStub(this);

        /*
         * Since there is only one stateProvider for the trace instead of
         * building a new one at each call to buildStateSystem, we need this
         * safe guard so we don't try to start a state provider again
         */
        private boolean fStarted = false;

        public ITmfStateSystem getStateSystem() {
            return getStateSystems().get(STATE_ID);
        }

        public StateProviderStub getStateProvider() {
            return stateProvider;
        }

        @Override
        public IStatus buildStateSystem() {
            if (!fStarted) {
                fStarted = true;
                ITmfStateSystem ss = TmfStateSystemFactory.newInMemHistory(stateProvider, false);
                fStateSystems.put(STATE_ID, ss);
            }
            return Status.OK_STATUS;
        }
    }

    private class ExperimentStub extends TmfExperiment {

        private static final String STATE_ID = "org.eclipse.linuxtools.tmf.core.tests.statesystem.ExperimentStateSystemTest.ExperimentStub";

        private final StateProviderStub stateProvider = new StateProviderStub(this);

        /*
         * Since there is only one stateProvider for the trace instead of
         * building a new one at each call to buildStateSystem, we need this
         * safe guard so we don't try to start a state provider again
         */
        private boolean fStarted = false;

        public ExperimentStub(Class<? extends ITmfEvent> type, String id, ITmfTrace[] traces) {
            super(type, id, traces);
        }

        public ITmfStateSystem getStateSystem() {
            return getStateSystems().get(STATE_ID);
        }

        public StateProviderStub getStateProvider() {
            return stateProvider;
        }

        @Override
        public IStatus buildStateSystem() {
            if (!fStarted) {
                fStarted = true;
                ITmfStateSystem ss = TmfStateSystemFactory.newInMemHistory(stateProvider, false);
                fStateSystems.put(STATE_ID, ss);
            }
            return Status.OK_STATUS;
        }
    }

    private class StateProviderStub extends AbstractTmfStateProvider {

        private static final String ID = "stub";
        private boolean isStarted = false;

        public StateProviderStub(ITmfTrace trace) {
            super(trace, ITmfEvent.class, ID + trace.getName());
        }

        public boolean isStarted() {
            return isStarted;
        }

        @Override
        public int getVersion() {
            return 0;
        }

        @Override
        public ITmfStateProvider getNewInstance() {
            return new StateProviderStub(getTrace());
        }

        @Override
        protected void eventHandle(ITmfEvent event) {
            if (isStarted == false) {
                isStarted = true;
            }
        }
    }

}
