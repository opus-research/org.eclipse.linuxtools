package org.eclipse.linuxtools.lttng.core.tests.trace;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngLocation;
import org.eclipse.linuxtools.internal.lttng.core.trace.LTTngTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.osgi.framework.FrameworkUtil;

/*
 Functions tested here :
 	public LTTngTrace(String path) throws Exception
    public LTTngTrace(String path, boolean skipIndexing) throws Exception

    public TmfTraceContext seekLocation(Object location) {
    public TmfTraceContext seekEvent(TmfTimestamp timestamp) {
    public TmfTraceContext seekEvent(long position) {

    public TmfEvent getNextEvent(TmfTraceContext context) {
    public Object getCurrentLocation() {

    public LttngEvent parseEvent(TmfTraceContext context) {

    public int getCpuNumber() {
 */

@SuppressWarnings("nls")
public class LTTngTraceTest extends TestCase {

    private final static String tracepath1="traceset/trace-15316events_nolost_newformat";
    private final static String wrongTracePath="/somewhere/that/does/not/exist";

    private final static int 	traceCpuNumber=1;

    private final static boolean skipIndexing=true;

    private final static long   firstEventTimestamp = 13589759412128L;
    private final static long   secondEventTimestamp = 13589759419903L;
    private final static Long   locationAfterFirstEvent = 13589759412128L;

    private final static String tracename = "traceset/trace-15316events_nolost_newformat";

    private final static long	indexToSeekFirst = 0;
    private final static Long   locationToSeekFirst = 13589759412128L;
    private final static long   contextValueAfterFirstEvent = 13589759412128L;
    private final static String firstEventReference = tracename + "/metadata_0";


    private final static long   timestampToSeekTest1 = 13589826657302L;
    private final static Long	indexToSeekTest1 = 7497L;
    private final static long   locationToSeekTest1 = 13589826657302L;
    private final static long   contextValueAfterSeekTest1 = 13589826657302L;
    private final static String seek1EventReference = tracename + "/vm_state_0";
    private final static long   seekTimestamp = 13589826657302L;
    private final static long   nextEventTimestamp = 13589826659739L;
    private final static long   nextnextEventTimestamp = 13589826662017L;

    private final static long   timestampToSeekLast = 13589906758692L;
    private final static Long	indexToSeekLast = 15315L;
    private final static long   locationToSeekLast = 13589906758692L;
    private final static long   contextValueAfterSeekLast = 13589906758692L;
    private final static String seekLastEventReference = tracename + "/kernel_0";

    private static LTTngTrace testStream = null;
    private LTTngTrace prepareStreamToTest() {
        if (testStream == null)
            try {
                final URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(tracepath1), null);
                final File testfile = new File(FileLocator.toFileURL(location).toURI());
                final LTTngTrace tmpStream = new LTTngTrace(null, testfile.getPath(), false);
                testStream = tmpStream;
            }
        catch (final Exception e) {
            System.out.println("ERROR : Could not open " + tracepath1);
            testStream = null;
        }
        else
            testStream.seekEvent(0L);


        return testStream;
    }

    public void testTraceConstructors() {
        // Default constructor
        // Test constructor with argument on a wrong tracepath, skipping indexing
        try {
            new LTTngTrace(null, wrongTracePath, skipIndexing);
            fail("Construction with wrong tracepath should fail!");
        }
        catch( final Exception e) {
        }

        // Test constructor with argument on a correct tracepath, skipping indexing
        try {
            final URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path(tracepath1), null);
            final File testfile = new File(FileLocator.toFileURL(location).toURI());
            new LTTngTrace(null, testfile.getPath(), skipIndexing);
        }
        catch( final Exception e) {
            fail("Construction with correct tracepath failed!");
        }
        //        System.out.println("Test completed");
    }

    public void testGetNextEvent() {
        TmfEvent tmpEvent = null;
        final LTTngTrace testStream1 = prepareStreamToTest();

        final TmfContext tmpContext = new TmfContext(null, 0);
        // We should be at the beginning of the trace, so we will just read the first event now
        tmpEvent = testStream1.getNext(tmpContext );
        assertNotSame("tmpEvent is null after first getNextEvent()",null,tmpEvent );
        assertEquals("tmpEvent has wrong timestamp after first getNextEvent()",firstEventTimestamp,tmpEvent.getTimestamp().getValue() );

        // Read the next event as well
        tmpEvent = testStream1.getNext( tmpContext);
        assertNotSame("tmpEvent is null after second getNextEvent()",null,tmpEvent );
        assertEquals("tmpEvent has wrong timestamp after second getNextEvent()",secondEventTimestamp,tmpEvent.getTimestamp().getValue() );
    }

    public void testParseEvent() {
        TmfEvent tmpEvent = null;
        final LTTngTrace testStream1 = prepareStreamToTest();

        final TmfContext tmpContext = new TmfContext(null, 0);
        // We should be at the beginning of the trace, so we will just parse the first event now
        tmpEvent = testStream1.parseEvent(tmpContext );
        assertNotSame("tmpEvent is null after first parseEvent()",null,tmpEvent );
        assertEquals("tmpEvent has wrong timestamp after first parseEvent()",firstEventTimestamp,tmpEvent.getTimestamp().getValue() );

        // Use parseEvent again. Should be the same event
        tmpEvent = testStream1.parseEvent(tmpContext );
        assertNotSame("tmpEvent is null after first parseEvent()",null,tmpEvent );
        assertEquals("tmpEvent has wrong timestamp after first parseEvent()",firstEventTimestamp,tmpEvent.getTimestamp().getValue() );
    }

    public void testSeekEventTimestamp() {
        TmfEvent tmpEvent = null;
        ITmfContext tmpContext = new TmfContext(null, 0);
        final LTTngTrace testStream1 = prepareStreamToTest();

        // We should be at the beginning of the trace, we will seek at a certain timestamp
        tmpContext = testStream1.seekEvent(new TmfTimestamp(timestampToSeekTest1, (byte) -9, 0));
        tmpEvent = testStream1.getNext(tmpContext);
        assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
        assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterSeekTest1,((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
        assertNotSame("tmpEvent is null after first seekEvent()",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after first seekEvent()", seek1EventReference.contains(tmpEvent.getReference()));

        // Seek to the last timestamp
        tmpContext = testStream1.seekEvent(new TmfTimestamp(timestampToSeekLast, (byte) -9, 0));
        tmpEvent = testStream1.getNext(tmpContext);
        assertNotSame("tmpContext is null after seekEvent() to last",null,tmpContext );
        assertEquals("tmpContext has wrong timestamp after seekEvent() to last",contextValueAfterSeekLast,((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
        assertNotSame("tmpEvent is null after seekEvent() to last ",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after seekEvent() to last", seekLastEventReference.contains(tmpEvent.getReference()));

        // Seek to the first timestamp (startTime)
        tmpContext = testStream1.seekEvent(new TmfTimestamp(firstEventTimestamp, (byte) -9, 0));
        tmpEvent = testStream1.getNext(tmpContext);
        assertNotSame("tmpEvent is null after seekEvent() to start ",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after seekEvent() to start", firstEventReference.contains(tmpEvent.getReference()));
        assertNotSame("tmpContext is null after seekEvent() to first",null,tmpContext );
        assertEquals("tmpContext has wrong timestamp after seekEvent() to first",contextValueAfterFirstEvent,((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
    }

    public void testSeekEventIndex() {
        TmfEvent tmpEvent = null;
        ITmfContext tmpContext = new TmfContext(null, 0);
        final LTTngTrace testStream1 = prepareStreamToTest();

        // We should be at the beginning of the trace, we will seek at a certain timestamp
        tmpContext = testStream1.seekEvent(indexToSeekTest1);
        tmpEvent = testStream1.getNext(tmpContext);
        assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
        assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterSeekTest1,((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
        assertNotSame("tmpEvent is null after first seekEvent()",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after first seekEvent()", seek1EventReference.contains(tmpEvent.getReference()));

        // Seek to the last timestamp
        tmpContext = testStream1.seekEvent(indexToSeekLast);
        tmpEvent = testStream1.getNext(tmpContext);
        assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
        assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterSeekLast,((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
        assertNotSame("tmpEvent is null after seekEvent() to last ",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after seekEvent() to last", seekLastEventReference.contains(tmpEvent.getReference()));

        // Seek to the first timestamp (startTime)
        tmpContext = testStream1.seekEvent(indexToSeekFirst);
        tmpEvent = testStream1.getNext(tmpContext);
        assertNotSame("tmpContext is null after first seekEvent()",null,tmpContext );
        assertEquals("tmpContext has wrong timestamp after first seekEvent()",contextValueAfterFirstEvent,((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
        assertNotSame("tmpEvent is null after seekEvent() to start ",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after seekEvent() to start", firstEventReference.contains(tmpEvent.getReference()));
    }

    public void testSeekLocation() {
        TmfEvent tmpEvent = null;
        ITmfContext tmpContext = new TmfContext(null, 0);
        final LTTngTrace testStream1 = prepareStreamToTest();

        // We should be at the beginning of the trace, we will seek at a certain timestamp
        tmpContext = testStream1.seekEvent(new LttngLocation(locationToSeekTest1));
        tmpEvent = testStream1.getNext(tmpContext);
        assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
        assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterSeekTest1,((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
        assertNotSame("tmpEvent is null after first seekLocation()",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after first seekLocation()", seek1EventReference.contains(tmpEvent.getReference()));

        // Seek to the last timestamp
        tmpContext = testStream1.seekEvent(new LttngLocation(locationToSeekLast));
        tmpEvent = testStream1.getNext(tmpContext);
        assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
        assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterSeekLast,((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
        assertNotSame("tmpEvent is null after seekLocation() to last ",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after seekLocation() to last", seekLastEventReference.contains(tmpEvent.getReference()));

        // Seek to the first timestamp (startTime)
        tmpContext = testStream1.seekEvent(new LttngLocation(locationToSeekFirst));
        tmpEvent = testStream1.getNext(tmpContext);
        assertNotSame("tmpContext is null after first seekLocation()",null,tmpContext );
        assertEquals("tmpContext has wrong timestamp after first seekLocation()",contextValueAfterFirstEvent,((TmfTimestamp)tmpEvent.getTimestamp()).getValue() );
        assertNotSame("tmpEvent is null after seekLocation() to start ",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after seekLocation() to start", firstEventReference.contains(tmpEvent.getReference()));
    }

    public void testLocationOperations() {
        TmfEvent tmpEvent = null;
        ITmfContext tmpContext = new TmfContext(null, 0);
        final LTTngTrace testStream1 = prepareStreamToTest();

        // Test LttngLocation after a seek
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        LttngLocation location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationSeek());
        assertEquals("location has wrong operation time", seekTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a parse
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationParse());
        assertEquals("location has wrong operation time", seekTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a getNext
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationReadNext());
        assertEquals("location has wrong operation time", seekTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a parse and parse
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationParse());
        assertEquals("location has wrong operation time", seekTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a getNext and getNext
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationReadNext());
        assertEquals("location has wrong operation time", nextEventTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", nextnextEventTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a getNext and parse
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationParse());
        assertEquals("location has wrong operation time", nextEventTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a parse and getNext
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationReadNext());
        assertEquals("location has wrong operation time", seekTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a parse, getNext and parse
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationParse());
        assertEquals("location has wrong operation time", nextEventTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a parse, getNext and getNext
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationReadNext());
        assertEquals("location has wrong operation time", nextEventTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", nextnextEventTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a getNext, parse and parse
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationParse());
        assertEquals("location has wrong operation time", nextEventTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a getNext, parse and getNext
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationReadNext());
        assertEquals("location has wrong operation time", nextEventTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", nextnextEventTimestamp, tmpEvent.getTimestamp().getValue());

        // Test LttngLocation after a getNext, getNext and parse
        tmpContext = testStream1.seekEvent(new LttngLocation(seekTimestamp));
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", seekTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.getNext(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextEventTimestamp, tmpEvent.getTimestamp().getValue());
        tmpEvent = testStream.parseEvent(tmpContext);
        assertEquals("tmpEvent has wrong timestamp", nextnextEventTimestamp, tmpEvent.getTimestamp().getValue());
        location = (LttngLocation) tmpContext.getLocation().clone();
        assertTrue("location has wrong flag", location.isLastOperationParse());
        assertEquals("location has wrong operation time", nextnextEventTimestamp, location.getOperationTimeValue());
        tmpContext = testStream1.seekEvent(location);
        tmpEvent = testStream1.getNext(tmpContext);
        assertTrue("tmpContext is null after getNextEvent()", tmpEvent != null);
        assertEquals("tmpEvent has wrong timestamp", nextnextEventTimestamp, tmpEvent.getTimestamp().getValue());
    }

    public void testConcurrentOperations() {
        final LTTngTrace testStream = prepareStreamToTest();
        ITmfEvent event1 = null;
        ITmfEvent event2 = null;
        ITmfContext context1;
        ITmfContext context2;

        // Test concurrent interference (seek) after a seek
        context1 = testStream.seekEvent(new LttngLocation(seekTimestamp));
        context2 = testStream.seekEvent(new LttngLocation(timestampToSeekLast));
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());

        // Test concurrent interference (parseEvent) after a seek
        context2 = testStream.seekEvent(new LttngLocation(timestampToSeekLast));
        context1 = testStream.seekEvent(new LttngLocation(seekTimestamp));
        event2 = testStream.parseEvent(context2);
        assertTrue("event is null after parseEvent()", event2 != null);
        assertEquals("event has wrong timestamp", timestampToSeekLast, event2.getTimestamp().getValue());
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());

        // Test concurrent interference (getNext) after a seek
        context2 = testStream.seekEvent(new LttngLocation(timestampToSeekLast));
        context1 = testStream.seekEvent(new LttngLocation(seekTimestamp));
        event2 = testStream.getNext(context2);
        assertTrue("event is null after getNext()", event2 != null);
        assertEquals("event has wrong timestamp", timestampToSeekLast, event2.getTimestamp().getValue());
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());

        // Test concurrent interference (seek) after a parseEvent
        context1 = testStream.seekEvent(new LttngLocation(seekTimestamp));
        event1 = testStream.parseEvent(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());
        context2 = testStream.seekEvent(new LttngLocation(timestampToSeekLast));
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());

        // Test concurrent interference (parseEvent) after a parseEvent
        context2 = testStream.seekEvent(new LttngLocation(timestampToSeekLast));
        context1 = testStream.seekEvent(new LttngLocation(seekTimestamp));
        event1 = testStream.parseEvent(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());
        event2 = testStream.parseEvent(context2);
        assertTrue("event is null after parseEvent()", event2 != null);
        assertEquals("event has wrong timestamp", timestampToSeekLast, event2.getTimestamp().getValue());
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());

        // Test concurrent interference (getNext) after a parseEvent
        context2 = testStream.seekEvent(new LttngLocation(timestampToSeekLast));
        context1 = testStream.seekEvent(new LttngLocation(seekTimestamp));
        event1 = testStream.parseEvent(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());
        event2 = testStream.getNext(context2);
        assertTrue("event is null after getNext()", event2 != null);
        assertEquals("event has wrong timestamp", timestampToSeekLast, event2.getTimestamp().getValue());
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());

        // Test concurrent interference (seek) after a getNext
        context1 = testStream.seekEvent(new LttngLocation(seekTimestamp));
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());
        context2 = testStream.seekEvent(new LttngLocation(timestampToSeekLast));
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", nextEventTimestamp, event1.getTimestamp().getValue());

        // Test concurrent interference (parseEvent) after a getNext
        context2 = testStream.seekEvent(new LttngLocation(timestampToSeekLast));
        context1 = testStream.seekEvent(new LttngLocation(seekTimestamp));
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());
        event2 = testStream.parseEvent(context2);
        assertTrue("event is null after parseEvent()", event2 != null);
        assertEquals("event has wrong timestamp", timestampToSeekLast, event2.getTimestamp().getValue());
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", nextEventTimestamp, event1.getTimestamp().getValue());

        // Test concurrent interference (getNext) after a getNext
        context2 = testStream.seekEvent(new LttngLocation(timestampToSeekLast));
        context1 = testStream.seekEvent(new LttngLocation(seekTimestamp));
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", seekTimestamp, event1.getTimestamp().getValue());
        event2 = testStream.getNext(context2);
        assertTrue("event is null after getNext()", event2 != null);
        assertEquals("event has wrong timestamp", timestampToSeekLast, event2.getTimestamp().getValue());
        event1 = testStream.getNext(context1);
        assertTrue("event is null after getNext()", event1 != null);
        assertEquals("event has wrong timestamp", nextEventTimestamp, event1.getTimestamp().getValue());
    }

    public void testGetter() {
        TmfEvent tmpEvent = null;
        final LTTngTrace testStream1 = prepareStreamToTest();

        // Move to the first event to have something to play with
        tmpEvent = testStream1.parseEvent( new TmfContext(null, 0));

        // Test current event
        assertNotSame("tmpEvent is null after first event",null,tmpEvent );
        assertTrue("tmpEvent has wrong reference after first event", firstEventReference.contains(tmpEvent.getReference()));
        assertNotSame("tmpContext is null after first seekEvent()",null,testStream1.getCurrentLocation() );
        assertTrue("tmpContext has wrong timestamp after first seekEvent()",locationAfterFirstEvent.equals( ((LttngLocation)testStream1.getCurrentLocation()).getOperationTimeValue()) );

        // Test CPU number of the trace
        assertSame("getCpuNumber() return wrong number of cpu",traceCpuNumber ,testStream1.getCpuNumber() );
    }

    public void testToString() {
        final LTTngTrace testStream1 = prepareStreamToTest();

        // Move to the first event to have something to play with
        testStream1.parseEvent( new TmfContext(null, 0) );

        // Just make sure toString() does not return null or the java reference
        assertNotSame("toString returned null",null, testStream1.toString() );
        assertNotSame("toString is not overridded!", testStream1.getClass().getName() + '@' + Integer.toHexString(testStream1.hashCode()), testStream1.toString() );
    }

}
