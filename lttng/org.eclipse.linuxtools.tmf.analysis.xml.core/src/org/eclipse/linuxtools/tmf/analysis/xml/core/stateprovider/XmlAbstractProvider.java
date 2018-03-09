/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.Messages;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is the base class for all XML state providers
 *
 * @author Florian Wininger
 */
public abstract class XmlAbstractProvider extends AbstractTmfStateProvider {

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

    /** StateID */
    private String fStateId;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param stateid
     *            in the XML file
     */
    public XmlAbstractProvider(CtfTmfTrace trace, String stateid) {
        super(trace, CtfTmfEvent.class, "LTTng Kernel"); //$NON-NLS-1$
        fStateId = stateid;
    }

    /**
     * Function to load the XML file structure Load stateValues and locations
     *
     * @param path
     *            Path to the XML file
     * @return stateprovider node
     */
    protected Node loadXMLFile(IPath path) {

        try {
            File XMLFile = path.toFile();
            if (XMLFile == null || !XMLFile.exists() || !XMLFile.isFile()) {
                return null;
            }

            // Load the XML File
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XMLFile);
            doc.getDocumentElement().normalize();

            // get State Providers
            NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);
            Element stateproviderNode = null;

            for (int i = 0; i < stateproviderNodes.getLength(); i++) {
                Element node = (Element) stateproviderNodes.item(i);
                String analysisid = node.getAttribute(TmfXmlStrings.ANALYSIS_ID);
                if (analysisid.equals(fStateId)) {
                    stateproviderNode = node;
                }
            }

            if (stateproviderNode == null) {
                return null;
            }

            return stateproviderNode;
        } catch (ParserConfigurationException e) {
            Activator.logError("Error loading XML file", e); //$NON-NLS-1$
        } catch (SAXException e) {
            Activator.logError(String.format(Messages.XmlUtils_XmlValidationError, e.getLocalizedMessage()), e);
        } catch (IOException e) {
            Activator.logError("Error loading XML file", e); //$NON-NLS-1$
        }

        return null;
    }

    /**
     * Get the state id of the state provider
     *
     * @return The state id of the state provider
     */
    public String getStateId() {
        return fStateId;
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

}