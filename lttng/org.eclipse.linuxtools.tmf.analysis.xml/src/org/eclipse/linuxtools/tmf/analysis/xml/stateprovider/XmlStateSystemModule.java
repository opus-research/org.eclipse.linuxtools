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

package org.eclipse.linuxtools.tmf.analysis.xml.stateprovider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * Module for the xml state systems
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class XmlStateSystemModule extends TmfStateSystemAnalysisModule {

    private IPath fXmlFile;
    private static final String EXTENSION = ".ht"; //$NON-NLS-1$

    @Override
    protected ITmfStateProvider getStateProvider() {
        if (getTrace() instanceof CtfTmfTrace) {
            return new XmlStateProvider((CtfTmfTrace)getTrace(), getId(), fXmlFile);
        }
        return null;
    }

    @Override
    protected String getSsFileName() {
        return getId() + EXTENSION;
    }

    /**
     * Sets the file name of the xml file containing the state provider
     *
     * @param file
     *            XML File name
     */
    public void setXmlFile(IPath file) {
        fXmlFile = file;
    }

}
