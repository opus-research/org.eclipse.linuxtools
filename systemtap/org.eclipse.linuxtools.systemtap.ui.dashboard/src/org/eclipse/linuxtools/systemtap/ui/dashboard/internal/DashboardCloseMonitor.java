/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.internal;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchWindow;

import org.eclipse.linuxtools.systemtap.ui.dashboard.views.ActiveModuleBrowserView;

/**
 * This listener is responsible for monitoring workbench close actions.  It is used 
 * to veto a shutdown if there are modules still running and the user does not really
 * want to shutdown.
 * @author Ryan Morse
 */
public class DashboardCloseMonitor implements IWorkbenchListener {
	public boolean preShutdown(IWorkbench workbench, boolean forced) {
		boolean close = true;
		if(!forced) {
			IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			IViewPart ivp = window.getActivePage().findView(ActiveModuleBrowserView.ID);
			
			if (ivp != null){
				ActiveModuleBrowserView ambv = (ActiveModuleBrowserView)ivp;
				if(ambv.anyRunning()) {
					String msg = MessageFormat.format(Localization.getString("DashboardCloseMonitor.StillRunning"), (Object[])null); //$NON-NLS-1$
					close = MessageDialog.openQuestion(window.getShell(), "Closing...", msg);
				}
			}
			
		}
		return close;
	}
	
	public void postShutdown(IWorkbench workbench) {}
}
