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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.profiling.provider.ProviderOptionsTab;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public abstract class ProviderLaunchShortcut extends ProfileLaunchShortcut {

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(getLaunchConfigID());
	}

	@Override
	protected ILaunchConfiguration findLaunchConfiguration(IBinary bin, String mode) {
		// create a launch configuration based on the shortcut
		ILaunchConfiguration config = createConfiguration(bin, false);
		boolean exists = false;

		try {
			for (ILaunchConfiguration cfg : getLaunchManager().getLaunchConfigurations()){
				if (areEqual(config, cfg)){
					exists = true;
				}
			}
		} catch (CoreException e) {
			exists = true;
		}

		// only save the configuration if it does not exist
		if (! exists) {
			createConfiguration(bin);
		}

		return super.findLaunchConfiguration(bin, mode);
	}

	/**
	 * @param cfg1 a launch configuration
	 * @param cfg2 a launch configuration
	 * @return true if the launch configurations contain the exact
	 * same attributes, and false otherwise.
	 */
	private boolean areEqual(ILaunchConfiguration cfg1,
			ILaunchConfiguration cfg2) {
		try {
			return cfg1.getAttributes().equals(cfg2.getAttributes());
		} catch (CoreException e) {
			return false;
		}
	}


	@Override
	protected void setDefaultProfileAttributes(ILaunchConfigurationWorkingCopy wc) {

		// acquire a provider id to run.
		String providerId = ProviderLaunchConfigurationDelegate.getProviderIdToRun(getProfilingType());
		// get configuration shortcut associated with provider id.
		ProfileLaunchShortcut shortcut= ProfileLaunchShortcut.getLaunchShortcutProviderFromId(providerId);
		// set attributes related to the specific profiling shortcut configuration.
		shortcut.setDefaultProfileLaunchShortcutAttributes(wc);

		wc.setAttribute(ProviderOptionsTab.PROVIDER_CONFIG_ATT, providerId);
	}

	/**
	 * Get profiling type of this plug-in.
	 *
	 * @return String profiling type this plug-in supports.
	 */
	protected abstract String getLaunchConfigID();

	public abstract String getProfilingType();

}
