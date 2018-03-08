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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.widgets.Display;

/**
 * Handler for saving a perf profile session.
 */
public class PerfSaveSessionHandler extends AbstractSaveDataHandler {
	private static String DATA_EXT = "data"; //$NON-NLS-1$

	@Override
	protected void saveData(String filename) {
		// get paths
		IPath newDataLoc = getNewDataLocation(filename, DATA_EXT);
		IPath defaultDataLoc = PerfPlugin.getDefault().getPerfProfileData();

		// get files
		File newDataFile = newDataLoc.toFile();
		File defaultDataFile = defaultDataLoc.toFile();

		if(canSave(newDataFile)){
			// rename default perf data file
			if (defaultDataFile.renameTo(newDataFile)) {
				PerfPlugin.getDefault().setPerfProfileData(newDataLoc);
				PerfPlugin.getDefault().getProfileView()
						.setContentDescription(newDataLoc.toOSString());
			} else {
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						Messages.PerfSaveSession_no_data_found_title,
						Messages.PerfSaveSession_no_data_found_msg);
			}
		}

	}

	@Override
	public boolean verifyData() {
		IPath defaultDataLoc = PerfPlugin.getDefault().getPerfProfileData();
		return defaultDataLoc != null && !defaultDataLoc.isEmpty();
	}

}
