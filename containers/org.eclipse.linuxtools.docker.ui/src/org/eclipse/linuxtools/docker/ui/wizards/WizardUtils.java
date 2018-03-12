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

package org.eclipse.linuxtools.docker.ui.wizards;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImageSearchResult;
import org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageSearch;
import org.eclipse.swt.widgets.Shell;

/**
 * Utility class to open Wizards while keeping their implementation in the
 * '.internal' package.
 */
public class WizardUtils {

	/**
	 * Opens the {@link ImageSearch} wizards using the given
	 * {@link IDockerConnection} and the given {@link Shell}
	 * 
	 * @param connection
	 *            the current {@link IDockerConnection}
	 * @param shell
	 *            the current {@link Shell}
	 * @return the full name of the selected image (including the tag) or
	 *         <code>null</code> if the user cancelled the search.
	 */
	public static String openSearchWizard(final IDockerConnection connection,
			final Shell shell) {
		final ImageSearch imageSearchWizard = new ImageSearch(connection);
		final boolean completed = CommandUtils.openWizard(imageSearchWizard,
				shell);
		if (completed) {
			final IDockerImageSearchResult selectedSearchImage = imageSearchWizard
					.getSelectedImage();
			return selectedSearchImage.getName();
		}
		return null;
	}
}
