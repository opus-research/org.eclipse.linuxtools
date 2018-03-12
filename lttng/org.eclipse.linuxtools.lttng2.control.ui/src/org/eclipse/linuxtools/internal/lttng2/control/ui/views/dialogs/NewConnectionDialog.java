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
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.dialogs;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.linuxtools.internal.lttng2.control.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.remote.ui.IRemoteUIServices;
import org.eclipse.remote.ui.RemoteUIServices;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * <p>
 * Dialog box for connection information.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class NewConnectionDialog extends Dialog implements INewConnectionDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String TARGET_NEW_CONNECTION_ICON_FILE = "icons/elcl16/target_add.gif"; //$NON-NLS-1$

    private static final String REMOTE_CONNECTIONS_PREF_ID = "org.eclipse.remote.connections"; //$NON-NLS-1$
    private static String[] fRemoteServicesIDs;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The host combo box.
     */
    private TableViewer fConnectionTable = null;
    /**
     * The push button for creating a new connection.
     */
    private Button fNewButton = null;
    /**
     * The push button for creating a new connection.
     */
    private Button fEditButton = null;

    /**
     * Input list of existing remote hosts available for selection.
     */
    private IRemoteConnection[] fConnections;

    private IRemoteConnection fConnection;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     *
     * @param shell
     *            The shell
     */
    public NewConnectionDialog(Shell shell) {
        super(shell);
        setShellStyle(SWT.RESIZE | getShellStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_NewDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(TARGET_NEW_CONNECTION_ICON_FILE));
    }

    @Override
    protected Control createContents(Composite parent) {
        Control result = super.createContents(parent);
        fillTable();
        return result;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        // Main dialog panel
        Composite dialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        dialogComposite.setLayout(layout);
        dialogComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        new Label(dialogComposite, SWT.NONE).setText(Messages.TraceControl_NewNodeExistingConnectionGroupName);

        // Existing connections group
        GridData gd;
        fConnectionTable= new TableViewer(dialogComposite);
        fConnectionTable.getTable().setLayoutData(gd= new GridData(SWT.FILL, SWT.FILL, true, true));
        gd.widthHint = convertWidthInCharsToPixels(40);
        gd.heightHint = convertHeightInCharsToPixels(10);
        fConnectionTable.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                IRemoteConnection rc = (IRemoteConnection) element;
                return rc.getName() + " - " + rc.getAddress(); //$NON-NLS-1$
            }
        });
        fConnectionTable.setContentProvider(new ArrayContentProvider());
        fConnectionTable.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                onSelectionChanged();
            }
        });
        fConnectionTable.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });

        Composite buttons= new Composite(dialogComposite, SWT.NONE);
        layout= new GridLayout(3, true);
        layout.marginHeight = layout.marginWidth = 0;
        buttons.setLayout(layout);
        buttons.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

        new Label(buttons, SWT.NONE);

        fEditButton = new Button(buttons, SWT.PUSH);
        fEditButton.setText(Messages.TraceControl_NewNodeEditButtonName);
        setButtonLayoutData(fEditButton);
        fEditButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onEditConnection();
            }
        });

        fNewButton = new Button(buttons, SWT.PUSH);
        fNewButton.setText(Messages.TraceControl_NewNodeCreateButtonText);
        setButtonLayoutData(fNewButton);
        fNewButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onNewConnection();
            }
        });

        return dialogComposite;
    }

    private void fillTable() {
        setConnection();

        fConnections= getRemoteConnections();
        boolean haveItems = fConnections.length > 0;
        fConnectionTable.setInput(Arrays.asList(fConnections));
        if (haveItems && fConnectionTable.getSelection().isEmpty()) {
            fConnectionTable.setSelection(new StructuredSelection(fConnections[0]));
        }
    }

    void onSelectionChanged() {
        setConnection();
        getButton(OK).setEnabled(fConnection != null);
        fEditButton.setEnabled(canEdit(fConnection));
    }

    private static boolean canEdit(IRemoteConnection conn) {
        if (conn == null) {
            return false;
        }
        IRemoteServices rs = conn.getRemoteServices();
        return (rs.getCapabilities() & IRemoteServices.CAPABILITY_EDIT_CONNECTIONS) != 0;
    }

    void onNewConnection() {
        PreferenceDialog dlg = PreferencesUtil.createPreferenceDialogOn(getShell(), REMOTE_CONNECTIONS_PREF_ID, new String[] { REMOTE_CONNECTIONS_PREF_ID }, null);
        dlg.open();
        fillTable();
    }

    void onEditConnection() {
        setConnection();
        if (fConnection != null) {
            IRemoteUIServices ui = RemoteUIServices.getRemoteUIServices(fConnection.getRemoteServices());
            if (ui != null) {
                IRemoteUIConnectionManager connManager = ui.getUIConnectionManager();
                if (connManager != null) {
                    IRemoteUIConnectionWizard wiz = connManager.getConnectionWizard(getShell());
                    wiz.setConnection(fConnection.getWorkingCopy());
                    IRemoteConnectionWorkingCopy result = wiz.open();
                    if (result != null) {
                        result.save();
                        fConnectionTable.refresh();
                    }
                }
            }
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {
        setConnection();
        if (fConnection != null) {
            super.okPressed();
        }
    }

    private void setConnection() {
        IStructuredSelection ss= (IStructuredSelection) fConnectionTable.getSelection();
        fConnection= (IRemoteConnection) ss.getFirstElement();
    }

    @Override
    public IRemoteConnection getConnection() {
        return fConnection;
    }


    IRemoteConnection[] getRemoteConnections() {
        List<IRemoteConnection> result= new ArrayList<>();
        for(String id : getRemoteServicesIDs()) {
            addConnections(RemoteServices.getRemoteServices(id), result);
        }
        Collections.sort(result, new Comparator<IRemoteConnection>() {
            @Override
            public int compare(IRemoteConnection o1, IRemoteConnection o2) {
                return Collator.getInstance().compare(o1.getName(), o2.getName());
            }
        });
        return result.toArray(new IRemoteConnection[result.size()]);
    }

    private static String[] getRemoteServicesIDs() {
        if (fRemoteServicesIDs == null) {
            IExtensionRegistry registry = Platform.getExtensionRegistry();
            IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.remote.core", "remoteServices"); //$NON-NLS-1$ //$NON-NLS-2$
            List<String> result= new ArrayList<>();
            for (IConfigurationElement ce : extensionPoint.getConfigurationElements()) {
                String id= ce.getAttribute("id"); //$NON-NLS-1$
                if (!result.contains(id)) {
                    result.add(id);
                }
            }
            Collections.sort(result);
            fRemoteServicesIDs= result.toArray(new String[result.size()]);
        }
        return fRemoteServicesIDs;
    }

    private static void addConnections(IRemoteServices rs, List<IRemoteConnection> result) {
        if (rs != null) {
            result.addAll(rs.getConnectionManager().getConnections());
        }
    }
}
