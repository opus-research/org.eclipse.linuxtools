package org.eclipse.linuxtools.internal.gcov.view.annotatedsource;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gcov.view.annotatedsource.messages"; //$NON-NLS-1$
	public static String CoverageAnnotationColumn_line_mulitiple_exec;
	public static String CoverageAnnotationColumn_line_exec_once;
	public static String CoverageAnnotationColumn_line_never_exec;
	public static String CoverageAnnotationColumn_non_exec_line;
	public static String OpenSourceFileAction_open_error;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
