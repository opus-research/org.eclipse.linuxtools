package org.eclipse.linuxtools.internal.gcov.action;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gcov.action.messages"; //$NON-NLS-1$
	public static String OpenGCAction_gcov_error;
	public static String OpenGCAction_file_dne_run;
	public static String OpenGCAction_file_dne_compile;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
