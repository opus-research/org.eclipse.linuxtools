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

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlModule;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlHeadInfo;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * Module for the xml state systems
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class XmlStateSystemModule extends TmfStateSystemAnalysisModule
		implements IXmlModule {

	private IPath fXmlFile;
	private XmlHeadInfo fHeadInfo = null;

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
	protected @NonNull ITmfStateProvider createStateProvider() {
		if (!(getTrace() instanceof CtfTmfTrace)) {
		    throw new IllegalStateException("XmlStateSystemModule: trace should be of type CtfTmfTrace"); //$NON-NLS-1$
		}
		return new XmlStateProvider((CtfTmfTrace) getTrace(), getId(), fXmlFile);
	}

	@Override
    public String getName() {
	    String name = fHeadInfo.getName();
	    if (name == null) {
	        name = getId();
	    }
	    return name;
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

	@Override
	public void setHeadInfo(XmlHeadInfo headInfo) {
		fHeadInfo = headInfo;
	}

    /**
     * @return file XML File name
     */
    public IPath getXmlFile() {
        return fXmlFile;
    }

}
