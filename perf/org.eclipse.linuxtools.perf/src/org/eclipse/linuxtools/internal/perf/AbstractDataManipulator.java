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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;

/**
 * This class represents the general flow of a perf command being
 * set up, executed, and having its data collected.
 */
public abstract class AbstractDataManipulator implements IPerfData {

	private String text;
	private String title;
	private File workDir;
	private ILaunch launch;

	AbstractDataManipulator (String title, File workDir) {
		this.title = title;
		this.workDir = workDir;
	}

	@Override
	public String getPerfData() {
		return text;
	}

	protected File getWorkDir(){
		return workDir;
	}
	@Override
	public String getTitle () {
		return title;
	}

	public void setLaunch (ILaunch launch) {
		this.launch = launch;
	}

	public void performCommand(String[] cmd, int fd) {
		try {

			Process proc;
			if (workDir != null) {
				Path path = new Path(workDir.getAbsolutePath());
				IFileStore workDirStore = EFS.getLocalFileSystem().getStore(path);
				proc = RuntimeProcessFactory.getFactory().exec(cmd, null, workDirStore, null);
			} else {
				proc = RuntimeProcessFactory.getFactory().exec(cmd, null);
			}
			StringBuffer strBuffData = new StringBuffer();
			StringBuffer strBuffTmp = new StringBuffer();

			switch (fd) {
			case 2:
				strBuffTmp = readStream(proc.getInputStream());
				strBuffData = readStream(proc.getErrorStream());
				break;
			case 1:
				// fall through to default case
			default:
				strBuffData = readStream(proc.getInputStream());
				strBuffTmp = readStream(proc.getErrorStream());
			}

			text = strBuffData.toString();
			printConsole(proc, strBuffTmp.toString());

		} catch (IOException e) {
			text = ""; //$NON-NLS-1$
		}
	}

	/**
	 * Read entire stream contents.
	 *
	 * @param stream InputStream to read from.
	 * @return a StringBuffer containing the contents of the stream.
	 * @throws IOException if I/O exception occurs.
	 */
	private StringBuffer readStream(InputStream stream) throws IOException {
		BufferedReader buff = new BufferedReader(new InputStreamReader(stream));
		StringBuffer strBuff = new StringBuffer();

		try {
			String line;
			while ((line = buff.readLine()) != null) {
				strBuff.append(line);
				strBuff.append("\n"); //$NON-NLS-1$
			}
		} finally {
			try {
				if (buff != null) {
					buff.close();
				}
			} catch (IOException e) {
				/* continue */
			}
		}
		return strBuff;
	}

	/**
	 * Print string to process console.
	 *
	 * @param p Process to retrieve console from.
	 * @param s String to be printed.
	 */
	private void printConsole(Process p, String s) {

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
				if (x.getName().contains(configName) && x instanceof IOConsole) {
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
	 * A combination of setting up the command to run and executing it.
	 * This often calls performCommand(String [] cmd).
	 */
	public abstract void parse();

}
