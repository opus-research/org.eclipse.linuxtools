/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adapted for TMF Trace Model 1.0
 *   Alexandre Montplaisir - Port to JUnit4
 *   Marc-Andre Laperle - Adapted to CustomTxtTrace
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTrace;
import org.eclipse.linuxtools.internal.tmf.ui.parsers.custom.CustomTxtTraceDefinition;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceIndex;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for indexing using a CustomTxtTrace.
 */
@SuppressWarnings("javadoc")
public class CustomTxtIndexTest {

    private static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss:SSS";
    private static final String TRACE_DIRECTORY = System.getProperty("java.io.tmpdir") + File.separator + "dummyTxtTrace";
    private static final String TRACE_PATH = TRACE_DIRECTORY + File.separator + "test.txt";
    private static final String DEFINITION_PATH = "tracesets" + File.separator + "txt" + File.separator + "testTxtDefinition.xml";
    private static final int BLOCK_SIZE = 100;
    private static final int NB_EVENTS = 10000;
    private TestTrace fTrace = null;

    @Before
    public void setUp() throws Exception {
        setupTrace();
    }

    @After
    public void tearDown() {
        String directory = TmfTraceManager.getSupplementaryFileDir(fTrace);
        try {
            fTrace.dispose();
            fTrace = null;
        } finally {
            File dir = new File(directory);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (File file : files) {
                    file.delete();
                }
                dir.delete();
            }

            File trace = new File(TRACE_DIRECTORY);
            if (trace.exists()) {
                trace.delete();
            }
        }

    }

    private static CustomTxtTraceDefinition createDefinition() {
        CustomTxtTraceDefinition[] definitions = CustomTxtTraceDefinition.loadAll(new File(DEFINITION_PATH).toString());
        return definitions[0];
    }

    private TestTrace createTrace() throws Exception {
        CustomTxtTraceDefinition definition = createDefinition();
        final File file = new File(TRACE_PATH);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (int i = 0; i < NB_EVENTS; ++i) {
            SimpleDateFormat f = new SimpleDateFormat(TIMESTAMP_FORMAT);
            String eventStr = f.format(new Date(i)) + " hello world\n";
            writer.write(eventStr);
        }
        writer.close();

        return new TestTrace(file.toString(), definition, BLOCK_SIZE);
    }

    private synchronized void setupTrace() throws Exception {
        File traceDirectory = new File(TRACE_DIRECTORY);
        if (traceDirectory.exists()) {
            traceDirectory.delete();
        }
        traceDirectory.mkdir();
        if (fTrace == null) {
            fTrace = createTrace();
            fTrace.indexTrace(true);
        }
    }

    private class TestTrace extends CustomTxtTrace {
        public TestTrace(String path, CustomTxtTraceDefinition createDefinition, int blockSize) throws TmfTraceException {
            super(null, createDefinition, path, blockSize);
            setIndexer(new TestIndexer(this, blockSize));
        }

        @Override
        public TestIndexer getIndexer() {
            return (TestIndexer) super.getIndexer();
        }
    }

    private static class TestIndexer extends TmfBTreeTraceIndexer {

        public TestIndexer(ITmfTrace trace, int interval) {
            super(trace, interval);
        }

        public ITmfTraceIndex getCheckpoints() {
            return getTraceIndex();
        }
    }

    // ------------------------------------------------------------------------
    // Verify checkpoints
    // ------------------------------------------------------------------------

    @Test
    public void testTmfTraceIndexing() {
        verifyIndexContent();
    }

    private void verifyIndexContent() {
        assertEquals("getCacheSize", BLOCK_SIZE, fTrace.getCacheSize());
        assertEquals("getTraceSize", NB_EVENTS, fTrace.getNbEvents());
        assertEquals("getRange-start", 0, fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS - 1, fTrace.getTimeRange().getEndTime().getValue());
        assertEquals("getStartTime", 0, fTrace.getStartTime().getValue());
        assertEquals("getEndTime", NB_EVENTS - 1, fTrace.getEndTime().getValue());

        ITmfTraceIndex checkpoints = fTrace.getIndexer().getCheckpoints();
        int pageSize = fTrace.getCacheSize();
        assertTrue("Checkpoints exist", checkpoints != null);
        assertEquals("Checkpoints size", NB_EVENTS / BLOCK_SIZE, checkpoints.size());

        // Validate that each checkpoint points to the right event
        for (int i = 0; i < checkpoints.size(); i++) {
            ITmfCheckpoint checkpoint = checkpoints.get(i);
            TmfContext context = new TmfContext(checkpoint.getLocation(), i * pageSize);
            ITmfEvent event = fTrace.parseEvent(context);
            assertTrue(context.getRank() == i * pageSize);
            assertTrue((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
        }
    }

    @Test
    public void testReopenIndex() throws Exception {
        fTrace.dispose();
        fTrace = createTrace();
        assertFalse(fTrace.getIndexer().getCheckpoints().isCreatedFromScratch());
        fTrace.indexTrace(true);

        verifyIndexContent();
    }

}