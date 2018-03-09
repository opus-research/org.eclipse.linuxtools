/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.ui.tests.module;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.tests.shared.TmfXmlTestFiles;
import org.eclipse.linuxtools.tmf.analysis.xml.ui.module.XmlAnalysisModuleSource;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the xml analysis module source class
 *
 * @author Geneviève Bastien
 */
public class XmlAnalysisModuleSourceTest {

    private static final String SS_MODULE = "polymtl.kernel.sp";

    private static void emptyXmlFolder() {
        File fFolder = XmlUtils.getXmlFilesPath().toFile();
        if (!(fFolder.isDirectory() && fFolder.exists())) {
            return;
        }
        for (File xmlFile : fFolder.listFiles()) {
            xmlFile.delete();
        }
        XmlAnalysisModuleSource.notifyModuleChange();
    }

    /**
     * Empty the xml directory before the test, just in case
     */
    @Before
    public void setUp() {
        emptyXmlFolder();
    }

    /**
     * Empty the xml directory after the test
     */
    @After
    public void cleanUp() {
        emptyXmlFolder();
    }

    /**
     * Test the {@link XmlAnalysisModuleSource#getAnalysisModules()} method
     */
    @Test
    public void testPopulateModules() {
        XmlAnalysisModuleSource module = new XmlAnalysisModuleSource();

        Map<String, IAnalysisModuleHelper> modules = module.getAnalysisModules();
        assertTrue(modules.isEmpty());

        /* use the test valid file for test */
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("Test file does not exist");
        }

        XmlUtils.addXmlFile(testXmlFile);
        XmlAnalysisModuleSource.notifyModuleChange();
        modules = module.getAnalysisModules();

        assertFalse(modules.isEmpty());

        assertTrue(findStateSystemModule(modules));
    }

    private static boolean findStateSystemModule(Map<String, IAnalysisModuleHelper> modules) {
        return modules.containsKey(SS_MODULE);
    }

    /**
     * Test the executable list refresh from the module helper
     */
    @Test
    public void testPopulateModulesWithAnalysisManager() {

        Map<String, IAnalysisModuleHelper> modules = TmfAnalysisManager.getAnalysisModules();
        assertFalse(findStateSystemModule(modules));

        /* use the test valid file for test */
        File testXmlFile = TmfXmlTestFiles.VALID_FILE.getFile();
        if ((testXmlFile == null) || !testXmlFile.exists()) {
            fail("Test file does not exist");
        }

        XmlUtils.addXmlFile(testXmlFile);
        XmlAnalysisModuleSource.notifyModuleChange();
        modules = TmfAnalysisManager.getAnalysisModules();
        assertTrue(findStateSystemModule(modules));
    }
}
