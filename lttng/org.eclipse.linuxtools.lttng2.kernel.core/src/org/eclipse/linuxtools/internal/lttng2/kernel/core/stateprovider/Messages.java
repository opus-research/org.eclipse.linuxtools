/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the state systems
 *
 * @author Matthew Khouzam
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.messages"; //$NON-NLS-1$
    /**
     * Lttng Kernel CPU usage
     */
    public static String CumulativeCpuUsageProvider_CpuUsage;

    /**
     * attribute not found message
     */
    public static String CumulativeCpuUsageProvider_AttributeNotFoundMessage;

    /**
     * Bad state value type message (you got a string instead of an int for example)
     */
    public static String CumulativeCpuUsageProvider_StateValueTypeMessage;

    /**
     * Time range exception message
     */
    public static String CumulativeCpuUsageProvider_TimeRangeExceptionMessage;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
