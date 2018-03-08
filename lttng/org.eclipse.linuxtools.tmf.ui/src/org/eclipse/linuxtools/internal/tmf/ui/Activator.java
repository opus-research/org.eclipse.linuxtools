/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.properties.TmfTimePreferences;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class Activator extends AbstractUIPlugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.tmf.ui"; //$NON-NLS-1$

	/**
	 * The shared instance
	 */
	private static Activator plugin;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	/**
	 * Constructor
	 */
	public Activator() {
	}

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

	/**
	 * Returns the TMF UI plug-in instance.
	 *
	 * @return the TMF UI plug-in instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

    // ------------------------------------------------------------------------
    // AbstractUIPlugin
    // ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		TmfUiTracer.init();
		TmfTraceElement.init();
		TmfTimePreferences.init();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
	    TmfUiTracer.stop();
		plugin = null;
		super.stop(context);
	}

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

	/**
	 * Gets an image object using given path within plug-in.
	 *
	 * @param path path to image file
	 *
	 * @return image object
	 */
    public Image getImageFromPath(String path){
        return getImageDescripterFromPath(path).createImage();
    }

    /**
     * Gets an image descriptor using given path within plug-in.
     *
     * @param path path to image file
     *
     * @return image descriptor object
     */
    public ImageDescriptor getImageDescripterFromPath(String path){
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Gets a image object from the image registry based on the given path.
     * If the image is not in the registry it will be registered.
     *
     * @param path to the image file
     * @return image object
     */
    public Image getImageFromImageRegistry(String path) {
        Image icon = getImageRegistry().get(path);
        if (icon == null) {
            icon = getImageDescripterFromPath(path).createImage();
            plugin.getImageRegistry().put(path, icon);
        }
        return icon;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
     */
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put(ITmfImageConstants.IMG_UI_ZOOM, getImageFromPath(ITmfImageConstants.IMG_UI_ZOOM));
        reg.put(ITmfImageConstants.IMG_UI_ZOOM_IN, getImageFromPath(ITmfImageConstants.IMG_UI_ZOOM_IN));
        reg.put(ITmfImageConstants.IMG_UI_ZOOM_OUT, getImageFromPath(ITmfImageConstants.IMG_UI_ZOOM_OUT));
        reg.put(ITmfImageConstants.IMG_UI_SEQ_DIAGRAM_OBJ, getImageFromPath(ITmfImageConstants.IMG_UI_SEQ_DIAGRAM_OBJ));
        reg.put(ITmfImageConstants.IMG_UI_ARROW_COLLAPSE_OBJ, getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_COLLAPSE_OBJ));
        reg.put(ITmfImageConstants.IMG_UI_ARROW_UP_OBJ, getImageFromPath(ITmfImageConstants.IMG_UI_ARROW_UP_OBJ));
    }

    /**
     * Logs a message with severity INFO in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public void logInfo(String message) {
        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity INFO in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception A exception to log
     */
    public void logInfo(String message, Throwable exception) {
        getLog().log(new Status(IStatus.INFO, PLUGIN_ID, message, exception));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public void logWarning(String message) {
        getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity WARNING in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception A exception to log
     */
    public void logWarning(String message, Throwable exception) {
        getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message, exception));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the plug-in.
     *
     * @param message A message to log
     */
    public void logError(String message) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
    }

    /**
     * Logs a message and exception with severity ERROR in the runtime log of the plug-in.
     *
     * @param message A message to log
     * @param exception A exception to log
     */
    public void logError(String message, Throwable exception) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, exception));
    }
}
