/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.XmlStrings;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is class implement the Provider structure for the state provider and
 * filter
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
    private String fStateID;

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
        fStateID = stateid;
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
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XMLFile);
            doc.getDocumentElement().normalize();

            // get State Providers
            NodeList stateproviderNodes = doc.getElementsByTagName(XmlStrings.STATEPROVIDER);
            Element stateproviderNode = null;

            for (int i = 0; i < stateproviderNodes.getLength(); i++) {
                Element node = (Element) stateproviderNodes.item(i);
                String analysisid = node.getAttribute(XmlStrings.ANALYSISID);
                if (analysisid.equals(fStateID)) {
                    stateproviderNode = node;
                }
            }

            if (stateproviderNode == null) {
                return null;
            }

            return stateproviderNode;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @return STATEID
     */
    public String getStateID() {
        return fStateID;
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        /* We can only set up the locations once the state system is assigned */
        super.assignTargetStateSystem(ssb);
    }

}