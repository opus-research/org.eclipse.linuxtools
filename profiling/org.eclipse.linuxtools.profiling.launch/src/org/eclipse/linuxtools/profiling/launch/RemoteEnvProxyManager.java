/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Rodrigo Fraxino De Araujo <rfaraujo@br.ibm.com>
 *     IBM Corporation - Wainer Santos Moschetta <wainersm@br.ibm.com>
 *******************************************************************************/

package org.eclipse.linuxtools.profiling.launch;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;

public class RemoteEnvProxyManager extends RemoteProxyManager implements IRemoteEnvProxyManager {

	private Map<String, IRemoteEnvProxyManager> remoteManagers = new HashMap<String, IRemoteEnvProxyManager>();

	@Override
	protected IRemoteEnvProxyManager getRemoteManager(String schemeId) throws CoreException {
		IRemoteEnvProxyManager remoteManager = remoteManagers.get(schemeId);
		if (remoteManager == null) {
			IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(ProfileLaunchPlugin.PLUGIN_ID, IRemoteProxyManager.EXTENSION_POINT_ID);
			IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
			for(int i = 0; i < infos.length; i++) {
				IConfigurationElement configurationElement = infos[i];
				if (configurationElement.getName().equals(IRemoteProxyManager.MANAGER_NAME)) {
					if (configurationElement.getAttribute(IRemoteProxyManager.SCHEME_ID).equals(schemeId)) {
						Object obj = configurationElement.createExecutableExtension(EXT_ATTR_CLASS);
						if (obj instanceof IRemoteProxyManager) {
							remoteManager = (IRemoteEnvProxyManager) obj;
							remoteManagers.put(schemeId, remoteManager);
							break;
						}
					}
				}
			}
		}
		return remoteManager;
	}

	public Map<String, String> getEnv(IProject project) throws CoreException {
		String scheme = mapping.getSchemeFromNature(project);
		if (scheme!=null) {
			IRemoteEnvProxyManager manager = getRemoteManager(scheme);
			return manager.getEnv(project);
		}
		URI projectURI = project.getLocationURI();
		return getEnv(projectURI);
	}

	public Map<String, String> getEnv(URI uri) throws CoreException {
		String scheme = uri.getScheme();
		if (scheme != null && !scheme.equals(LOCALSCHEME)){
			IRemoteEnvProxyManager manager = getRemoteManager(scheme);
			if (manager != null)
				return manager.getEnv(uri);
		}
		return System.getenv();
	}

}
