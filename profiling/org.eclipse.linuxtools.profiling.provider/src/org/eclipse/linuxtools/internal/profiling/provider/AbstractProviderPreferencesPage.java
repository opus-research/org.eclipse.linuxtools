package org.eclipse.linuxtools.internal.profiling.provider;

import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.linuxtools.internal.profiling.provider.launch.Messages;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchShortcut;

public abstract class AbstractProviderPreferencesPage extends
		FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static IScopeContext configScopeInstance = ConfigurationScope.INSTANCE;

	public AbstractProviderPreferencesPage() {
		super(GRID);
	}

	public void init(IWorkbench workbench) {
		if (!getProfilingType().equals("generic")) {
			final IPreferenceStore store = new ScopedPreferenceStore(
					configScopeInstance, getPluginID());
			setPreferenceStore(store);
			//setDescription(Messages.ProviderPreferencesPage_0);
		} else {
			setDescription("Profiling Preferences");
		}
	}

	public void initializeDefaultPreferences() {
		if (!getProfilingType().equals("generic")) {
			super.performDefaults();
			String providerId = ProfileLaunchShortcut
					.getDefaultLaunchShortcutProviderId(getProfilingType());
			configScopeInstance.getNode(getPluginID())
					.put(getKey(), providerId);
		}
	}

	@Override
	protected void createFieldEditors() {
		if (!getProfilingType().equals("generic")) {
			HashMap<String, String> map = ProfileLaunchConfigurationTabGroup
					.getProviderNamesForType(getProfilingType());
			// 2d array containing launch provider names on the first column and
			// corresponding id's on the second.
			String[][] providerList = new String[map.size()][2];
			int i = 0;
			for (Entry<String, String> entry : map.entrySet()) {
				providerList[i][0] = entry.getKey();
				providerList[i][1] = entry.getValue();
				i++;
			}
			RadioGroupFieldEditor editor = new RadioGroupFieldEditor(getKey(),
					Messages.ProviderPreferencesPage_1, 1, providerList,
					getFieldEditorParent());
			addField(editor);
		}
	}

	/**
	 * Get configuration scope.
	 *
	 * @return unique launch provider identifier.
	 * @since 1.2
	 */
	public static IScopeContext getConfigurationScope() {
		return configScopeInstance;
	}

	/**
	 * Get preferences configuration scope node key.
	 *
	 * @return String unique key to access preferences configuration scope node.
	 */
	protected abstract String getKey();

	/**
	 * Get plug-in id.
	 *
	 * @return String unique id of this plug-in.
	 */
	protected abstract String getPluginID();

	/**
	 * Get profiling type of this plug-in.
	 *
	 * @return String profiling type this plug-in supports.
	 */
	protected abstract String getProfilingType();

}
