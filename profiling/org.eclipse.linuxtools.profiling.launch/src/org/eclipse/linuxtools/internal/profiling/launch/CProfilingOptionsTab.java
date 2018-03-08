/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.profiling.launch.provider.ProviderProfileConstants;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;

// Special profiling options tab to use with the org.eclipse.cdt.launch.profilingProvider extension
// to extend the Local C/C++ Application configuration to handle profiling.  We do not rename
// the configuration as done in the normal profiling framework since it belongs to the CDT.
/**
 * @since 2.0
 */
public class CProfilingOptionsTab extends CLaunchConfigurationTab {

	String type;
	String name;
	Composite top;
	Combo providerCombo;
	AbstractLaunchConfigurationTab[] tabs;
	ILaunchConfiguration initial;
	SortedMap<String, String> comboItems;
	CTabFolder tabgroup;
	Image img;
	String defaultType;

	// if tabs are being initialized do not call performApply()
	HashMap<String, Boolean> initialized = new HashMap<String, Boolean> ();

	/**
	 * ProviderOptionsTab constructor.
	 *
	 * @param profilingType String type of profiling this tab will be used for.
	 * @param profilingName String name of this tab to be displayed.
	 */
	public CProfilingOptionsTab() {
		name = Messages.ProfilingTabName;
	}

	public void createControl(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout(1, true));
		
		providerCombo = new Combo(top, SWT.READ_ONLY);
		comboItems = ProviderFramework
				.getAllProviderNames();
		Set<String> providerNames = comboItems.keySet();
		providerCombo.setItems(providerNames.toArray(new String[0]));

		tabgroup = new CTabFolder(top, SWT.NONE);
		tabgroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));

		providerCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String curProviderId = comboItems.get(providerCombo.getText());
				loadTabGroupItems(tabgroup, curProviderId);
				initializeFrom(initial);
				// Since we are calling initializeFrom manually, we have to
				// update the launch configuration dialog manually to ensure
				// initial validation on the configuration.
				updateLaunchConfigurationDialog();
				top.layout();
			}
		});
	}

	public void loadTabGroupItems(CTabFolder tabgroup, String curProviderId) {
		// dispose of old tabs and their state
		for (CTabItem item : tabgroup.getItems()) {
			item.dispose();
		}
		setErrorMessage(null);
		initialized.clear();

		ProfileLaunchConfigurationTabGroup tabGroupConfig;

		if (curProviderId == null || "".equals(curProviderId)) {
			// get the id of a provider
			if (defaultType == null) {
				String[] categories = ProviderFramework.getProviderCategories();
				if (categories.length == 0) {
					setErrorMessage(Messages.ProfilingTab_no_profilers_installed);
					return;
				}
				for (String category : categories) {
					// Give precedence to timing category if present
					if (category.equals("timing")){ //$NON-NLS-1$
						defaultType = "timing"; //$NON-NLS-1$
					}
				}
				// if default category still not set, take first one found
				if (defaultType == null)
					defaultType = categories[0];
			}
			curProviderId = ProviderFramework.getProviderIdToRun(null, defaultType);
		}

		// starting initialization of this tab's controls
		initialized.put(curProviderId, false);

		tabGroupConfig = ProviderFramework.getTabGroupProviderFromId(curProviderId);
		if (tabGroupConfig == null) {
			String profilingToolName = null;
			try {
				profilingToolName = initial.getAttribute(ProviderProfileConstants.PROVIDER_CONFIG_TOOLNAME_ATT, (String)null);
			} catch (CoreException e) {
				// do nothing
			}
			if (profilingToolName == null)
				setErrorMessage(NLS.bind(Messages.ProfilingTab_specified_providerid_not_installed, curProviderId));
			else
				setErrorMessage(NLS.bind(Messages.ProfilingTab_specified_profiler_not_installed, profilingToolName));
			return;
		}
		tabs = tabGroupConfig.getProfileTabs();
		setProvider(curProviderId);

		// Show provider name in combo.
		int itemIndex = getComboItemIndexFromId(curProviderId);
		providerCombo.select(itemIndex);
		
		// create the tab item, and load the specified tab inside
		for (ILaunchConfigurationTab tab : tabs) {
			tab.setLaunchConfigurationDialog(getLaunchConfigurationDialog());
			CTabItem item = new CTabItem(tabgroup, SWT.NONE);
			item.setText(tab.getName());
			item.setImage(tab.getImage());

			tab.createControl(tabgroup);
			item.setControl(tab.getControl());
			tabgroup.setSelection(0);
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		if (providerCombo != null && !providerCombo.getText().equals("")) {
			for (AbstractLaunchConfigurationTab tab : tabs) {
				tab.setDefaults(configuration);
			}
		}
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		/**
		 * First time the configuration is selected.
		 *
		 * This is a cheap way to get access to the launch configuration. Our
		 * tabs are loaded dynamically, so the tab group doesn't "know" about
		 * them. We get access to this launch configuration to ensure that we
		 * can properly load the widgets the first time.
		 */

		// update current configuration (initial) with configuration being
		// passed in
		initial = configuration;


		// check if there exists a launch provider id in the configuration
		if (initial != null) {
			try {
				String providerId = initial.getAttribute(
						ProviderProfileConstants.PROVIDER_CONFIG_ATT, (String)null);
				// load provider corresponding to specified ids
				loadTabGroupItems(tabgroup, providerId);
			} catch (CoreException e) {
				// continue, initialize tabs
			}
		}
		if (tabs != null) {
			for (AbstractLaunchConfigurationTab tab : tabs) {
				tab.initializeFrom(configuration);
			}
		}

		// finished initialization
		initialized.put(getProviderId(), true);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		// make sure tabs are not null, and the tab's controls have been
		// initialized.

		Boolean isInitialized = initialized.get(getProviderId());
		isInitialized = (isInitialized != null) ? isInitialized : false;

		if (tabs != null && isInitialized) {
			for (AbstractLaunchConfigurationTab tab : tabs) {
				tab.performApply(configuration);
			}
		}
	}

	/**
	 * Set the provider attribute in the specified configuration.
	 *
	 * @param configuration a configuration
	 */
	private void setProvider(String providerId) {
		try {
			ILaunchConfigurationWorkingCopy wc = initial.getWorkingCopy();
			wc.setAttribute(ProviderProfileConstants.PROVIDER_CONFIG_ATT,
					providerId);
			initial = wc.doSave();
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Get the provider ID for the provider of the currently loaded
	 * configuration.
	 *
	 * @return the provider ID or an empty string if the configuration
	 * has no provider ID defined.
	 */
	private String getProviderId() {
		try {
			return initial.getAttribute(
					ProviderProfileConstants.PROVIDER_CONFIG_ATT, "");
		} catch (CoreException e) {
			return "";
		}
	}

	/**
	 * Get Combo item name from specified id
	 *
	 * @param id provider id
	 * @return name of item, <code>null</code> if no entry found with given id.
	 */
	private String getComboItemNameFromId(String id) {
		for (Entry<String, String> entry : comboItems.entrySet()) {
			if (id.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Get index of specific name in the combo items list
	 *
	 * @param name name of item
	 * @return index of given name, -1 if it not found
	 */
	private int getItemIndex(Combo combo, String name) {
		int itemCount = combo.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			if (combo.getItem(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get index of specific id in the provider combo items list
	 *
	 * @param id
	 * @return index of given id in provider combo items list, -1 if it not found.
	 */
	private int getComboItemIndexFromId(String id) {
		String providerName = getComboItemNameFromId(id);
		return getItemIndex(providerCombo, providerName);
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		String provider;
		try {
			provider = config.getAttribute(
					ProviderProfileConstants.PROVIDER_CONFIG_ATT, "");
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			return false;
		}
		if (provider.equals("")) {
			return false;
		}

		Boolean isInitialized = initialized.get(getProviderId());

		if (isInitialized) {
			// Tabs should not be null after initialization.
			if (tabs == null) {
				return false;
			}

			// Validate tab configurations of underlying tool.
			for (AbstractLaunchConfigurationTab tab : tabs) {
				if (!tab.isValid(config)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get name of profiling type that used for this tab.
	 * 
	 * @return String profiling name.
	 */
	public String getName() {
		return name;
	}

	@Override
	public Image getImage() {
		if (img == null)
		   img = AbstractUIPlugin.imageDescriptorFromPlugin(ProfileLaunchPlugin.PLUGIN_ID, 
				"icons/time_obj.gif").createImage(); //$NON-NLS-1$
		return img;
	}
	
	@Override
	public void dispose() {
		if (img != null)
			img.dispose();
		super.dispose();
	}
}
