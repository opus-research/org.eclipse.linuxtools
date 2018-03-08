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
package org.eclipse.linuxtools.internal.profiling.launch.provider;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.Messages;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class AbstractProviderPreferencesPage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage, IExecutableExtension {

	// Profiling type
	private String type;

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		Hashtable<String, String> parameters = (Hashtable<String, String>) data;
		String profilingType = parameters
				.get(ProviderProfileConstants.INIT_DATA_TYPE_KEY);

		if (profilingType == null) {
			profilingType = "";
		}

		setProfilingType(profilingType);
	}

	public AbstractProviderPreferencesPage() {
		super(GRID);
	}

	public void init(IWorkbench workbench) {
			final IPreferenceStore store = new ScopedPreferenceStore(
					ConfigurationScope.INSTANCE, type);
			setPreferenceStore(store);

	}

	public void initializeDefaultPreferences() {
			super.performDefaults();
			String providerId = ProviderFramework
					.getHighestProviderId(type);
			ConfigurationScope.INSTANCE.getNode(type)
					.put(ProviderProfileConstants.PREFS_KEY, providerId);

	}

	@Override
	protected void createFieldEditors() {
		String providerId = ProviderFramework
				.getHighestProviderId(type);
		
		getPreferenceStore().setDefault(ProviderProfileConstants.PREFS_KEY, providerId);

		HashMap<String, String> map = ProviderFramework
				.getProviderNamesForType(type);
		// 2d array containing launch provider names on the first column and
		// corresponding id's on the second.
		String[][] providerList = new String[map.size()][2];
		int i = 0;
		for (Entry<String, String> entry : map.entrySet()) {
			providerList[i][0] = entry.getKey();
			providerList[i][1] = entry.getValue();
			i++;
		}
		RadioGroupFieldEditor editor = new RadioGroupFieldEditor(
				ProviderProfileConstants.PREFS_KEY,
				Messages.ProviderPreferencesPage_1, 1, providerList,
				getFieldEditorParent());
		editor.setPreferenceStore(getPreferenceStore());
		addField(editor);

	}

	/**
	 * Set profiling type.
	 *
	 * @param profilingType String profiling type.
	 */
	private void setProfilingType(String profilingType) {
		type = profilingType;
	}
}
