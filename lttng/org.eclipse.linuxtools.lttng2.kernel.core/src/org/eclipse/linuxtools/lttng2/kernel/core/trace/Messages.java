package org.eclipse.linuxtools.lttng2.kernel.core.trace;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng2.kernel.core.trace.messages"; //$NON-NLS-1$
    /**
     * The domain is not "kernel"
     */
    public static String CtfKernelTrace_DomainError;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
