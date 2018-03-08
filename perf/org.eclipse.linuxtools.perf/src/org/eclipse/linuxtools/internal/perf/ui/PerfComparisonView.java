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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.linuxtools.internal.perf.model.PMSymbolMatch;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

public class PerfComparisonView extends ViewPart {

	private TableViewer tableviewer;
	private ArrayList<PMSymbolMatch> result = new ArrayList<PMSymbolMatch>();
	private static final String[] COL_TITLES = { "Function",
			"New Overhead (%)", "Old Overhead (%)", "Result", "Event", "" };
	private static final int[] COL_BOUNDS = { 100, 133, 135, 100, 100, 1 };

	@Override
	public void createPartControl(Composite parent) {

		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);

		tableviewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		createColumns(parent, tableviewer);

		final Table table = tableviewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tableviewer.setContentProvider(new ArrayContentProvider());

		// Layout for the table
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;

		tableviewer.setContentProvider(new ArrayContentProvider());
		tableviewer.getControl().setLayoutData(gridData);
	}

	private void createColumns(final Composite parent, final TableViewer viewer) {
		

		final PerfComparisonTableSorter tableSorter = new PerfComparisonTableSorter();
		final Table table = tableviewer.getTable();

		for (int i = 0; i < COL_TITLES.length; i++) {
			createTableViewerColumn(COL_TITLES[i], COL_BOUNDS[i], i, tableSorter,
					table);
		}

		tableviewer.setLabelProvider(new PerfComparisonContentProvider());
		tableviewer.setSorter(tableSorter);
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound,
			final int colIndex, final PerfComparisonTableSorter tableSorter,
			final Table table) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(
				tableviewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tableSorter.setColumnIndex(colIndex);
				int dir = table.getSortDirection();

				if (table.getSortColumn() == column) {
					dir = (dir == SWT.UP) ? SWT.DOWN : SWT.UP;
				} else {
					dir = SWT.DOWN;
				}

				table.setSortDirection(dir);
				table.setSortColumn(column);
				tableviewer.refresh();
			}
		});
		return viewerColumn;
	}

	@Override
	public void setFocus() {
		tableviewer.getControl().setFocus();
	}

	public void setResult(ArrayList<PMSymbolMatch> result) {
		this.result = result;
	}

	public void refreshView() {
		tableviewer.setInput(result.toArray());
		tableviewer.refresh();
	}
}
