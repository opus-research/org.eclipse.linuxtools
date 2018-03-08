/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.model;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.launch.PerfLaunchShortcut;

public class PMSymbolMatchUtils {

	/**
	 * Get list of PMSymbol elements from the perf report model generated from the
	 * specified perf data file
	 *
	 * @param perfDataFile the perf data file to be reported
	 * @return array of PMSymbol elements
	 */
	private static ArrayList<PMSymbol> getSymbols(IFile perfDataFile) {
		// Get perf repot  tree model generated from specified perf data file.
		TreeParent invisibleRoot = getPerfModelFromFile(perfDataFile);

		if (invisibleRoot.getChildren().length == 0) {
			return null;
		}

		ArrayList<PMSymbol> symbols = new ArrayList<PMSymbol>();
	
		// fill array with all PMSymbol objects in the perf report tree model.
		fillSymbolsArray(invisibleRoot, symbols);

		return symbols;
	}

	private static void fillSymbolsArray(TreeParent treeElement,
			ArrayList<PMSymbol> symbols) {
		if (treeElement instanceof PMSymbol) {
			symbols.add((PMSymbol) treeElement);
		} else {
			for (TreeParent child : treeElement.getChildren()) {
				fillSymbolsArray(child, symbols);
			}
		}
	}

	private static TreeParent getPerfModelFromFile(IFile perfDataFile) {
		String projectName = perfDataFile.getProject().getName();

		// create perf launch configuration to be used to create the perf report tree model 
		PerfLaunchShortcut perfConfig = new PerfLaunchShortcut();
		ILaunchConfiguration config = perfConfig
				.createDefaultConfiguration(projectName);

		PerfCore.Report(config, null, null, null, perfDataFile.getLocation()
				.toOSString(), null);

		// return model from reported perf data file
		return PerfPlugin.getDefault().getModelRoot();
	}

	/**
	 * Build matches given two files
	 *
	 * @param staleData older data file
	 * @param freshData newer data file
	 * @return array of PMSymbolMatch's, each containing a PMSymbol match pair
	 */
	public static ArrayList<PMSymbolMatch> buildResults(IFile staleData,
			IFile freshData) {
		ArrayList<PMSymbolMatch> result = new ArrayList<PMSymbolMatch>();
		ArrayList<PMSymbol> stale = getSymbols(staleData);
		ArrayList<PMSymbol> fresh = getSymbols(freshData);

		if (stale == null || fresh == null) {
			return result;
		}

		ArrayList<PMSymbol> staleNoMatch = (ArrayList<PMSymbol>) stale.clone();

		// Gather all matches/non-matches of the fresh dsos against the stale.
		boolean added = false;
		for (PMSymbol freshElement : fresh) {
			added = true;
			for (PMSymbol staleElement : stale) {
				if (equals(freshElement, staleElement)) {
					result.add(new PMSymbolMatch(freshElement, staleElement));
					staleNoMatch.remove(staleElement);
					added = false;
					break;
				}
			}
			// New dso with no match in the stale list
			if (added) {
				result.add(new PMSymbolMatch(freshElement, null));
			}
		}
		// Gather stale dsos with no match in the fresh list
		for (PMSymbol staleElement : staleNoMatch) {
			result.add(new PMSymbolMatch(null, staleElement));
		}
		return result;
	}

	/**
	 * Check TreeParent's are equal (i.e. have the same ancestors)
	 *
	 * @param t1 TreeParent element
	 * @param t2 TreeParent element
	 * @return TreeParent's equality
	 */
	public static boolean equals(TreeParent t1, TreeParent t2) {
		// base case (invisible root)
		if ("".equals(t1.getName()) && "".equals(t2.getName())) {
			return true;
		} else if (t1.equals(t2.getName())) {
			return equals(t1.getParent(), t2.getParent());
		} else {
			return false;
		}
	}
}