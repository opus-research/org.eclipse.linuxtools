/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.io.InputStream;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;

public interface DockerClient2 extends DockerClient {

	public InputStream archiveContainer(String containerId, String path)
			throws DockerException, InterruptedException;

}
