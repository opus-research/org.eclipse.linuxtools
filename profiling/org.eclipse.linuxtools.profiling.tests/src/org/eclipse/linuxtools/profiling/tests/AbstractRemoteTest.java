/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Rodrigo Fraxino De Araujo <rfaraujo@br.ibm.com>
 *******************************************************************************/
package org.eclipse.linuxtools.profiling.tests;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteFileService;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public abstract class AbstractRemoteTest extends AbstractTest {
	private final String PLUGIN_ID="org.eclipse.linuxtools.profiling.tests";
    public static final String REMOTE_NATURE_ID = "org.eclipse.ptp.rdt.core.remoteNature"; //$NON-NLS-1$
    public static final String REMOTE_SERVICES = "org.eclipse.ptp.remote.RemoteTools"; //$NON-NLS-1$
    public static final String REMOTE_MAKE_NATURE = "org.eclipse.ptp.rdt.core.remoteMakeNature"; //$NON-NLS-1$
    public static final String REMOTE_MAKE_BUILDER = "org.eclipse.ptp.rdt.core.remoteMakeBuilder"; //$NON-NLS-1$
    public static final String BUILD_SERVICE = "org.eclipse.ptp.rdt.core.BuildService"; //$NON-NLS-1$
    public static final String CINDEX_SERVICE = "org.eclipse.ptp.rdt.core.CIndexingService"; //$NON-NLS-1$
    public static final String RDT_CINDEX_SERVICE = "org.eclipse.ptp.rdt.server.dstore.RemoteToolsCIndexServiceProvider"; //$NON-NLS-1$
    public static final String TOOLCHAIN_ID = "org.eclipse.ptp.rdt.managedbuild.toolchain.gnu.base"; //$NON-NLS-1$
    public static final String PTP_EXE = "org.eclipse.ptp.rdt.managedbuild.target.gnu.exe"; //$NON-NLS-1$
    public static final String DEBUG = "Debug"; //$NON-NLS-1$
    public static final String USERNAME = ""; //$NON-NLS-1$
    private static final String PASSWORD = ""; //$NON-NLS-1$
    // Sets localhost as default connection if no remote host is given
    private static String HOST = "localhost"; //$NON-NLS-1$
    public static String CONNECTION_NAME = "localhost"; //$NON-NLS-1$
    public static final String RESOURCES_DIR = "resources/"; //$NON-NLS-1$

    private IRemoteConnectionWorkingCopy fRemoteConnection;

    /**
     * @deprecated As of 1.1, this should not be used because PTP no more provides
     *  rdt managed projects.
     *
     * Create a CDT project outside the default workspace.
     *
     * @param bundle            The plug-in bundle.
     * @param projname            The name of the project.
     * @param absProjectPath    Absolute path to the directory to which the project should be mapped
     *                             outside the workspace.
     * @return                    A new external CDT project.
     * @throws CoreException
     * @throws URISyntaxException
     * @throws IOException
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    @Deprecated protected IProject createRemoteExternalProject(Bundle bundle,
            final String projname, final String absProjectPath,
            final String sourceFile) throws CoreException, URISyntaxException, IOException {
        return null;
    }
    /**
     * @deprecated As of 1.1, this should not be used because PTP no more provides
     *  rdt managed projects.
     */
    @Deprecated protected IProject createRemoteExternalProjectAndBuild(Bundle bundle,
            String projname, String absProjectPath, String sourceFile, String host,
            String connectionName) throws CoreException, URISyntaxException, IOException {
        HOST = host;
        CONNECTION_NAME = connectionName;
        IProject proj = createRemoteExternalProject(bundle, projname, absProjectPath, sourceFile);
        buildProject(proj);
        return proj;
    }

    /**
     * @deprecated As of 1.1, this should not be used because PTP no more provides
     *  rdt managed projects.
     */
    @Deprecated protected IProject createRemoteExternalProjectAndBuild(Bundle bundle,
            String projname, String absProjectPath, String sourceFile) throws CoreException, URISyntaxException, IOException {
        IProject proj = createRemoteExternalProject(bundle, projname, absProjectPath, sourceFile);
        buildProject(proj);
        return proj;
    }

        protected void deleteResource (String directory) {
                IRemoteServicesManager sm = getServicesManager();
                IRemoteConnection conn = sm.getConnectionType("ssh").getConnection(CONNECTION_NAME);
                assertNotNull(conn);
                IRemoteFileService fileManager = conn.getService(IRemoteFileService.class);
                assertNotNull(fileManager);
                final IFileStore dstFileStore = fileManager.getResource(directory);
                try {
                    dstFileStore.delete(EFS.NONE, null);
                } catch (CoreException e) {
                }
            }

    private IRemoteServicesManager getServicesManager() {
		BundleContext context = Platform.getBundle(PLUGIN_ID).getBundleContext();
		ServiceReference<IRemoteServicesManager> ref = context.getServiceReference(IRemoteServicesManager.class);
		assertNotNull(ref);
		return context.getService(ref);
    }

}
