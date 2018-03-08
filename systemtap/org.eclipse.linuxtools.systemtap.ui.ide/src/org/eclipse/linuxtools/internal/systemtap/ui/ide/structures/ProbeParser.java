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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeDefinitionNode;
import org.eclipse.linuxtools.systemtap.ui.structures.TreeNode;

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
 * @since 2.0
 */
public class ProbeParser extends TapsetParser {

	private TreeNode probes;

	static ProbeParser parser = null;
	public static ProbeParser getInstance(){
		if (parser != null)
			return parser;

		String[] tapsets = IDEPlugin.getDefault().getPreferenceStore()
				.getString(IDEPreferenceConstants.P_TAPSETS).split(File.pathSeparator);
		parser = new ProbeParser(tapsets);

		return parser;
	}

	private ProbeParser(String[] tapsets) {
		super(tapsets, "Probe Parser"); //$NON-NLS-1$
		probes = new TreeNode("", false); //$NON-NLS-1$
	}

	/**
	 * Returns the root node of the tree of the probe alias generated by
	 * parseFiles.  Probes are grouped by target location.
	 * @return A tree of tapset probe aliases grouped by probe location.
	 */
	public synchronized TreeNode getProbes() {
		return probes;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		String s = collect(null);
		s = addStaticProbes(s);
		parseProbes(s);
		probes.sortTree();
		fireUpdateEvent();	//Inform listeners that everything is done
		return new Status(IStatus.OK, IDEPlugin.PLUGIN_ID, ""); //$NON-NLS-1$
	}

	/**
	 * Returns a String containing all of the content from the probe
	 * point list, including variables and their type.
	 *
	 * stap -L
	 * Will list all available probe points
	 * @return the probe points consolidated into a single string
	 */
	private String collect(String script) {
		String[] options;
		if(null == script) {
			script = "**"; //$NON-NLS-1$
			options = new String[] {"-L"};   //$NON-NLS-1$
		} else {
			options = null;
		}

		return runStap(options, script);
	}

	/**
	 * Parses the output generated from running stap -L. Pulls out all functions
	 * and probe aliases from the provided string. Populates the probe and function
	 * trees.
	 *
	 * ProbeTree organized as:
	 *	Root->Files->ProbePoints->Variables
	 *
	 * FunctionTree organized as:
	 *	Root->Files->Functions
	 * @param s The entire output from running stap -L.
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
		probes.dispose();
	}
}
