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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.cdt.build.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.managedbuilder.core.ManagedCProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.osgi.framework.Bundle;

public abstract class AbstractRemoteTest extends AbstractTest {
	public static final String REMOTE_NATURE_ID = "org.eclipse.ptp.rdt.core.remoteNature"; //$NON-NLS-1$
	protected IProject proj;
	private static final String USERNAME = ""; //$NON-NLS-1$
	private static final String PASSWORD = ""; //$NON-NLS-1$
	private static final String HOST = "localhost"; //$NON-NLS-1$
	private static final String CONNECTION_NAME = "localhost"; //$NON-NLS-1$

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
	@Override
	protected IProject createExternalProject(Bundle bundle,
			final String projname, final String absProjectPath,
			final String sourceFile) throws CoreException, URISyntaxException, IOException,
			InvocationTargetException, InterruptedException {
		
		IProject externalProject;
		// Turn off auto-building
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription wspDesc = workspace.getDescription();
		wspDesc.setAutoBuilding(false);
		workspace.setDescription(wspDesc);

		// Create external project
		IWorkspaceRoot root = workspace.getRoot();
		externalProject = root.getProject(projname);
		IProjectDescription description = workspace
				.newProjectDescription(projname);
		
		fRemoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices("org.eclipse.ptp.remote.RemoteTools"); //$NON-NLS-1$
		assertNotNull(fRemoteServices);

		IRemoteConnectionManager connMgr = fRemoteServices.getConnectionManager();
		assertNotNull(connMgr);

		try {
			fRemoteConnection = connMgr.newConnection(CONNECTION_NAME); //$NON-NLS-1$
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertNotNull(fRemoteConnection);
		fRemoteConnection.setAddress(HOST);
		fRemoteConnection.setUsername(USERNAME);
		fRemoteConnection.setPassword(PASSWORD);

		try {
			fRemoteConnection.open(new NullProgressMonitor());
		} catch (RemoteConnectionException e) {
			fail(e.getLocalizedMessage());
		}
		assertTrue(fRemoteConnection.isOpen());
		
		
		URI fileProjectURL = new URI(absProjectPath);
		description.setLocationURI(fileProjectURL);
		externalProject = CCorePlugin.getDefault().createCDTProject(
				description, externalProject, new NullProgressMonitor());
		assertNotNull(externalProject);
		externalProject.open(null);
		CProjectNature.addCNature(externalProject, new NullProgressMonitor());
		CCProjectNature.addCCNature(externalProject, new NullProgressMonitor());
		CProjectNature.addNature(externalProject, AbstractRemoteTest.REMOTE_NATURE_ID, new NullProgressMonitor());

		try {
			// CDT opens the Project with BACKGROUND_REFRESH enabled which causes the
			// refresh manager to refresh the project 200ms later. This Job interferes
			// with the resource change handler firing see: bug 271264
			Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_REFRESH, null);
		} catch (Exception e) {
			// Ignore
		}
		assertTrue(externalProject.isOpen());

		// Import boiler-plate files which can then be built and profiled
		URL location = FileLocator.find(bundle, new Path(
				"resources/" + projname), null); //$NON-NLS-1$
		File testDir = new File(FileLocator.toFileURL(location).toURI());
		ImportOperation op = new ImportOperation(externalProject.getFullPath(),
				testDir, FileSystemStructureProvider.INSTANCE,
				new IOverwriteQuery() {
					public String queryOverwrite(String pathString) {
						return ALL;
					}
				});
		op.setCreateContainerStructure(false);
		op.run(null);

		IStatus status = op.getStatus();
		if (!status.isOK()) {
			throw new CoreException(status);
		}
		// Make sure import went well
		assertNotNull(externalProject.findMember(new Path(sourceFile)));

		// Index the project
		IIndexManager indexMgr = CCorePlugin.getIndexManager();
		indexMgr.joinIndexer(IIndexManager.FOREVER, new NullProgressMonitor());

		// These natures must be enabled at this point to continue
		assertTrue(externalProject
				.isNatureEnabled(ScannerConfigNature.NATURE_ID));
		assertTrue(externalProject
				.isNatureEnabled(ManagedCProjectNature.MNG_NATURE_ID));

		return externalProject;
	}

}
