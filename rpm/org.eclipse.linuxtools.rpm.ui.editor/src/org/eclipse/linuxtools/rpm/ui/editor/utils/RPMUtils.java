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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Utility class for RPM UI Editor related things.
 *
 */
public class RPMUtils {

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

	private static final String PROJECT_HOLDER = "%projectName"; //$NON-NLS-1$
	private static final String CONSOLE_NAME = "Packager Console (%projectName)"; //$NON-NLS-1$

	/**
	 * @param packageName The name of the package(RPM) this console will be for.
	 * @return A console instance.
	 */
	public static MessageConsole getConsole(String packageName) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		String projectConsoleName = CONSOLE_NAME.replace(PROJECT_HOLDER, packageName);
		MessageConsole ret = null;
		for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles()) {
			if (cons.getName().equals(projectConsoleName)) {
				ret = (MessageConsole) cons;
			}
		}
		// no existing console, create new one
		if (ret == null) {
			ret = new MessageConsole(projectConsoleName,
					AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/rpm.gif")); //$NON-NLS-1$
		}
		conMan.addConsoles(new IConsole[] { ret });
		ret.clearConsole();
		ret.activate();
		return ret;
	}

}
