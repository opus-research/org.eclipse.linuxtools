/*******************************************************************************
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.perf.model.PMCommand;
import org.eclipse.linuxtools.internal.perf.model.PMDso;
import org.eclipse.linuxtools.internal.perf.model.PMEvent;
import org.eclipse.linuxtools.internal.perf.model.PMFile;
import org.eclipse.linuxtools.internal.perf.model.PMSymbol;
import org.eclipse.linuxtools.internal.perf.model.TreeParent;
import org.eclipse.linuxtools.profiling.launch.ConfigUtils;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class PerfCore {
	public static String spitStream(BufferedReader br, String blockTitle, PrintStream print) {

		StringBuffer strBuf = new StringBuffer();
		String line = null;
		try {
			while (( line = br.readLine()) != null){
				strBuf.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String str = strBuf.toString();
		if (!str.trim().equals("")) {
			if (print != null) {
				print.println(blockTitle + ": \n" +str + "\n END OF " + blockTitle);
			} else {
				System.out.println(blockTitle + ": \n" +str + "\n END OF " + blockTitle);
			}
		}
		return str;
	}
	// Maps event lists to host names for caching
	private static HashMap<String,HashMap<String, ArrayList<String>>> eventsHostMap = null;
	private static HashMap<String,ArrayList<String>> eventList = null;
	public static HashMap<String,ArrayList<String>> getEventList() { 
		return getEventList(null);
	}

	/**
	 * Gets the list of events for a given launch configuration. Uses a cache for each host
	 * @param config
	 * @return
	 */
	public static HashMap<String,ArrayList<String>> getEventList(ILaunchConfiguration config) {
		String projectHost = getHostName(config);

		if(eventsHostMap == null){
			eventsHostMap = new HashMap<String, HashMap<String,ArrayList<String>>>();
		}

		// local projects have null hosts
		if(projectHost == null){
			projectHost = "local";
		}

		eventList = eventsHostMap.get(projectHost);

		if(eventList == null){
			eventList = loadEventList(config);
			eventsHostMap.put(projectHost, eventList);
		}
		return eventList;
	}


	/**
	 *
	 * @param config
	 * @return the name of the host in which the config's project is stored
	 */
	private static String getHostName(ILaunchConfiguration config){
		String projectName = null;
		try {
			projectName = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		} catch (CoreException e) {
			return null;
		}
		if (projectName == null) {
			return null;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if(project == null){
			return null;
		}
		return project.getLocationURI().getHost();

	}

	public static HashMap<String,ArrayList<String>> loadEventList(ILaunchConfiguration config) {
		HashMap<String,ArrayList<String>> events = new HashMap<String,ArrayList<String>>();
		IProject project = null;
		if (config==null) {
			if (!PerfCore.checkPerfInPath()) {
				return events;
			}
		} else {
			ConfigUtils configUtils = new ConfigUtils(config);
			try {
				project = ConfigUtils.getProject(configUtils.getProjectName());
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			if (!PerfCore.checkRemotePerfInPath(project)) {
				return events;
			}
		}
		Process p = null;
		BufferedReader input = null;
		try {
			// Alternatively can try with -i flag
			p = RuntimeProcessFactory.getFactory().exec(new String[] {PerfPlugin.PERF_COMMAND, "list"}, project); //(char 1 as -t is a custom field seperator

			/*
			 * Old versions of Perf will send events list to stderr instead of stdout
			 * Checking if stdout is empty then read from stderr
			 */
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));

		} catch( IOException e ) {
			e.printStackTrace();
		} 
		String line;
		try {
			while (( line = input.readLine()) != null){
				if (line.contains("[")) {
					String event;
					String cat;
					if (line.contains(PerfPlugin.STRINGS_HWBREAKPOINTS)) {
						cat = PerfPlugin.STRINGS_HWBREAKPOINTS;
						event = line.substring(1,line.indexOf("[", 0)).trim();
					} else if (line.contains(PerfPlugin.STRINGS_RAWHWEvents)) {
						cat = PerfPlugin.STRINGS_RAWHWEvents;
						event = line.substring(1,line.indexOf("[", 0)).trim();
					} else {
						event = line.substring(1,line.indexOf("[", 0)).trim();
						if (event.contains("OR")) {
							event = event.split("OR")[0]; //filter out the abbreviations.
						}
						cat = line.replaceFirst(".*\\[(.+)\\]", "$1").trim();
					}
					ArrayList<String> catevs = events.get(cat);
					if (catevs == null) {
						catevs = new ArrayList<String>();
						events.put(cat, catevs);
					}
					catevs.add(event.trim());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
		}
		return events;
	}


	//Gets the current version of perf
	public static String getPerfVersion(ILaunchConfiguration config, String[] environ, IPath workingDir) {
		ConfigUtils configUtils = new ConfigUtils(config);
		IProject project = null;
		try {
			project = ConfigUtils.getProject(configUtils.getProjectName());
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		Process p = null;
		IRemoteFileProxy proxy = null;
		IFileStore workingDirFileStore = null;

		if (workingDir == null) {
			try {
				p = RuntimeProcessFactory.getFactory().exec(new String [] {PerfPlugin.PERF_COMMAND, "--version"}, project);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				proxy = RemoteProxyManager.getInstance().getFileProxy(new URI(workingDir.toOSString()));
				workingDirFileStore = proxy.getResource(workingDir.toOSString());
				p = RuntimeProcessFactory.getFactory().exec(new String [] {PerfPlugin.PERF_COMMAND, "--version"}, environ, workingDirFileStore, project);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}			

		//p.waitFor();
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		return spitStream(input, "Perf --version STDOUT", null);
	}

	public static boolean checkPerfInPath()
	{
		try 
		{
			Runtime.getRuntime().exec(new String [] {PerfPlugin.PERF_COMMAND, "--version"});			
		} 
		catch (IOException e) 
		{
			return false;
		}
		return true;
	}

	public static boolean checkRemotePerfInPath(IProject project) {
		try 
		{
			RuntimeProcessFactory.getFactory().exec(new String [] {PerfPlugin.PERF_COMMAND, "--version"}, project);			
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public String getRemoteProjectPath(String projectName) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		return project.getName();
	}

	//Generates a perf record command string with the options set in the given config. (If null uses default).
	public static String [] getRecordString(ILaunchConfiguration config) {
		String [] base = new String [] {PerfPlugin.PERF_COMMAND, "record", "-f"};
		if (config == null) {
			return base;
		} else {
			ArrayList<String> newCommand = new ArrayList<String>();
			newCommand.addAll(Arrays.asList(base));
			try {
				if (config.getAttribute(PerfPlugin.ATTR_Record_Realtime, PerfPlugin.ATTR_Record_Realtime_default))
					newCommand.add("-r");
				if (config.getAttribute(PerfPlugin.ATTR_Record_Verbose, PerfPlugin.ATTR_Record_Verbose_default))
					newCommand.add("-v");
				if (config.getAttribute(PerfPlugin.ATTR_Multiplex, PerfPlugin.ATTR_Multiplex_default))
					newCommand.add("-M");
				List<?> selE = config.getAttribute(PerfPlugin.ATTR_SelectedEvents, PerfPlugin.ATTR_SelectedEvents_default);
				if (!config.getAttribute(PerfPlugin.ATTR_DefaultEvent, PerfPlugin.ATTR_DefaultEvent_default) 
						&& selE != null) {					
					for(Object e : selE) {
						newCommand.add("-e");
						newCommand.add((String)e);
					}
				}
			} catch (CoreException e) { }			
			return newCommand.toArray(new String[] {});
		}
	}

	public static String[] getReportString(ILaunchConfiguration config, String perfDataLoc) {
		ArrayList<String> base = new ArrayList<String>();
		base.addAll(Arrays.asList(new String [] {PerfPlugin.PERF_COMMAND, "report", "--sort", "comm,dso,sym", "-n", "-t", "" + (char)1 }));//(char 1 as -t is a custom field seperator)
		if (config != null) {
			try {
				String kernelLoc = config.getAttribute(PerfPlugin.ATTR_Kernel_Location, PerfPlugin.ATTR_Kernel_Location_default);
				if (kernelLoc != PerfPlugin.ATTR_Kernel_Location_default) {
					base.add("--vmlinux");
					base.add(kernelLoc);
				}
				if (config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, PerfPlugin.ATTR_ModuleSymbols_default))
					base.add("-m");

				/*
				 * danielhb, 12/14/2011 - some systems, like ubuntu and sles, does not have
				 * the -U option. The binary fails to execute in those systems when this
				 * option is enabled. 
				 * I'm disabling it to make the plug-in runnable for them. This
				 * will probably need to be revisited in the future, probably when this
				 * flag is implemented by the Perf binary of those systems.
				 */
				/*
				if (config.getAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, PerfPlugin.ATTR_HideUnresolvedSymbols_default))
					base.add("-U");
				 */
				if (perfDataLoc != null) {
					base.add("-i");
					base.add(perfDataLoc);
				}
			} catch (CoreException e) { }			
		}
		return base.toArray( new String[base.size()] );
	}

	public static String[] getAnnotateString(ILaunchConfiguration config, String dso, String symbol, String perfDataLoc, boolean OldPerfVersion) {
		ArrayList<String> base = new ArrayList<String>();
		if (OldPerfVersion) {
			base.addAll( Arrays.asList( new String[]{PerfPlugin.PERF_COMMAND, "annotate", "-s", symbol, "-l", "-P"} ) );
		} else {
			base.addAll( Arrays.asList( new String[]{PerfPlugin.PERF_COMMAND, "annotate", "-d", dso, "-s", symbol, "-l", "-P"} ) );
		}
		if (config != null) {
			try {
				String kernelLoc = config.getAttribute(PerfPlugin.ATTR_Kernel_Location, PerfPlugin.ATTR_Kernel_Location_default);
				if (kernelLoc != PerfPlugin.ATTR_Kernel_Location_default) {
					base.add("--vmlinux");
					base.add(kernelLoc);
				}
				if (config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, PerfPlugin.ATTR_ModuleSymbols_default))
					base.add("-m");
				if (perfDataLoc != null) {
					base.add("-i");
					base.add(perfDataLoc);
				}
			} catch (CoreException e) { }			
		}

		//(Annotate string per symbol)
		//if (PerfPlugin.DEBUG_ON) System.out.println(Arrays.toString( (String[])base.toArray( new String[base.size()] ) ));
		return base.toArray( new String[base.size()] );
	}
	//Runs Perf Record on the given binary and records into perf.data before calling Report() to feed in the results. 
	public static void Record(ILaunchConfiguration config, String binaryPath) {
		ConfigUtils configUtils = new ConfigUtils(config);
		IProject project = null;
		try {
			project = ConfigUtils.getProject(configUtils.getProjectName());
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		BufferedReader error = null;
		Process perfRecord = null;
		try {
			if (project==null) {
				perfRecord = Runtime.getRuntime().exec(ArrayUtil.addAll(getRecordString(null), new String [] {binaryPath}));
			} else {
				perfRecord = RuntimeProcessFactory.getFactory().exec(ArrayUtil.addAll(getRecordString(null), new String [] {binaryPath}), project);
			}
			error = new BufferedReader(new InputStreamReader(perfRecord.getErrorStream()));
			perfRecord.waitFor();			
			spitStream(error,"Perf Record STDERR", null);
		} catch( IOException e ) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//Report();
	}
	public static void Report() {
		Report(null,null,null,null,null,null);
	}
	// Runs assuming perf.data has already been recorded, environ and workingDir can be set to null to use default
	//perfDataLoc is optional - it is used to provide a pre-existing data file instead of something recorded from
	//whatever project is being profiled. It is only used for junit tests atm.
	public static void Report(ILaunchConfiguration config, String[] environ, IPath workingDir, IProgressMonitor monitor, String perfDataLoc, PrintStream print) {
		ConfigUtils configUtils = new ConfigUtils(config);
		IProject project = null;
		try {
			project = ConfigUtils.getProject(configUtils.getProjectName());
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		TreeParent invisibleRoot = PerfPlugin.getDefault().getModelRoot();  
		if (invisibleRoot == null) {
			invisibleRoot = new TreeParent("");
			PerfPlugin.getDefault().setModelRoot(invisibleRoot);
		} else {
			invisibleRoot.clear();
		}

		boolean OldPerfVersion = false;
		if (getPerfVersion(config, environ, workingDir).contains("perf version 0.0.2.PERF")) {
			OldPerfVersion = true;
			if (print != null) { print.println("WARNING: You are running an older version of Perf, please update if you can. The plugin may produce unpredictable results."); }
		}


		BufferedReader input = null;
		BufferedReader error = null;
		Process p = null;

		if (monitor != null && monitor.isCanceled()) { RefreshView(); return; }

		try {
			if (workingDir==null) {
				p = RuntimeProcessFactory.getFactory().exec(getReportString(config, perfDataLoc), project);
			} else {
				p = RuntimeProcessFactory.getFactory().exec(getReportString(config, workingDir.toOSString() + PerfPlugin.PERF_DEFAULT_DATA), project);
			}

			//			p.waitFor();
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			//spitting error stream moved to end of while loop, due to commenting of p.waitFor()
		} catch( IOException e ) {
			e.printStackTrace();
			/*} catch (InterruptedException e) {
			e.printStackTrace();*/
		} 


		PerfCore.parseRemoteReport(config, workingDir, monitor, perfDataLoc, print,
				invisibleRoot, OldPerfVersion, input, error, project);

		RefreshView();
	}

	/**
	 * Parse and build a tree model from the report of a perf data file
	 * @param config launch configuration
	 * @param workingDir working directory configuration
	 * @param monitor  monitor
	 * @param perfDataLoc location of perf data file
	 * @param print print stream
	 * @param invisibleRoot  root of the model
	 * @param OldPerfVersion boolean old perf version flag
	 * @param input input stream from perf data file report
	 * @param error output stream to where all standard error is written to
	 */
	public static void parseReport(ILaunchConfiguration config,
			IPath workingDir, IProgressMonitor monitor, String perfDataLoc,
			PrintStream print, TreeParent invisibleRoot,
			boolean OldPerfVersion, BufferedReader input, BufferedReader error) {
		PerfCore.parseRemoteReport(config, workingDir, monitor, perfDataLoc, print,
				invisibleRoot, OldPerfVersion, input, error, null);
	}

	public static void parseRemoteReport(ILaunchConfiguration config,
			IPath workingDir, IProgressMonitor monitor, String perfDataLoc,
			PrintStream print, TreeParent invisibleRoot,
			boolean OldPerfVersion, BufferedReader input, BufferedReader error, IProject project) {
		if (monitor != null && monitor.isCanceled()) { RefreshView(); return; }
		String line = null;
		String items[];
		float percent;

		Process p = null;
		double samples;
		String comm,dso,symbol;
		boolean kernelFlag;
		PMEvent currentEvent = null;
		PMCommand currentCommand = null;
		PMDso currentDso = null;
		PMFile currentFile = null;
		PMSymbol currentSym = null;
		try {
			while (( line = input.readLine()) != null){
				if (monitor != null && monitor.isCanceled()) { RefreshView(); return; }
				// line containing report information
				if ((line.startsWith("#"))) {
					if (line.contains("Events:") || line.contains("Samples:")) {
						String[] tmp = line.trim().split(" ");
						String event = tmp[tmp.length - 1];
						// In this case, the event name is single quoted
						if (line.contains("Samples:")){
							event = event.substring(1, event.length() -1);
						}
						currentEvent = new PMEvent(event);
						invisibleRoot.addChild(currentEvent);
						currentCommand = null;
						currentDso = null;
					} else if (line.contains("Samples:")) { //"samples" was used instead of events in an older version, some incompatibilities may arise.
						if (print != null) { print.println("WARNING: You are running an older version of Perf, please update if you can. The plugin may produce unpredictable results."); }
						invisibleRoot.addChild(new PMEvent("WARNING: You are running an older version of Perf, the plugin may produce unpredictable results."));
					}
					// contains profiled information
				} else {
					items = line.trim().split(""+(char)1); // using custom field separator. for default whitespace use " +"
					if (items.length != 5) {
						continue;
					}
					percent = Float.parseFloat(items[0]); //percent column
					samples = Double.parseDouble(items[1].trim()); //samples column
					comm = items[2].trim(); //command column
					dso = items[3].trim(); //dso column
					symbol = items[4].trim(); //symbol column 
					kernelFlag = (""+symbol.charAt(1)).equals("k");			

					// initialize current command if it doesn't exist
					if ((currentCommand == null) || (!currentCommand.getName().equals(comm))) {
						currentCommand = (PMCommand) currentEvent.getChild(comm);
						if(currentCommand == null) {
							currentCommand = new PMCommand(comm);
							currentEvent.addChild(currentCommand);
						}
					}

					// initialize current dso if it doesn't exist
					if ((currentDso == null) || (!currentDso.getName().equals(dso))) {
						currentDso = (PMDso) currentCommand.getChild(dso);
						if (currentDso == null) {
							currentDso = new PMDso(dso,kernelFlag);
							currentCommand.addChild(currentDso);
						}
					}

					/*
					 *  Initialize the current file, and symbol
					 *
					 *  We won't know the name of the file containing the symbol
					 *  until we run 'perf annotate' to resolve it, so for now we
					 *  attach all symbols as children of 'Unfiled Symbols'.
					 */
					currentFile = currentDso.getFile(PerfPlugin.STRINGS_UnfiledSymbols);
					currentSym = new PMSymbol(symbol, samples, percent);
					currentFile.addChild(currentSym);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		spitStream(error,"Perf Report STDERR", print);

		boolean SourceLineNumbers = PerfPlugin.ATTR_SourceLineNumbers_default;
		boolean Kernel_SourceLineNumbers = PerfPlugin.ATTR_Kernel_SourceLineNumbers_default;
		try {
			// Check if resolving source file/line numbers is selected
			SourceLineNumbers = config.getAttribute(PerfPlugin.ATTR_SourceLineNumbers, PerfPlugin.ATTR_SourceLineNumbers_default);
			Kernel_SourceLineNumbers = config.getAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, PerfPlugin.ATTR_Kernel_SourceLineNumbers_default);
		} catch (CoreException e2) {
			SourceLineNumbers = false;
		}

		if (monitor != null && monitor.isCanceled()) { RefreshView(); return; }

		boolean hasProfileData = invisibleRoot.getChildren().length != 0;

		if (SourceLineNumbers) {
			for (TreeParent ev : invisibleRoot.getChildren()) {
				if (!(ev instanceof PMEvent)) continue;
				for (TreeParent cmd : ev.getChildren()) {
					if (!(cmd instanceof PMCommand)) continue;
					for (TreeParent d : cmd.getChildren()) {
						if (!(d instanceof PMDso)) continue;					
						currentDso = (PMDso)d;
						if ((!Kernel_SourceLineNumbers) && currentDso.isKernelDso()) continue;
						for (TreeParent s : currentDso.getFile(PerfPlugin.STRINGS_UnfiledSymbols).getChildren()) {
							if (!(s instanceof PMSymbol)) continue;

							if (monitor != null && monitor.isCanceled()) { RefreshView(); return; }


							currentSym = (PMSymbol)s;
							String[] annotateCmd;
							if (workingDir == null) {
								annotateCmd = getAnnotateString(config, currentDso.getName(), currentSym.getName().substring(4), perfDataLoc, OldPerfVersion);
							} else {
								String perfDefaultDataLoc = workingDir + "/" + PerfPlugin.PERF_DEFAULT_DATA;
								annotateCmd = getAnnotateString(config, currentDso.getName(), currentSym.getName().substring(4), perfDefaultDataLoc, OldPerfVersion);
							}

							try {
								if(project==null) p = Runtime.getRuntime().exec(annotateCmd);
								else p = RuntimeProcessFactory.getFactory().exec(annotateCmd, project);
								input = new BufferedReader(new InputStreamReader(p.getInputStream()));
								error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
							} catch (IOException e) {
								e.printStackTrace();
							}

							PerfCore.parseAnnotation(monitor, input,
									workingDir, currentDso, currentSym);
						}

						if (currentDso.getFile(PerfPlugin.STRINGS_UnfiledSymbols).getChildren().length == 0) {
							currentDso.removeChild(currentDso.getFile(PerfPlugin.STRINGS_UnfiledSymbols));
						}
						spitStream(error,"Perf Annotate STDERR", print);
					}
				}
			}
		}

		if (print != null) {
			if (hasProfileData) {
				print.println("Profile data loaded into Perf Profile View.");
			} else {
				print.println("No profile data generated to be displayed.");
			}
		}
	}

	/**
	 * Parse annotation file for a dso given a symbol
	 * @param monitor monitor
	 * @param input annotation file input stream
	 * @param workingDir working directory configuration
	 * @param currentDso dso
	 * @param currentSym symbol
	 */
	public static void parseAnnotation(IProgressMonitor monitor,
			BufferedReader input, IPath workingDir, PMDso currentDso,
			PMSymbol currentSym) {
		if (monitor != null && monitor.isCanceled()) { RefreshView(); return; }

		boolean grabBlock = false;
		boolean blockStarted = false;
		String dsoName,lineRef;
		String line = null;
		String items[];
		float percent;

		try {
			while (( line = input.readLine()) != null){
				if (line.startsWith("Sorted summary for file")) {
					grabBlock = true;
					dsoName = line.replace("Sorted summary for file ","");
					blockStarted = false;
					if ((workingDir != null) && (dsoName.startsWith("./"))) {
						if (workingDir.toOSString().endsWith("/")) {
							dsoName = workingDir.toOSString() + dsoName.substring(2); // path already ends with '/', so trim './'
						} else {
							dsoName = workingDir.toOSString() + dsoName.substring(1); // path doesn't have '/', so trim just the '.'
						}
					}
					currentDso.setPath(dsoName);
				} else if (line.startsWith("---")) {
					if (blockStarted) {
						blockStarted = false;
						grabBlock = false;
					} else {
						blockStarted = true;
					}
				} else if (grabBlock && blockStarted) {
					//process the line.
					items = line.trim().split(" +");
					if (items.length != 2) {
						continue;
					}
					percent = Float.parseFloat(items[0]);
					lineRef = items[1];
					items = lineRef.split(":");
					if (currentDso == null) {
						//if (PerfPlugin.DEBUG_ON) System.err.println("Parsed line ref without being in valid block, shouldn't happen.");
						break;
					} else {
						currentSym.addPercent(Integer.parseInt(items[1]), percent);
						// Symbol currently in 'Unfiled Symbols' but we now know the actual parent
						if (currentSym.getParent().getName().equals(PerfPlugin.STRINGS_UnfiledSymbols)) {
							currentSym.getParent().removeChild(currentSym);
							currentDso.getFile(items[0]).addChild(currentSym);
							// Symbol has 2 (or more) parents
						} else if (!((PMFile)currentSym.getParent()).getPath().equals(items[0])) {
							currentSym.markConflict();
							currentSym.getParent().removeChild(currentSym);
							currentDso.getFile(PerfPlugin.STRINGS_MultipleFilesForSymbol).addChild(currentSym);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void RefreshView()
	{
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				//Try to switch the active view to Perf.
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(PerfPlugin.VIEW_ID);
					PerfPlugin.getDefault().getProfileView().refreshModel();
				} catch (NullPointerException e) {
					e.printStackTrace();					
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
	}
}