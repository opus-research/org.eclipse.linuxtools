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
package org.eclipse.linuxtools.internal.vagrant.ui.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.vagrant.ui.commands.messages"; //$NON-NLS-1$
	public static String VagrantToolBarContributionItem_destroy;
	public static String VagrantToolBarContributionItem_open;
	public static String VagrantToolBarContributionItem_ssh;
	public static String VagrantToolBarContributionItem_start;
	public static String VagrantToolBarContributionItem_stop;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
