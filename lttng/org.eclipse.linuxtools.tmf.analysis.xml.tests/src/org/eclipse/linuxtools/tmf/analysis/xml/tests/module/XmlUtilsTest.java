/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.tests.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.tmf.analysis.xml.module.XmlUtils;
import org.junit.Test;

/**
 * Tests for the XmlUtils class
 * 
 * @author Geneviève Bastien
 */
public class XmlUtilsTest {

	/**
	 * Test the getXmlFilesPath method
	 */
	@Test
	public void testXmlPath() {
		IPath xmlPath = XmlUtils.getXmlFilesPath();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IPath workspacePath = workspace.getRoot().getRawLocation();
		workspacePath = workspacePath.addTrailingSeparator()
				.append(".metadata").addTrailingSeparator().append(".plugins")
				.addTrailingSeparator()
				.append("org.eclipse.linuxtools.tmf.analysis.xml")
				.addTrailingSeparator().append("xml_files");

		assertEquals(xmlPath, workspacePath);
	}

	/**
	 * test the xmlValidate method
	 */
	@Test
	public void testXmlValidate() {		
		File testXmlFile = new File("test_xml_files/test_valid.xml");
		if (!testXmlFile.exists()) {
			fail("Test file does not exist");
		}		
		assertTrue(XmlUtils.xmlValidate(testXmlFile));
		
		testXmlFile = new File("test_xml_files/test_invalid.xml");
        if (!testXmlFile.exists()) {
            fail("Test file does not exist");
        }       
        assertFalse(XmlUtils.xmlValidate(testXmlFile));
	}

}
