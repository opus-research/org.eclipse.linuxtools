/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;

public class RunImageLaunchConfigurationTabGroup
		extends AbstractLaunchConfigurationTabGroup {

	public RunImageLaunchConfigurationTabGroup() {
	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		setTabs(new AbstractLaunchConfigurationTab[] { new RunImageMainTab(),
				new RunImageVolumesTab(), new RunImagePortsTab(),
				new RunImageLinksTab(), new RunImageResourcesTab(),
				new RunImageEnvironmentTab() });
	}

}
