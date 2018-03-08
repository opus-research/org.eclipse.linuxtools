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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.internal.perf.model.PMStatEntry;

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

		// Gather comparison results in a string.
		for (PMStatEntry statEvent : statsDiff) {
			result += statEvent.toString() + "\n"; //$NON-NLS-1$
		}
	}

	private ArrayList<PMStatEntry> getComparisonStats() {
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

	private ArrayList<PMStatEntry> collectStats(IFile file) {
		ArrayList<PMStatEntry> result = new ArrayList<PMStatEntry>();
		File statFile = file.getLocation().toFile();
		BufferedReader statReader = null;
		try {
			statReader = new BufferedReader(new FileReader(statFile));

			// pattern for a valid perf stat entry
			Pattern entryPattern = Pattern.compile("^" + OCCURRENCE //$NON-NLS-1$
					+ "\\s" + EVENT //$NON-NLS-1$
					+ "\\s*(\\#\\s+" + METRICS //$NON-NLS-1$
					+ UNITS + ")?" //$NON-NLS-1$
					+ DELTA + "?" //$NON-NLS-1$
					+ "(\\s" + SCALE + ")?$"); //$NON-NLS-1$//$NON-NLS-2$

			// pattern for last stat entry (seconds elapsed):
			Pattern totalTimePattern = Pattern.compile("^" + OCCURRENCE //$NON-NLS-1$
					+ "\\s(seconds\\stime\\selapsed)\\s+" //$NON-NLS-1$
					+ DELTA);

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
			// TODO: Error dialog: Selected file does not exist.
			e.printStackTrace();
		} catch (IOException e){
			// TODO: Error dialog: could not read line.
		}finally {
			try {
				statReader.close();
			} catch (IOException e) {
				// TODO: Error dialog: Resource leak
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
}
