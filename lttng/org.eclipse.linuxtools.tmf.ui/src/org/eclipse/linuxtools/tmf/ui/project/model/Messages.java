/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouamé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.osgi.util.NLS;

/**
 * Message strings for TMF model handling.
 *
 * @author Jean-Christian Kouamé
 * @since 3.0
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.project.model.messages"; //$NON-NLS-1$

    /** The category of the resource properties */
    public static String TmfTraceElement_ResourceProperties;

    /** The category of the trace properties */
    public static String TmfTraceElement_TraceProperties;

    /** The descriptor for the name property */
    public static String TmfTraceElement_Name;

    /** The descriptor for the path property */
    public static String TmfTraceElement_Path;

    /** The descriptor for the location properties */
    public static String TmfTraceElement_Location;

    /** The descriptor for the event type property */
    public static String TmfTraceElement_EventType;

    /** The description for the linked property */
    public static String TmfTraceElement_IsLinked;

    /** Error opening a trace */
    public static String TmfOpenTraceHelper_ErrorOpeningTrace;
    /** Could not link trace */
    public static String TmfOpenTraceHelper_LinkFailed;
    /** No trace type match */
    public static String TmfOpenTraceHelper_NoTraceTypeMatch;
    /** Open Trace*/
    public static String TmfOpenTraceHelper_OpenTrace;
    /** Reduce was too efficient, no candidates found! */
    public static String TmfOpenTraceHelper_ReduceError;
    /** No trace type */
    public static String TmfOpenTraceHelper_NoTraceType;
    /** Init error */
    public static String TmfOpenTraceHelper_InitError;
    /** Error */
    public static String TmfOpenTraceHelper_Error;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
