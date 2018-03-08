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

import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTraces;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
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
 * access from either the signal manager.
 *
 * @author Alexandre Montplaisir
 */
public class ExperimentStateSystemTest {

    private ExperimentStub exp;
    private CtfTmfTraceStub trace1;
    private CtfTmfTraceStub trace2;

    /**
     * Class set-up. Making sure the test traces are present.
     */
    @BeforeClass
    public static void classSetUp() {
        assumeTrue(CtfTmfTestTraces.tracesExist());
    }

    /**
     * Test set-up
     */
    @Before
    public void setUp() {
        /* Set up the traces */
        trace1 = new CtfTmfTraceStub();
        trace2 = new CtfTmfTraceStub();
        try {
            trace1.initTrace(null, CtfTestTraces.getTestTracePath(0), ITmfEvent.class);
            trace2.initTrace(null, CtfTestTraces.getTestTracePath(1), ITmfEvent.class);
        } catch (TmfTraceException e) {
            fail();
        }
        exp = new ExperimentStub(ITmfEvent.class, "exp", new ITmfTrace[] { trace1, trace2 });

        /* Ensure that nothing is started at the moment */
        assertFalse(exp.getStateProvider().isStarted());
        assertFalse(trace1.getStateProvider().isStarted());
        assertFalse(trace2.getStateProvider().isStarted());
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
                openTrace(trace1);
                trace1.getStateSystem().waitUntilBuilt();

                assertFalse(exp.getStateProvider().isStarted());
                assertTrue(trace1.getStateProvider().isStarted());
                assertFalse(trace2.getStateProvider().isStarted());
            }
        };
        thread.start();
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
                openTrace(exp);
                exp.getStateSystem().waitUntilBuilt();

                assertTrue(exp.getStateProvider().isStarted());
                assertTrue(trace1.getStateProvider().isStarted());
                assertTrue(trace2.getStateProvider().isStarted());
            }
        };
        thread.start();
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
                openTrace(trace1);
                openTrace(exp); // Let's test the concurrent requests too!
                trace1.getStateSystem().waitUntilBuilt();
                exp.getStateSystem().waitUntilBuilt();

                assertTrue(exp.getStateProvider().isStarted());
                assertTrue(trace1.getStateProvider().isStarted());
                assertTrue(trace1.getStateProvider().isStarted());
            }
        };
        thread.start();
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
                openTrace(trace1);
                openTrace(trace2);
                trace1.getStateSystem().waitUntilBuilt();
                trace2.getStateSystem().waitUntilBuilt();

                assertFalse(exp.getStateProvider().isStarted());
                assertTrue(trace1.getStateProvider().isStarted());
                assertTrue(trace2.getStateProvider().isStarted());
            }
        };
        thread.start();
    }

    /**
     * Build ALL THE THINGS!
     */
    @Test
    public void testEverything() {
        final Thread thread = new Thread("Test Experiment State System") { //$NON-NLS-1$

            @Override
            public void run() {
                // openTrace(trace1);
                // openTrace(trace2);
                openTrace(exp);
                exp.getStateSystem().waitUntilBuilt();
                trace1.getStateSystem().waitUntilBuilt();
                trace2.getStateSystem().waitUntilBuilt();

                assertTrue(exp.getStateProvider().isStarted());
                assertTrue(trace1.getStateProvider().isStarted());
                assertTrue(trace2.getStateProvider().isStarted());
            }
        };
        thread.start();
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

        public ITmfStateSystem getStateSystem() {
            return getStateSystems().get(STATE_ID);
        }

        public StateProviderStub getStateProvider() {
            return stateProvider;
        }

        @Override
        public void buildStateSystem() {
            ITmfStateSystem ss = TmfStateSystemFactory.newInMemHistory(stateProvider, false);
            fStateSystems.put(STATE_ID, ss);
        }
    }

    private class ExperimentStub extends TmfExperiment {

        private static final String STATE_ID = "org.eclipse.linuxtools.tmf.core.tests.statesystem.ExperimentStateSystemTest.ExperimentStub";

        private final StateProviderStub stateProvider = new StateProviderStub(this);

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
        public void buildStateSystem() {
            ITmfStateSystem ss = TmfStateSystemFactory.newInMemHistory(stateProvider, false);
            fStateSystems.put(STATE_ID, ss);
        }
    }

    private class StateProviderStub extends AbstractTmfStateProvider {

        private static final String id = "stub";
        private boolean isStarted = false;

        public StateProviderStub(ITmfTrace trace) {
            super(trace, ITmfEvent.class, id);
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
