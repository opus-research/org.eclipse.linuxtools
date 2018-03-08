/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch.provider.launch;

import org.eclipse.cdt.debug.core.ICDTProfileDelegate;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

// Special version of a launch delegate for use with the org.eclipse.cdt.launch.profilingProvider
// extension.  It also implements the CDT ICDTProfileDelegate which allows the CDT code to
// do attribute initialization.
public class CProfilingLaunchConfigurationDelegate extends
		ProviderLaunchConfigurationDelegate implements ICDTProfileDelegate {

	public void setDefaultProfileLaunchAttributes(
			ILaunchConfigurationWorkingCopy wc) {
		// TODO: get type from preferences or properties
		String providerId = getProviderIdToRun(wc, "timing"); //$NON-NLS-1$
		ProfileLaunchShortcut shortcut = ProviderFramework.getLaunchShortcutProviderFromId(providerId);
		shortcut.setDefaultProfileLaunchShortcutAttributes(wc);
		wc.setAttribute(ProviderProfileConstants.PROVIDER_CONFIG_ATT,
				providerId);
		wc.setAttribute("org.eclipse.cdt.launch.profilingProvider", //$NON-NLS-1$
				providerId);

		// set tool name in configuration.
		String providerToolName = ProviderFramework.getProviderToolNameFromId(providerId);
		wc.setAttribute(ProviderProfileConstants.PROVIDER_CONFIG_TOOLNAME_ATT, providerToolName);
	}

}
