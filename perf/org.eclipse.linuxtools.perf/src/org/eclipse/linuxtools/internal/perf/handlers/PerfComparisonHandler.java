package org.eclipse.linuxtools.internal.perf.handlers;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.internal.perf.ReportComparisonData;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.ui.ReportComparisonView;

public class PerfComparisonHandler extends AbstractComparisonHandler {

	@Override
	protected boolean isValidFile(IFile file) {
		if (file != null) {
			return PerfSaveSessionHandler.DATA_EXT.equals(file
					.getFileExtension())
					|| "old".equals(file.getFileExtension()); //$NON-NLS-1$
		}
		return false;
	}

	@Override
	protected void handleComparison(IFile oldData, IFile newData) {
		String title = MessageFormat.format(Messages.ContentDescription_0,
				new Object[] { oldData.getName(), newData.getName() });

		// get corresponding files
		File oldDatum = oldData.getLocation().toFile();
		File newDatum = newData.getLocation().toFile();

		// create comparison data and run comparison.
		ReportComparisonData diffData = new ReportComparisonData(title, oldDatum, newDatum);
		diffData.parse();

		PerfPlugin.getDefault().setReportDiffData(diffData);
		ReportComparisonView.refreshView();
	}

}
