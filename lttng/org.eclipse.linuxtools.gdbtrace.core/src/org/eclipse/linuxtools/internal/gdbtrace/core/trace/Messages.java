package org.eclipse.linuxtools.internal.gdbtrace.core.trace;

import org.eclipse.osgi.util.NLS;

/**
 *
 * @author ematkho
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gdbtrace.core.trace.messages"; //$NON-NLS-1$
    /**
     * File not found
     */
    public static String GdbTrace_FileNotFound;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
