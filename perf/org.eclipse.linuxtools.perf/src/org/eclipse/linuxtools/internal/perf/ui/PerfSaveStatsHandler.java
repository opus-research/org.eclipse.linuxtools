package org.eclipse.linuxtools.internal.perf.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.StatData;
import org.eclipse.swt.widgets.Display;

public class PerfSaveStatsHandler extends AbstractSaveDataHandler {

	private static String DATA_EXT = "stat"; //$NON-NLS-1$

	@Override
	protected void saveData(String filename) {
		IPath newDataLoc = getNewDataLocation(filename, DATA_EXT);
		StatData statData = PerfPlugin.getDefault().getStatData();
		File statsData = new File(newDataLoc.toOSString());

		if (verifyFile(statsData)) {
			BufferedWriter bw = null;
			try {
				statsData.createNewFile();
				FileWriter fw = new FileWriter(statsData.getAbsoluteFile());
				bw = new BufferedWriter(fw);
				bw.write(statData.getPerfData());
				bw.close();
			} catch (IOException e) {
				String errorMsg = MessageFormat.format(
						Messages.PerfSaveStat_error_msg,
						new Object[] { statsData.getName() });
				MessageDialog.openError(Display.getCurrent().getActiveShell(),
						Messages.PerfSaveStat_error_title, errorMsg);
			}
		}
	}

	@Override
	protected boolean verifyData() {
		StatData statData = PerfPlugin.getDefault().getStatData();
		return statData != null && statData.getPerfData() != null;
	}

}
