/*******************************************************************************
 * (C) Copyright 2012 IBM Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch.provider.remote;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderOptionsTab;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyProfileLaunchConfigurationTabGroup;

/**
 * This (temporary) class is a copy of the ProfileLaunchConfigurationTabGroup
 * class, but with different inheritance 
 * (RemoteProxyProfileLaunchConfigurationTabGroup instead of 
 * ProfileLaunchConfigurationTabGroup). I couldn't figure out a clean way 
 * to host both local and remote tab groups using the existing
 * class and Java doesn't allow multiple inheritance (that way one class 
 * could extend both remote and local tab groups classes).
 * 
 * This class can be deleted if remote profiling starts using the same
 * tab group provider as the local one. See 
 * RemoteProxyProfileLaunchConfigurationTabGroup for more info about
 * that.
 * 
 * 
 * @author danielhb
 *
 * @since 2.0
 */
public class ProviderLaunchConfigurationTabGroup extends 
RemoteProxyProfileLaunchConfigurationTabGroup implements IExecutableExtension {

	// Profiling type.
	private String type;

	// Profiling type name to be displayed.
	private String name;

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		Map<String, String> parameters = (Map<String, String>) data;
		String profilingType = parameters
				.get(ProviderProfileConstants.INIT_DATA_TYPE_KEY);
		String profilingTypeName = parameters
				.get(ProviderProfileConstants.INIT_DATA_NAME_KEY);

		if (profilingType == null) {
			profilingType = "";
		}
		if (profilingTypeName == null) {
			profilingTypeName = "";
		}

		setProfilingType(profilingType);
		setProfilingTypeName(profilingTypeName);
	}

	@Override
	public AbstractLaunchConfigurationTab[] getProfileTabs() {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<AbstractLaunchConfigurationTab>();
		tabs.add(new ProviderOptionsTab(type, name));

		return tabs.toArray(new AbstractLaunchConfigurationTab [] {});
	}

	/**
	 * Set profiling type.
	 *
	 * @param profilingType
	 */
	private void setProfilingType(String profilingType) {
		type = profilingType;
	}

	/**
	 * Set profiling type name to be displayed.
	 *
	 * @param profilingTypeName
	 */
	private void setProfilingTypeName(String profilingTypeName) {
		name = profilingTypeName;
	}
}
