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


public class TimingPropertyPage extends AbstractProviderPropertyPage {

	public TimingPropertyPage() {
		super();
	}

	@Override
	protected String getType() {
		return "timing"; //$NON-NLS-1$
	}

	@Override
	protected String getPrefPageId() {
		return "org.eclipse.linuxtools.profiling.provider.TimingPreferencePage"; //$NON-NLS-1$
	}

}
