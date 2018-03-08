/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.launch;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class PerfDefaultLaunchConfig extends ProfileLaunchShortcut {

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(
				PerfPlugin.LAUNCHCONF_ID);
	}

	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) throws CoreException {
		wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
		wc.setAttribute(IDebugUIConstants.ATTR_CAPTURE_IN_CONSOLE, true);
	}

	/**
	 * Create an ILaunchConfiguration instance given the project's name.
	 * 
	 * @param projectName
	 * @return ILaunchConfiguration based on String projectName
	 */
	public ILaunchConfiguration createDefaultConfiguration(String projectName) {
		ILaunchConfiguration config = null;
		try {
			ILaunchConfigurationType configType = getLaunchConfigType();
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(
					null,
					getLaunchManager().generateLaunchConfigurationName(
							projectName));
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
					projectName);
			config = wc;

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}
}