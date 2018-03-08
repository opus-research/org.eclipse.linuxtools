package org.eclipse.linuxtools.internal.systemtap.ui.graphicalrun.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.graphicalrun.actions.messages"; //$NON-NLS-1$
	public static String RunScriptChartAction_couldNotSwitchToGraphicPerspective;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
