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

import org.eclipse.osgi.util.NLS;

/**
 * Generic messages for the bar charts
 *
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.barchart.messages"; //$NON-NLS-1$

    public static String AbstractBarChartView_NextText;
    public static String AbstractBarChartView_NextTooltip;
    public static String AbstractBarChartView_PreviousText;
    public static String AbstractBarChartView_PreviousTooltip;
    public static String BarChartPresentationProvider_multipleStates;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
