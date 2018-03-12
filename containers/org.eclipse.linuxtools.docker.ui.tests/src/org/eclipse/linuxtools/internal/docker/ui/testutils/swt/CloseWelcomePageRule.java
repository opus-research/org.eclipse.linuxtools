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

package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import static org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory.withPartName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.ui.IViewReference;
import org.junit.rules.ExternalResource;

/**
 * Closes the Welcome page
 */
public class CloseWelcomePageRule extends ExternalResource {

	@Override
	protected void before() {
		final SWTWorkbenchBot bot = new SWTWorkbenchBot();
		bot.views().stream().filter(v -> v.getReference().getTitle().equals("Welcome")).forEach(v -> v.close()); //$NON-NLS-1$
		bot.perspectiveById("org.eclipse.linuxtools.docker.ui.perspective").activate(); //$NON-NLS-1$
		bot.view(allOf(instanceOf(IViewReference.class), withPartName("Docker Explorer"))); //$NON-NLS-1$
	}
}
