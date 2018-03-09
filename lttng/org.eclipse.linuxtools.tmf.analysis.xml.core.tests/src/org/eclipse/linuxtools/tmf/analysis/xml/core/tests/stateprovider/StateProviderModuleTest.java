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

package org.eclipse.linuxtools.tmf.analysis.xml.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlHeadInfo;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateSystemModule;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.XmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.tests.shared.TmfXmlTestFiles;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Test suite for the XmlStateSystemModule Test. It tests the reading of the
 * file, the header and the module's proper functioning as a module, but not the
 * state system building, which is covered by another test suite.
 *
 * @author Geneviève Bastien
 */
public class StateProviderModuleTest {

    private static String ANALYSIS_ID = "polymtl.kernel.sp";
    private static String ANALYSIS_NAME = "Xml kernel State System";

    private XmlStateSystemModule fModule;

    private static Document getXmlDocumentFromFile(File file) {
        /* Initialize the state provider module */
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(file);

            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException e) {
            fail("Xml document parse exception");
        } catch (SAXException e) {
            fail("Exception parsing xml file");
        } catch (IOException e) {
            fail("File io exception");
        }
        return null;
    }

    /**
     * Test the module construction
     */
    @Test
    public void testModuleConstruction() {

        Document doc = getXmlDocumentFromFile(TmfXmlTestFiles.VALID_FILE.getFile());

        /* get State Providers modules */
        NodeList stateproviderNodes = doc
                .getElementsByTagName(XmlStrings.STATEPROVIDER);
        assertTrue(stateproviderNodes.getLength() > 0);

        Element node = (Element) stateproviderNodes.item(0);
        fModule = new XmlStateSystemModule();
        String moduleId = node.getAttribute(XmlStrings.ANALYSISID);
        fModule.setId(moduleId);
        assertEquals(ANALYSIS_ID, fModule.getId());

        fModule.setXmlFile(new Path(TmfXmlTestFiles.VALID_FILE.getFile().getAbsolutePath()));
        NodeList head = node.getElementsByTagName(XmlStrings.HEAD);
        XmlHeadInfo headInfo = null;
        if (head.getLength() == 1) {
            headInfo = new XmlHeadInfo(head.item(0));
        }
        fModule.setHeadInfo(headInfo);

        assertEquals(ANALYSIS_NAME, fModule.getName());
    }

    /**
     * Test the module executes correctly
     */
    @Test
    public void testModuleExecution() {
        Document doc = getXmlDocumentFromFile(TmfXmlTestFiles.VALID_FILE.getFile());

        /* get State Providers modules */
        NodeList stateproviderNodes = doc
                .getElementsByTagName(XmlStrings.STATEPROVIDER);

        Element node = (Element) stateproviderNodes.item(0);
        fModule = new XmlStateSystemModule();
        String moduleId = node.getAttribute(XmlStrings.ANALYSISID);
        fModule.setId(moduleId);

        fModule.setXmlFile(new Path(TmfXmlTestFiles.VALID_FILE.getFile().getAbsolutePath()));
        NodeList head = node.getElementsByTagName(XmlStrings.HEAD);
        XmlHeadInfo headInfo = null;
        if (head.getLength() == 1) {
            headInfo = new XmlHeadInfo(head.item(0));
        }
        fModule.setHeadInfo(headInfo);

        try {
            assumeTrue(CtfTmfTestTrace.KERNEL.exists());
            ITmfTrace trace = CtfTmfTestTrace.KERNEL.getTrace();
            fModule.setTrace(trace);
            fModule.schedule();
            assertTrue(fModule.waitForCompletion(new NullProgressMonitor()));
        } catch (TmfAnalysisException e) {
            fail("Cannot set trace " + e.getMessage());
        } finally {
            CtfTmfTestTrace.KERNEL.dispose();
        }

    }
}
