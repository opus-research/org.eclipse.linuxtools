/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.profiling.launch.RemoteConnection;
import org.eclipse.linuxtools.profiling.launch.RemoteConnectionException;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;

/**
 * This class represents the general flow of a perf command being
 * set up, executed, and having its data collected.
 */
public abstract class AbstractDataManipulator extends BaseDataManipulator implements
IPerfData {

	private String text;
	private String title;
	private ILaunch launch;
	private IPath pathWorkDir;
	private List<Thread> threads;
	private IProject project;

	AbstractDataManipulator (String title, IPath pathWorkDir, IProject project) {
		this(title, pathWorkDir);
		this.project=project;
	}

	AbstractDataManipulator (String title, IPath pathWorkDir) {
		this.title = title;
		this.pathWorkDir=pathWorkDir;
		threads = new ArrayList<>();
	}

	@Override
	public String getPerfData() {
		return text;
	}

	protected IPath getWorkDir(){
		return pathWorkDir;
	}

	@Override
	public String getTitle () {
		return title;
	}

	public void setLaunch (ILaunch launch) {
		this.launch = launch;
	}

	public void performCommand(String[] cmd, int fd) {
		BufferedReader buffData = null;
		BufferedReader buffTemp = null;
		URI pathWorkDirURI = null;
		try {

			Process proc = null;
			RemoteConnection exeRC = null;
			try {
				pathWorkDirURI = new URI(pathWorkDir.toOSString());
				exeRC = new RemoteConnection(pathWorkDirURI);
			} catch (RemoteConnectionException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
			} catch (URISyntaxException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.MsgProxyError, Messages.MsgProxyError);
			}
			IFileStore workDirStore = exeRC.getRmtFileProxy().getResource(pathWorkDirURI.getPath());
			proc = RuntimeProcessFactory.getFactory().exec(cmd, null, workDirStore, project);
			StringBuffer data = new StringBuffer();
			StringBuffer temp = new StringBuffer();

			switch (fd) {
			case 2:
				buffData = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				buffTemp = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				readStream(buffTemp, temp);
				readStream(buffData, data);
				break;
			case 1:
				// fall through to default case
			default:
				buffData = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				buffTemp = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				readStream(buffData, data);
				readStream(buffTemp, temp);
				break;
			}
			joinAll();
			text = data.toString();
			printToConsole(proc, temp.toString());
		} catch (IOException e) {
			text = ""; //$NON-NLS-1$
		} catch (InterruptedException e){
			text = ""; //$NON-NLS-1$
		}finally {
			try {
				if (buffData != null) {
					buffData.close();
				}
				if (buffTemp != null) {
					buffTemp.close();
				}
			} catch (IOException e) {
				// continue
			}
		}
	}

	/**
	 * Write entire contents of BufferedReader into given StringBuffer.
	 *
	 * @param buff BufferedReader to read from.
	 * @param strBuff StringBuffer to write to.
	 */
	private void readStream(final BufferedReader buff,
			final StringBuffer strBuff) {
		Thread readThread = new Thread(new Runnable() {
			@Override
			public void run() {
				strBuff.append(getBufferContents(buff));
			}
		});
		readThread.start();
		threads.add(readThread);
	}

	/**
	 * Print String to process console.
	 *
	 * @param p Process to get console from.
	 * @param s String to print.
	 */
	private void printToConsole(Process p, String s){
		if (launch != null) {
			String configName = launch.getLaunchConfiguration().getName();
			// Console will try to read from stream so create afterwards
			// Console will have the configuration name as a substring
			DebugPlugin.newProcess(launch, p, ""); //$NON-NLS-1$

			ConsolePlugin plugin = ConsolePlugin.getDefault();
			IConsoleManager conMan = plugin.getConsoleManager();
			IConsole[] existing = conMan.getConsoles();
			IOConsole binaryOutCons = null;
			PrintStream print;

			// Find the console
			for (IConsole x : existing) {
				if (x.getName().contains(configName) &&
						x instanceof IOConsole) {
					binaryOutCons = (IOConsole) x;
				}
			}

			// Get the printstream via the outputstream.
			// Get ouput stream
			if (binaryOutCons != null) {
				OutputStream outputTo = binaryOutCons.newOutputStream();
				print = new PrintStream(outputTo);
				print.println(s);
			}
		}
	}

	/**
	 * Wait for all working threads to finish.
	 *
	 * @throws InterruptedException
	 */
	private void joinAll() throws InterruptedException {
		for (Thread thread : threads) {
			thread.join();
		}
	}

	/**
	 * A combination of setting up the command to run and executing it.
	 * This often calls performCommand(String [] cmd).
	 */
	public abstract void parse();

}
