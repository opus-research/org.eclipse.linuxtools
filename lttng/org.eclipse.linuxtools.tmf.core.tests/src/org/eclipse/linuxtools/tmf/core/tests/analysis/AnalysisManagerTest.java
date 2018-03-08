/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.Test;

/**
 * Test suite for the TmfAnalysisModule class
 */
public class AnalysisManagerTest {

    private static final String DIRECTORY = "testfiles";
    private static final String TEST_STREAM = "A-Test-10K";

    /**
     * Test suite for the getAnalaysisModule method
     */
    @Test
    public void testGetAnalysisModules() {
        Map<String, IAnalysisModule> modules = TmfAnalysisManager.getAnalysisModules();
        /* At least 3 modules should be found */
        assertTrue(modules.size() >= 3);

        IAnalysisModule module = modules.get("org.eclipse.linuxtools.tmf.core.tests.analysis.test2");
        assertTrue(module.isAutomatic());

        module = modules.get("org.eclipse.linuxtools.tmf.core.tests.analysis.test");
        assertFalse(module.isAutomatic());
    }

    /**
     * Test suite for getAnalysisModule(ITmfTrace) Use the test TMF trace and
     * test Ctf trace as sample traces
     */
    @Test
    public void testListForTraces() {

        /* Generic TmfTrace */
        TmfTraceStub trace = null;
        final URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
        try {
            File test = new File(FileLocator.toFileURL(location).toURI());
            trace = new TmfTraceStub(test.toURI().getPath(), ITmfTrace.DEFAULT_TRACE_CACHE_SIZE, false, null, null);
            Map<String, IAnalysisModule> map = TmfAnalysisManager.getAnalysisModules(trace);

            assertTrue(map.containsKey("org.eclipse.linuxtools.tmf.core.tests.analysis.test"));
            assertTrue(map.containsKey("org.eclipse.linuxtools.tmf.core.tests.analysis.test2"));
            assertFalse(map.containsKey("org.eclipse.linuxtools.tmf.core.tests.analysis.testctf"));

        } catch (URISyntaxException e) {
            fail("Cannot generate trace");
        } catch (IOException e) {
            fail("Cannot generate trace");
        } catch (TmfTraceException e) {
            fail("Cannot generate trace");
        } finally {
            if (trace != null) {
                trace.dispose();
            }
        }

        /* Ctf trace */
        CtfTmfTrace ctftrace = null;
        try {
            String PATH = CtfTmfTestTraces.getTestTracePath(0);
            ctftrace = new CtfTmfTrace();
            ctftrace.initTrace((IResource) null, PATH, CtfTmfEvent.class);
            Map<String, IAnalysisModule> map = TmfAnalysisManager.getAnalysisModules(ctftrace);

            assertTrue(map.containsKey("org.eclipse.linuxtools.tmf.core.tests.analysis.test"));
            assertTrue(map.containsKey("org.eclipse.linuxtools.tmf.core.tests.analysis.test2"));
            assertTrue(map.containsKey("org.eclipse.linuxtools.tmf.core.tests.analysis.testctf"));
        } catch (TmfTraceException e) {
            fail("Cannot generate ctf trace");
        } finally {
            if (ctftrace != null) {
                ctftrace.dispose();
            }
        }
    }
}
