/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    	Roberto Oliveira (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch.ui.rdt.proxy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionHostService;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;

public class RDTConnection {
	private static RDTConnection manager;
	private final String REMOTE_SERVICES_ID = "org.eclipse.remote.JSch"; //$NON-NLS-1$

	public static RDTConnection getInstance() {
		if (manager == null)
			manager = new RDTConnection();
		return manager;
	}

	/**
	 * Get all created connections name
	 * 
	 * @return connections name
	 */
	public List<String> getConnectionsName() {
		List<String> connectionsName = new ArrayList<>();
		IRemoteServicesManager servicesManager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connType = servicesManager.getConnectionType(REMOTE_SERVICES_ID);
		for (IRemoteConnection conn : connType.getConnections()) {
			connectionsName.add(conn.getName());
		}
		return connectionsName;
	}

	/**
	 * Get connection host name
	 * 
	 * @param connectionName the connection name
	 * @return connection host
	 */
	public String getConnectionHost(String connectionName) {
		IRemoteConnection conn = getConnection(connectionName);
		if (conn == null) {
			return null;
		}
		IRemoteConnectionHostService hostService = conn.getService(IRemoteConnectionHostService.class);
		return hostService.getHostname();
	}

	/**
	 * Get connection user name
	 * 
	 * @param connectionName the connection name
	 * @return connection user
	 */
	public String getConnectionUser(String connectionName) {
		IRemoteConnection conn = getConnection(connectionName);
		if (conn == null) {
			return null;
		}
		IRemoteConnectionHostService hostService = conn.getService(IRemoteConnectionHostService.class);
		return hostService.getUsername();
	}

	/**
	 * Get connection port number
	 *
	 * @param connectionName the connection name
	 * @return connection port number
	 */
	public int getConnectionPort(String connectionName) {
		IRemoteConnection conn = getConnection(connectionName);
		if (conn == null) {
			return -1;
		}
		IRemoteConnectionHostService hostService = conn.getService(IRemoteConnectionHostService.class);
		return hostService.getPort();
	}

	/**
	 * Get connection password
	 *
	 * @param connectionName the connection name
	 * @return connection password
	 */
	public String getConnectionPasswd(String connectionName) {
		IRemoteConnection conn = getConnection(connectionName);
		if (conn == null) {
			return null;
		}
		String pass = conn.getSecureAttribute("JSCH_PASSWORD_ATTR"); //$NON-NLS-1$
		return pass;
	}

	private IRemoteConnection getConnection(String connectionName) {
		IRemoteServicesManager servicesManager = Activator.getService(IRemoteServicesManager.class);
		IRemoteConnectionType connType = servicesManager.getConnectionType(REMOTE_SERVICES_ID);
		IRemoteConnection conn = connType.getConnection(connectionName);
		return conn;
	}
}