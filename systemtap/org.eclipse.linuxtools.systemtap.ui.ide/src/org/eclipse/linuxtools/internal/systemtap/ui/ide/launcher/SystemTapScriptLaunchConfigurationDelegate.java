/*******************************************************************************
 * Copyright (c) 2012 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.RunScriptByPathAction;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.ui.PlatformUI;

public class SystemTapScriptLaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		RunScriptByPathAction action = new RunScriptByPathAction();
		IPreferenceStore preferenceStore = ConsoleLogPlugin.getDefault().getPreferenceStore();

		// Path
		String path = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.SCRIPT_PATH_ATTR, ""); //$NON-NLS-1$
		action.init(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), path);

		// User Name
		String userName = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.USER_NAME_ATTR, ""); //$NON-NLS-1$
		preferenceStore.setValue(ConsoleLogPreferenceConstants.SCP_USER, userName);

		// User Password
		String password = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.USER_PASS_ATTR, ""); //$NON-NLS-1$
		preferenceStore.setValue(ConsoleLogPreferenceConstants.SCP_PASSWORD, password);

		// Run locally and/or as current user.
		boolean runAsCurrentUser = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.CURRENT_USER_ATTR, true);
		boolean runLocal = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.LOCAL_HOST_ATTR, true);
		action.setLocalScript(runLocal && runAsCurrentUser);

		// Host Name.
		String hostName = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.HOST_NAME_ATTR, "localhost"); //$NON-NLS-1$
		preferenceStore.setValue(ConsoleLogPreferenceConstants.HOST_NAME, hostName);

		action.run();
	}

}
