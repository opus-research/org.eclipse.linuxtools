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

import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.linuxtools.internal.perf.model.PMSymbol;
import org.eclipse.linuxtools.internal.perf.model.PMSymbolMatch;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

class PerfDiffLabelProvider implements ITableLabelProvider, ITableColorProvider {

	public static final String COLOR_RED = "75, 200, 75";
	public static final String COLOR_GREEN = "200, 75, 75";

	@Override
	public Color getBackground(Object element, int columnIndex) {
		PMSymbolMatch match = (PMSymbolMatch) element;
		String result = match.getResult();
		switch (columnIndex) {
		case 3:
			if (result != null && result.equals(PMSymbolMatch.SYMBOL_MATCHED)) {
				float resFloat = match.getOverheadDifference();
				if (resFloat < 0) {
					return new Color(Display.getCurrent(),
							StringConverter.asRGB(COLOR_RED));
				} else if (resFloat > 0) {
					return new Color(Display.getCurrent(),
							StringConverter.asRGB(COLOR_GREEN));
				} else {
					return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
				}
			} else {
				return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
			}
		default:
			return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
		}
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		PMSymbolMatch p = (PMSymbolMatch) element;
		switch (columnIndex) {
		// function column
		case 0:
			return p.getName();
		// new overhead column
		case 1:
			PMSymbol fresh = p.getFresh();
			return (fresh == null) ? "--" : String.valueOf(fresh.getPercent());
		// old overhead column
		case 2:
			PMSymbol stale = p.getStale();
			return (stale == null) ? "--" : String.valueOf(stale.getPercent());
		// overhead difference column
		case 3:
			String result = p.getResult();
			if(result != null && result.equals(PMSymbolMatch.SYMBOL_MATCHED)){
				String difference = String.valueOf(p.getOverheadDifference());
				// Add percentage sign if result is a float.
				return difference + "%";
			} else{
				return result;
			}
		// event column
		case 4:
			return p.getEvent();
		case 5:
			return "";
		default:
			return "";
		}
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Color getForeground(Object element, int columnIndex) {
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}