package org.eclipse.linuxtools.profiling.time;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TimeProviderPlugin extends AbstractUIPlugin {

	/**
	 * Plug-in name
	 */
	public static final String PLUGIN_NAME = "Time"; //$NON-NLS-1$

	/**
	 * Plug-in id
	 */
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.profiling.time"; //$NON-NLS-1$

	/**
	 * Plug-in id of snapshot launch configuration type
	 */
	public static final String PLUGIN_CONFIG_ID = "org.eclipse.linuxtools.profiling.time.launchConfigurationType"; //$NON-NLS-1$

	/**
	 * Type of profiling this plug-in supports
	 */
	public static final String PROFILING_TYPE = "time"; //$NON-NLS-1$

	/**
	 * The shared instance
	 */
	private static TimeProviderPlugin plugin;

	/**
	 * The constructor
	 */
	public TimeProviderPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TimeProviderPlugin getDefault() {
		return plugin;
	}

}
