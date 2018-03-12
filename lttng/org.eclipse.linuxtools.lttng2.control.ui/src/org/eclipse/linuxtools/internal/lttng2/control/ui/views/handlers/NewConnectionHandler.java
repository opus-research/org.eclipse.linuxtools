/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann               - Initial API and implementation
 *   Anna Dushistova(Montavista) - [382684] Allow reusing already defined connections that have Files and Shells subsystems
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.handlers;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.Workaround_Bug449362;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.INewConnectionDialog;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs.TraceControlDialogFactory;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <p>
 * Command handler for creation new connection for trace control.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class NewConnectionHandler extends BaseControlViewHandler {

    private static final String PARAMETER_REMOTE_SERVICES_ID = "org.eclipse.linuxtools.lttng2.control.ui.remoteServicesIdParameter"; //$NON-NLS-1$
    private static final String PARAMETER_CONNECTION_NAME = "org.eclipse.linuxtools.lttng2.control.ui.connectionNameParameter"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The parent trace control component the new node will be added to.
     */
    private ITraceControlComponent fRoot = null;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        assert (fRoot != null);

        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return false;
        }

        IRemoteConnection connection = getConnection(event.getParameters());
        if (connection != null) {
            fLock.lock();
            try {
                // successful creation of host
                TargetNodeComponent node = null;
                if (!fRoot.containsChild(connection.getName())) {
                    node = new TargetNodeComponent(connection.getName(), fRoot, connection);
                    fRoot.addChild(node);
                } else {
                    node = (TargetNodeComponent)fRoot.getChild(connection.getName());
                }

                node.connect();
            } finally {
                fLock.unlock();
            }
        }
        return null;
    }

    private static IRemoteConnection getConnection(Map<?,?> parameters) {
        // First check whether arguments have been supplied
        Object remoteServicesId = parameters.get(PARAMETER_REMOTE_SERVICES_ID);
        Object connectionName = parameters.get(PARAMETER_CONNECTION_NAME);
        if (remoteServicesId != null && connectionName != null) {
            if (!Workaround_Bug449362.triggerRSEStartup(remoteServicesId.toString())) {
                // Skip the connection in order to avoid an infinite loop
            } else {
                IRemoteServices rs = RemoteServices.getRemoteServices(remoteServicesId.toString());
                if (rs != null) {
                    return rs.getConnectionManager().getConnection(connectionName.toString());
                }
            }
            return null;
        }

        // Without the arguments, open dialog box for the node name and address
        final INewConnectionDialog dialog = TraceControlDialogFactory.getInstance().getNewConnectionDialog();
        if (dialog.open() == Window.OK) {
            return dialog.getConnection();
        }

        return null;
    }

    @Override
    public boolean isEnabled() {

        // Get workbench page for the Control View
        IWorkbenchPage page = getWorkbenchPage();
        if (page == null) {
            return false;
        }

        ITraceControlComponent root = null;

        // no need to verify part because it has been already done in getWorkbenchPage()
        IWorkbenchPart part = page.getActivePart();
        root = ((ControlView) part).getTraceControlRoot();

        boolean isEnabled = root != null;

        fLock.lock();
        try {
            fRoot = null;
            if (isEnabled) {
                fRoot = root;
            }
        } finally {
            fLock.unlock();
        }

        return isEnabled;
    }
}
