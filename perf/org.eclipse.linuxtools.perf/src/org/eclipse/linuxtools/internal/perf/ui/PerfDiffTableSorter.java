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
package org.eclipse.linuxtools.internal.perf.ui;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.linuxtools.internal.perf.model.PMSymbolMatch;

class PerfDiffTableSorter extends ViewerSorter {
	private int index;
	private int direction;

	/**
	 * Update index with given value, if this is the second time this column is
	 * being selected, in a row, change the direction of the sorting.
	 *
	 * @param val value to set index with
	 */
	public void setColumnIndex(int val) {
		if (index == val) {
			direction = 1 - direction;
		} else {
			direction = 0;
			index = val;
		}
	}

	/**
	 * Compare two objects in a selected column. Comparison varies depending on
	 * what column these objects are being compared in, which is represented by
	 * the current value of index:
	 *
	 * 	- 0	"Function" column, compare function names.
	 * 	- 1 "New Overhead column", compare percentages between
	 * 		newer objects contained in each PMSymbolMatch object.
	 * 	- 2	"Old Overhead column", compare percentages between older objects
	 *		contained in each PMSymbolMatch object.
	 * 	- 3	"Result" column, compare the actual results of the comparisons.
	 * 	- 4	"Event" column, compare the event names of the PMSymbolMatch objects.
	 *
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		PMSymbolMatch s1 = (PMSymbolMatch) e1;
		PMSymbolMatch s2 = (PMSymbolMatch) e2;
		int ret = 0;
		switch (index) {
		// function column
		case 0:
			String matchName1 = s1.getName();
			matchName1 = (matchName1 == null) ? "" : matchName1;

			String matchName2 = s2.getName();
			matchName2 = (matchName2 == null) ? "" : matchName2;

			ret = matchName1.compareTo(matchName2);
			break;
		// new column
		case 1:
			ret = s1.compareFreshSymbol(s2);
			break;
		// old column
		case 2:
			ret = s1.compareStaleSymbol(s2);
			break;
		// result column
		case 3:
			ret = s1.compareResult(s2);
			break;
		// event column
		case 4:
			ret = s1.getEvent().compareTo(s2.getEvent());
			break;
		case 5:
			break;
		default:
			ret = 0;
		}
		if (direction == 1) {
			// Reverse sorting direction
			return -ret;
		}
		return ret;
	}

}