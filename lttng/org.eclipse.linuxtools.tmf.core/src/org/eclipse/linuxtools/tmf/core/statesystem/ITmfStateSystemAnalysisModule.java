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
 *   Alexandre Montplaisir - Reduced to one single state system per module
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

/**
 * Interface for analysis modules providing a state system
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public interface ITmfStateSystemAnalysisModule {

    /**
     * Return the state system provided by this analysis module
     *
     * @return The state system
     */
    ITmfStateSystem getStateSystem();

    /**
     * Return the ID given to this state system
     *
     * @return The state system ID
     */
    String getStateSystemId();

}
