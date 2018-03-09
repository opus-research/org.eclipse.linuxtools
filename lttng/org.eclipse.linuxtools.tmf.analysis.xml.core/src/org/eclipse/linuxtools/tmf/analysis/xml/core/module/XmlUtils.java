/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Class containing some utilities for the XML plug-in packages: for example, it
 * manages the XML files and validates them
 *
 * @author Geneviève Bastien
 */
public class XmlUtils {

    /** Sub-directory of the plugin where XML files are stored */
    private static final String XML_DIRECTORY = "xml_files"; //$NON-NLS-1$

    private static final String XSD = "xmldefinition.xsd"; //$NON-NLS-1$

    private static String fLastError = new String();

    /**
     * Get the path where the XML files are stored. Create it if it does not
     * exist
     *
     * @return path to XML files
     */
    public static IPath getXmlFilesPath() {
        IPath path = Activator.getDefault().getStateLocation();
        path = path.addTrailingSeparator().append(XML_DIRECTORY);

        /* Check if directory exists, otherwise create it */
        File dir = path.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return path;
    }

    /**
     * Validate the XML file input with the XSD schema
     *
     * @param xml
     *            XML file to validate
     * @return whether the XML validates
     */
    public static boolean xmlValidate(File xml) {
        URL url = XmlUtils.class.getResource(XSD);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source xmlFile = new StreamSource(xml);
        try {
            Schema schema = schemaFactory.newSchema(url);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);
        } catch (SAXParseException e) {
            fLastError = String.format(Messages.XmlUtils_XmlParseError, e.getLineNumber(), e.getLocalizedMessage());
            Activator.logError(fLastError);
            return false;
        } catch (SAXException e) {
            fLastError = String.format(Messages.XmlUtils_XmlValidationError, e.getLocalizedMessage());
            Activator.logError(fLastError);
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Adds an XML file to the plugin's path. The XML file should have been
     * validated using the {@link XmlUtils#xmlValidate(File)} method before
     * calling this method.
     *
     * @param fromFile
     *            The XML file to copy
     * @return Whether the file was successfully added
     */
    public static boolean addXmlFile(File fromFile) {
        /* Copy file to path */
        File toFile = getXmlFilesPath().addTrailingSeparator()
                .append(fromFile.getName()).toFile();

        try {
            if (!toFile.exists()) {
                toFile.createNewFile();
            }
        } catch (IOException e) {
            fLastError = Messages.XmlUtils_ErrorCopyingFile;
            Activator.logError(fLastError, e);
            return false;
        }

        try (FileInputStream fis = new FileInputStream(fromFile);
                FileOutputStream fos = new FileOutputStream(toFile);
                FileChannel source = fis.getChannel();
                FileChannel destination = fos.getChannel();) {
            destination.transferFrom(source, 0, source.size());
        } catch (IOException e) {
            fLastError = Messages.XmlUtils_ErrorCopyingFile;
            Activator.logError(fLastError, e);
            return false;
        }
        return true;
    }

    /**
     * Return the last error message that was obtained either when validating an
     * XML file or manipulating the files. Typically, one of the boolean
     * functions of this class would have returned false and the error message
     * will have been updated. It can be either logged or displayed to the user
     * by the caller.
     *
     * @return The last error message
     */
    public static String getLastError() {
        return fLastError;
    }
}
