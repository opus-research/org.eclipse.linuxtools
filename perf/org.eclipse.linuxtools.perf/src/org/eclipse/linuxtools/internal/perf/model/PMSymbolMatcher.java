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
import org.eclipse.linuxtools.internal.perf.launch.PerfDefaultLaunchConfig;

public class PMSymbolMatcher {

	/**
	 * Get PMSymbol's from report of file
	 * 
	 * @param file
	 *            the file to be reported
	 * @return array of PMSymbol's
	 */
	private static ArrayList<PMSymbol> getSymbols(IFile file) {
		String projectName = file.getProject().getName();

		PerfDefaultLaunchConfig perfConfig = new PerfDefaultLaunchConfig();
		ILaunchConfiguration config = perfConfig
				.createDefaultConfiguration(projectName);

		PerfCore.Report(config, null, null, null, file.getLocation()
				.toOSString(), null);

		// get model from reported perf data file
		TreeParent invisibleRoot = PerfPlugin.getDefault().getModelRoot();

		if (invisibleRoot.getChildren().length == 0) {
			return null;
		}

		ArrayList<PMSymbol> symbols = new ArrayList<PMSymbol>();

		// get all PMSymbols
		for (TreeParent event : invisibleRoot.getChildren()) {
			for (TreeParent cmd : event.getChildren()) {
				for (TreeParent dso : cmd.getChildren()) {
					for (TreeParent dsoFile : dso.getChildren()) {
						for (TreeParent sym : dsoFile.getChildren()) {
							symbols.add((PMSymbol) sym);
						}
					}
				}
			}
		}
		return symbols;
	}

	/**
	 * Get PMSymbol's matches from the two given PMSymbol arrays
	 * 
	 * @param stale
	 *            PMSymbol array of older PMSymbol's
	 * @param fresh
	 *            PMSymbol array of newer PMSymbol's
	 * @return array of PMSymbolMatch's, each containing a PMSymbol match pair
	 */
	private static ArrayList<PMSymbolMatch> buildMatches(
			ArrayList<PMSymbol> stale, ArrayList<PMSymbol> fresh) {
		ArrayList<PMSymbolMatch> result = new ArrayList<PMSymbolMatch>();

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
				result.add(new PMSymbolMatch((PMSymbol) freshElement, null));
			}
		}
		// Gather stale dsos with no match in the fresh list
		for (PMSymbol staleElement : staleNoMatch) {
			result.add(new PMSymbolMatch(null, (PMSymbol) staleElement));
		}
		return result;
	}

	/**
	 * Build matches given two files
	 * 
	 * @param staleData
	 *            older data file
	 * @param freshData
	 *            newer data file
	 * @return array of PMSymbolMatch's, each containing a PMSymbol match pair
	 */
	public static ArrayList<PMSymbolMatch> buildResults(IFile staleData,
			IFile freshData) {
		ArrayList<PMSymbol> staleDsos = getSymbols(staleData);
		ArrayList<PMSymbol> freshDsos = getSymbols(freshData);
		if (staleDsos == null || freshDsos == null) {
			return null;
		}
		ArrayList<PMSymbolMatch> result = buildMatches(staleDsos, freshDsos);
		return result;
	}

	/**
	 * Check TreeParent's are equal (i.e. have the same ancestors)
	 * 
	 * @param t1
	 *            TreeParent element
	 * @param t2
	 *            TreeParent element
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