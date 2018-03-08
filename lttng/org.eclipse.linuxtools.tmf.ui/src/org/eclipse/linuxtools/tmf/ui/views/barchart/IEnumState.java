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

package org.eclipse.linuxtools.tmf.ui.views.barchart;

import org.eclipse.swt.graphics.RGB;

/**
 * This interface is used in the presentation providers. The enums defined in
 * the concrete BarChartPresentationProvider classes must implement this
 * interface to take advantage of the base class functionnalities.
 *
 * @since 2.0
 */
public interface IEnumState {

    /**
     * Provides the enum name function
     *
     * @return The name of this state values
     */
    public String name();

    /**
     * @return the color associated with a state
     */
    public RGB rgb();

    /**
     * Provides the enum ordinal function
     *
     * @return the enum ordinal()
     */
    int ordinal();
}
