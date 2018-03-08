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
package org.eclipse.linuxtools.internal.profiling.provider.launch;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.profiling.provider.AbstractProviderPreferencesPage;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;
//import org.eclipse.linuxtools.profiling.snapshot.SnapshotPreferencesPage;

public abstract class ProviderLaunchShortcut extends ProfileLaunchShortcut {

	@Override
	public void launch(IBinary bin, String mode) {
		ProfileLaunchShortcut provider = null;
		String providerId = null;
		// Get default launch provider id from preference store
		providerId = getProviderIdFromPreferences();
		if (!providerId.equals("")) {
			provider = ProfileLaunchShortcut
					.getLaunchShortcutProviderFromId(providerId);
		}
		if (provider == null) {
			// Get self assigned default
			providerId = ProfileLaunchShortcut
					.getDefaultLaunchShortcutProviderId(getProfilingType());
			provider = ProfileLaunchShortcut
					.getLaunchShortcutProviderFromId(providerId);
			if (provider == null) {
				// Get highest priority provider
				providerId = ProfileLaunchConfigurationTabGroup
						.getHighestProviderId(getProfilingType());
				provider = ProfileLaunchShortcut
						.getLaunchShortcutProviderFromId(providerId);
			}
		}
		if (provider != null){
			provider.launch(bin, mode);
		}else{
			handleFail(Messages.ProviderLaunchShortcut_0 + getProfilingType());
		}
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return null;
	}

	// use configuration scope to implement
	public String getProviderIdFromPreferences(){
		IScopeContext configScope = AbstractProviderPreferencesPage
				.getConfigurationScope();
		return configScope.getNode(getPluginID())
				.get(getKey(), "");
	}

	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) {
		//TODO determine what should be done here
	}

	/**
	 * Get preferences configuration scope node key.
	 *
	 * @return String unique key to access preferences configuration scope node.
	 */
	protected abstract String getKey();

	/**
	 * Get plug-in id.
	 *
	 * @return String unique id of this plug-in.
	 */
	protected abstract String getPluginID();

	/**
	 * Get profiling type of this plug-in.
	 *
	 * @return String profiling type this plug-in supports.
	 */
	protected abstract String getProfilingType();

}
