/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.structures.TapsetLibrary;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the IDE. This class contains lifecycle controls
 * for the plugin.
 * @see org.eclipse.ui.plugin.AbstractUIPlugin
 * @author Ryan Morse
 */
public class IDEPlugin extends AbstractUIPlugin {
	private IWorkbenchListener workbenchListener;
	private static IDEPlugin plugin;
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.systemtap.ui.ide"; //$NON-NLS-1$
	public static final String SPACE = "space"; //$NON-NLS-1$
	public static final String INSERT = "insert"; //$NON-NLS-1$
	public static final String DO_NOT_INSERT = "do not insert"; //$NON-NLS-1$
	public static final String TAB = "tab"; //$NON-NLS-1$

	public IDEPlugin() {
		plugin = this;
	}

	/**
	 * Called by the Eclipse Workbench at plugin activation time. Starts the plugin lifecycle.
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		workbenchListener = new IDECloseMonitor();
		plugin.getWorkbench().addWorkbenchListener(workbenchListener);
		TapsetLibrary.init();
	}

	/**
	 * Called by the Eclipse Workbench to deactivate the plugin.
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		TapsetLibrary.stop();
		ScriptConsole.stopAll();
		plugin.getWorkbench().removeWorkbenchListener(workbenchListener);
		plugin = null;
	}

	/**
	 * Returns this plugin's instance.
	 */
	public static IDEPlugin getDefault() {
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
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Create an uri to be used to connect to the remote machine
	 */
	public URI createRemoteUri(String path) {
		IPreferenceStore p = ConsoleLogPlugin.getDefault().getPreferenceStore();
		String user = p.getString(ConsoleLogPreferenceConstants.SCP_USER);
		String host = p.getString(ConsoleLogPreferenceConstants.HOST_NAME);
		if (path == null)
		 {
			path = ""; //$NON-NLS-1$
		}
		try {
			URI uri = new URI("ssh", user, host, -1, path, null, null); //$NON-NLS-1$
			return uri;
		} catch (URISyntaxException uri) {
			return null;
		}
	}
	
	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
	}

	public static void logException(Throwable e, final String title, String message) {
		if (e instanceof InvocationTargetException) {
			e = ((InvocationTargetException) e).getTargetException();
		}
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else {
			if (message == null)
				message = e.getMessage();
			if (message == null)
				message = e.toString();
			status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, e);
		}
		ResourcesPlugin.getPlugin().getLog().log(status);
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		final IStatus fstatus = status;
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				ErrorDialog.openError(null, title, null, fstatus);
			}
		});
	}

	public static void logException(Throwable e) {
		logException(e, null, null);
	}

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, e.getMessage(), e);
		log(status);
	}

}
