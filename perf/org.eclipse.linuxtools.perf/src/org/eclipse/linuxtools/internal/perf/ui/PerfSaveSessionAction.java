/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.ui;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.widgets.Display;

public class PerfSaveSessionAction extends Action {

	public PerfSaveSessionAction() {
		super(Messages.PerfSaveSession_title);
	}

	@Override
	public void run() {
		InputDialog dialog = new InputDialog(Display.getCurrent()
				.getActiveShell(), Messages.PerfSaveSession_title,
				Messages.PerfSaveSession_msg, "", null);

		if (dialog.open() == Window.OK) {
			String fileName = dialog.getValue();
			IPath defaultDataLoc = PerfPlugin.getDefault().getPerfProfileData();

			if ("".equals(fileName)) {
				MessageDialog.openWarning(
						Display.getCurrent().getActiveShell(),
						Messages.PerfSaveSession_invalid_filename_title,
						Messages.PerfSaveSession_invalid_filename_msg);
				run();
				return;
			} else if (defaultDataLoc == null || defaultDataLoc.isEmpty()) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						Messages.PerfSaveSession_no_data_found_title,
						Messages.PerfSaveSession_no_data_found_msg);
				return;

			}

			IPath newDataLoc = defaultDataLoc.removeLastSegments(1).append(fileName);
			File newDataFile = newDataLoc.toFile();
			File defaultDataFile = defaultDataLoc.toFile();

			defaultDataFile.renameTo(newDataFile);
		}
	}

}
