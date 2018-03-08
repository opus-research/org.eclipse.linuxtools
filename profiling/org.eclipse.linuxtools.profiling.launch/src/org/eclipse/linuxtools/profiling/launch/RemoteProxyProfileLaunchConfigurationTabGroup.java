/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Rafael Teixeira <rafaelmt@linux.vnet.ibm.com> - Switched to
 * 	RemoteProxyCMainTab
 *******************************************************************************/

package org.eclipse.linuxtools.profiling.launch;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

/**
 * Daniel HB, 2012/12/14 - Changed superclass from AbstractLaunchConfigurationTabGroup to
 * ProfileLaunchConfigurationTabGroup to be compatible with the tab groups used by
 * the local profile launchers. In the future this class can disappear and 
 * ProfileLaunchConfigurationTabGroup will provide both local and remote tabs if we 
 * can distinguish the launch configuration 'mode' from local and remote. Both local 
 * and remote is 'profile'. Changing the remote mode to something else than 'profile' 
 * makes the launch configuration vanish from the "Profile Configurations" of Eclipse. 
 * It would require to create a new category of profiling, called "remote profiling" 
 * that would have its own remote launchers separated from the local ones. Maybe
 * this should be the initial design of remote profiling in the first place?
 * 
 * @since 2.0
 */
public abstract class RemoteProxyProfileLaunchConfigurationTabGroup extends ProfileLaunchConfigurationTabGroup {

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		ArrayList<AbstractLaunchConfigurationTab> tabs = new ArrayList<AbstractLaunchConfigurationTab>();
		tabs.add(new RemoteProxyCMainTab());
		tabs.add(new CArgumentsTab());

		tabs.addAll(Arrays.asList(getProfileTabs()));

		tabs.add(new EnvironmentTab());
		tabs.add(new SourceLookupTab());
		tabs.add(new CommonTab());

		setTabs(tabs.toArray(new AbstractLaunchConfigurationTab[tabs.size()]));
	}

}
