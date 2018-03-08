/*******************************************************************************
 * Copyright (c) 2012 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class SystemTapScriptOptionsTab extends
		AbstractLaunchConfigurationTab {

	static final String SCRIPT_PATH_ATTR = "ScriptPath"; //$NON-NLS-1$
	static final String CURRENT_USER_ATTR = "executeAsCurrentUser"; //$NON-NLS-1$
	static final String USER_NAME_ATTR = "userName"; //$NON-NLS-1$
	static final String USER_PASS_ATTR = "userPassword"; //$NON-NLS-1$
	static final String LOCAL_HOST_ATTR = "executeOnLocalHost"; //$NON-NLS-1$
	static final String HOST_NAME_ATTR = "hostName"; //$NON-NLS-1$
	static final String RUN_WITH_CHART = "runWithChart"; //$NON-NLS-1$

	private Composite cmpChkBoxes = null;
	private Composite cmpTxtBoxes = null;
	private Button checkBox[] = new Button[IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length];
	private Label label[] = new Label[IDEPreferenceConstants.STAP_STRING_OPTIONS.length];
	private Text text[] = new Text[IDEPreferenceConstants.STAP_STRING_OPTIONS.length];

	@Override
	public void createControl(Composite parent) {

		GridLayout singleColumnGridLayout = new GridLayout();
		singleColumnGridLayout.numColumns = 1;
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		comp.setLayout(singleColumnGridLayout);
		comp.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true));

		// Check boxes
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		cmpChkBoxes = new Composite(comp, SWT.NONE);
		cmpChkBoxes.setLayout(gridLayout);
		cmpChkBoxes.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true));

		for(int i=0; i<IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length; i++) {
			checkBox[i] = new Button(cmpChkBoxes, SWT.CHECK);
			checkBox[i]
					.setText(IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.LABEL]
							+ " (" + IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.FLAG] + ")"); //$NON-NLS-1$//$NON-NLS-2$
			checkBox[i].addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateLaunchConfigurationDialog();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					updateLaunchConfigurationDialog();
				}
			});
		}

		// Labels and Text fields
		cmpTxtBoxes = new Composite(comp, SWT.NONE);
		cmpTxtBoxes.setLayout(gridLayout);
		cmpTxtBoxes.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true));

		for(int i=0; i < IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
			label[i] = new Label(cmpTxtBoxes, SWT.NONE);
			label[i].setText(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.FLAG] + " " + IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.LABEL]); //$NON-NLS-1$
			label[i].setBackground(cmpChkBoxes.getBackground());
			text[i] = new Text(cmpTxtBoxes, SWT.BORDER);
			text[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			text[i].addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					updateLaunchConfigurationDialog();
				}
			});
		}
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IPreferenceStore store = IDEPlugin.getDefault().getPreferenceStore();

		for(int i=0; i<IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length; i++) {
			configuration.setAttribute(IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.KEY],
					store.getBoolean(IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.KEY]));
		}

		for(int i=0; i<IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
			configuration.setAttribute(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.KEY],
					store.getString(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.KEY]));
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			// ide.stap.stapoptions
			for (int i = 0; i < IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length; i++) {
				checkBox[i].setSelection(configuration.getAttribute(
						IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.KEY], false));
			}

			for (int i = 0; i < IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
				text[i].setText(configuration.getAttribute(
						IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.KEY], "")); //$NON-NLS-1$
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// ide.stap.stapoptions
		for (int i = 0; i < IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length; i++) {
			configuration.setAttribute(IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.KEY], checkBox[i].getSelection());
		}

		for (int i = 0; i < IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
			configuration.setAttribute(
					IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.KEY], text[i].getText());
		}
	}

	@Override
	public String getName() {
		return Messages.SystemTapScriptLaunchConfigurationTab_9;
	}
}
