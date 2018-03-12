/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc..
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteEnvProxyManager;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;

public class RDTProxyManager implements IRemoteEnvProxyManager {

    public static final String SYNC_NATURE = "org.eclipse.ptp.rdt.sync.core.remoteSyncNature"; //$NON-NLS-1$

    @Override
    public IRemoteFileProxy getFileProxy(URI uri) throws CoreException {
        return new RDTFileProxy(uri);
    }

    @Override
    public IRemoteFileProxy getFileProxy(IProject project) throws CoreException {
        return new RDTFileProxy(project);
    }

    @Override
    public IRemoteCommandLauncher getLauncher(URI uri) {
        return new RDTCommandLauncher(uri);
    }

    @Override
    public IRemoteCommandLauncher getLauncher(IProject project) {
        return new RDTCommandLauncher(project);
    }

    @Override
    public String getOS(URI uri) {
        IRemoteServices services = RemoteServices.getRemoteServices(uri);
        IRemoteConnection connection = services.getConnectionManager().getConnection(uri);
        String os = connection.getProperty(IRemoteConnection.OS_NAME_PROPERTY);
        if (os == null || os.isEmpty()) {
            //FIXME: need better way to get this property
            return "Linux"; //$NON-NLS-1$
        }
        return os;
    }

    @Override
    public String getOS(IProject project) {
        URI uri = project.getLocationURI();
        return getOS(uri);
    }

    @Override
    public Map<String, String> getEnv(URI uri) {
        IRemoteServices services = RemoteServices.getRemoteServices(uri);
        IRemoteConnection connection = services.getConnectionManager().getConnection(uri);
        if(!connection.isOpen()) {
            try {
                connection.open(null);
            } catch (RemoteConnectionException e) {
                Status status = new Status(IStatus.ERROR, e.getMessage(), Activator.PLUGIN_ID);
                Activator.getDefault().getLog().log(status);
                return Collections.emptyMap();
            }
        }
        /*
         * It is common to export functions declaration in the environment so
         * this pattern filters out them because they get truncated and might
         * end up on failure. When a function is exported it makes a mess in ENV
         * and none of LT plugins working remotely because they are not find on
         * path.
         *
         * ie: BASH_FUNC_module()=() { eval `/usr/bin/modulecmd bash $*`, }
         */
        Pattern functionPattern = Pattern.compile("(.+)\\(\\)=([\\(\\)\\s{].+[\n]*.+)");  //$NON-NLS-1$
        Matcher m;
        Map<String, String> envMap = new HashMap<>();
        Map<String, String> envTemp = connection.getEnv();
        for (String key : envTemp.keySet()) {
            String value = envTemp.get(key);
            String env = key + "=" + value; //$NON-NLS-1$
            m = functionPattern.matcher(env);
            if (!m.matches()) {
                envMap.put(key, value);
            }
        }
        return envMap;
    }

    @Override
    public Map<String, String> getEnv(IProject project) {
        URI uri = project.getLocationURI();
        return getEnv(uri);
    }

}
