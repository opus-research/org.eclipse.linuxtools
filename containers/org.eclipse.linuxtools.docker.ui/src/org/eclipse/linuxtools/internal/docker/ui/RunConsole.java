/*******************************************************************************
 * Copyright (c) 2010, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *     Red Hat Inc - modified to use in Docker UI
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerContainerState;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.tm.terminal.view.ui.interfaces.ITerminalsView;
import org.eclipse.tm.terminal.view.ui.manager.ConsoleManager;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;

/**
 * RpmConsole is used to output rpm/rpmbuild output.
 *
 */
public class RunConsole extends IOConsole {

	/** Id of this console. */
	public static final String ID = "containerLog"; //$NON-NLS-1$
	public static final String CONTAINER_LOG_TITLE = "ContainerLog.title"; //$NON-NLS-1$
	public static final String DEFAULT_ID = "__DEFAULT_ID__"; //$NON-NLS-1$

	private String containerId;
	private String id;
	private IDockerConnection connection;

	private OutputStream outputStream;
	private boolean attached = false;
	private boolean isTerminal = false;

	/**
	 * Returns a reference to the console that is for the given container id. If
	 * such a console does not yet exist, it will be created.
	 *
	 * @param containerId
	 *            The container id this console will be for. Must not be
	 *            <code>null</code>.
	 * @return A console instance or <code>null</code> if the given containerId
	 *         was <code>null</code>.
	 */
	public static RunConsole findConsole(IDockerConnection connection,
			String containerId) {
		if (containerId == null)
			return null;
		return findConsole(connection, containerId, DEFAULT_ID);
	}

	/**
	 * Returns a reference to the console that is for the given container id. If
	 * such a console does not yet exist, it will be created.
	 *
	 * @param container
	 *            The container this console will be for. Must not be
	 *            <code>null</code>.
	 * @return A console instance or <code>null</code> if the given containerId
	 *         was <code>null</code>.
	 */
	public static RunConsole findConsole(IDockerContainer container) {
		if (container == null)
			return null;
		return findConsole(container.getConnection(), container.id(),
				DEFAULT_ID, container.name());
	}

	/**
	 * Returns a reference to the console that is for the given container id. If
	 * such a console does not yet exist, it will be created.
	 *
	 * @param containerId
	 *            The container this console will be for. Must not be
	 *            <code>null</code>.
	 * @param id
	 *            The secondary id used to identify consoles belonging to
	 *            various owners.
	 * @return A console instance.
	 */
	public static RunConsole findConsole(IDockerConnection connection,
			String containerId, String id) {
		return findConsole(connection, containerId, id,
				containerId.substring(0, 8));
	}

	/**
	 * Returns a reference to the console that is for the given container id. If
	 * such a console does not yet exist, it will be created.
	 *
	 * @param containerId
	 *            The container this console will be for. Must not be
	 *            <code>null</code>.
	 * @param id
	 *            The secondary id used to identify consoles belonging to
	 *            various owners.
	 * @param name
	 *            the name of the console to create if it did not already exist
	 * @return A console instance.
	 */
	public static RunConsole findConsole(IDockerConnection connection,
			String containerId, String id,
			String name) {
		RunConsole ret = null;
		IDockerContainerInfo info = connection.getContainerInfo(containerId);

		if (info.config().tty()) {
			ITerminalsView tv = ConsoleManager.getInstance().findConsoleView(
					containerId,
					"org.eclipse.linuxtools.docker." + connection.hashCode()); //$NON-NLS-1$
			if (tv == null) {
				ret = new RunConsole(connection, containerId, id, name);
				ret.setTerminal(true);
			}
		} else {
			for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
					.getConsoles()) {
				if (cons instanceof RunConsole
						&& ((RunConsole) cons).containerId.equals(containerId)
						&& ((RunConsole) cons).connection == connection
						&& ((RunConsole) cons).id.equals(id)) {
					ret = (RunConsole) cons;
				}
			}
			// no existing console, create new one
			if (ret == null) {
				ret = new RunConsole(connection, containerId, id, name);
				ConsolePlugin.getDefault().getConsoleManager()
						.addConsoles(new IConsole[] { ret });
				ret.setTerminal(false);
			}
		}

		return ret;
	}

	/**
	 * Set the title of the RunConsole
	 * 
	 * @param name
	 *            - new title
	 */
	public void setTitle(String name) {
		setName(name);
	}

	/**
	 * Set whether this RunConsole represents a TM Terminal
	 * 
	 * @param isTerminal
	 *            - true if this is a TM Terminal, false otherwise
	 */
	public void setTerminal(boolean isTerminal) {
		this.isTerminal = isTerminal;
	}

	/**
	 * The console will be attached to the underlying container.
	 *
	 * @param connection
	 *            The connection associated with this console.
	 */
	public void attachToConsole(final IDockerConnection connection) {
		final InputStream in = getInputStream();
		final OutputStream out = newOutputStream();
		Thread t = new Thread(() -> {
			try {
				DockerConnection conn = (DockerConnection) connection;
				if (conn.getContainerInfo(containerId).config().openStdin()) {
					IDockerContainerState state = conn
							.getContainerInfo(containerId).state();
					do {
						if (!state.running())
							Thread.sleep(1000);
					} while (!state.running() && state.finishDate() == null);
					conn.attachCommand(containerId, in, out);
				}
			} catch (Exception e) {
			}
		});
		t.start();
		attached = true;
	}

	public void attachToConsole(final IDockerConnection connection,
			String containerId) {
		this.connection = connection;
		this.containerId = containerId;
		attachToConsole(connection);
	}

	/**
	 * Indicates whether the console is attached to the container's terminal.
	 * 
	 * @return true if the console is attached and false otherwise.
	 */
	public boolean isAttached() {
		/*
		 * TODO: This will still return true when the corresponding container is
		 * stopped but not currently a problem as we always attach to a
		 * container about to be started.
		 */
		return attached;
	}

	/**
	 * Remove a given console from the Console view
	 * 
	 * @param console
	 *            the console to remove
	 */
	public static void removeConsole(RunConsole console) {
		console.closeOutputStream();
		ConsolePlugin.getDefault().getConsoleManager()
				.removeConsoles(new IConsole[] { console });
	}

	/**
	 * Show this console in the Console View.
	 */
	public void showConsole() {
		// Show this console
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this);
	}

	/**
	 * Close the last output stream opened for this console.
	 */
	public void closeOutputStream() {
		try {
			if (outputStream != null)
				outputStream.close();
		} catch (IOException e) {
			// do nothing...we tried
		}
	}

	/**
	 * Get a new output stream for this console
	 * 
	 * @return An output stream for writing to the console
	 */
	public OutputStream getOutputStream() {
		outputStream = new ConsoleOutputStream(newOutputStream());
		// outputStream = newOutputStream();
		return outputStream;
	}

	/**
	 * Creates the console.
	 *
	 * @param containerId
	 *            The container id this console is for.
	 * @param id
	 *            The id to associate with this console.
	 * @param name
	 *            The name to use for the console.
	 */
	private RunConsole(IDockerConnection connection, String containerId,
			String id, String name) {
		super(DVMessages.getFormattedString(CONTAINER_LOG_TITLE, name), ID,
				null, true);
		this.connection = connection;
		this.containerId = containerId;
		this.id = id;
	}
}
