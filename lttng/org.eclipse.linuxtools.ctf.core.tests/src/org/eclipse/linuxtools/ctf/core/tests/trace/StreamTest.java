package org.eclipse.linuxtools.ctf.core.tests.trace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.tests.TestParams;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.core.trace.StreamInput;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The class <code>StreamTest</code> contains tests for the class
 * <code>{@link Stream}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
public class StreamTest {

    private Stream fixture;

    /**
     * Launch the test.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new org.junit.runner.JUnitCore().run(StreamTest.class);
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFReaderException
     */
    @Before
    public void setUp() throws CTFReaderException {
        fixture = new Stream(TestParams.createTrace());
        fixture.setEventContext(new StructDeclaration(1L));
        fixture.setPacketContext(new StructDeclaration(1L));
        fixture.setEventHeader(new StructDeclaration(1L));
        fixture.setId(1L);
        fixture.addInput(new StreamInput(new Stream(TestParams.createTrace()),
                (FileChannel) null, TestParams.getEmptyFile()));
    }

    /**
     * Perform post-test clean-up.
     */
    @After
    public void tearDown() {
        // Add additional tear down code here
    }

    /**
     * Run the Stream(CTFTrace) constructor test.
     *
     * @throws CTFReaderException
     */
    @Test
    public void testStream() throws CTFReaderException {
        CTFTrace trace = TestParams.createTrace();
        Stream result = new Stream(trace);
        assertNotNull(result);
    }

    /**
     * Run the void addEvent(EventDeclaration) method test with the basic
     * event.
     * @throws ParseException
     */
    @Test
    public void testAddEvent_base() throws ParseException {
        EventDeclaration event = new EventDeclaration();
        fixture.addEvent(event);
    }

    /**
     * Run the boolean eventContextIsSet() method test.
     */
    @Test
    public void testEventContextIsSet() {
        assertTrue(fixture.isEventContextSet());
    }
    /**
     * Run the boolean eventContextIsSet() method test.
     */
    @Test
    public void testToString() {
        assertNotNull(fixture.toString());
    }

    /**
     * Run the boolean eventHeaderIsSet() method test.
     */
    @Test
    public void testEventHeaderIsSet() {
        assertTrue(fixture.isEventHeaderSet());
    }

    /**
     * Run the StructDeclaration getEventContextDecl() method test.
     */
    @Test
    public void testGetEventContextDecl() {
        assertNotNull(fixture.getEventContextDecl());
    }

    /**
     * Run the StructDeclaration getEventHeaderDecl() method test.
     */
    @Test
    public void testGetEventHeaderDecl() {
        assertNotNull(fixture.getEventHeaderDecl());
    }

    /**
     * Run the HashMap<Long, EventDeclaration> getEvents() method test.
     */
    @Test
    public void testGetEvents() {
        HashMap<Long, IEventDeclaration> result = fixture.getEvents();
        assertNotNull(result);
    }

    /**
     * Run the Long getId() method test.
     */
    @Test
    public void testGetId() {
        Long result = fixture.getId();
        assertNotNull(result);
    }

    /**
     * Run the StructDeclaration getPacketContextDecl() method test.
     */
    @Test
    public void testGetPacketContextDecl() {
        StructDeclaration result = fixture.getPacketContextDecl();
        assertNotNull(result);
    }

    /**
     * Run the Set<StreamInput> getStreamInputs() method test.
     */
    @Test
    public void testGetStreamInputs() {
        Set<StreamInput> result = fixture.getStreamInputs();
        assertNotNull(result);
    }

    /**
     * Run the CTFTrace getTrace() method test.
     */
    @Test
    public void testGetTrace() {
        CTFTrace result = fixture.getTrace();
        assertNotNull(result);
    }

    /**
     * Run the boolean idIsSet() method test.
     */
    @Test
    public void testIdIsSet() {
        boolean result = fixture.isIdSet();
        assertTrue(result);
    }

    /**
     * Run the boolean packetContextIsSet() method test.
     */
    @Test
    public void testPacketContextIsSet() {
        boolean result = fixture.isPacketContextSet();
        assertTrue(result);
    }


    /**
     * Run the void setEventContext(StructDeclaration) method test.
     */
    @Test
    public void testSetEventContext() {
        StructDeclaration eventContext = new StructDeclaration(1L);
        fixture.setEventContext(eventContext);
    }

    /**
     * Run the void setEventHeader(StructDeclaration) method test.
     */
    @Test
    public void testSetEventHeader() {
        StructDeclaration eventHeader = new StructDeclaration(1L);
        fixture.setEventHeader(eventHeader);
    }

    /**
     * Run the void setId(long) method test.
     */
    @Test
    public void testSetId() {
        long id = 1L;
        fixture.setId(id);
    }

    /**
     * Run the void setPacketContext(StructDeclaration) method test.
     */
    @Test
    public void testSetPacketContext() {
        StructDeclaration packetContext = new StructDeclaration(1L);
        fixture.setPacketContext(packetContext);
    }
}