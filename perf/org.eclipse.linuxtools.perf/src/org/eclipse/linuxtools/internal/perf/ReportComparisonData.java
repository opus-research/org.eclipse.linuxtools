package org.eclipse.linuxtools.internal.perf;

import java.io.File;

public class ReportComparisonData extends AbstractDataManipulator {

	private File oldFile;
	private File newFile;

	public ReportComparisonData(String title, File oldFile, File newFile) {
		super(title, null);
		this.oldFile = oldFile;
		this.newFile = newFile;
	}

	@Override
	public void parse() {
		performCommand(getCommand(), 1);
	}

	protected String[] getCommand() {
		return new String[] { PerfPlugin.PERF_COMMAND,
				"diff", //$NON-NLS-1$
				oldFile.getAbsolutePath(),
				newFile.getAbsolutePath() };
	}

}
