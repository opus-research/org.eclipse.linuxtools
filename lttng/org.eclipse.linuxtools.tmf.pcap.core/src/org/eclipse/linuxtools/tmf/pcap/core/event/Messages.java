package org.eclipse.linuxtools.tmf.pcap.core.event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

@NonNullByDefault(false)
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.pcap.core.event.messages"; //$NON-NLS-1$
    public static String PcapEventType_DefaultContext;
    public static String PcapEventType_DefaultTypeID;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
