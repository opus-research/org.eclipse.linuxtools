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
package org.eclipse.linuxtools.profiling.coverage.launch;

import org.eclipse.linuxtools.internal.profiling.provider.launch.ProviderLaunchShortcut;
import org.eclipse.linuxtools.profiling.coverage.CoverageProfileConstants;

/**
 * 
 * Profiling short-cut proxy that uses the launch short-cut for the
 * chosen memory profiling provider.
 *
 */
public class CoverageLaunchShortcut extends ProviderLaunchShortcut {
	
	@Override
	protected String getLaunchConfigID() {
		return CoverageProfileConstants.PLUGIN_CONFIG_ID;
	}

	@Override
	public String getProfilingType() {
		return CoverageProfileConstants.PROFILING_TYPE;
	}

}
