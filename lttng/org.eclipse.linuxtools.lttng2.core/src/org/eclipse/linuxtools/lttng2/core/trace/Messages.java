package org.eclipse.linuxtools.lttng2.core.trace;

import org.eclipse.osgi.util.NLS;

/**
 * LTTng Messages
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng2.core.trace.messages"; //$NON-NLS-1$
    /**
     * UST domain error, should be "ust" but was not
     */
    public static String LTTngUstTrace_USTDomainError;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
