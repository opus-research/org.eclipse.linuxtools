/*******************************************************************************
 * Copyright (c) 2012-2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 * Eric Williams <ericwill@redhat.com> - modification for Javadocs
 *******************************************************************************/

package org.eclipse.linuxtools.internal.javadocs.ui;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.javadocs.ui.preferences.JavaDocMessages;
import org.eclipse.linuxtools.internal.javadocs.ui.preferences.PreferenceConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle.
 */
public class JavaDocPlugin extends AbstractUIPlugin {


	// The plug-in ID
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.javadocs"; //$NON-NLS-1$
    private static final String REGENERATE_MSG = "Java.Doc.Regenerate.msg"; //$NON-NLS-1$

    // The shared instance
    private static JavaDocPlugin plugin;

    // Startup job
    private static Job k;
	
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        k = new JavadocStartupJob(JavaDocMessages.getString(REGENERATE_MSG)) ;
        k.schedule();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        k.cancel();
        plugin = null;
        super.stop(context);
    }
	

    /**
     * Job used to load Javadoc data on startup.
     *
     */
    private static class JavadocStartupJob extends Job {

        private IProgressMonitor runMonitor;

        public JavadocStartupJob(String name) {
            super(name);
        }

        @Override
        protected void canceling() {
            if (runMonitor != null)
                runMonitor.setCanceled(true);
        };

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            runMonitor = monitor;
            if (monitor.isCanceled())
                return Status.CANCEL_STATUS;
            IPreferenceStore ps = JavaDocPlugin.getDefault()
                    .getPreferenceStore();
            String javadocDir = ps.getString(PreferenceConstants.JAVADOCS_DIRECTORY);
            IPath javadocPath = new Path(javadocDir);
            File javadoc = javadocPath.toFile();
            if (!javadoc.exists()) {
                // No input data to process so quit now
                monitor.done();
                return Status.OK_STATUS;
            }

            return Status.OK_STATUS;
        }

    };

    /**
	 * Returns the shared instance of the plugin.
	 * 
	 * @return  the shared instance of the plugin
	 */
	public static JavaDocPlugin getDefault() {
        return plugin;
    }
}
