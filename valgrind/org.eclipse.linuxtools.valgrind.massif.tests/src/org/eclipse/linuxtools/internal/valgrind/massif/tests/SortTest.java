/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.massif.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifLaunchConstants;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifSnapshot;
import org.eclipse.linuxtools.internal.valgrind.massif.MassifViewPart;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SortTest extends AbstractMassifTest {

	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("alloctest"); //$NON-NLS-1$

		ILaunchConfiguration config = createConfiguration(proj.getProject());
		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(MassifLaunchConstants.ATTR_MASSIF_STACKS, true);
		wc.doSave();
		doLaunch(config, "testStacks"); //$NON-NLS-1$
	}

	@Override
	@After
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}
	@Test
	public void testSortSnapshots() {
		checkSortColumn(0);
	}
	@Test
	public void testSortTime() {
		checkSortColumn(1);
	}
	@Test
	public void testSortTotal() {
		checkSortColumn(2);
	}
	@Test
	public void testSortUseful() {
		checkSortColumn(3);
	}
	@Test
	public void testSortExtra() {
		checkSortColumn(4);
	}
	@Test
	public void testSortStacks() {
		checkSortColumn(5);
	}

	private void checkSortColumn(int column) {
		MassifViewPart view = (MassifViewPart) ValgrindUIPlugin.getDefault()
				.getView().getDynamicView();
		TableViewer viewer = view.getTableViewer();
		TableColumn control = viewer.getTable().getColumn(column);

		// Test ascending
		control.notifyListeners(SWT.Selection, null);
		assertEquals(SWT.UP, viewer.getTable().getSortDirection());
		assertEquals(control, viewer.getTable().getSortColumn());
		checkOrder(viewer, column, true);

		// Test descending
		control.notifyListeners(SWT.Selection, null);
		assertEquals(SWT.DOWN, viewer.getTable().getSortDirection());
		assertEquals(control, viewer.getTable().getSortColumn());
		checkOrder(viewer, column, false);
	}

	private void checkOrder(TableViewer viewer, int column, boolean ascending) {
		TableItem[] items = viewer.getTable().getItems();
		for (int i = 0; i < items.length - 1; i++) {
			MassifSnapshot first = (MassifSnapshot) items[i].getData();
			MassifSnapshot second = (MassifSnapshot) items[i + 1].getData();

			switch (column) {
			case 0:
				assertTrue(ascending ? first.getNumber() <= second.getNumber() : first.getNumber() >= second.getNumber());
				break;
			case 1:
				assertTrue(ascending ? first.getTime() <= second.getTime() : first.getTime() >= second.getTime());
				break;
			case 2:
				assertTrue(ascending ? first.getTotal() <= second.getTotal() : first.getTotal() >= second.getTotal());
				break;
			case 3:
				assertTrue(ascending ? first.getHeapBytes() <= second.getHeapBytes() : first.getHeapBytes() >= second.getHeapBytes());
				break;
			case 4:
				assertTrue(ascending ? first.getHeapExtra() <= second.getHeapExtra() : first.getHeapExtra() >= second.getHeapExtra());
				break;
			case 5:
				assertTrue(ascending ? first.getStacks() <= second.getStacks() : first.getStacks() >= second.getStacks());
				break;
			default:
				fail();
			}
		}
	}
}
