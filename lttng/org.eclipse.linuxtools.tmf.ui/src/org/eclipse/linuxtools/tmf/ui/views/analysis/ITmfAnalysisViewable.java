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

package org.eclipse.linuxtools.tmf.ui.views.analysis;

import java.util.List;

/**
 * Interface that analysis module must implement if they want to publish views
 *
 * @since 3.0
 */
public interface ITmfAnalysisViewable {

    /**
     * Gets a list of the ids of the views provided by this module
     *
     * @return The list of view id
     */
    List<String> getViews();

}
