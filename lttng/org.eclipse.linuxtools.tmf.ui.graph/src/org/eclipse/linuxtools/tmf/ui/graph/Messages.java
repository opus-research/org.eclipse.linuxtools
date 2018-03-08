/*******************************************************************************
 * Copyright (c) 2013 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xavier Raynaud - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.graph;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.graph.messages"; //$NON-NLS-1$
    public static String PlottingDialog_HelpMsg;
    public static String PlottingDialog_InvalidValueOnXAxis;
    public static String PlottingDialog_InvalidValueOnYAxis;
    public static String PlottingDialog_Title;
    public static String PlottingDialog_ValuesOnXAxis;
    public static String PlottingDialog_ValuesOnYAxis;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
