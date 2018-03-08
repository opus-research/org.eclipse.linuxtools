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

package org.eclipse.linuxtools.tmf.analysis.xml.module;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.Activator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Class containing some utilities for the xml plug-in packages
 *
 * @author Geneviève Bastien
 */
public class XmlUtils {

	/** Sub-directory of the plugin where xml files are stored */
    private static final String XML_DIRECTORY = "xml_files"; //$NON-NLS-1$

    private static final String XSD = "xmldefinition.xsd"; //$NON-NLS-1$

    /**
     * Get the path where the xml files are stored
     *
     * @return path to xml files
     */
    public static IPath getXmlFilesPath() {
        IPath path = Activator.getDefault().getStateLocation();
        path = path.addTrailingSeparator().append(XML_DIRECTORY);
        return path;
    }

    /**
     * Validate the XML file input with the xsd schema
     *
     * @param xml
     *            file
     * @return is a valid XML
     */
    public static boolean xmlValidate(File xml) {
        URL url = XmlAnalysisModule.class.getResource(XSD);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source xmlFile = new StreamSource(xml);
        try {
            Schema schema = schemaFactory.newSchema(url);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
        } catch (SAXParseException e) {
            System.out.println("XML Error:");//$NON-NLS-1$
            System.out.println("Line: " + e.getLineNumber());//$NON-NLS-1$
            System.out.println("Reason: " + e.getLocalizedMessage());//$NON-NLS-1$
            return false;
        } catch (SAXException e) {
            System.out.println("Reason: " + e.getLocalizedMessage());//$NON-NLS-1$
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
