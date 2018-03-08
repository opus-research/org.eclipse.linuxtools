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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatData;

/**
 * Handler for saving a perf statistics session.
 */
public class PerfSaveStatsHandler extends AbstractSaveDataHandler {

	private static String DATA_EXT = "stat"; //$NON-NLS-1$

	@Override
	public File saveData(String filename) {
		IPath newDataLoc = getNewDataLocation(filename, DATA_EXT);
		StatData statData = PerfPlugin.getDefault().getStatData();

		File statsData = new File(newDataLoc.toOSString());

		if (canSave(statsData)) {
			BufferedWriter bw = null;
			try {
				statsData.createNewFile();
				FileWriter fw = new FileWriter(statsData.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				bw.write(statData.getPerfData());
				return statsData;
			} catch (IOException e) {
				openErroDialog(Messages.PerfSaveStat_error_title,
						Messages.PerfSaveStat_error_msg,
						newDataLoc.lastSegment());
			} finally {
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	@Override
	public boolean verifyData() {
		StatData statData = PerfPlugin.getDefault().getStatData();
		return statData != null && statData.getPerfData() != null;
	}

}
