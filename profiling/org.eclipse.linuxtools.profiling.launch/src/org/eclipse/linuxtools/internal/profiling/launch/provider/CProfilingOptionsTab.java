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
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.Messages;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.ProviderFramework;
import org.eclipse.linuxtools.profiling.launch.ProfileLaunchConfigurationTabGroup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

// Special profiling options tab to use with the org.eclipse.cdt.launch.profilingProvider extension
// to extend the Local C/C++ Application configuration to handle profiling.  We do not rename
// the configuration as done in the normal profiling framework since it belongs to the CDT.
public class CProfilingOptionsTab extends CLaunchConfigurationTab {

	String type;
	String name;
	Composite top;
	Combo providerCombo;
	AbstractLaunchConfigurationTab[] tabs;
	ILaunchConfiguration initial;
	HashMap<String, String> comboItems;
	CTabFolder tabgroup;

	// if tabs are being initialized do not call performApply()
	HashMap<String, Boolean> initialized = new HashMap<String, Boolean> ();

	/**
	 * ProviderOptionsTab constructor.
	 *
	 * @param profilingType String type of profiling this tab will be used for.
	 * @param profilingName String name of this tab to be displayed.
	 */
	public CProfilingOptionsTab() {
		//TODO: use spinner to get category and have default category
		type = "timing"; //$NON-NLS-1$ 
		name = Messages.ProfilingTabName_0;
	}

	public void createControl(Composite parent) {
		top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout(1, true));
		providerCombo = new Combo(top, SWT.READ_ONLY);
		comboItems = ProviderFramework
				.getProviderNamesForType(getProfilingType());
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
		initialized.clear();

		ProfileLaunchConfigurationTabGroup tabGroupConfig;

		if (curProviderId == null || "".equals(curProviderId)) {
			// get the id of a provider
			curProviderId = ProviderFramework.getProviderIdToRun(null, getProfilingType());
		}

		// starting initialization of this tab's controls
		initialized.put(curProviderId, false);

		tabGroupConfig = ProviderFramework.getTabGroupProviderFromId(curProviderId);
		if (tabGroupConfig == null) {
			// no provider found
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
						ProviderProfileConstants.PROVIDER_CONFIG_ATT, "");
				if (providerId != null && !providerId.equals("")) {
					// load provider corresponding to specified id
					loadTabGroupItems(tabgroup, providerId);
				} else {
					// find a provider to load if none found
					loadTabGroupItems(tabgroup, null);
				}
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
	private int getItemIndex(String name) {
		int itemCount = providerCombo.getItemCount();
		for (int i = 0; i < itemCount; i++) {
			if (providerCombo.getItem(i).equals(name)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Get index of specific id in the combo items list
	 *
	 * @param id
	 * @return index of given id in combo items list, -1 if it not found.
	 */
	private int getComboItemIndexFromId(String id) {
		String providerName = getComboItemNameFromId(id);
		return getItemIndex(providerName);
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
			setErrorMessage(Messages.ProviderOptionsTab_0);
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
	 * Get profiling type of the configuration.
	 * 
	 * @return String profiling type this plug-in supports.
	 */
	protected String getProfilingType() {
		return type;
	}

	/**
	 * Get name of profiling type that used for this tab.
	 * 
	 * @return String profiling name.
	 */
	public String getName() {
		return name;
	}

}
