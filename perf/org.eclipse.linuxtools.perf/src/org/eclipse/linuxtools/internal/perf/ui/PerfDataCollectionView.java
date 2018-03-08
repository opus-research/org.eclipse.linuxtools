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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

public class PerfDataCollectionView extends ViewPart {

	private static final String ID = "org.eclipse.linuxtools.internal.perf.views.SessionCompareView";

	public static ArrayList<Object> selectedFiles = new ArrayList<Object>();

	private TableViewer tableviewer;

	// This keeps track of the first selection event
	private boolean starting = true;

	// listener registered with the selection service
	public ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart,
				ISelection selection) {
			if (selectedFiles.size() >= 2)
				return;
			// ignore our own selections
			if (sourcepart != PerfDataCollectionView.this) {
				if (starting) {
					starting = false;
					return;
				}
				showSelection(sourcepart, selection);
			}
		}
	};

	/**
	 * Show selected files
	 * 
	 * @param sourcepart
	 *            source part of selection
	 * @param selection
	 *            selected item
	 */
	public void showSelection(IWorkbenchPart sourcepart, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object[] o = ss.toArray();
			selectedFiles.add(o[0]);
			showItems(selectedFiles.toArray());
		}
	}

	private void showItems(Object[] items) {
		tableviewer.setInput(items);
		tableviewer.refresh();
	}

	public void createPartControl(Composite parent) {
		setContentDescription("Select data files to compare from the Project Explorer");
		tableviewer = new TableViewer(parent, SWT.NONE);
		tableviewer.setLabelProvider(new WorkbenchLabelProvider());
		tableviewer.setContentProvider(new ArrayContentProvider());

		// provide tableviewer selection
		getSite().setSelectionProvider(tableviewer);

		contributeToActionBars();
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(listener);
	}

	private void contributeToActionBars() {
		Action clearAction = new PerfDataClearAction();
		Action diffAction = new PerfDiffAction();

		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(clearAction);
		toolBar.add(diffAction);
	}

	public void setFocus() {
		tableviewer.getControl().setFocus();
	}

	public void refreshView() {
		showItems(selectedFiles.toArray());
	}

	public void dispose() {
		// remove listener when disposing view
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(listener);
		super.dispose();
	}

	public void clearSelections() {
		starting = true;
		selectedFiles.clear();
		refreshView();
	}

	public void restart() {
		starting = true;
	}

	public ArrayList<Object> getSelectedFiles() {
		return selectedFiles;
	}
}