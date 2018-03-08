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

	public void setColumnIndex(int val) {
		if (index == val) {
			direction = 1 - direction;
		} else {
			direction = 0;
			index = val;
		}
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		PMSymbolMatch s1 = (PMSymbolMatch) e1;
		PMSymbolMatch s2 = (PMSymbolMatch) e2;
		int ret = 0;
		switch (index) {
		// function column
		case 0:
			ret = s1.getName().compareTo(s2.getName());
			break;
		// new column
		case 1:
			ret = s1.compareSymbol(s2, /* compare fresh */true);
			break;
		// old column
		case 2:
			ret = s1.compareSymbol(s2, /* compare stale */false);
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
			return -ret;
		}
		return ret;
	}

}