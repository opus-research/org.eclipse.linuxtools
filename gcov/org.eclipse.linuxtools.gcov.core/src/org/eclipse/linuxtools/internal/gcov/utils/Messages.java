package org.eclipse.linuxtools.internal.gcov.utils;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gcov.utils.messages"; //$NON-NLS-1$
	public static String GcovStringReader_null_string;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
