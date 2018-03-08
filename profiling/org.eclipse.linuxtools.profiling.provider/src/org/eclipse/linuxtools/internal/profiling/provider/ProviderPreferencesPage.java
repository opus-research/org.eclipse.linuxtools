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

package org.eclipse.linuxtools.internal.profiling.provider;

public class ProviderPreferencesPage extends AbstractProviderPreferencesPage {

	private static final String TYPE = "generic"; //$NON-NLS-1$
	private static final String KEY = "provider"; //$NON-NLS-1$
	private static final String QUALIFIER = "org.eclipse.linuxtools.profiling.snapshot"; //$NON-NLS-1$

	@Override
	public String getProfilingType() {
		return TYPE;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getPluginID() {
		return QUALIFIER;
	}

}