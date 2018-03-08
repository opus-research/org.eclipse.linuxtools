/*******************************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.ide.structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.StringOutputStream;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IUpdateListener;
import org.eclipse.linuxtools.systemtap.ui.structures.ui.ExceptionErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Runs stap -vp1 & stap -up2 in order to get all of the probes/functions
 * that are defined in the tapsets.  Builds probeAlias and function trees
 * with the values obtained from the tapsets.
 *
 * Ugly code is a result of two issues with getting stap output.  First,
 * many tapsets do not work under stap -up2.  Second since the output
 * is not a regular language, we can't create a nice lexor/parser combination
 * to do everything nicely.
 * @author Ryan Morse
 */
public class TapsetParser extends Job {

	private ArrayList<IUpdateListener> listeners;

	private TreeNode functions;
	private TreeNode probes;

	private String[] tapsets;
	private boolean cancelRequested;

	static TapsetParser parser = null;
	public static TapsetParser getInstance(){
		if (parser != null)
			return parser;

		String[] tapsets = IDEPlugin.getDefault().getPreferenceStore()
				.getString(IDEPreferenceConstants.P_TAPSETS).split(File.pathSeparator);
		parser = new TapsetParser(tapsets);

		return parser;
	}

	private TapsetParser(String[] tapsets) {
		super("Tapset Parser"); //$NON-NLS-1$
		this.tapsets = tapsets;
		listeners = new ArrayList<IUpdateListener>();
		functions = new TreeNode("", false); //$NON-NLS-1$
		probes = new TreeNode("", false); //$NON-NLS-1$
		cancelRequested = false;
	}

	@Override
	protected void canceling() {
		super.canceling();
		this.cancelRequested = true;
	}
	/**
	 * Returns the root node of the tree of functions generated by
	 * parseFiles.  Functions are grouped by source file.
	 * @return A tree of tapset functions grouped by file.
	 */
	public synchronized TreeNode getFunctions() {
		return functions;
	}

	/**
	 * Returns the root node of the tree of the probe alias generated by
	 * parseFiles.  Probes are grouped by target location.
	 * @return A tree of tapset probe aliases grouped by probe location.
	 */
	public synchronized TreeNode getProbes() {
		return probes;
	}

	/**
	 * This method checks to see if the parser completed executing on its own.
	 * @return Boolean indicating whether or not the thread finished on its own.
	 */
	public boolean isFinishSuccessful() {
		IStatus result = getResult();
		return result != null && result.isOK();
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		String s = readPass1(null);
		s = addStaticProbes(s);

		parseProbes(s);
		runPass2Functions();

		sortTrees();

		fireUpdateEvent();	//Inform listeners that everything is done
		return new Status(IStatus.OK, IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
	}

	/**
	 * This method will register a new listener with the parser
	 * @param listener The listener that will receive updateEvents
	 */
	public void addListener(IUpdateListener listener) {
		if (null != listener) {
			listeners.add(listener);
		}
	}

	/**
	 * This method will unregister the listener with the parser
	 * @param listener The listener that no longer wants to recieve update events
	 */
	public void removeListener(IUpdateListener listener) {
		if (null != listener) {
			listeners.remove(listener);
		}
	}

	/**
	 * This method will fire an updateEvent to all listeners.
	 */
	private void fireUpdateEvent() {
		for (IUpdateListener listener : listeners) {
			listener.handleUpdateEvent();
		}
	}

	/**
	 * Runs the stap with the given options and returns the output generated
	 * @param options String[] of any optional parameters to pass to stap
	 * @param probe String containing the script to run stap on
	 * @since 1.2
	 */
	protected String runStap(String[] options, String probe) {
		String[] args = null;

		int size = 2;	//start at 2 for stap, script, options will be added in later
		if (null != tapsets && tapsets.length > 0 && tapsets[0].trim().length() > 0) {
			size += tapsets.length<<1;
		}
		if (null != options && options.length > 0 && options[0].trim().length() > 0) {
			size += options.length;
		}

		args = new String[size];
		args[0] = ""; //$NON-NLS-1$
		args[size-1] = probe;
		args[size-2] = ""; //$NON-NLS-1$

		//Add extra tapset directories
		if(null != tapsets && tapsets.length > 0 && tapsets[0].trim().length() > 0) {
			for(int i=0; i<tapsets.length; i++) {
				args[2+(i<<1)] = "-I"; //$NON-NLS-1$
				args[3+(i<<1)] = tapsets[i];
			}
		}
		if(null != options && options.length > 0 && options[0].trim().length() > 0) {
			for(int i=0; i<options.length; i++)
				args[args.length-options.length-1+i] = options[i];
		}

		StringOutputStream str = new StringOutputStream();
		StringOutputStream strErr = new StringOutputStream();
		try {
			URI uri;
			if (IDEPlugin.getDefault().getPreferenceStore().getBoolean(IDEPreferenceConstants.P_REMOTE_PROBES)) {
				uri = IDEPlugin.getDefault().createRemoteUri(null);
			} else {
				uri = new URI(Path.ROOT.toOSString());
			}
			IRemoteCommandLauncher launcher = RemoteProxyManager.getInstance().getLauncher(uri);
			Process process = launcher.execute(new Path("stap"), args, null, null, null); //$NON-NLS-1$
			if(process == null){
				displayError(Messages.TapsetParser_CannotRunStapTitle, Messages.TapsetParser_CannotRunStapMessage);
			}
			launcher.waitAndRead(str, strErr, new NullProgressMonitor());
		} catch (URISyntaxException e) {
			ExceptionErrorDialog.openError(Messages.TapsetParser_ErrorRunningSystemtap, e);
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.TapsetParser_ErrorRunningSystemtap, e);
		}

		return str.toString();
	}

	/**
	 * Returns a String containing all of the content from the probe
	 * point list, including variables and their type.
	 *
	 * stap -v -p1 -L
	 * Will list all available probe points
	 * @return the probe points consolidated into a single string
	 */
	private String readPass1(String script) {
		String[] options;
		if(null == script) {
			script = "**"; //$NON-NLS-1$
			options = new String[] {"-p1", "-v", "-L"};   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		} else {
			options = null;
		}

		return runStap(options, script);
	}

	/**
	 * Parses the output generated from running stap -v -p1 -L. Pulls out all functions
	 * and probe aliases from the provided string. Populates the probe and function
	 * trees.
	 *
	 * ProbeTree organized as:
	 * 	Root->Files->ProbePoints->Variables
	 *
	 * FunctionTree organized as:
	 * 	Root->Files->Functions
	 * @param s The entire output from running stap -v -p1 -L.
	 */
	private void parseProbes(String s) {
		String token = null;
		StringBuilder prev = new StringBuilder(""); //$NON-NLS-1$
		TreeNode currentProbe = null;
		TreeNode group = null;

	 	StringTokenizer st = new StringTokenizer(s, "\n", false); //$NON-NLS-1$
 		st.nextToken(); //skip the stap command itself
	 	while(st.hasMoreTokens() && !cancelRequested){
	 		String tokenString = st.nextToken();

			// If the token starts with '_' or '__' it is a private probe so
			// skip it.
			if (tokenString.startsWith("_")) { //$NON-NLS-1$
				continue;
			}

	 		int firstDotIndex = tokenString.indexOf('.');
 			String groupName = tokenString;
	 		if (firstDotIndex > 0){
	 			groupName = tokenString.substring(0, firstDotIndex);
	 		}

			// If the current probe belongs to a group other than
			// the most recent group. This should rarely be needed because the
			// probe list is sorted... mostly.
	 		if(group == null || !group.getData().equals(groupName)){
	 			group = probes.getChildByName(groupName);
	 		}

	 		// Create a new group and add it
	 		if(group == null){
	 			group = new TreeNode(groupName, groupName, true);
	 			probes.add(group);
	 		}

	 		StringTokenizer probe = new StringTokenizer(tokenString);
 			prev.setLength(0);

 			// The first token is the probe name
 			token = probe.nextToken();
 			currentProbe = new TreeDefinitionNode("probe " + token, token, null, true); //$NON-NLS-1$
 			group.add(currentProbe);

 			// the remaining tokens are variable names and variable types name:type.
	 		while(probe.hasMoreTokens()){
	 			token = probe.nextToken();

				// Because some variable types contain spaces (var2:struct task_struct)
	 			// the only way to know if we have the entire string representing a
	 			// variable is if we reach the next token containing a ':' or we reach
	 			// the end of the stream.
	 			if (token.contains(":") && prev.length() > 0){ //$NON-NLS-1$
	 				prev.setLength(prev.length() - 1); // Remove the trailing space.
	 				currentProbe.add(new TreeNode(prev.toString(), prev.toString(), false));
	 				prev.setLength(0);
	 			}
	 			prev.append(token + " "); //$NON-NLS-1$
	 		}

 			// Add the last token if there is one
	 		if (prev.length() > 0){
	 			prev.setLength(prev.length() - 1); // Remove the trailing space.
	 			currentProbe.add(new TreeNode(prev.toString(), prev.toString(), false));
	 		}
	 	}
	}

	/**
	 * This method is used to build up the list of functions that were found
	 * during the first pass of stap.  Stap is invoked by: $stap -v -p1 -e
	 * 'probe begin{}' and parsing the output.
	 */
	private void runPass2Functions() {
		int i = 0;
		TreeNode parent;
		String script = "probe begin{}"; //$NON-NLS-1$
		String result = runStap(new String[] {"-v", "-p1", "-e"}, script);   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		StringTokenizer st = new StringTokenizer(result, "\n", false); //$NON-NLS-1$
		st.nextToken(); //skip that stap command
		String tok = ""; //$NON-NLS-1$
		String regex = "^function .*\\)\n$"; //match ^function and ending the line with ')' //$NON-NLS-1$
		Pattern p = Pattern.compile(regex, Pattern.MULTILINE | Pattern.UNIX_LINES | Pattern.COMMENTS);
		Pattern secondp = Pattern.compile("[\\W]"); //take our function line and split it up //$NON-NLS-1$
		Pattern underscorep = Pattern.compile("^function _.*"); //remove any lines that "^function _" //$NON-NLS-1$
		Pattern allCaps = Pattern.compile("[A-Z_1-9]*"); //$NON-NLS-1$
		while(st.hasMoreTokens()) {
			tok = st.nextToken().toString();
			Matcher m = p.matcher(tok);
			while(m.find()) {
				// this gives us function foo (bar, bar)
				// we need to strip the ^function and functions with a leading _
				String[] us = underscorep.split(m.group().toString());

				for(String s : us) {
					String[] test = secondp.split(s);
					i = 0;
					for(String t : test) {
						// If i== 1 this is a function name.
						// Ignore ALL_CAPS functions; they are not meant for end
						// user use.
						if(i == 1 && !allCaps.matcher(t).matches()) {
							functions.add(new TreeNode(t, t, true));
						}
						else if(i > 1 && t.length() >= 1) {
							parent = functions.getChildAt(functions.getChildCount()-1);
							parent.add(new TreeDefinitionNode("function " + t, t, parent.getData().toString(), false)); //$NON-NLS-1$
						}
						i++;
					}
				}
			}
		}
		functions.sortTree();
	}

	protected void sortTrees() {
		functions.sortTree();
		probes.sortTree();
	}

	/**
	 * Reads the static probes list and adds it to the probes tree.
	 * This function assumes that the probes list will be sorted.
	 * @return
	 */
	private String addStaticProbes(String probeList) {
		StringBuilder probes = new StringBuilder(probeList);

		BufferedReader input = null;
		try {
			URL location = IDEPlugin.getDefault().getBundle().getEntry("completion/static_probe_list.properties"); //$NON-NLS-1$
			location = FileLocator.toFileURL(location);
			input = new BufferedReader(new FileReader(new File(location.getFile())));
			String line = input.readLine();
			while (line != null && !cancelRequested){
				probes.append('\n');
				probes.append(line);
				line = input.readLine();
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return probes.toString();
	}

	/**
	 * This method will clean up everything from the run.
	 */
	public void dispose() {
		functions.dispose();
		probes.dispose();
	}

	private void displayError(final String title, final String error){
    	Display.getDefault().asyncExec(new Runnable() {
    		@Override
			public void run() {
    			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    			MessageDialog.openWarning(window.getShell(), title, error);
    		}
    	});
	}

}
