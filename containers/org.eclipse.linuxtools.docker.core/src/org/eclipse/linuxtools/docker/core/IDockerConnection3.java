/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @since 3.0
 */
public interface IDockerConnection3 {

	public IDockerContainerExit waitForContainer(String id)
			throws DockerException, InterruptedException;

	public void attachLog(final String id, final OutputStream out,
			final OutputStream err)
			throws DockerException, InterruptedException, IOException;

}
