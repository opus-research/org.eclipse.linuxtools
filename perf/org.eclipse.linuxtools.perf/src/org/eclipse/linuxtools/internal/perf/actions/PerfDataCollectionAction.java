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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.perf.ui.PerfDataCollectionView;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class PerfDataCollectionAction extends Action {
	public PerfDataCollectionAction() {
		super(Messages.MsgSelectionDiff);
	}

	@Override
	public void run() {
		MessageDialog
				.openInformation(Display.getCurrent().getActiveShell(),
						Messages.MsgSelectionDiff,
						Messages.MsgSelectFiles);
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		try {
			IWorkbenchPage wbp = workbenchWindow.getActivePage();
			wbp.showView("org.eclipse.linuxtools.perf.ui.PerfSessionCompareView");
			PerfDataCollectionView vp = (PerfDataCollectionView) wbp
					.findView("org.eclipse.linuxtools.perf.ui.PerfSessionCompareView");

			vp.clearSelections();
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}