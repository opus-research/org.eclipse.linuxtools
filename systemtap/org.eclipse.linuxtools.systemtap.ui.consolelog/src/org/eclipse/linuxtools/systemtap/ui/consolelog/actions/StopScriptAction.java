/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsoleManager;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsoleManager.ActiveConsoleObserver;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;



/**
 * A class that handles stopping the <code>ScriptConsole</code>.
 * @author Ryan Morse
 */
public class StopScriptAction extends ConsoleAction implements ActiveConsoleObserver, IPropertyListener{

	private IAction action;

	/**
	 * This is the main method of the class. It handles stopping the
	 * currently active <code>ScriptConsole</code>.
	 */
	@Override
	public void run() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				ScriptConsole console = ScriptConsoleManager.getInstance().getActive();
				if(null != console && console.isRunning()){
					console.stop();
				}
			}
		});
	}

	@Override
	public void init(IViewPart view) {
		updateEnablement();
		view.addPropertyListener(this);
		ScriptConsoleManager.getInstance().addActiveConsoleObserver(this);
	}

	private void updateEnablement(){
		if (this.action != null){
			this.action.setEnabled(ScriptConsoleManager.getInstance().isActiveConsoleRunning());
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.action = action;
	}

	/**
	 * This method will stop all consoles that are running.
	 */
	public void stopAll() {
		IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		ScriptConsole console;

		for(int i=0; i<ic.length; i++) {
			if (ic[i] instanceof ScriptConsole){
				console = (ScriptConsole)ic[i];
				if(console.isRunning())
					console.stop();
			}
		}
	}

	/**
	 * This method will check to see if any scripts are currently running.
	 * @return - boolean indicating whether any scripts are running
	 */
	public boolean anyRunning() {
		IConsole ic[] = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		ScriptConsole console;

		for(int i=0; i<ic.length; i++) {
			if (ic[i] instanceof ScriptConsole){
				console = (ScriptConsole)ic[i];
				if(console.isRunning())
					return true;
			}
		}
		return false;
	}

	public void propertyChanged(Object source, int propId) {
		updateEnablement();
	}

	public void consoleActivated(ScriptConsole console) {
		this.action.setEnabled(console.isRunning());
	}

}
