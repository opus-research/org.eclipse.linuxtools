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

package org.eclipse.linuxtools.tmf.core.analysis;

/**
 * Abstract class for parameter providers, implements methods and
 * functionalities to warn the analysis module of parameter changed
 *
 * @author Geneviève Bastien
 *
 */
public abstract class TmfAbstractAnalysisParamProvider implements IAnalysisParameterProvider {

    private final IAnalysisModule fModule;

    /**
     * Constructor with analysis module as parameter
     *
     * @param module
     *            Analysis module to hook with
     */
    public TmfAbstractAnalysisParamProvider(IAnalysisModule module) {
        if (module == null) {
            throw new IllegalArgumentException();
        }
        fModule = module;
    }

    /**
     * Pushes a parameter change in the analysisModule it is hooked with
     *
     * @param name
     *            Name of the parameter
     * @param value
     *            Value of the parameter
     */
    protected void setAnalysisParameter(String name, Object value) {
        fModule.setParameter(name, value);
    }
}
