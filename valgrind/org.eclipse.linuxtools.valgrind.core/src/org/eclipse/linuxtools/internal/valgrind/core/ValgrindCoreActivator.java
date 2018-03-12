package org.eclipse.linuxtools.internal.valgrind.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class ValgrindCoreActivator extends Plugin {
    private static ValgrindCoreActivator plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

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
    public static ValgrindCoreActivator getDefault() {
        return plugin;
    }

}
