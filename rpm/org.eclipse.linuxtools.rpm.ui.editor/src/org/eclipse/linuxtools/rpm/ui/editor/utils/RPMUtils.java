/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for RPM UI Editor related things.
 *
 */
public class RPMUtils {

	/**
	 * Utility classes should not have a public or default constructor.
	 */
	private RPMUtils() {}

	/**
	 * Show an error dialog.
	 *
	 * @param shell A valid shell
	 * @param title The error dialog title
	 * @param message The message to be displayed.
	 */
	public static void showErrorDialog(final Shell shell,
			final String title, final String message) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(shell, title, message);
			}
		});
	}

	/**
	 * Check if the line passed in is a valid URL.
	 *
	 * @param line The line to check if is a valid URL.
	 * @return True if valid URL, false otherwise.
	 */
	public static boolean isValidUrl(String line) {
		boolean rc = false;

		try {
			new URL(line);
			rc = true;
		} catch (MalformedURLException e) {}

		return rc;
	}

	public static boolean isValidFile(String line) {
		boolean rc = false;

		Pattern pattern = Pattern.compile("[\\w\\d-\\.]+(?:\\.[^\\.]+$)?"); //$NON-NLS-1$
		Matcher variableMatcher = pattern.matcher(line);

		if (variableMatcher.matches()) {
			rc = true;
		}

		return rc;
	}

	/**
	 * Get the file from the URL if any.
	 *
	 * @param url The URL to get the file from.
	 * @return Return the filename.
	 */
	public static String getURLFilename(String url) {
		String rc = ""; //$NON-NLS-1$

		try {
			int lastSegment = url.lastIndexOf("/") + 1; //$NON-NLS-1$
			rc = url.substring(lastSegment).trim();
		} catch (IndexOutOfBoundsException e) {
			SpecfileLog.logError(e);
		}

		return rc;
	}

	/**
	 * Check if the file exists within the current project.
	 * It will first check the root of the project and then the sources. If the
	 * file cannot be found in either, return false.
	 * An empty file name would immediately return false.
	 *
	 * @return True if the file exists.
	 */
	public static boolean fileExistsInSources(IFile original, String fileName) {
		boolean rc = true;
		if (fileName.trim().equals("")) { //$NON-NLS-1$
			return false;
		}
		IContainer container = original.getParent();
		IResource resourceToOpen = container.findMember(fileName);
		IFile file = null;

		if (resourceToOpen == null) {
			IResource sourcesFolder = container.getProject().findMember(
					"SOURCES"); //$NON-NLS-1$
			file = container.getFile(new Path(fileName));
			if (sourcesFolder != null) {
				file = ((IFolder) sourcesFolder).getFile(new Path(fileName));
			}
			if (!file.exists()) {
				rc = false;
			}
		}

		return rc;
	}
}
