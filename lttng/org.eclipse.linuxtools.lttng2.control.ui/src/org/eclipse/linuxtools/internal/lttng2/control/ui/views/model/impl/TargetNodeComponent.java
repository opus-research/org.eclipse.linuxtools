/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Bernd Hufmann - Updated for support of LTTng Tools 2.1
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl;

import static java.text.MessageFormat.format;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.linuxtools.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.property.TargetNodePropertySource;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.ICommandShell;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.IRemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote.RemoteSystemProxy;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.service.ILttngControlService;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.service.LTTngControlServiceFactory;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * <p>
 * Implementation of the trace node component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TargetNodeComponent extends TraceControlComponent implements IRemoteConnectionChangeListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Path to icon file for this component (state connected).
     */
    public static final String TARGET_NODE_CONNECTED_ICON_FILE = "icons/obj16/target_connected.gif"; //$NON-NLS-1$
    /**
     * Path to icon file for this component (state disconnected).
     */
    public static final String TARGET_NODE_DISCONNECTED_ICON_FILE = "icons/obj16/target_disconnected.gif"; //$NON-NLS-1$

    private static final ILttngControlService NULL_CONTROL_SERVICE = new NullControlService();

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The node connection state.
     */
    private TargetNodeState fState = TargetNodeState.DISCONNECTED;
    /**
     * The image to be displayed in state disconnected.
     */
    private Image fDisconnectedImage = null;
    /**
     * The connection implementation.
     */
    private IRemoteConnection fHost = null;
    /**
     * The remote proxy implementation.
     */
    private IRemoteSystemProxy fRemoteProxy = null;
    /**
     * The control service for LTTng specific commands.
     */
    private ILttngControlService fService = null;
    /**
     * The command shell for issuing commands.
     */
    private ICommandShell fShell = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param name - the name of the component
     * @param parent - the parent of the component
     * @param host - the host connection implementation
     * @param proxy - the remote proxy implementation
     */
    public TargetNodeComponent(String name, ITraceControlComponent parent, IRemoteConnection host, IRemoteSystemProxy proxy) {
        super(name, parent);
        setImage(TARGET_NODE_CONNECTED_ICON_FILE);
        fDisconnectedImage = Activator.getDefault().loadIcon(TARGET_NODE_DISCONNECTED_ICON_FILE);
        fHost = host;
        fRemoteProxy = proxy;
        fRemoteProxy.addConnectionChangeListener(this);
        setToolTip(fHost.getName());
    }

    /**
     * Constructor (using default proxy)
     * @param name - the name of the component
     * @param parent - the parent of the component
     * @param host - the host connection implementation
     */
    public TargetNodeComponent(String name, ITraceControlComponent parent, IRemoteConnection host) {
        this(name, parent, host, new RemoteSystemProxy(host));
    }

    @Override
    public void dispose() {
        fRemoteProxy.removeConnectionChangeListener(this);
        fRemoteProxy.dispose();
        disposeControlService();
    }

    private void disposeControlService() {
        fService = null;
        final ICommandShell shell = fShell;
        if (shell != null) {
            shell.disconnect();
            fShell = null;
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public Image getImage() {
        if (fState == TargetNodeState.CONNECTED) {
            return super.getImage();
        }
        return fDisconnectedImage;
    }

    @Override
    public TargetNodeState getTargetNodeState() {
        return fState;
    }

    @Override
    public void setTargetNodeState(TargetNodeState state) {
        fState = state;
        fireComponentChanged(TargetNodeComponent.this);
    }

    @Override
    public ILttngControlService getControlService() {
        return fService == null ? NULL_CONTROL_SERVICE : fService;
    }

    @Override
    public void setControlService(ILttngControlService service) {
        fService = service;
    }

    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new TargetNodePropertySource(this);
        }
        return null;
    }

    /**
     * @return the remote connection associated with this node
     */
    public IRemoteConnection getRemoteConnection() {
        return fHost;
    }

    /**
     * @return remote system proxy implementation
     */
    public IRemoteSystemProxy getRemoteSystemProxy() {
        return fRemoteProxy;
    }

    /**
     * @return all available sessions.
     */
    public TraceSessionComponent[] getSessions() {
        List<ITraceControlComponent> compenents = getChildren(TraceSessionGroup.class);
        if (compenents.size() > 0) {
            TraceSessionGroup group = (TraceSessionGroup)compenents.get(0);
            List<ITraceControlComponent> sessions = group.getChildren(TraceSessionComponent.class);
            return sessions.toArray(new TraceSessionComponent[sessions.size()]);
        }
        return new TraceSessionComponent[0];
    }

    /**
     * @return node version
     */
    public String getNodeVersion() {
        // Control service is null during connection to node
        if (getControlService() != NULL_CONTROL_SERVICE) {
            return getControlService().getVersionString();
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns if node supports filtering of events
     * @return <code>true</code> if node supports filtering else <code>false</code>
     */
    public boolean isEventFilteringSupported() {
        return getControlService().isVersionSupported("2.1.0"); //$NON-NLS-1$
    }

    /**
     * Returns if node supports networks streaming or not
     * @return <code>true</code> if node supports filtering else <code>false</code>
     *
     */
    public boolean isNetworkStreamingSupported() {
        return getControlService().isVersionSupported("2.1.0"); //$NON-NLS-1$
    }

    /**
     * Returns if node supports configuring buffer type  or not
     * @return <code>true</code> if node supports buffer type configuration else <code>false</code>
     */
    public boolean isBufferTypeConfigSupported() {
        return getControlService().isVersionSupported("2.2.0"); //$NON-NLS-1$
    }

    /**
     * Returns if node supports trace file rotation or not
     * @return <code>true</code> if node supports trace file rotation else <code>false</code>
     */
    public boolean isTraceFileRotationSupported() {
        return getControlService().isVersionSupported("2.2.0"); //$NON-NLS-1$
    }

    /**
     * Returns if node supports periodical flush for metadata or not
     * @return <code>true</code> if node supports periodical flush for metadata else <code>false</code>
     */
    public boolean isPeriodicalMetadataFlushSupported() {
        return getControlService().isVersionSupported("2.2.0"); //$NON-NLS-1$
    }
    /**
     * Returns if node supports snapshots or not
     * @return <code>true</code> if it supports snapshots else <code>false</code>
     *
     */
    public boolean isSnapshotSupported() {
        return getControlService().isVersionSupported("2.3.0"); //$NON-NLS-1$
    }
    /**
     * Returns if node supports live or not
     * @return <code>true</code> if it supports live else <code>false</code>
     *
     */
    public boolean isLiveSupported() {
        return getControlService().isVersionSupported("2.4.0"); //$NON-NLS-1$;
    }
    /**
     * Returns if node supports adding contexts on event
     * @return <code>true</code> if it supports adding contexts on events else <code>false</code>
     *
     */
    public boolean isContextOnEventSupported() {
        return !getControlService().isVersionSupported("2.2.0"); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void connectionChanged(IRemoteConnectionChangeEvent e) {
        if (fState == TargetNodeState.CONNECTING) {
            return;
        }

        switch (e.getType()) {
        case IRemoteConnectionChangeEvent.CONNECTION_CLOSED:
        case IRemoteConnectionChangeEvent.CONNECTION_ABORTED:
            handleDisconnected();
            break;
        case IRemoteConnectionChangeEvent.CONNECTION_OPENED:
            handleConnected();
            break;
        default:
            break;
        }
    }

    /**
     * Method to connect this node component to the remote target node.
     */
    public void connect() {
        if (fState == TargetNodeState.DISCONNECTED) {
            try {
                setTargetNodeState(TargetNodeState.CONNECTING);
                Job job = new Job(format(Messages.TraceControl_OpenConnectionTo, getName())) {
                    @Override
                    protected IStatus run(IProgressMonitor monitor) {
                        try {
                            fRemoteProxy.connect(monitor);
                            return Status.OK_STATUS;
                        } catch (Exception e) {
                            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_ConnectionFailure, e);
                        }
                    }
                };
                job.addJobChangeListener(new JobChangeAdapter() {
                    @Override
                    public void done(IJobChangeEvent event) {
                        IStatus status = event.getResult();
                        if (status.isOK()) {
                            handleConnected();
                        } else {
                            handleDisconnected();
                            if (status.getSeverity() != IStatus.CANCEL) {
                                Activator.getDefault().getLog().log(status);
                            }
                        }
                    }
                });
                job.schedule();
            } catch (Exception e) {
                setTargetNodeState(TargetNodeState.DISCONNECTED);
                Activator.getDefault().logError(Messages.TraceControl_ConnectionFailure + " (" + getName() + "). \n", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    /**
     * Method to disconnect this node component to the remote target node.
     */
    public void disconnect() {
        if (fState == TargetNodeState.CONNECTED) {
            try {
                setTargetNodeState(TargetNodeState.DISCONNECTING);
                fRemoteProxy.disconnect();
            } catch (Exception e) {
                Activator.getDefault().logError(Messages.TraceControl_DisconnectionFailure + " (" + getName() + "). \n", e); //$NON-NLS-1$ //$NON-NLS-2$
            } finally {
                handleDisconnected();
            }
        }
    }

    /**
     * Retrieves the trace configuration from the target node and populates the
     * information in the tree model. The execution is done in a own job.
     */
    public void getConfigurationFromNode() {
        Job job = new Job(Messages.TraceControl_RetrieveNodeConfigurationJob) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {

                try {
                    // Get provider information from node
                    TraceProviderGroup providerGroup = new TraceProviderGroup(Messages.TraceControl_ProviderDisplayName, TargetNodeComponent.this);
                    addChild(providerGroup);

                    // Get session information from node
                    TraceSessionGroup sessionGroup = new TraceSessionGroup(Messages.TraceControl_AllSessionsDisplayName, TargetNodeComponent.this);
                    addChild(sessionGroup);

                    providerGroup.getProviderFromNode(monitor);
                    sessionGroup.getSessionsFromNode(monitor);
                } catch (ExecutionException e) {
                    removeAllChildren();
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TraceControl_RetrieveNodeConfigurationFailure, e);
                }

                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();
    }

    /**
     * Refresh the node configuration
     */
    public void refresh() {
        removeAllChildren();
        getConfigurationFromNode();
    }

    // ------------------------------------------------------------------------
    // Helper function
    // ------------------------------------------------------------------------

    /**
     * @return returns the control service for LTTng specific commands.
     * @throws ExecutionException
     */
    private ILttngControlService createControlService() throws ExecutionException {
        if (fService == null) {
            try {
                fShell = fRemoteProxy.createCommandShell();
                fService = LTTngControlServiceFactory.getInstance().getLttngControlService(fShell);
            } catch (ExecutionException e) {
                disposeControlService();
                throw e;
            }
        }
        return fService;
    }

    /**
     * Handles the connected event.
     */
    private void handleConnected() {
        try {
            createControlService();
            getConfigurationFromNode();
            // Set connected only after the control service has been created and the jobs for creating the
            // sub-nodes are scheduled.
            setTargetNodeState(TargetNodeState.CONNECTED);
        } catch (final ExecutionException e) {
            // Disconnect only if no control service, otherwise stay connected.
            if (getControlService() == NULL_CONTROL_SERVICE) {
                fState = TargetNodeState.CONNECTED;
                disconnect();
            }

            // Notify user
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    ErrorDialog er = new ErrorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                            Messages.TraceControl_ErrorTitle, Messages.TraceControl_RetrieveNodeConfigurationFailure,
                            new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e),
                            IStatus.ERROR);
                    er.open();
                }
            });
            Activator.getDefault().logError(Messages.TraceControl_RetrieveNodeConfigurationFailure + " (" + getName() + "). \n", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Handles the disconnected event.
     */
    private void handleDisconnected() {
        disposeControlService();
        setTargetNodeState(TargetNodeState.DISCONNECTED);
        removeAllChildren();
    }

    @Override
    public void addChild(ITraceControlComponent component) {
        if (getTargetNodeState() == TargetNodeState.DISCONNECTED) {
            return;
        }
        super.addChild(component);
    }
}
