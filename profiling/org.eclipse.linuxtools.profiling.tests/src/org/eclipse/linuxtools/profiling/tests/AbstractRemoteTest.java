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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileSystem;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.ptp.rdt.core.remotemake.RemoteMakeBuilder;
import org.eclipse.ptp.rdt.core.resources.RemoteMakeNature;
import org.eclipse.ptp.rdt.ui.serviceproviders.IRemoteToolsIndexServiceProvider;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public abstract class AbstractRemoteTest extends AbstractTest {
	public static final String REMOTE_NATURE_ID = "org.eclipse.ptp.rdt.core.remoteNature"; //$NON-NLS-1$
	public static final String REMOTE_SERVICES = "org.eclipse.ptp.remote.RemoteTools"; //$NON-NLS-1$
	public static final String REMOTE_MAKE_NATURE = "org.eclipse.ptp.rdt.core.remoteMakeNature"; //$NON-NLS-1$
	public static final String REMOTE_MAKE_BUILDER = "org.eclipse.ptp.rdt.core.remoteMakeBuilder"; //$NON-NLS-1$
	public static final String BUILD_SERVICE = "org.eclipse.ptp.rdt.core.BuildService"; //$NON-NLS-1$
	public static final String CINDEX_SERVICE = "org.eclipse.ptp.rdt.core.CIndexingService"; //$NON-NLS-1$
	public static final String RDT_CINDEX_SERVICE = "org.eclipse.ptp.rdt.server.dstore.RemoteToolsCIndexServiceProvider"; //$NON-NLS-1$
	public static final String CDT_BUILD = "cdt.managedbuild.config.gnu.exe.debug"; //$NON-NLS-1$
	public static final String DEBUG = "Debug"; //$NON-NLS-1$
	private static final String USERNAME = ""; //$NON-NLS-1$
	private static final String PASSWORD = ""; //$NON-NLS-1$
	// Sets localhost as default connection if no remote host is given
	private static String HOST = "localhost"; //$NON-NLS-1$
	private static String CONNECTION_NAME = "localhost"; //$NON-NLS-1$
	public static final String RESOURCES_DIR = "resources/"; //$NON-NLS-1$

	private IRemoteServices fRemoteServices;
	private IRemoteConnection fRemoteConnection;

	/**
	 * Create a CDT project outside the default workspace.
	 *
	 * @param bundle			The plug-in bundle.
	 * @param projname			The name of the project.
	 * @param absProjectPath	Absolute path to the directory to which the project should be mapped
	 * 							outside the workspace.
	 * @return					A new external CDT project.
	 * @throws CoreException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	protected IProject createRemoteExternalProject(Bundle bundle,
			final String projname, final String absProjectPath,
			final String sourceFile) throws CoreException, URISyntaxException, IOException {

		IProject externalProject;
		// Turn off auto-building
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription wspDesc = workspace.getDescription();
		wspDesc.setAutoBuilding(false);
		workspace.setDescription(wspDesc);

		// Create external project
		IWorkspaceRoot root = workspace.getRoot();
		externalProject = root.getProject(projname);
		IProjectDescription description = workspace.newProjectDescription(projname);

		// Get services responsible for handling the remote connection
		fRemoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(REMOTE_SERVICES);
		assertNotNull(fRemoteServices);

		// Create connection manager
		IRemoteConnectionManager connMgr = fRemoteServices.getConnectionManager();
		assertNotNull(connMgr);

		try {
			// Creates a localhost connection or a remote one if one is
			// specified by createRemoteExternalProjectAndBuild
			fRemoteConnection = connMgr.newConnection(CONNECTION_NAME);
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(fRemoteConnection);
		// Sets the connection arguments
		fRemoteConnection.setAddress(HOST);
		fRemoteConnection.setUsername(USERNAME);
		fRemoteConnection.setPassword(PASSWORD);

		try {
			fRemoteConnection.open(new NullProgressMonitor());
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertTrue(fRemoteConnection.isOpen());

		// Sets the localtion of the remote project
		// RDT format is as follows: remotetools://connectionName/directory
		URI fileProjectURL = new URI(absProjectPath);
		description.setLocationURI(fileProjectURL);
		// Creates CDT project
		externalProject = CCorePlugin.getDefault().createCDTProject(
				description, externalProject, new NullProgressMonitor());
		assertNotNull(externalProject);
		assertTrue(externalProject.isOpen());
		// Add the necessary natures to the remote project
		CProjectNature.addCNature(externalProject, new NullProgressMonitor());
		CCProjectNature.addCCNature(externalProject, new NullProgressMonitor());
		CProjectNature.addNature(externalProject, AbstractRemoteTest.REMOTE_NATURE_ID, new NullProgressMonitor());
		// Since it is a remote makefile project, add the make nature
		CProjectNature.addNature(externalProject, REMOTE_MAKE_NATURE, new NullProgressMonitor());
		RemoteMakeNature.updateProjectDescription(externalProject, REMOTE_MAKE_BUILDER, new NullProgressMonitor());
		// Creates a service model required by RDT projects
		ServiceModelManager.getInstance().addConfiguration(externalProject, ServiceModelManager.getInstance().newServiceConfiguration(externalProject.getName()));
		ServiceModelManager smm = ServiceModelManager.getInstance();
		// Creates a Service Configuration for this CDT project
		// RDT needs it in order to build the project later
		IServiceConfiguration config = ServiceModelManager.getInstance().newServiceConfiguration(externalProject.getName());

		IService buildService = smm.getService(BUILD_SERVICE);
		IServiceProviderDescriptor descriptor = buildService.getProviderDescriptor(RemoteBuildServiceProvider.ID);
		RemoteBuildServiceProvider rbsp = (RemoteBuildServiceProvider) smm.getServiceProvider(descriptor);
		if (rbsp != null) {
			rbsp.setRemoteToolsConnection(fRemoteConnection);
			config.setServiceProvider(buildService, rbsp);
		}
		IService indexingService = smm.getService(CINDEX_SERVICE);
		descriptor = indexingService.getProviderDescriptor(RDT_CINDEX_SERVICE);
		IRemoteToolsIndexServiceProvider provider = (IRemoteToolsIndexServiceProvider) smm
				.getServiceProvider(descriptor);
		if (provider != null) {
			provider.setConnection(fRemoteConnection);
			config.setServiceProvider(indexingService, provider);
		}
		// Adds the service configuration with the properties defined to the CDT project
		smm.addConfiguration(externalProject, config);
		smm.setActiveConfiguration(externalProject, config);
		smm.saveModelConfiguration();

		// Adds a description and a configuration the the CDT project
		ICProjectDescriptionManager mngr =
				CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription des = mngr.createProjectDescription(externalProject,
				false);
		ManagedProject mProj = new ManagedProject(des);
		Configuration cfg = new Configuration(mProj, null, CDT_BUILD, DEBUG);
		cfg.getEditableBuilder().setBuildPath(absProjectPath);
		CConfigurationData configurationData = cfg.getConfigurationData();
		assertNotNull(configurationData);
		des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, configurationData);
		mngr.setProjectDescription(externalProject, des);

		RemoteMakeNature.updateProjectDescription(externalProject, RemoteMakeBuilder.REMOTE_MAKE_BUILDER_ID, new NullProgressMonitor());
		ManagedBuildManager.saveBuildInfo(externalProject, true);
		try {
			// CDT opens the Project with BACKGROUND_REFRESH enabled which causes the
			// refresh manager to refresh the project 200ms later. This Job interferes
			// with the resource change handler firing see: bug 271264
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		} catch (Exception e) {
			// Ignore
		}
		assertTrue(externalProject.isOpen());

		// The source file in the plug-in test package is copied to the specified directory 
		IRemoteFileProxy proxy = RemoteProxyManager.getInstance().getFileProxy(externalProject);
		IFileSystem fileSystem = EFS.getLocalFileSystem();
		IFileStore resourcesDir = fileSystem.getStore(URI.create(RESOURCES_DIR + projname));
		IFileStore iFileStoreDest = proxy.getResource(absProjectPath);
		resourcesDir.copy(iFileStoreDest, EFS.OVERWRITE , null);

		// Index the project
		IIndexManager indexMgr = CCorePlugin.getIndexManager();
		indexMgr.joinIndexer(IIndexManager.FOREVER, new NullProgressMonitor());

		return externalProject;
	}
	protected IProject createRemoteExternalProjectAndBuild(Bundle bundle,
			String projname, String absProjectPath, String sourceFile, String host,
			String connectionName) throws CoreException, URISyntaxException, IOException {
		HOST = host;
		CONNECTION_NAME = connectionName;
		IProject proj = createRemoteExternalProject(bundle, projname, absProjectPath, sourceFile);
		buildProject(proj);
		return proj;
	}

	protected IProject createRemoteExternalProjectAndBuild(Bundle bundle,
			String projname, String absProjectPath, String sourceFile) throws CoreException, URISyntaxException, IOException {
		IProject proj = createRemoteExternalProject(bundle, projname, absProjectPath, sourceFile);
		buildProject(proj);
		return proj;
	}

}
