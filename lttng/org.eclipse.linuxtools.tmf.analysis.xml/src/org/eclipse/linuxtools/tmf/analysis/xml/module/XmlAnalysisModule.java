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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.stateprovider.XmlStateSystemModule;
import org.eclipse.linuxtools.tmf.analysis.xml.stateprovider.model.XmlStrings;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Analysis module who builds the analysis module in the plugin's xml file
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class XmlAnalysisModule extends TmfAbstractAnalysisModule {

    private static List<IAnalysisModule> fModules = null;

    /**
     * Package Id It's used to access in the xmldefinition.xsd
     */
    // private static final String id = "org.eclipse.linuxtools.tmf.core.xmlstateprovider"; //$NON-NLS-1$

    @Override
    public List<IAnalysisModule> getExecutableModules() {
        if (fModules == null) {
            fModules = populateAnalysisModules();
        }
        return fModules;
    }

    private static List<IAnalysisModule> populateAnalysisModules() {
        List<IAnalysisModule> list = new ArrayList<IAnalysisModule>();
        IPath pathToFiles = XmlUtils.getXmlFilesPath();
        File fFolder = pathToFiles.toFile();
        if (!(fFolder.isDirectory() && fFolder.exists())) {
            return list;
        }
        for (File XmlFile : fFolder.listFiles()) {
            if (!XmlUtils.xmlValidate(XmlFile)) {
                continue;
            }

            try {
                /* Load the XML File */
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(XmlFile);
                doc.getDocumentElement().normalize();

                /* get State Providers modules */
                NodeList stateproviderNodes = doc.getElementsByTagName(XmlStrings.STATEPROVIDER);
                for (int i = 0; i < stateproviderNodes.getLength(); i++) {
                    Element node = (Element) stateproviderNodes.item(i);
                    String analysisid = node.getAttribute(XmlStrings.ANALYSISID);
                    XmlStateSystemModule module = new XmlStateSystemModule();
                    module.setName(analysisid);
                    module.setId(analysisid);
                    module.setXmlFile(new Path(XmlFile.getAbsolutePath()));
                    module.registerOutput(new TmfAnalysisViewOutput("org.eclipse.linuxtools.tmf.ui.views.ssvisualizer")); //$NON-NLS-1$
                    list.add(module);
                }
            } catch (ParserConfigurationException e) {
				Activator.logError("Error opening xml file", e); //$NON-NLS-1$
            } catch (SAXException e) {
                Activator.logError("Error opening xml file", e); //$NON-NLS-1$
            } catch (IOException e) {
                Activator.logError("Error opening xml file", e); //$NON-NLS-1$
            }
        }
        return list;
    }

    @Override
    public Bundle getBundle() {
        return Activator.getDefault().getBundle();
    }

    @Override
    protected boolean executeAnalysis() {
        // TODO Auto-generated method stub
        return false;
    }

}
