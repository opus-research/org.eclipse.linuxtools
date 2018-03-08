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
package org.eclipse.linuxtools.internal.perf.actions;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.perf.model.PMSymbolMatch;
import org.eclipse.linuxtools.internal.perf.model.PMSymbolMatchUtils;
import org.eclipse.linuxtools.internal.perf.ui.PerfDataCollectionView;
import org.eclipse.linuxtools.internal.perf.ui.PerfDiffView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class PerfDiffAction extends Action {
	public PerfDiffAction() {
		super(Messages.MsgSelectionDiff);
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

			/** Checking file validity **/
			if (selectedFiles.size() < 2) {
				MessageDialog.openWarning(
						Display.getCurrent().getActiveShell(),
						Messages.MsgWarning_0, Messages.MsgWarning_1);
			} else {
				IFile staleData;
				IFile freshData;

				// Check if selections are proper files.
				try {
					staleData = (IFile) selectedFiles.get(0);
					freshData = (IFile) selectedFiles.get(1);
				} catch (ClassCastException e) {
					e.printStackTrace();
					MessageDialog.openError(Display.getCurrent()
							.getActiveShell(), Messages.MsgError_0,
							Messages.MsgError_1);
					vp.clearSelections();
					return;
				}
			/** ------------------------------------ **/

				// Gather comparison results for each PMSymbol in the selected data files
				ArrayList<PMSymbolMatch> result = PMSymbolMatchUtils.buildResults(
						staleData, freshData);

				if (result.isEmpty()) {
					MessageDialog.openError(Display.getCurrent()
							.getActiveShell(), Messages.MsgError_0,
							Messages.MsgError_1);
					vp.clearSelections();
					wbp.showView("org.eclipse.linuxtools.perf.ui.PerfSessionCompareView");
				} else {

					//TODO: Why is this being done twice??
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