/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation.
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.process;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;

public class LocalSystemtapProcess {

	public Process execute(String[] args, URI uri, OutputStream stdOut, OutputStream standardError) throws CoreException{
		IRemoteCommandLauncher launcher = RemoteProxyManager.getInstance().getLauncher(uri);
		Process process = launcher.execute(new Path("stap"), args, null, null, null); //$NON-NLS-1$
		if (process != null && stdOut !=null && standardError != null){
			launcher.waitAndRead(stdOut, standardError, new NullProgressMonitor());
		}
		return process;
	}

	public Process execute(String[] cmd, String[] envVars) throws IOException {
		return RuntimeProcessFactory.getFactory().exec(cmd, envVars, null);
	}

}
