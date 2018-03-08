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

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.perf.model.PMSymbolMatch;
import org.eclipse.linuxtools.internal.perf.model.PMSymbolMatchUtils;
import org.eclipse.linuxtools.internal.perf.ui.PerfComparisonView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class PerfComparisonAction extends Action {
	// selected files
	public static ArrayList<IFile> selectedFiles = new ArrayList<IFile>();

	// previous selection
	private ISelection prevSelection;

	// workbench listener
	private ISelectionListener workbenchListener;

	public PerfComparisonAction() {
		super(Messages.MsgSelectionDiff);
	}

	@Override
	public void run() {
		MessageDialog.openInformation(Display.getCurrent().getActiveShell(),
				Messages.MsgSelectionDiff, Messages.MsgSelectFiles);

		// initialize workbench listener
		workbenchListener = new ISelectionListener() {
			@Override
			public void selectionChanged(IWorkbenchPart sourcepart,
					ISelection selection) {
				handleSelection(sourcepart, selection);
			}
		};

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		workbenchWindow.getSelectionService().addSelectionListener(
				workbenchListener);
	}

	/**
	 * Get file from specified selection.
	 * @param selection the selection
	 * @return the selection's associated <code>IFile</code>
	 * @throws ClassCastException
	 */
	private IFile getFile(ISelection selection) throws ClassCastException {
		return (IFile) ((IStructuredSelection) selection).getFirstElement();
	}

	/**
	 * Compare selected data files.
	 */
	private void compareSelections() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		try {
			// check selections.
			if (selectedFiles.size() < 2) {
				MessageDialog.openWarning(
						Display.getCurrent().getActiveShell(),
						Messages.MsgWarning_0, Messages.MsgWarning_1);
			} else {
				IFile staleData;
				IFile freshData;

				// make sure selections are proper files.
				try {
					staleData = selectedFiles.get(0);
					freshData = selectedFiles.get(1);
				} catch (ClassCastException e) {
					e.printStackTrace();
					MessageDialog.openError(Display.getCurrent()
							.getActiveShell(), Messages.MsgError_0,
							Messages.MsgError_1);
					return;
				}

				// gather comparison results for each PMSymbol from selected data files
				ArrayList<PMSymbolMatch> result = PMSymbolMatchUtils
						.buildResults(staleData, freshData);

				if (result.isEmpty()) {
					MessageDialog.openError(Display.getCurrent()
							.getActiveShell(), Messages.MsgError_0,
							Messages.MsgError_1);
				} else {
					// set results in comparison view
					IWorkbenchPage wbp = workbenchWindow.getActivePage();
					PerfComparisonView diffView = (PerfComparisonView) wbp
							.showView("org.eclipse.linuxtools.perf.ui.PerfSessionDiffView");
					diffView.setResult(result);
					diffView.refreshView();

					wbp.showView("org.eclipse.linuxtools.perf.ui.PerfSessionDiffView");
				}
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		} finally {
			clearSelections();
		}
	}

	/**
	 * Handle selected items.
	 * @param sourcepart the workbench part containing the selection
	 * @param selection current selection
	 */
	private void handleSelection(IWorkbenchPart sourcepart, ISelection selection) {
		if (!(selection.equals(prevSelection))) {
			try {
				IFile file = getFile(selection);
				if (file != null) {
					selectedFiles.add(file);
					if (selectedFiles.size() == 2) {

						// confirmation message arguments
						Object[] confirmMsgArgs = new String[] {
								selectedFiles.get(0).getName(),
								selectedFiles.get(1).getName() };

						// confirmation message
						String confirmMsg = MessageFormat.format(
								Messages.MsgConfirm_msg, confirmMsgArgs);

						// confirm selections with user
						boolean confirm = MessageDialog.openConfirm(Display
								.getCurrent().getActiveShell(),
								Messages.MsgConfirm_title, confirmMsg);

						if (confirm) {
							compareSelections();
						} else {
							clearSelections();
						}
					}
				}
			} catch (ClassCastException ex) {
				// continue, there are other selections
			}
		}
		prevSelection = selection;
	}

	private void clearSelections(){
		PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow()
		.getSelectionService()
		.removeSelectionListener(workbenchListener);
		selectedFiles.clear();
	}
}