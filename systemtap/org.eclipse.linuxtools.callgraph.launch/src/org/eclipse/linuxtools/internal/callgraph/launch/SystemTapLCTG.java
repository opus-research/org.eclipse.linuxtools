/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.launch;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;

/**
 * Stripped down version of the Profiling LaunchConfigurationTabGroup
 * 
 *
 */
public abstract class SystemTapLCTG extends ProfileLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<AbstractLaunchConfigurationTab>();
		
		tabs.addAll(Arrays.asList(getProfileTabs()));

		
		setTabs(tabs.toArray(new AbstractLaunchConfigurationTab[tabs.size()]));
	}
	
	@Override
	public abstract AbstractLaunchConfigurationTab[] getProfileTabs();
}