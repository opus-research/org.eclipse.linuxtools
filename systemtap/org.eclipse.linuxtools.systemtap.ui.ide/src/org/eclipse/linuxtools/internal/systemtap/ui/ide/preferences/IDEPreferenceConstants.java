/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

public class IDEPreferenceConstants {
	//ide
	public static final String P_STORED_TREE = "UseStoredTreePreference"; //$NON-NLS-1$
	public static final String P_REMOTE_PROBES = "RemoteProbes"; //$NON-NLS-1$

	//ide.path
	public static final String P_KERNEL_SOURCE = "KernelSourcePreference"; //$NON-NLS-1$
	public static final String P_EXCLUDED_KERNEL_SOURCE = "ExcludedKernelSource"; //$NON-NLS-1$
	public static final String P_REMOTE_LOCAL_KERNEL_SOURCE = "RemoteLocalKernelSource"; //$NON-NLS-1$

	//ide.stap.tapsets
	public static final String P_TAPSETS = "TapsetPreference"; //$NON-NLS-1$

	//ide.editor
	public static final String P_EDITOR_BACKGROUND = "EditorBackgroundPreference"; //$NON-NLS-1$
	public static final String P_SHOW_LINE_NUMBERS = "ShowLineNumbers"; //$NON-NLS-1$

	//ide.editor.codeassist
	public static final String P_USE_CODE_ASSIST = "UseCodeAssistPreference"; //$NON-NLS-1$
	public static final String P_COMPLETION = "CompletionPreference"; //$NON-NLS-1$
	public static final String P_COMPLETION_INSERT = "CompletionInsertPreference"; //$NON-NLS-1$
	public static final String P_COMPLETION_OVERWRITE = "CompletionOverwritePreference"; //$NON-NLS-1$
	public static final String P_ACTIVATION_DELAY = "ActivationDelayPreference"; //$NON-NLS-1$
	public static final String P_ACTIVATION_TRIGGER = "ActivationTriggerPreference"; //$NON-NLS-1$

	//ide.editor.preferenceconstants
	public static final String P_CONDITIONAL_FILTERS = "ConditionalFilters"; //$NON-NLS-1$

	public static final int FLAG = 0;
	public static final int LABEL = 1;
	public static final int KEY = 2;
	public static final int TOOLTIP = 3;

	public static final String[][] STAP_BOOLEAN_OPTIONS  = new String[][] {
		{"-k", "Keep temporary directory", "kStapPreference", "keep temporary directory"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"-u", "Unoptimized translation", "uStapPreference", "unoptimized translation"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"-b", "Bulk (relayfs) mode", "bStapPreference", "bulk (relayfs) mode"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"-t", "Benchmarking timing information", "tStapPreference", "benchmarking timing information"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"-v", "Increase verbosity [0]", "vStapPreference", "increase verbosity [0]"}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"--runtime=dyninst", "Use dyninst", "dyninstStapPreference", "Dyninst mode allows you to probe userspace processes without root access. This mode requires a -c COMMAND or a -x PID"}}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	public static final String[][] STAP_STRING_OPTIONS = new String[][] {
		{"-p", "Stop at pass", "pStapPreference", "stop after pass NUM 1-5"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"-s", "Buffer sizse", "sStapPreference", "buffer size in megabytes"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"-R", "Runtime directory", "RStapPreference", "Look for the systemtap runtime sources in the given directory."},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"-r", "Kernel release", "rStapPreference", "use kernel RELEASE"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"-o", "Output file", "oStapPreference", "send output to file"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		{"-x", "target PID", "xStapPreference", "sets target() to PID"}}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	public static final String[] STAP_CMD_OPTION = new String[] {"-c", "CMD run CMD under systemtap", "cStapPreference", "start the probes, run CMD, and exit when it finishes"};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	//ide.editor.syntaxcoloring
	public static final String P_STP_DEFAULT_COLOR = "stpDefaultColorPreference"; //$NON-NLS-1$
	public static final String P_STP_KEYWORD_COLOR = "stpKeywordColorPreference"; //$NON-NLS-1$
	public static final String P_STP_EMBEDDED_C_COLOR = "stpEmbeddedCColorPreference"; //$NON-NLS-1$
	public static final String P_STP_EMBEDDED_COLOR = "stpEmbeddedColorPreference"; //$NON-NLS-1$
	public static final String P_STP_COMMENT_COLOR = "stpCommentColorPreference"; //$NON-NLS-1$
	public static final String P_STP_TYPE_COLOR = "stpTypeColorPreference"; //$NON-NLS-1$
	public static final String P_STP_STRING_COLOR = "stpStringColorPreference"; //$NON-NLS-1$
	public static final String P_C_DEFAULT_COLOR = "cDefaultColorPreference"; //$NON-NLS-1$
	public static final String P_C_KEYWORD_COLOR = "cKeywordColorPreference"; //$NON-NLS-1$
	public static final String P_C_COMMENT_COLOR = "cCommentColorPreference"; //$NON-NLS-1$
	public static final String P_C_PREPROCESSOR_COLOR = "cPreprocessorColorPreference"; //$NON-NLS-1$
	public static final String P_C_TYPE_COLOR = "cTypeColorPreference"; //$NON-NLS-1$
	public static final String P_C_STRING_COLOR = "cStringColorPreference"; //$NON-NLS-1$

}
