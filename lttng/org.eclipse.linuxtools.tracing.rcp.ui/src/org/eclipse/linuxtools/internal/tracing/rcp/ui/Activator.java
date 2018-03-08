/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.tracing.rcp.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author Bernd Hufmann
 */
public class Activator extends AbstractUIPlugin {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.lttng2.rcp.ui"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	// The shared instance
	private static Activator fPlugin;

    // ------------------------------------------------------------------------
    // Constructor(s)
    // ------------------------------------------------------------------------
	/**
	 * The default constructor
	 */
	public Activator() {
	}

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return fPlugin;
    }

    // ------------------------------------------------------------------------
    // Operation
    // ------------------------------------------------------------------------
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		fPlugin = this;
	}

	@Override
    public void stop(BundleContext context) throws Exception {
		fPlugin = null;
		super.stop(context);
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
