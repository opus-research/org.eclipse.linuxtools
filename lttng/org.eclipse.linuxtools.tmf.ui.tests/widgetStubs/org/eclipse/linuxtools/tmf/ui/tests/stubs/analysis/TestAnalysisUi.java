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

package org.eclipse.linuxtools.tmf.ui.tests.stubs.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.ui.views.analysis.ITmfAnalysisViewable;
import org.osgi.framework.Bundle;

/**
 * Stub for an analysis module implementing the ITmfAnalysisViewable interface
 */
public class TestAnalysisUi extends TmfAbstractAnalysisModule implements ITmfAnalysisViewable {

    @Override
    protected boolean executeAnalysis() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public List<String> getViews() {
        List<String> list = new ArrayList<String>();
        list.add("org.eclipse.linuxtools.tmf.ui.tests.testAnalysisView");
        return list;
    }

    @Override
    public Bundle getBundle() {
        return Platform.getBundle("org.eclipse.linuxtools.tmf.ui.tests");
    }

}
