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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for RPM UI Editor related things.
 * 
 */
public class RPMUtils {

	/*
	 * (non-Javadoc)
	 * @see org.fedoraproject.eclipse.packager.IProjectRoot#getSpecfileModel()
	 */
	public static Specfile parseToSpecfile(IFile file) {
		SpecfileParser parser = new SpecfileParser();
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					file.getContents()));
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Specfile specfile = parser.parse(sb.toString());
		return specfile;
	}

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

}
