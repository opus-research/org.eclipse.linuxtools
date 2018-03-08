/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.provider;

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.linuxtools.internal.profiling.provider.launch.Messages;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public abstract class AbstractProviderPropertyPage extends PropertyPage {
	
	protected abstract String getType();
	protected abstract String getPrefPageId();
	
	private IAdaptable element = null;

	private RadioGroupFieldEditor projectSettingsGroup;
	private Composite composite;
	private Link fLink;
	private Button useProjectSetting;

	@Override
	protected Control createContents(final Composite parent) {
		composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout( new GridLayout(2, false));
		
		getPreferenceStore().setDefault(ProviderProfileConstants.USE_PROJECT_SETTINGS + getType(), false);
		getPreferenceStore().setDefault(ProviderProfileConstants.PREFS_KEY + getType(),
				ProfileLaunchConfigurationTabGroup.getHighestProviderId(getType()));

		useProjectSetting = new Button(composite, SWT.CHECK);
		useProjectSetting.setText(Messages.UseProjectSetting_0);
		useProjectSetting.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		useProjectSetting.setSelection(getPreferenceStore().getBoolean(ProviderProfileConstants.USE_PROJECT_SETTINGS + getType()));
		useProjectSetting.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateOptionsEnable();
			}
		});

		fLink= new Link(composite, SWT.NULL);
		fLink.setText(Messages.PreferenceLink_0);
		fLink.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));
		fLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), getPrefPageId(), null, null).open();
			}
		});
		

		HashMap<String, String> map = ProfileLaunchConfigurationTabGroup
				.getProviderNamesForType(getType());
		// 2d array containing launch provider names on the first column and
		// corresponding id's on the second.
		String[][] providerList = new String[map.size()][2];
		int i = 0;
		for (Entry<String, String> entry : map.entrySet()) {
			providerList[i][0] = entry.getKey();
			providerList[i][1] = entry.getValue();
			i++;
		}
		projectSettingsGroup = new RadioGroupFieldEditor(
			ProviderProfileConstants.PREFS_KEY + getType(),
			Messages.ProviderPreferencesPage_1, 1, providerList,
			composite, true);
		projectSettingsGroup.setPage(this);
		projectSettingsGroup.setPreferenceStore(getPreferenceStore());
		projectSettingsGroup.load();

		updateOptionsEnable();
		
		return composite;
	}
	
	private void updateOptionsEnable() {
		if (useProjectSetting.getSelection() == true) {
			projectSettingsGroup.setEnabled(true, composite);
			projectSettingsGroup.getRadioBoxControl(composite).setEnabled(true);
			fLink.setVisible(false);
		} else {
			projectSettingsGroup.getRadioBoxControl(composite).setEnabled(false);
			projectSettingsGroup.setEnabled(false, composite);
			fLink.setVisible(true);
		}
	}

	@Override
	protected void performDefaults() {
		if (useProjectSetting.getSelection() == true)
			projectSettingsGroup.loadDefault();
		updateOptionsEnable();
	}
	
	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(ProviderProfileConstants.USE_PROJECT_SETTINGS + getType(), useProjectSetting.getSelection());
		projectSettingsGroup.store();
		return super.performOk();
	}

	@Override
	protected void performApply() {
		getPreferenceStore().setValue(ProviderProfileConstants.USE_PROJECT_SETTINGS + getType(), useProjectSetting.getSelection());
		projectSettingsGroup.store();
		super.performApply();
	}

	/**
	 * Receives the object that owns the properties shown in this property page.
	 *
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public void setElement(IAdaptable element) {
		this.element = element;
		IAdaptable e = getElement();
		if (e != null) {
			setPreferenceStore(new ScopedPreferenceStore(
						new ProjectScope(((IResource)e).getProject()),
						ProviderProfileConstants.PLUGIN_ID));
		}
	}

	/**
	 * Delivers the object that owns the properties shown in this property page.
	 *
	 * @see org.eclipse.ui.IWorkbenchPropertyPage#getElement()
	 */
	@Override
	public IAdaptable getElement() {
		if (element == null)
			return element;
		if (!(element instanceof IResource))
			return (IAdaptable) element.getAdapter(IResource.class);
		return element;
	}

}
