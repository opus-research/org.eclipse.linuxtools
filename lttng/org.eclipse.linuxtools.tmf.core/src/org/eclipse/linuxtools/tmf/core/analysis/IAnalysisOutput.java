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
 * Interface for all output types of analysis
 * @since 3.0
 */
public interface IAnalysisOutput {

    /**
     * Gets the name of the output
     *
     * @return Name of the output
     */
    String getName();

    /**
     * Does the output of the analysis
     *
     * @param module The analysis module to output
     */
    void outputAnalysis(IAnalysisModule module);

}
