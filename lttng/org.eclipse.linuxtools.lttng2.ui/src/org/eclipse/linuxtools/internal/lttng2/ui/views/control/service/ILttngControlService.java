/**********************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.service;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IBaseEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.ISessionInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IUstProviderInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.LogLevelType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;


/**
* <p>
* Interface for LTTng trace control command service.
* </p>
*
* @author Bernd Hufmann
*/
public interface ILttngControlService {

    /**
     * @return the version string.
     */
    public String getVersion();

    /**
     * Checks if given version is supported by this ILTTngControlService implementation.
     *
     * @param version The version to check
     * @return <code>true</code> if version is supported else <code>false</code>
     */
    public boolean isVersionSupported(String version);

    /**
     * Retrieves the existing sessions names from the node.
     *
     * @param monitor
     *            - a progress monitor
     * @return an array with session names.
     * @throws ExecutionException
     *             If the command fails
     */
    public String[] getSessionNames(IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Retrieves the session information with the given name the node.
     *
     * @param sessionName
     *            - the session name
     * @param monitor
     *            - a progress monitor
     * @return session information
     * @throws ExecutionException
     *             If the command fails
     */
    public ISessionInfo getSession(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Retrieves the kernel provider information (i.e. the kernel events)
     *
     * @param monitor
     *            - a progress monitor
     * @return the list of existing kernel events.
     * @throws ExecutionException
     *             If the command fails
     */
    public List<IBaseEventInfo> getKernelProvider(IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Retrieves the UST provider information from the node.
     *
     * @return - the UST provider information.
     * @throws ExecutionException
     *             If the command fails
     */
    public List<IUstProviderInfo> getUstProvider() throws ExecutionException;

    /**
     * Retrieves the UST provider information from the node.
     *
     * @param monitor
     *            - a progress monitor
     * @return the UST provider information.
     * @throws ExecutionException
     *             If the command fails
     */
    public List<IUstProviderInfo> getUstProvider(IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Creates a session with given session name and location.
     *
     * @param sessionName
     *            - a session name to create
     * @param sessionPath
     *            - a path for storing the traces (use null for default)
     * @param monitor
     *            - a progress monitor
     * @return the session information
     * @throws ExecutionException
     *             If the command fails
     */
    public ISessionInfo createSession(String sessionName, String sessionPath,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * Destroys a session with given session name.
     *
     * @param sessionName
     *            - a session name to destroy
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void destroySession(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Starts a session with given session name.
     *
     * @param sessionName
     *            - a session name to start
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void startSession(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Stops a session with given session name.
     *
     * @param sessionName
     *            - a session name to stop
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void stopSession(String sessionName, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Enables a list of channels for given session and given channel
     * information (configuration).
     *
     * @param sessionName
     *            - a session name to create
     * @param channelNames
     *            - a list of channel names to be enabled
     * @param isKernel
     *            - a flag to indicate Kernel or UST (true for Kernel, false for
     *            UST)
     * @param info
     *            - channel information used for creation of a channel (or null
     *            for default)
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableChannels(String sessionName, List<String> channelNames,
            boolean isKernel, IChannelInfo info, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Disables a list of channels for given session and given channel
     * information (configuration).
     *
     * @param sessionName
     *            - a session name to create
     * @param channelNames
     *            - a list of channel names to be enabled
     * @param isKernel
     *            - a flag to indicate Kernel or UST (true for Kernel, false for
     *            UST)
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void disableChannels(String sessionName, List<String> channelNames,
            boolean isKernel, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Enables a list of events with no additional parameters.
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name or null for default channel
     * @param eventNames
     *            - a list of event names to be enabled, or null (list of size =
     *            0)for all events .
     * @param isKernel
     *            - a flag for indicating kernel or UST.
     * @param filterExpression
     *            - a filter expression
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableEvents(String sessionName, String channelName,
            List<String> eventNames, boolean isKernel, String filterExpression,
            IProgressMonitor monitor)
            throws ExecutionException;


    /**
     * Enables all syscall events.
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name or null for default channel
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableSyscalls(String sessionName, String channelName,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables a dynamic probe or dynamic function entry/return probe.
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name or null for default channel
     * @param eventName
     *            - a event name
     * @param isFunction
     *            - true for dynamic function entry/return probe else false
     * @param probe
     *            - a dynamic probe information
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableProbe(String sessionName, String channelName,
            String eventName, boolean isFunction, String probe,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * Enables events using log level
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name (null for default channel)
     * @param eventName
     *            - a event name
     * @param logLevelType
     *            - a log level type
     * @param level
     *            - a log level
     * @param filterExpression
     *            - a filter expression
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void enableLogLevel(String sessionName, String channelName,
            String eventName, LogLevelType logLevelType, TraceLogLevel level,
            String filterExpression,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * Disables a list of events with no additional parameters.
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name (null for default channel)
     * @param eventNames
     *            - a list of event names to enabled.
     * @param isKernel
     *            - a flag for indicating kernel or UST.
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void disableEvent(String sessionName, String channelName,
            List<String> eventNames, boolean isKernel, IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Gets all available context names to be added to channels/events.
     *
     * @param monitor
     *            The progress monitor
     * @return the list of available contexts
     * @throws ExecutionException
     *             If the command fails
     */
    public List<String> getContextList(IProgressMonitor monitor)
            throws ExecutionException;

    /**
     * Add contexts to given channels and or events
     *
     * @param sessionName
     *            - a session name
     * @param channelName
     *            - a channel name (null for all channels)
     * @param eventName
     *            - a event name (null for all events)
     * @param isKernel
     *            - a flag for indicating kernel or UST.
     * @param contexts
     *            - a list of name of contexts to add
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void addContexts(String sessionName, String channelName,
            String eventName, boolean isKernel, List<String> contexts,
            IProgressMonitor monitor) throws ExecutionException;

    /**
     * Executes calibrate command to quantify LTTng overhead.
     *
     * @param isKernel
     *            - a flag for indicating kernel or UST.
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void calibrate(boolean isKernel, IProgressMonitor monitor)
            throws ExecutionException;
}
