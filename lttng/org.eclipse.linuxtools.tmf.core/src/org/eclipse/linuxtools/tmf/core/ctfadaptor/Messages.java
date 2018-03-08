package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.osgi.util.NLS;

/**
 * @author Matthew Khouzam
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.core.ctfadaptor.messages"; //$NON-NLS-1$
    /**
     * "CTF"
     */
    public static String CtfTmfTrace_FormatName;
    /**
     * And error message
     */
    public static String CtfTmfTrace_MajorNotSet;
    /**
     * Read error
     */
    public static String CtfTmfTrace_ReadingError;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
