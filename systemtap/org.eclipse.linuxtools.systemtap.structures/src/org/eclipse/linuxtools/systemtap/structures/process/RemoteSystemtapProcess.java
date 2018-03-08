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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class RemoteSystemtapProcess {

	private Session session;

	RemoteSystemtapProcess(String user, String host, String password) throws JSchException{
		JSch jsch = new JSch();
		session = jsch.getSession(user, host, 22);
		session.setPassword(password);
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no"); //$NON-NLS-1$//$NON-NLS-2$
		session.setConfig(config);
		session.connect();
	}

	public Channel executeRemote (String command) throws JSchException{

		Channel channel = session.openChannel("exec"); //$NON-NLS-1$
		((ChannelExec) channel).setCommand(command);

		channel.setInputStream(null, true);
		channel.setOutputStream(System.out, true);
		channel.setExtOutputStream(System.err, true);

		return channel;
	}

	public Session getSession() {
		return this.session;
	}

}
