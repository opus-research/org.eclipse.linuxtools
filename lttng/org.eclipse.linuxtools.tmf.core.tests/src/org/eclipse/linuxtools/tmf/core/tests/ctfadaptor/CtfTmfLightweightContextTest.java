package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfLightweightContext;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.junit.Test;

public class CtfTmfLightweightContextTest {
    private static final String PATH = TestParams.getPath();

    CtfTmfTrace fixture;

    class SeekerThread extends Thread{
        long val;

        /**
         * @param val the val to set
         */
        public void setVal(long val) {
            this.val = val;
        }
    }

    @Test
    public void testTooManyContexts() throws TmfTraceException, InterruptedException {
        long begin = 1332170682440133097L;
        long end = 1332170692664579801L;
        final int lwcCount = 1000;
        double increment = (end - begin) / lwcCount;
        final ArrayList<Long> vals = new ArrayList<Long>();
        fixture = new CtfTmfTrace();
        fixture.initTrace((IResource) null, PATH, CtfTmfEvent.class);
        final ArrayList<Thread> threads = new ArrayList<Thread>();
        final ArrayList<CtfTmfLightweightContext> tooManyContexts = new ArrayList<CtfTmfLightweightContext>();
        CtfTmfLightweightContext context = new CtfTmfLightweightContext(fixture);
        context.seek(0);
        System.out.println("Indexing trace"); //$NON-NLS-1$
        int count =0;
        while( fixture.getNext(context) != null ){
            count++;
        }
        System.out.println("Indexed " + count + " events"); //$NON-NLS-1$ //$NON-NLS-2$

        for (double i = begin; i < end; i += increment) {
            SeekerThread thread = new SeekerThread(){
                @Override
                public void run() {
                    CtfTmfLightweightContext lwc = new CtfTmfLightweightContext(fixture);
                    lwc.seek(val);
                    fixture.getNext(lwc);
                    synchronized(fixture){
                        vals.add(lwc.getCurrentEvent().getTimestampValue());
                        tooManyContexts.add(lwc);
                    }
                }
            };
            thread.setVal((long)i);
            threads.add(thread);
            thread.start();
        }
        System.out.println("Threads started"); //$NON-NLS-1$
        for( Thread t: threads){
            t.join();
        }
        System.out.println("Threads joined"); //$NON-NLS-1$
        for( Long val : vals){
            assertTrue(val >= begin);
            assertTrue(val <= end);
        }
    }
}
