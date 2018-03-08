/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rdt.proxy;

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.rdt.proxy.Activator;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;

public class RDTFileProxy implements IRemoteFileProxy {

	private IRemoteFileManager manager;

	private void initialize(URI uri) throws CoreException {
		IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(uri);
		services.initialize();
		IRemoteConnection connection = services.getConnectionManager().getConnection(uri);
		if (connection != null)
			manager = services.getFileManager(connection);
		else
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					Activator.getResourceString("Connection.error"))); //$NON-NLS-1$
	}

	public RDTFileProxy(URI uri) throws CoreException {
		initialize(uri);
	}

	public RDTFileProxy(IProject project) throws CoreException {
		URI uri = project.getLocationURI();
		initialize(uri);
	}

	@Override
	public URI toURI(IPath path) {
		return manager.toURI(path);
	}

	@Override
	public URI toURI(String path) {
		return manager.toURI(path);
	}

	@Override
	public String toPath(URI uri) {
		return manager.toPath(uri);
	}

	@Override
	public String getDirectorySeparator() {
		return manager.getDirectorySeparator();
	}

	@Override
	public IFileStore getResource(String path) {
		return manager.getResource(path);
	}

}
