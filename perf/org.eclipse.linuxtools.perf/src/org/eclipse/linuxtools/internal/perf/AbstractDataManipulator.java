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
public abstract class AbstractDataManipulator {

	private String text;
	private String title;
	private ILaunch launch;

	AbstractDataManipulator (String title) {
		this.title = title;
	}

	public String getPerfData() {
		return text;
	}

	public String getTitle () {
		return title;
	}

	public void setLaunch (ILaunch launch) {
		this.launch = launch;
	}

	public void performCommand(String[] cmd, int fd) {
		BufferedReader buff = null;
		BufferedReader bufftmp = null;

		try {

			Process proc = RuntimeProcessFactory.getFactory().exec(cmd, null);

			switch (fd) {
			case 1:
				buff = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				bufftmp = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				break;
			case 2:
				buff = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
				bufftmp = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				break;
			default:
				buff = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				bufftmp = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			}

			StringBuffer strBuff = new StringBuffer();
			StringBuffer strBuffTmp = new StringBuffer();
			String line = ""; //$NON-NLS-1$

			// If the buffer is not being consumed, the other one may block.
			while ((line = bufftmp.readLine()) != null) {
				strBuffTmp.append(line);
				strBuffTmp.append("\n"); //$NON-NLS-1$
			}

			while ((line = buff.readLine()) != null) {
				strBuff.append(line);
				strBuff.append("\n"); //$NON-NLS-1$
			}
			text = strBuff.toString();

			if (launch != null) {
				String configName = launch.getLaunchConfiguration().getName();
				// Console will try to read from stream so create afterwards
				// Console will have the configuration name as a substring
				DebugPlugin.newProcess(launch, proc, ""); //$NON-NLS-1$

				ConsolePlugin plugin = ConsolePlugin.getDefault();
				IConsoleManager conMan = plugin.getConsoleManager();
				IConsole[] existing = conMan.getConsoles();
				IOConsole binaryOutCons = null;
				PrintStream print;

				// Find the console
				for (IConsole x : existing) {
					if (x.getName().contains(configName)) { //$NON-NLS-1$
						binaryOutCons = (IOConsole) x;
					}
				}

				// Get the printstream via the outputstream.
				// Get ouput stream
				if (binaryOutCons != null) {
					OutputStream outputTo = binaryOutCons.newOutputStream();
					print = new PrintStream(outputTo);
					print.println(strBuffTmp.toString());
				}
			}
		} catch (IOException e) {
			text = ""; //$NON-NLS-1$
		} finally {
			try {
				if (buff != null) {
					buff.close();
				}
				if (bufftmp != null) {
					bufftmp.close();
				}
			} catch (IOException e) {
				// continue
			}
		}
	}

	/**
	 * A combination of setting up the command to run and executing it.
	 * This often calls performCommand(String [] cmd).
	 */
	public abstract void parse();

}
