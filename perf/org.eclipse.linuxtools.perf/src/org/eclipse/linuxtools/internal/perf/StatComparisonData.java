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
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.perf.handlers.Messages;
import org.eclipse.linuxtools.internal.perf.model.PMStatEntry;
import org.eclipse.swt.widgets.Display;

/**
 * Class containing all functionality for comparting perf statistics data.
 */
public class StatComparisonData {
	// Reg-ex strings
	public static final String DECIMAL = "\\d+[\\.\\,\\d]*"; //$NON-NLS-1$
	public static final String PERCENTAGE = "(\\d+(\\.\\d+)?)\\%"; //$NON-NLS-1$
	public static final String OCCURRENCE = "(" + DECIMAL + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String EVENT = "(\\w+[\\-\\w]+)"; //$NON-NLS-1$
	public static final String METRICS = "(" + DECIMAL + ")"; //$NON-NLS-1$//$NON-NLS-2$
	public static final String UNITS = "([a-zA-Z\\/\\s\\%]*)"; //$NON-NLS-1$
	public static final String DELTA = "(\\(\\s\\+\\-\\s*" + PERCENTAGE + "\\s\\))"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String SCALE = "(\\[\\s*" + PERCENTAGE + "\\])"; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String TIME_UNIT = "(seconds\\stime\\selapsed)";  //$NON-NLS-1$
	public static final String TIME = "seconds time elapsed";  //$NON-NLS-1$

	private IFile oldFile;
	private IFile newFile;
	private String result = ""; //$NON-NLS-1$
	private String title;

	public StatComparisonData(String title, IFile oldFile, IFile newFile) {
		this.title = title;
		this.oldFile = oldFile;
		this.newFile = newFile;
	}

	public String getResult() {
		return result;
	}

	public String getTitle() {
		return title;
	}

	public void runComparison() {
		ArrayList<PMStatEntry> statsDiff = getComparisonStats();

		if (!statsDiff.isEmpty()) {
			String[][] statsDiffStr = new String[statsDiff.size()][];
			int currentRow = 0;

			// gather comparison results in a string array
			for (PMStatEntry statEntry : statsDiff) {
				statsDiffStr[currentRow] = statEntry.toStringArray();
				currentRow++;
			}

			// apply format to each entry and set the result
			String format = getFormat(statsDiffStr);
			for (String[] statEntry : statsDiffStr) {
				result += String.format(format, (Object[]) statEntry);
			}
		} else{

		}
	}

	public ArrayList<PMStatEntry> getComparisonStats() {
		ArrayList<PMStatEntry> oldStats = collectStats(oldFile);
		ArrayList<PMStatEntry> newStats = collectStats(newFile);
		ArrayList<PMStatEntry> result = new ArrayList<PMStatEntry>();

		for (PMStatEntry oldEntry : oldStats) {
			for (PMStatEntry newEntry : newStats) {
				if (oldEntry.equals(newEntry)) {
					result.add(oldEntry.compare(newEntry));
					continue;
				}
			}
		}

		return result;
	}

	public ArrayList<PMStatEntry> collectStats(IFile file) {
		ArrayList<PMStatEntry> result = new ArrayList<PMStatEntry>();
		File statFile = file.getLocation().toFile();
		BufferedReader statReader = null;
		try {
			statReader = new BufferedReader(new FileReader(statFile));

			// pattern for a valid perf stat entry
			Pattern entryPattern = Pattern.compile("^" + OCCURRENCE //$NON-NLS-1$
					+ "\\s" + EVENT //$NON-NLS-1$
					+ "\\s*(\\#\\s+" + METRICS + UNITS + ")?" //$NON-NLS-1$ //$NON-NLS-2$
					+ DELTA + "?" //$NON-NLS-1$
					+ "(\\s" + SCALE + ")?$"); //$NON-NLS-1$//$NON-NLS-2$

			// pattern for last stat entry (seconds elapsed):
			Pattern totalTimePattern = Pattern.compile("^" + OCCURRENCE  //$NON-NLS-1$
					+ "\\s" + TIME_UNIT  //$NON-NLS-1$
					+ "\\s+" + DELTA); //$NON-NLS-1$

			String line;
			while((line = statReader.readLine()) != null ){
				line = line.trim();
				Matcher match = entryPattern.matcher(line);
				String occurrence, event, usage, units, delta, scale;
				PMStatEntry statEntry;

				if(match.find()){

					// extract information from groups
					occurrence = match.group(1);
					event = match.group(2);
					usage = match.group(4);
					units = match.group(5);
					delta = match.group(7);
					scale = match.group(11);

					// create stat entry
					statEntry = new PMStatEntry(toFloat(occurrence), event,
							toFloat(usage), units, toFloat(delta),
							toFloat(scale));

					// add stat entry to results list
					result.add(statEntry);
				} else if(line.contains(TIME)){
					match = totalTimePattern.matcher(line);
					if(match.find()){
						occurrence = match.group(1);
						event = match.group(2);
						delta = match.group(4);

						// create stat entry
						statEntry = new PMStatEntry(toFloat(occurrence),
								event, 0, null, toFloat(delta), 0);

						result.add(statEntry);
					}
				}
			}
			return result;
		} catch (FileNotFoundException e) {
			openErroDialog(Messages.MsgError, e.getMessage(), ""); //$NON-NLS-1$
			e.printStackTrace();
		} catch (IOException e) {
			openErroDialog(Messages.MsgError, e.getMessage(), ""); //$NON-NLS-1$
		} finally {
			try {
				statReader.close();
			} catch (IOException e) {
				openErroDialog(Messages.PerfResourceLeak_title,
						Messages.PerfResourceLeak_msg, file.getName());
			}
		}

		return result;
	}

	public static float toFloat(String str) {
		try {
			// remove commas from number string representation
			return (str == null) ? 0 : Float.parseFloat(str.replace(",", "")); //$NON-NLS-1$//$NON-NLS-2$
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public String getFormat(String[][] table) {
		// all entries have the same number of columns
		int[] maxCharLen = new int[table[0].length];

		// collect max number of characters per column
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
				maxCharLen[j] = Math.max(maxCharLen[j], table[i][j].length());
			}
		}

		// prepare format arguments
		ArrayList<Integer> arguments = new ArrayList<Integer>();
		for (int length : maxCharLen) {
			arguments.add(length);
		}

		// generate format string
		String entryFormat = String.format("   %%1$%1$1ds " //$NON-NLS-1$
				+ "%%2$-%2$1ds   #  " //$NON-NLS-1$
				+ "%%3$%3$1ds " //$NON-NLS-1$
				+ "%%4$-%4$1ds  " //$NON-NLS-1$
				+ "( +- %%5$%5$1ds )\n" //$NON-NLS-1$
				, arguments.toArray());

		return entryFormat;
	}

	/**
	 * open error dialog informing user of comparison failure.
	 * @param filename
	 */
	public void openErroDialog(String title, String pattern, String arg) {
		String errorMsg = MessageFormat.format(pattern, new Object[] { arg });
		MessageDialog.openError(Display.getCurrent().getActiveShell(), title,
				errorMsg);
	}

}
