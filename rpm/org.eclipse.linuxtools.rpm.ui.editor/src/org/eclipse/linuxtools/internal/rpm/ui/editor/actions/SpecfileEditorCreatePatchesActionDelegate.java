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

package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.rpm.ui.editor.RPMHandlerUtils;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileSource;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.ui.editor.SpecfileEditor;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.utils.RPMUtils;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

public class SpecfileEditorCreatePatchesActionDelegate extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShellChecked(event);
		final IResource resource = RPMHandlerUtils.getResource(event);
		final RPMProject rpj = RPMHandlerUtils.getRPMProject(resource);
		final Specfile specfile = ((SpecfileEditor) HandlerUtil
				.getActiveEditor(event)).getSpecfile();

		final byte[] empty = "".getBytes(); //$NON-NLS-1$
		final InputStream source = new ByteArrayInputStream(empty);

		if (specfile != null) {
			List<SpecfileSource> specfileSourceList = specfile.getPatches();
			if (specfileSourceList != null) {
				IFile file = null;
				try {
					MessageConsoleStream out = getConsole(Messages.CreatePatches_consoleName).newMessageStream();

					for (SpecfileSource ss : specfileSourceList) {
						file = rpj.getConfiguration().getSourcesFolder().getFile(new Path(ss.getFileName()));
						if (!file.exists()) {
							try {
								file.create(source, IResource.NONE, null);
								out.write(NLS.bind(Messages.CreatePatches_consoleCreated, ss.getFileName()));
							} catch (CoreException e) {
								SpecfileLog.logError(NLS.bind(Messages.CreatePatches_errorOnFileCreation, ss.getFileName()), e);
								RPMUtils.showErrorDialog(shell,	Messages.CreatePatches_error, NLS.bind(Messages.CreatePatches_errorOnFileCreation, ss.getFileName()));
							}
						} else {
							out.write(NLS.bind(Messages.CreatePatches_consoleNotCreated, ss.getFileName()));
						}
						out.flush();
					}
					out.close();
				} catch (IOException e) {
					SpecfileLog.logError(Messages.CreatePatches_consoleError, e);
					RPMUtils.showErrorDialog(shell,	Messages.CreatePatches_error, Messages.CreatePatches_consoleError);
				}
			}
		}

		return null;
	}

	/**
	 * Get the console.
	 *
	 * @param consoleName
	 *            The name of the console.
	 * @return A console instance.
	 */
	public MessageConsole getConsole(String consoleName) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		String projectConsoleName = consoleName;
		MessageConsole ret = null;
		for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles()) {
			if (cons.getName().equals(projectConsoleName)) {
				ret = (MessageConsole) cons;
			}
		}
		// no existing console, create new one
		if (ret == null) {
			ret = new MessageConsole(projectConsoleName, null, null, true);
		}
		conMan.addConsoles(new IConsole[] { ret });
		ret.clearConsole();
		ret.activate();
		return ret;
	}
}
