/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerExit;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerContainerState;

public class ContainerCommandProcess extends Process {

	private String containerId;
	private IDockerConnection connection;
	private String imageName;
	private PipedInputStream stdout;
	private PipedInputStream stderr;
	private boolean keepContainer;
	private Thread thread;

	public ContainerCommandProcess(IDockerConnection connection,
			String imageName, String containerId, boolean keepContainer) {
		this.connection = connection;
		this.imageName = imageName;
		this.containerId = containerId;
		this.stdout = new PipedInputStream();
		this.stderr = new PipedInputStream();
		this.keepContainer = keepContainer;
		// Lambda Runnable
		Runnable logContainer = () -> {
			try (PipedOutputStream pipedStdout = new PipedOutputStream(stdout);
					PipedOutputStream pipedStderr = new PipedOutputStream(
							stderr)) {
				connection.attachLog(containerId, pipedStdout, pipedStderr);
				pipedStdout.flush();
				pipedStderr.flush();
			} catch (DockerException | InterruptedException | IOException e) {
				// do nothing but close output streams
			}
		};

		// start the thread
		this.thread = new Thread(logContainer);
		this.thread.start();
	}

	@Override
	public void destroy() {
		try {
			try {
				// TODO: see if there is a better way of draining the
				// container output before closing the streams. Note
				// that trying to join the attachLog thread does not
				// work.
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// ignore
			}
			this.stdout.close();
			this.stderr.close();
		} catch (IOException e) {
			// ignore
		}
		thread.interrupt();
	}

	@Override
	public int exitValue() {
		IDockerContainerInfo info = connection
				.getContainerInfo(containerId);
		if (info != null) {
			IDockerContainerState state = info.state();
			if (state != null) {
				if (state.paused() || state.restarting() || state.running()) {
					throw new IllegalThreadStateException(
							LaunchMessages.getFormattedString(
									"ContainerNotFinished.msg", containerId)); //$NON-NLS-1$
				}
				return state.exitCode();
			}
		}
		return -1;
	}

	@Override
	public InputStream getErrorStream() {
		return stderr;
	}

	@Override
	public InputStream getInputStream() {
		return stdout;
	}

	@Override
	public OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}

	@Override
	public int waitFor() throws InterruptedException {
		try {
			IDockerContainerExit exit = connection
					.waitForContainer(containerId);
			connection.stopLoggingThread(containerId);
			if (!keepContainer) {
				connection.removeContainer(containerId);
			}
			return exit.statusCode();
		} catch (DockerException e) {
			return -1;
		}
	}

	public String getImage() {
		return imageName;
	}

}
