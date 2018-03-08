package org.eclipse.linuxtools.internal.gprof.view;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.gprof.view.messages"; //$NON-NLS-1$
    public static String GmonView_filter_by_name;
    public static String GmonView_type_filter_text;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
