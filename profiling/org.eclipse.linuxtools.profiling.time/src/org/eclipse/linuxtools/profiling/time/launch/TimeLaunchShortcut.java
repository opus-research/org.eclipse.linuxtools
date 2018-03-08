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

import org.eclipse.linuxtools.internal.profiling.provider.launch.ProviderLaunchShortcut;
import org.eclipse.linuxtools.profiling.time.TimeProviderPlugin;

/**
 * The launch shortcut for time profiling type plug-ins.
 *
 */
public class TimeLaunchShortcut extends ProviderLaunchShortcut {

	@Override
	protected String getProfilingType() {
		return TimeProviderPlugin.PROFILING_TYPE;
	}

}
