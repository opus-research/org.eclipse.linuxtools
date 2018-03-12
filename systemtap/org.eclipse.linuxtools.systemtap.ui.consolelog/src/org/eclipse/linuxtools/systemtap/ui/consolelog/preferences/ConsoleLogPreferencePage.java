/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Roberto Oliveira
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.linuxtools.internal.profiling.launch.ui.rdt.proxy.RDTConnection;
import org.eclipse.linuxtools.internal.systemtap.ui.consolelog.preferences.Messages;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ConsoleLogPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private final String REMOTE_PREFERENCES_ID = "org.eclipse.remote.connections"; //$NON-NLS-1$
	private Combo cConnections;

	public ConsoleLogPreferencePage() {
		setPreferenceStore(ConsoleLogPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.ConsoleLogPreferencePage_PreferencesTitle);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);

		Label lbl = new Label(composite, SWT.NONE);
		lbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		lbl.setText(Messages.ConsoleLogPreferencePage_Connection);

		cConnections = new Combo(composite, SWT.READ_ONLY);
		updateConnectionsCombo();

		Button btConn = new Button(composite, SWT.NONE);
		btConn.setText(Messages.ConsoleLogPreferencePage_NewConnection);
		btConn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(getShell(), REMOTE_PREFERENCES_ID,
						null, null);
				pref.open();
			}
		});
		return composite;
	}

	@Override
	public void init(IWorkbench arg0) {
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(ConsoleLogPreferenceConstants.CONNECTION_NAME, cConnections.getText());
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		cConnections.setText(getPreferenceStore().getDefaultString(ConsoleLogPreferenceConstants.CONNECTION_NAME));
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		updateConnectionsCombo();
	}

	private void updateConnectionsCombo() {
		List<String> connectionsList = new ArrayList<>();
		connectionsList = RDTConnection.getInstance().getConnectionsName();
		String[] values = new String[connectionsList.size()];
		for (int i = 0; i < values.length; i++) {
			values[i] = connectionsList.get(i);
		}
		cConnections.setItems(values);
		cConnections.setText(getPreferenceStore().getString(ConsoleLogPreferenceConstants.CONNECTION_NAME));
		cConnections.getParent().layout();
	}
}