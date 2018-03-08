/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.graphicalrun.actions;

import java.io.IOException;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.systemtap.ui.consolelog.ScpClient;
import org.eclipse.linuxtools.systemtap.ui.consolelog.dialogs.SelectServerDialog;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.graphicalrun.structures.ChartStreamDaemon2;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingConstants;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingPerspective;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorView;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.dataset.DataSetWizard;
import org.eclipse.linuxtools.systemtap.ui.ide.IDESessionSettings;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.RunScriptAction;
import org.eclipse.linuxtools.systemtap.ui.ide.structures.StapErrorParser;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.linuxtools.systemtap.ui.structures.PasswordPrompt;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import com.jcraft.jsch.JSchException;

/**
 * Action used to run the systemTap script in the active editor.  This action will start stap
 * and send the output to both the <code>ScriptConsole</code> window and a <code>DataSet</code>.
 * @author Ryan Morse
 */
public class RunScriptChartAction extends RunScriptAction {
	public RunScriptChartAction() {
		super();
		LogManager.logDebug("initialized", this); //$NON-NLS-1$
	}

	@Override
	public void dispose() {
		LogManager.logDebug("disposed", this); //$NON-NLS-1$
		super.dispose();
	}

	/**
	 * The main body of this event. Starts by making sure the current editor is valid to run,
	 * then builds the command line arguments for stap and retrieves the environment variables.
	 * Next, it gets an instance of <code>ScriptConsole</code> to run the script. Finally, it
	 * Registers a new <code>ChartStreamDaemon2</code> to handle formating the script output
	 * for a <code>DataSet</code>. Once everything is setup, it will attempt to switch to the
	 * Graphing Perspective.
	 */
	@Override
	public void run() {
		LogManager.logDebug("Start run:", this); //$NON-NLS-1$
		continueRun = true;
		if(ConsoleLogPlugin.getDefault().getPreferenceStore().getBoolean(ConsoleLogPreferenceConstants.REMEMBER_SERVER)!=true &&
			new SelectServerDialog(fWindow.getShell()).open() == false)
			return;
	
		if(isValid()) {
			 try{	 
				 ScpClient scpclient = new ScpClient();
				 serverfileName = fileName.substring(fileName.lastIndexOf('/')+1);
				 tmpfileName="/tmp/"+ serverfileName; //$NON-NLS-1$
				 scpclient.transfer(fileName,tmpfileName);
			 } catch (JSchException e){
				 e.printStackTrace();
				 continueRun = false;
			 } catch (IOException e) {
				e.printStackTrace();
				continueRun = false;
			} finally {
			 }

			String[] script = null;
		 
			if(continueRun) script = buildStandardScript();
			if(continueRun) {
				//createClientSession();
				    String[] envVars = getEnvironmentVariables();
			    	ScriptConsole console = ScriptConsole.getInstance(serverfileName);
	                console.run(script, envVars, new PasswordPrompt(IDESessionSettings.password), new StapErrorParser());
	            
			//	subscription.addInputStreamListener(new ChartStreamDaemon2(console, dataSet, parser));
				console.getCommand().addInputStreamListener(new ChartStreamDaemon2(console, dataSet, parser));
				
				//Change to the graphing perspective
				try {
					IWorkbenchPage p = PlatformUI.getWorkbench().showPerspective(GraphingPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
					IViewPart ivp = p.showView(GraphSelectorView.ID);
					String name = console.getName();
					((GraphSelectorView)ivp).createScriptSet(name.substring(name.lastIndexOf('/')+1), dataSet);
				} catch(WorkbenchException we) {
					
				}
			}
		}
		
		LogManager.logDebug("End run:", this); //$NON-NLS-1$
	}
	
	/**
	 * This method is used to prompt the user for the parsing expression to be used in generating
	 * the <code>DataSet</code> from the scripts output.
	 */
	protected void getChartingOptions() {
		DataSetWizard wizard = new DataSetWizard(GraphingConstants.DataSetMetaData, getFilePath());
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.open();
		parser = wizard.getParser();
		
		dataSet = wizard.getDataSet();
		

		if(null == parser || null == dataSet)
		{
			continueRun = false;
		}
		wizard.dispose();
		
	}

	@Override
	protected String[] buildStandardScript() {
		getChartingOptions();
		return super.buildStandardScript();
	}

	private IDataSet dataSet = null;
	private IDataSetParser parser = null;

}
