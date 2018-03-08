/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.internal;

import java.io.File;

import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.plugin.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;

import org.eclipse.linuxtools.systemtap.ui.dashboard.structures.DashboardMetaData;
import org.eclipse.linuxtools.systemtap.ui.dashboard.internal.DashboardCloseMonitor;

/**
 * The main plugin class to be used in the desktop.
 */
public class DashboardPlugin extends AbstractUIPlugin {
	/**
	 * The constructor.
	 */
	public DashboardPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		workbenchListener = new DashboardCloseMonitor();
		plugin.getWorkbench().addWorkbenchListener(workbenchListener);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		plugin.getWorkbench().removeWorkbenchListener(workbenchListener);
		plugin = null;

		//Clean up temparary scripts
		removeFolder(DashboardMetaData.tempScriptFolder);
		removeFolder(DashboardMetaData.tempModuleFolder);
	}
	
	private void removeFolder(File folder) {
		try {
			File[] files = folder.listFiles();
			for(int j=0; j<files.length; j++) {
				if(files[j].isDirectory())
					removeFolder(files[j]);
				else
					files[j].delete();
			}
			folder.delete();
		} catch(Exception e) {}
	}

	/**
	 * Returns the shared instance.
	 */
	public static DashboardPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.linuxtools.systemtap.ui.dashboard", path);
	}
	
	private IWorkbenchListener workbenchListener;
	private static DashboardPlugin plugin;
}
