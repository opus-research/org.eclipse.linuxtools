/*******************************************************************************
 * Copyright (c) 2010, 2011 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *    Red Hat Inc. - rewrite to use RemoteConnection class
 *    Corey Ashford <cjashfor@us.ibm.com> - Modified for use with an RDT-based
 *                                          RemoteConnection class.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.remote.IRemoteConnectionConfigurationConstants;
import org.eclipse.cdt.launch.remote.RSEHelper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.profiling.launch.ConfigUtils;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.files.IFileService;
import org.eclipse.rse.services.files.IHostFile;
import org.osgi.framework.Version;

/**
 * @since 1.1
 */
public class ValgrindRemoteProxyLaunchDelegate extends ValgrindLaunchConfigurationDelegate {

    private static final String VALGRIND_CMD = "valgrind"; //$NON-NLS-1$

    private ConfigUtils configUtils;

    public ValgrindRemoteProxyLaunchDelegate() {
        super();
    }

    private static final String VERSION_OPT = "--version"; //$NON-NLS-1$

    private String whichVersion(IProject project) {
        String cmdArray[] = new String[2];
        cmdArray[0] = VALGRIND_CMD;
        cmdArray[1] = VERSION_OPT;

        try {

            Process p = RuntimeProcessFactory.getFactory().exec(cmdArray,
                    project);

            try (BufferedReader stdout = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                return stdout.readLine();
            }
        } catch (IOException e) {
            return null;
        }
    }

    private static final String VERSION_PREFIX = "valgrind-"; //$NON-NLS-1$
    private static final char VERSION_DELIMITER = '-';
    private static final Version MIN_VER = ValgrindLaunchPlugin.VER_3_3_0;

    private Version getValgrindVersion(IProject project) throws CoreException {
        Version valgrindVersion;
        String verString = whichVersion(project);

        if (verString == null || verString.isEmpty()){
            throw new CoreException(new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Couldn't_determine_version"))); //$NON-NLS-1$
        }

        verString = verString.replace(VERSION_PREFIX, ""); //$NON-NLS-1$

        if (verString.indexOf(VERSION_DELIMITER) > 0) {
            verString = verString.substring(0, verString.indexOf(VERSION_DELIMITER));
        }
        if (!verString.isEmpty()) {
            valgrindVersion = Version.parseVersion(verString);
        } else {
            throw new CoreException(new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Couldn't_determine_version"))); //$NON-NLS-1$
        }

        // check for minimum supported version
        if (valgrindVersion.compareTo(MIN_VER) < 0) {
            throw new CoreException(new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, NLS.bind(Messages.getString("ValgrindLaunchPlugin.Error_min_version"), valgrindVersion.toString(), MIN_VER.toString()))); //$NON-NLS-1$
        }
        return valgrindVersion;
    }

    @Override
    public void launch(final ILaunchConfiguration config, String mode,
            final ILaunch launch, IProgressMonitor m) throws CoreException {

        boolean remoteTmpDirectoryCreated = false;
        String tmpFolderName = null;
        IFileService fileService = null;

        if (m == null) {
            m = new NullProgressMonitor();
        } else {
            // check for cancellation
            if (m.isCanceled()) {
                return;
            }
        }

        SubMonitor monitor = SubMonitor.convert(m, 100);

        this.config = config;
        this.configUtils = new ConfigUtils(config);
        this.launch = launch;
        // tool that was launched
        this.toolID = getTool(config);
        // ask tool extension for arguments
        this.dynamicDelegate = getDynamicDelegate(toolID);

        if (!RSECorePlugin.isInitComplete(RSECorePlugin.INIT_MODEL)) {
            monitor.setTaskName(Messages.getString("ValgrindRemoteProxyLaunchDelegate.init_rse")); //$NON-NLS-1$
            try {
                RSECorePlugin.waitForInitCompletion(RSECorePlugin.INIT_MODEL);
            } catch (InterruptedException e) {
                throw new CoreException(new Status(IStatus.ERROR,
                        getPluginID(), IStatus.OK, e.getLocalizedMessage(), e));
            }
        }
        monitor.worked(2);
        try {
            IPath localOutputDir;

            // remove any output from previous run
            ValgrindUIPlugin.getDefault().resetView();
            // reset stored launch data
            getPlugin().setCurrentLaunchConfiguration(null);
            getPlugin().setCurrentLaunch(null);

            IProject project = configUtils.getProject();
            valgrindVersion = getValgrindVersion(project);

            fileService = (IFileService) RSEHelper
                    .getConnectedRemoteFileService(
                            RSEHelper.getCurrentConnection(config),
                            monitor.newChild(2));

            // Create empty tmp directory for log files on remote target
            tmpFolderName = createRemoteTmpDir(fileService, monitor);
            remoteTmpDirectoryCreated = true;

            // Create empty local output directory
            localOutputDir = createLocalOutputDir();

            // Kick off Valgrind in a a new process, this will download the
            // file to run valgrind against if skip download is not set.
            // The call will block and wait until valgrind has finished.
            runValgrind(monitor);

            // Retrieve the log file from the remote target.
            retrieveLogFiles(fileService, tmpFolderName, localOutputDir, monitor);

            // Parse the log files and display the results.
            processLogFiles(project, localOutputDir, monitor);


        } catch (CoreException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // remove remote log dir and all files under it (if created)
            if (fileService != null && tmpFolderName != null && remoteTmpDirectoryCreated) {
                try {
                    fileService.delete("/tmp/", //$NON-NLS-1$
                            tmpFolderName,
                            monitor.newChild(5));
                } catch (SystemMessageException e) {
                    e.printStackTrace();
                }
            }

            monitor.done();
        }
    }

    private String createRemoteTmpDir(IFileService fileService, SubMonitor monitor) throws SystemMessageException {
        // Create a directory on the remote target to store the Valgrind log
        // files into.
        String tmpFolderName = "eclipse-valgrind-" + System.currentTimeMillis(); //$NON-NLS-1$
        outputPath = Path.fromOSString("/tmp/" + tmpFolderName); //$NON-NLS-1$
        fileService.createFolder("/tmp/", //$NON-NLS-1$
                tmpFolderName,
                monitor.newChild(2));
        monitor.worked(2);

        return tmpFolderName;
    }


    private void setupArgs(StringBuffer allArgs, String remoteExePath) throws CoreException {
        String[] valgrindArgs = getValgrindArgumentsArray(config);
        String[] executableArgs = getProgramArgumentsArray(config);

        for (String valgrindArg : valgrindArgs) {
            allArgs.append(valgrindArg);
            allArgs.append(" "); //$NON-NLS-1$
        }
        allArgs.append(remoteExePath);
        allArgs.append(" "); //$NON-NLS-1$
        for (String executableArg : executableArgs) {
            allArgs.append(executableArg);
            allArgs.append(" "); //$NON-NLS-1$
        }
        System.out.println(allArgs.toString());
    }

    private IPath createLocalOutputDir() throws CoreException {
        try {
            IPath localOutputDir;
            IValgrindOutputDirectoryProvider provider =
                    getPlugin().getOutputDirectoryProvider();
            setOutputPath(config, provider.getOutputPath());

            localOutputDir = provider.getOutputPath();
            createDirectory(localOutputDir);

            return localOutputDir;
        } catch (IOException e2) {
            throw new CoreException(new Status(IStatus.ERROR,
                    ValgrindLaunchPlugin.PLUGIN_ID, IStatus.OK,
                    "", e2)); //$NON-NLS-1$
        }
    }

    private void runValgrind(SubMonitor monitor) throws CoreException, InterruptedException {
        StringBuffer allArgs = new StringBuffer(1024);

        IPath localExePath = CDebugUtils.verifyProgramPath(config);

        String remoteExePath = config.getAttribute(
                IRemoteConnectionConfigurationConstants.ATTR_REMOTE_PATH,
                ""); //$NON-NLS-1$

        String prelaunchCmd = config.getAttribute(
                IRemoteConnectionConfigurationConstants.ATTR_PRERUN_COMMANDS,
                ""); //$NON-NLS-1$

        setupArgs(allArgs, remoteExePath);

        // Download the binary to the remote before debugging. The RSEHelper
        // will check the skip download field in the config.
        if (localExePath != null) {
            monitor.setTaskName(Messages.getString("ValgrindRemoteProxyLaunchDelegate.download_exe")); //$NON-NLS-1$
            RSEHelper.remoteFileDownload(config, launch, localExePath.toString(),
                    remoteExePath, monitor.newChild(10));
        }

        monitor.setTaskName(Messages.getString("ValgrindRemoteProxyLaunchDelegate.start")); //$NON-NLS-1$
        Process remoteProcess = RSEHelper.remoteShellExec(config, prelaunchCmd,
                VALGRIND_CMD, allArgs.toString(), monitor.newChild(10));
        DebugPlugin.newProcess(launch, remoteProcess,
                renderProcessLabel(localExePath.toOSString()));

        // Wait until Valgrind has finished.
        int state = remoteProcess.waitFor();
        monitor.worked(10);

        monitor.setTaskName(Messages.getString("ValgrindRemoteProxyLaunchDelegate.process_log")); //$NON-NLS-1$
        if (state != IRemoteCommandLauncher.OK) {
            abort(Messages.getString("ValgrindLaunchConfigurationDelegate.Launch_exited_status") + " " //$NON-NLS-1$ //$NON-NLS-2$
                    + state + ". " + NLS.bind(Messages.getString("ValgrindRemoteProxyLaunchDelegate.see_reference"), "IRemoteCommandLauncher") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + "\n",  //$NON-NLS-1$
                    null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
        }
    }

    private void retrieveLogFiles(IFileService fileService, String tmpFolderName, IPath localOutputDir, SubMonitor monitor) throws SystemMessageException {
        // Retrieve the log files from the remote target and store on the
        // host ready for processing.
        IHostFile logFilesOnTarget[] = fileService.list(
                "/tmp/" + tmpFolderName, //$NON-NLS-1$
                 "*", //$NON-NLS-1$
                 IFileService.FILE_TYPE_FILES,
                 monitor.newChild(1));
        for (IHostFile logFile : logFilesOnTarget) {
            IPath localValgrindFile = localOutputDir.append(logFile.getName());
            // move remote log files to local directory
            fileService.download("/tmp/" + tmpFolderName, logFile.getName(), //$NON-NLS-1$
                    localValgrindFile.toFile(), false,
                    fileService.getEncoding(monitor.newChild(1)),
                    monitor.newChild(1));
        }
    }

    private void processLogFiles(IProject project, IPath localOutputDir, SubMonitor monitor) throws IOException, CoreException {
        //Store these for use by other classes
        getPlugin().setCurrentLaunchConfiguration(config);
        getPlugin().setCurrentLaunch(launch);

        // Parse Valgrind logs
        IValgrindMessage[] messages = parseLogs(localOutputDir);
        monitor.worked(20);

        // create launch summary string to distinguish this launch
        String valgrindPathString = RuntimeProcessFactory.getFactory().whichCommand(
                    VALGRIND_CMD, project);
        IPath valgrindFullPath = Path.fromOSString(valgrindPathString);
        launchStr = createLaunchStr(valgrindFullPath);

        // Create view
        ValgrindUIPlugin.getDefault().createView(launchStr, toolID);
        // Set log messages
        ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
        view.setMessages(messages);
        monitor.worked(5);

        // Pass off control to extender
        dynamicDelegate.handleLaunch(config, launch, localOutputDir,
                monitor.newChild(1));
        // Initialize tool-specific part of view
        dynamicDelegate.initializeView(view.getDynamicView(), launchStr,
                monitor.newChild(1));

        // refresh view
        ValgrindUIPlugin.getDefault().refreshView();
        // show view
        ValgrindUIPlugin.getDefault().showView();
        monitor.worked(5);
    }

    private String createLaunchStr(IPath valgrindPath) throws CoreException {
        IProject project = configUtils.getProject();
        URI projectURI = project.getLocationURI();

        String host = projectURI.getHost();

        // Host might be null since it's not needed for a well-formed URI. Try authority instead
        if(host == null){
            host = projectURI.getAuthority();
        }

        // If authority is also null, use a generic name
        String location;

        if(host == null){
            location = "remote host"; //$NON-NLS-1$
        } else {
            location = projectURI.getScheme() + "://" + host; //$NON-NLS-1$
        }

        return config.getName()
                + " [" + getPlugin().getToolName(toolID) + "]" + " " + valgrindPath.toString() + " on " + location; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    @Override
    protected String getPluginID() {
        return ValgrindLaunchPlugin.PLUGIN_ID;
    }
}
