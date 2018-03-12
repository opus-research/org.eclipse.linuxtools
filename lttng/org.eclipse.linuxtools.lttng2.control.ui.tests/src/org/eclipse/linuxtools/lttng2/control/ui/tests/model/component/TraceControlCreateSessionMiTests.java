package org.eclipse.linuxtools.lttng2.control.ui.tests.model.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;

/**
 * Machine interface Kernel session manipulation handling test cases.
 * LTTng 2.6
 */
public class TraceControlCreateSessionMiTests extends TraceControlCreateSessionTests {

    private static final String TEST_STREAM = "CreateSessionTestMi.cfg";

    @Override
    protected String getTestStream() {
        return TEST_STREAM;
    }

    @Override
    protected void ipv6NetworkSessionAssertion(TraceSessionComponent session) {
        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertEquals("tcp6://[ffff::eeee:dddd:cccc:0]:5342/mysession-20140820-153801 [data: 5343]", session.getSessionPath());
        assertTrue(session.isStreamedTrace());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
    }

    @Override
    protected void networkSessionAssertion(TraceSessionComponent session) {
        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertEquals("tcp4://172.0.0.1:1234/mysession-20140820-153527 [data: 2345]", session.getSessionPath());
        assertTrue(session.isStreamedTrace());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
    }

    @Override
    protected void ctrlDataSessionAssertion(TraceSessionComponent session) {
        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertEquals("tcp4://172.0.0.1:5342/ [data: 5343]", session.getSessionPath());
        assertTrue(session.isStreamedTrace());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
    }

    @Override
    protected void fileSessionAssertion(TraceSessionComponent session) {
        // Verify that session was created
        assertNotNull(session);
        assertEquals("mysession", session.getName());
        assertEquals("/tmp", session.getSessionPath());
        assertTrue(!session.isStreamedTrace());
        assertEquals(TraceSessionState.INACTIVE, session.getSessionState());
    }
}
