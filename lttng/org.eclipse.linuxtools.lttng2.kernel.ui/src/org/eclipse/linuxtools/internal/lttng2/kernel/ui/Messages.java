package org.eclipse.linuxtools.internal.lttng2.kernel.ui;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.kernel.ui.messages"; //$NON-NLS-1$

    public static String ControlFlowView_birthTimeColumn;
    public static String ControlFlowView_tidColumn;
    public static String ControlFlowView_ptidColumn;
    public static String ControlFlowView_processColumn;
    public static String ControlFlowView_traceColumn;

    public static String ControlFlowView_stateTypeName;
    public static String ControlFlowView_multipleStates;
    public static String ControlFlowView_nextProcessActionNameText;
    public static String ControlFlowView_nextProcessActionToolTipText;
    public static String ControlFlowView_previousProcessActionNameText;
    public static String ControlFlowView_previousProcessActionToolTipText;

    public static String ControlFlowView_attributeSyscallName;
    public static String ControlFlowView_attributeCpuName;

    public static String ResourcesView_stateTypeName;
    public static String ResourcesView_multipleStates;
    public static String ResourcesView_nextResourceActionNameText;
    public static String ResourcesView_nextResourceActionToolTipText;
    public static String ResourcesView_previousResourceActionNameText;
    public static String ResourcesView_previousResourceActionToolTipText;
    public static String ResourcesView_attributeCpuName;
    public static String ResourcesView_attributeIrqName;
    public static String ResourcesView_attributeSoftIrqName;
    public static String ResourcesView_attributeHoverTime;
    public static String ResourcesView_attributeTidName;
    public static String ResourcesView_attributeProcessName;
    public static String ResourcesView_attributeSyscallName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
