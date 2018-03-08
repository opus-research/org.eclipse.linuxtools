/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import org.eclipse.osgi.util.NLS;

/**
 * The messages for import trace wizards
 * @author Matthew Khouzam
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.messages"; //$NON-NLS-1$

    public static String ImportTraceWizard_importProblem ;
    public static String ImportTraceWizard_importCaption;
    public static String ImportTraceWizard_traceDisplayName;
    public static String ImportTraceWizard_linkTraces;
    public static String ImportTraceWizard_copyTraces;
    public static String ImportTraceWizard_overwriteTraces;
    public static String ImportTraceWizard_addFile;
    public static String ImportTraceWizard_addDirectory;
    public static String ImportTraceWizard_remove;
    public static String ImportTraceWizardDirectory_title;
    public static String ImportTraceWizardDirectory_hint;
    public static String ImportTraceWizardScanPage_renameError;
    public static String ImportTraceWizardScanPage_selectAtleastOne;

    public static String ImportTraceWizardScanPage_title;
    public static String ImportTraceWizard_selectAll;

    public static String ImportTraceWizardSelectTraceTypePage_title;
    public static String ImportTraceWizardPageOptions_noProjectSelected;

    public static String ImportTraceWizardPageOptions_title;

    public static String ImportTraceWizardPageScan_done;
    public static String ImportTraceWizardPageScan_scanning;
    public static String ImportTraceWizardPage_selectNone;
    public static String ImportTraceWizardPage_selectHint;
    public static String BatchImportTraceWizard_remove;
    public static String BatchImportTraceWizard_add;

    public static String BatchImportTraceWizard_errorImportingTraceResource;

    public static String BatchImportTraceWizard_selectProject;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
