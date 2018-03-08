package org.eclipse.linuxtools.lttng2.kernel.core.tests.event.matchandsync;

import static org.junit.Assert.*;

import org.eclipse.linuxtools.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.junit.Test;

import junit.framework.TestCase;



/**
 * @author gbastien
 *
 */
public class MatchAndSyncTest  extends TestCase{

    /**
     *
     */
    @Test
    public void testConstructor() {
        try {
            CtfTmfTrace trace1 = MatchAndSyncTestFiles.getTestTrace(1);
            CtfTmfTrace trace2 = MatchAndSyncTestFiles.getTestTrace(2);

            TcpEventMatching matching = new TcpEventMatching(trace1);

            CtfTmfTrace[] tracearr = new CtfTmfTrace[2];
            tracearr[0] = trace1;
            tracearr[1] = trace2;

            matching = new TcpEventMatching(tracearr);


        } catch (TmfTraceException e) {
            e.printStackTrace();
            fail ("Cannot generate traces"); //$NON-NLS-1$
        }

    }

    /**
    *
    */
   @SuppressWarnings("nls")
   @Test
   public void testMatching() {
       final String cr = System.getProperty("line.separator");//$NON-NLS-1$
       try {
           CtfTmfTrace trace1 = MatchAndSyncTestFiles.getTestTrace(1);
           CtfTmfTrace trace2 = MatchAndSyncTestFiles.getTestTrace(2);

           CtfTmfTrace[] tracearr = new CtfTmfTrace[2];
           tracearr[0] = trace1;
           tracearr[1] = trace2;

           TcpEventMatching oneTraceMatch = new TcpEventMatching(trace1);
           assertTrue(oneTraceMatch.matchEvents());

           TcpEventMatching twoTraceMatch = new TcpEventMatching(tracearr);
           assertTrue(twoTraceMatch.matchEvents());

           String stats = twoTraceMatch.printMatchingStats();
           assertEquals("Number of matches found: 41" + cr +
                   "Trace 0:"+cr +
                   "  3 unmatched incoming events" + cr +
                   "  2 unmatched outgoing events" + cr +
                   "Trace 1:" + cr +
                   "  3 unmatched incoming events" + cr +
                   "  2 unmatched outgoing events" + cr, stats);
       } catch (TmfTraceException e) {
           e.printStackTrace();
           fail ("Cannot generate traces"); //$NON-NLS-1$
       }

   }

}
