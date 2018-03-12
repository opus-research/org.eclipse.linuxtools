package org.eclipse.linuxtools.internal.tmf.pcap.core.util;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tmf.pcap.core.util.messages"; //$NON-NLS-1$
    public static @Nullable String PcapEventFactory_LinkType;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
