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
package org.eclipse.linuxtools.profiling.time;

import org.eclipse.linuxtools.internal.profiling.provider.ProviderOptionsTab;

public class TimeOptionsTab extends ProviderOptionsTab {

	public String getName() {
		return TimeProviderPlugin.PLUGIN_NAME;
	}

	@Override
	protected String getProfilingType() {
		return TimeProviderPlugin.PROFILING_TYPE;
	}
}
