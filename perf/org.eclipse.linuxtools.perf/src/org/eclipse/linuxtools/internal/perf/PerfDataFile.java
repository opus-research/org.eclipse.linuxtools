/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Class providing basic functionality to handle files that contain perf
 * data to be extracted.
 */
public class PerfDataFile extends BaseDataManipulator implements IPerfData {
	// Perf data file.
	private File dataFile;

	public PerfDataFile(File file) {
		dataFile = file;

	}

	@Override
	public String getPerfData() {
		return fileToString(dataFile);

	}

	@Override
	public String getTitle() {
		return (dataFile == null) ? null : dataFile.getPath();
	}

	/**
	 * Utility method to get all file contents as a String.
	 * @param file File to read.
	 * @return String file contents.
	 */
	private String fileToString(File file) {
		if (file == null | !file.exists()) {
			return ""; //$NON-NLS-1$
		}
		BufferedReader fileReader = null;
		String result = ""; //$NON-NLS-1$
		try {
			fileReader = new BufferedReader(new FileReader(file));
			result = getBufferContents(fileReader);
		} catch (FileNotFoundException e) {
		} finally {
			closeResource(fileReader);
		}
		return result;
	}
}
