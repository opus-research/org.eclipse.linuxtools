package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpu;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for Cpu usage view
 *
 * @author Matthew Khouzam
 *
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpu.messages"; //$NON-NLS-1$
    /**
     * The thread name for the builder
     */
    public static String CpuUsageView_ThreadName;
    /**
     * The chart title
     */
    public static String CpuUsageView_ViewTitle;
    /**
     * The X Axis value (time)
     */
    public static String CpuUsageView_XAxis;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
