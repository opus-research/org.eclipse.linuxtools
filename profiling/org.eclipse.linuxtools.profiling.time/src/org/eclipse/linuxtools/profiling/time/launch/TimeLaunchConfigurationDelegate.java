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
package org.eclipse.linuxtools.profiling.time.launch;

import org.eclipse.linuxtools.internal.profiling.provider.launch.ProviderLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.time.TimeProviderPlugin;

/**
 * The launch configuration delegate for time profiling plug-ins.
 *
 */
public class TimeLaunchConfigurationDelegate extends
		ProviderLaunchConfigurationDelegate {

	@Override
	protected String getPluginID() {
		return TimeProviderPlugin.PLUGIN_ID;
	}

	@Override
	public String getProfilingType() {
		return TimeProviderPlugin.PROFILING_TYPE;
	}

}
