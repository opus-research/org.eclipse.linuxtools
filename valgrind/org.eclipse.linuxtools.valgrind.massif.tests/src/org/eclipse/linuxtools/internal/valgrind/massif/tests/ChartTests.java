/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifHeapTreeNode;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifLaunchConstants;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartEditor;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartEditorInput;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.ChartLocationsDialog;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.HeapChart;
import org.eclipse.linuxtools.internal.valgrind.massif.charting.Messages;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;

public class ChartTests extends AbstractMassifTest {
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}

	public void testEditorName() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testEditorName"); //$NON-NLS-1$

		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IAction chartAction = getChartAction(view);
		assertNotNull(chartAction);
		chartAction.run();

		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		assertTrue(part.getTitle().startsWith("Heap Chart - alloctest")); //$NON-NLS-1$
	}

	public void testByteScalingKiB() throws Exception {
		byteScalingHelper(1, 1, 1024 * 10, "testByteScalingKiB"); //$NON-NLS-1$
	}

	public void testByteScalingMiB() throws Exception {
		byteScalingHelper(2, 1, 1024 * 1024 * 10, "testByteScalingMiB"); //$NON-NLS-1$
	}

	public void testByteScalingGiB() throws Exception {
		byteScalingHelper(3, 1024, 1024 * 1024 * 10, "testByteScalingGiB"); //$NON-NLS-1$
	}

	public void testByteScalingTiB() throws Exception {
		byteScalingHelper(4, 1024 * 1024, 1024 * 1024 *10, "testByteScalingTiB"); //$NON-NLS-1$
	}

	public void testChartCallback() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testChartCallback"); //$NON-NLS-1$

		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IAction chartAction = getChartAction(view);
		assertNotNull(chartAction);
		chartAction.run();

		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (part instanceof ChartEditor) {
			Chart control = ((ChartEditor) part).getControl();
			ILineSeries lsTotal = (ILineSeries) control.getSeriesSet().getSeries(Messages.getString("HeapChart.Total_Heap"));
			Point p1 = lsTotal.getPixelCoordinates(4);

			HeapChart heapChart = ((ChartEditorInput) ((ChartEditor)part).getEditorInput()).getChart();
			int x = control.getAxisSet().getXAxis(0).getPixelCoordinate(heapChart.time[4]);
			int y = control.getAxisSet().getYAxis(0).getPixelCoordinate(heapChart.dataTotal[4]);

			assertEquals(x, p1.x);
			assertEquals(y, p1.y);
		} else {
			fail();
		}
	}

	public void testChartLocationsDialog() throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		doLaunch(config, "testChartCallback"); //$NON-NLS-1$

		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		MassifSnapshot snapshot = view.getSnapshots()[7]; // peak		
		assertTrue(snapshot.isDetailed());

		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		ChartLocationsDialog dialog = new ChartLocationsDialog(parent);
		dialog.setInput(snapshot);
		dialog.setBlockOnOpen(false);
		dialog.open();

		MassifHeapTreeNode element = snapshot.getRoot().getChildren()[1];
		dialog.getTableViewer().setSelection(new StructuredSelection(element));
		dialog.getOkButton().notifyListeners(SWT.Selection, null);
		dialog.openEditorForResult();

		checkFile(proj.getProject(), element);
		checkLine(element);
	}

	private void byteScalingHelper(int ix, long times, long bytes, String testName) throws Exception {
		ILaunchConfiguration config = createConfiguration(proj.getProject());
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, String.valueOf(bytes) + " " + String.valueOf(times)); //$NON-NLS-1$
		wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_TIMEUNIT, MassifLaunchConstants.TIME_B);
		config = wc.doSave();

		doLaunch(config, testName);

		ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
		IAction chartAction = getChartAction(view);
		assertNotNull(chartAction);
		chartAction.run();

		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (part.getEditorInput() instanceof ChartEditorInput) {
			ChartEditorInput input = (ChartEditorInput) part.getEditorInput();
			HeapChart chart = input.getChart();
			assertEquals(HeapChart.getByteUnits()[ix], chart.getXUnits());
		} else {
			fail();
		}
	}

	private IAction getChartAction(IViewPart view) {
		IAction result = null;
		IToolBarManager manager = view.getViewSite().getActionBars().getToolBarManager();
		for (IContributionItem item : manager.getItems()) {
			if (item instanceof ActionContributionItem) {
				ActionContributionItem actionItem = (ActionContributionItem) item;
				if (actionItem.getAction().getId().equals(MassifViewPart.CHART_ACTION)) {
					result = actionItem.getAction();
				}
			}
		}
		return result;
	}
}
