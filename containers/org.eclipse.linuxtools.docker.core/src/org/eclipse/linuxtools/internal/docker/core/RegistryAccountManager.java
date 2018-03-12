/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;

public class RegistryAccountManager {

	private static String DOCKER_NODE = "org.eclipse.linuxtools.docker.ui.accounts";

	private static RegistryAccountManager instance;

	private RegistryAccountManager() {
	}

	public static RegistryAccountManager getInstance() {
		if (instance == null) {
			return new RegistryAccountManager();
		}
		return instance;
	}

	public List<IRegistryAccount> getAccounts() {
		return getAccounts(true);
	}

	public List<IRegistryAccount> getAccounts(boolean loadPassword) {
		List<IRegistryAccount> accounts = new ArrayList<>();
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		ISecurePreferences dockerNode = preferences.node(DOCKER_NODE);
		for (String key : dockerNode.keys()) {
			try {
				String[] tokens = key.split("_"); //$NON-NLS-1$
				String serverAddress = tokens[0];
				String username = tokens[1];
				String email = tokens[2];
				char[] password = null;
				if (loadPassword) {
					password = dockerNode.get(key, null).toCharArray();
				}
				RegistryAccountInfo account = new RegistryAccountInfo(serverAddress, username, email, password);
				accounts.add(account);
			} catch (StorageException e) {
				// Ignore the account
			}
		}
		return accounts;
	}

	public void add(IRegistryAccount info) {
		ISecurePreferences preferences = getDockerNode();
		char[] password = info.getPassword();
		String key = getKeyFor(info);
		try {
			preferences.put(key, new String(password), true);
		} catch (StorageException e) {
		}
	}

	public void remove(IRegistryAccount info) {
		ISecurePreferences preferences = getDockerNode();
		String key = getKeyFor(info);
		preferences.remove(key);
	}

	public IRegistryAccount getAccount(String serverAddress, String username,
			String email) {
		List<IRegistryAccount> accounts = getAccounts(true).stream()
				.filter(e -> e.getServerAddress().equals(serverAddress)
						&& e.getUsername().equals(username)
						&& e.getEmail().equals(email))
				.collect(Collectors.toList());
		return accounts.get(0);
	}

	private ISecurePreferences getDockerNode() {
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		ISecurePreferences dockerNode = preferences.node(DOCKER_NODE);
		return dockerNode;
	}

	private String getKeyFor(IRegistryAccount info) {
		return info.getServerAddress() + "_" + info.getUsername() + "_" + info.getEmail(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
