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
package org.eclipse.linuxtools.internal.perf.handlers;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.perf.IPerfData;
import org.eclipse.linuxtools.internal.perf.PerfDataFile;
import org.eclipse.linuxtools.internal.perf.StatComparisonData;
import org.eclipse.linuxtools.internal.perf.ui.StatComparisonView;
import org.eclipse.swt.widgets.Event;

/**
 * Class representing a menu action for {@link StatComparisonView}.
 */
public class PerfStatDiffMenuAction extends Action {

	/**
	 * Perf data types:
	 * 	- PERF_OLD : Represents older perf data file.
	 * 	- PERF_NEW : Represents newer perf data file.
	 * 	- PERF_DIFF: Represents diff data.
	 */
	public static enum Type {
		PERF_OLD, PERF_NEW, PERF_DIFF;
	}

	// type of perf data this actions is to handle
	private Type dataType;

	// secondary id of view where this menu action is located
	private String secondaryID;

	public PerfStatDiffMenuAction(Type type, String sID){
		dataType = type;
		secondaryID = sID;
		switch (dataType) {
		case PERF_OLD:
			setToolTipText(Messages.PerfStatDiffMenuAction_old_tooltip);
			setText(Messages.PerfStatDiffMenuAction_old_text);
			break;
		case PERF_NEW:
			setToolTipText(Messages.PerfStatDiffMenuAction_new_tooltip);
			setText(Messages.PerfStatDiffMenuAction_new_text);
			break;
		case PERF_DIFF:
			setToolTipText(Messages.PerfStatDiffMenuAction_stats_tooltip);
			setText(Messages.PerfStatDiffMenuAction_diff_text);
			break;
		}

	}

	@Override
	public void runWithEvent(Event event) {
		StatComparisonView view = (StatComparisonView) StatComparisonView.getView(secondaryID);
		StatComparisonData data = (view == null) ? null: (StatComparisonData) view.getDiffData();
		if (view != null && data !=null) {
			IPerfData perfData = null;
			boolean style = false;
			switch (dataType) {
			case PERF_OLD:
				perfData = new PerfDataFile(data.getOldData());
				break;
			case PERF_NEW:
				perfData = new PerfDataFile(data.getNewData());
				break;
			case PERF_DIFF:
				perfData = data;
				style = true;
				break;
			}
			view.updateData(perfData, style);
		}
	}
}
