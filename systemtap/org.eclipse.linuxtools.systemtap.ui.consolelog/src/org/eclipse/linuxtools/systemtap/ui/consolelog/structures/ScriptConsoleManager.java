/*******************************************************************************
 * Copyright (c) 2013 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

import java.util.LinkedList;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;

public class ScriptConsoleManager {

	static ScriptConsoleManager manager;

	public static ScriptConsoleManager getInstance(){
		if (manager == null){
			manager = new ScriptConsoleManager();
		}
		return manager;
	}

	public static interface ActiveConsoleObserver {
		public void consoleActivated(ScriptConsole console);
	}

	private LinkedList<ActiveConsoleObserver> activeConsoleObservers
	= new LinkedList<ActiveConsoleObserver>();

	/**
	 * This method is used to get a reference to a <code>ScriptConsole</code>.  If there
	 * is already an console that has the same name as that provided it will be stopped,
	 * cleared and returned to the caller to use.  If there is no console matching the
	 * provided name then a new <code>ScriptConsole</code> will be created for use.
	 * @param name The name of the console that should be returned if available.
	 * @return The console with the provided name, or a new instance if none exist.
	 */
	public ScriptConsole getConsoleInstance(String name) {
		ScriptConsole console = null;
		try {
			IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();

			//Prevent running the same script twice
			if(null != ic) {
				ScriptConsole activeConsole;
				for (IConsole consoleIterator: ic) {
					if (consoleIterator instanceof ScriptConsole){
						activeConsole = (ScriptConsole) consoleIterator;
						if(activeConsole.getName().endsWith(name)) {
							//Stop any script currently running
							activeConsole.stop();
							//Remove output from last run
							activeConsole.clearConsole();
							activeConsole.setName(name);
							console = activeConsole;
						}
					}
				}
			}

			if(null == console) {
				console = new ScriptConsole(name, null);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] {console});
			}
		} catch(NullPointerException npe) {
			console = null;
		}
		return console;
	}

	void activeConsoleChanged(ScriptConsole console){
		for (ActiveConsoleObserver observer : activeConsoleObservers) {
			observer.consoleActivated(console);
		}
	}

	public void addActiveConsoleObserver (ActiveConsoleObserver observer){
		activeConsoleObservers.add(observer);
	}

	public boolean isActiveConsoleRunning(){
		ScriptConsole active = getActive();
		return (active != null && getActive().isRunning());
	}

	/**
	 * Finds and returns the active console.
	 * @return The active <code>ScriptConsole<code> in the ConsoleView
	 */
	public ScriptConsole getActive() {
		IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(IConsoleConstants.ID_CONSOLE_VIEW);
		IConsole activeConsole = ((IConsoleView)ivp).getConsole();
		if (activeConsole instanceof ScriptConsole){
			return (ScriptConsole)activeConsole;
		}else{
			return null;
		}
	}

}
