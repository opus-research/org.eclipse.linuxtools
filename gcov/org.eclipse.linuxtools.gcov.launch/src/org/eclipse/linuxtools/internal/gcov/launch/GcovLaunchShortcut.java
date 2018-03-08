/*******************************************************************************
 * Copyright (c) 2008, 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Red Hat Inc. - modification to use code in this plug-in
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.gcov.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.gcov.launch.GcovLaunchPlugin;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public class GcovLaunchShortcut extends ProfileLaunchShortcut {


	@Override
	protected void setDefaultProfileAttributes(
			ILaunchConfigurationWorkingCopy wc) throws CoreException {
	}

	/**
	 * Method getValgrindLaunchConfigType.
	 * @return ILaunchConfigurationType
	 */
	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(GcovLaunchPlugin.LAUNCH_ID);
	}

}
