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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.perf.model.PMSymbolMatch;
import org.eclipse.linuxtools.internal.perf.model.PMSymbolMatcher;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class PerfDiffAction extends Action {
	public PerfDiffAction() {
		super("Session Comparison");
	}

	@Override
	public void run() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		try {
			IWorkbenchPage wbp = workbenchWindow.getActivePage();
			IWorkbench wb = PlatformUI.getWorkbench();
			IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
			IWorkbenchPage page = win.getActivePage();

			PerfDataCollectionView vp = (PerfDataCollectionView) page
					.findView("org.eclipse.linuxtools.perf.ui.PerfSessionCompareView");

			ArrayList<Object> selectedFiles = vp.getSelectedFiles();

			if (selectedFiles.size() < 2) {
				MessageDialog.openInformation(Display.getCurrent()
						.getActiveShell(), "Warning",
						"Please select two perf data files.");
				vp.restart();
			} else {
				IFile staleData;
				IFile freshData;

				// Check if selections are proper files.
				try {
					staleData = (IFile) selectedFiles.get(0);
					freshData = (IFile) selectedFiles.get(1);
				} catch (ClassCastException e) {
					e.printStackTrace();
					MessageDialog.openInformation(Display.getCurrent()
							.getActiveShell(), "Error",
							"Selections were not proper perf data files.");
					vp.clearSelections();
					return;
				}

				ArrayList<PMSymbolMatch> result = PMSymbolMatcher.buildResults(
						staleData, freshData);

				// If result is null, selected files were not proper perf data
				// files
				if (result == null) {
					MessageDialog.openInformation(Display.getCurrent()
							.getActiveShell(), "Error",
							"Selections were not proper perf data files.");
					vp.clearSelections();
				} else {

					PerfDiffView diffView = (PerfDiffView) wbp
							.showView("org.eclipse.linuxtools.perf.ui.PerfSessionDiffView");
					diffView.setResult(result);
					diffView.refreshView();

					wbp.showView("org.eclipse.linuxtools.perf.ui.PerfSessionDiffView");
				}
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}