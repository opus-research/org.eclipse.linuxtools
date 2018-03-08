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

import com.jcraft.jsch.JSchException;

public class SystemtapProcessFactory {

	public static LocalSystemtapProcess getSystemtapProcess() {
		return new LocalSystemtapProcess();
	}

	public static RemoteSystemtapProcess getRemoteSystemtapProcess(String user,
			String host, String password) throws JSchException {
		return new RemoteSystemtapProcess(user, host, password);
	}

}
