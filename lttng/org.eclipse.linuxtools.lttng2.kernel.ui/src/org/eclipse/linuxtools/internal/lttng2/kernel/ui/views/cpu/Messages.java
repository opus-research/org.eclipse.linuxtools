/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Jean-Christian Kouamé - accelerate the view using a cumulCpuUsageProvider
 *                             and monitors
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpu;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for Cpu usage view
 *
 * @author Matthew Khouzam
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpu.messages"; //$NON-NLS-1$
    /**
     * The chart title
     */
    public static String CpuUsageView_ViewTitle;
    /**
     * The X Axis value (time)
     */
    public static String CpuUsageView_XAxis;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
