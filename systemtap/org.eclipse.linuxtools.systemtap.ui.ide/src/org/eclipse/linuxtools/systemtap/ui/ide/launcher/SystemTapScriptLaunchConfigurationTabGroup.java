package org.eclipse.linuxtools.systemtap.ui.ide.launcher;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;

public class SystemTapScriptLaunchConfigurationTabGroup extends
		AbstractLaunchConfigurationTabGroup {

	public SystemTapScriptLaunchConfigurationTabGroup() {
	}

	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		AbstractLaunchConfigurationTab[] tabs = new AbstractLaunchConfigurationTab[] {
				new SystemTapScriptLaunchConfigurationTab(), new CommonTab() };
		setTabs(tabs);
	}

}
