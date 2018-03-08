package org.eclipse.linuxtools.internal.binutils.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.binutils.preferences.messages"; //$NON-NLS-1$
    public static String BinutilsPreferencePage_addr2line;
    public static String BinutilsPreferencePage_addr2line_flags;
    public static String BinutilsPreferencePage_cppfilt;
    public static String BinutilsPreferencePage_cppfilt_flags;
    public static String BinutilsPreferencePage_description;
    public static String BinutilsPreferencePage_nm;
    public static String BinutilsPreferencePage_nm_flags;
    public static String BinutilsPreferencePage_title;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
