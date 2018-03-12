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

import java.util.Arrays;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;

public class RegistryAccountInfo extends RegistryInfo
		implements IRegistryAccount {

	private String username;
	private String email;
	private char [] password;

	public RegistryAccountInfo(String serverAddress, String username, String email, char [] password) {
		super(serverAddress);
		this.username = username;
		this.email = email;
		this.password = password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public char [] getPassword() {
		if (password != null) {
			return password;
		}
		char[] password = null;
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		ISecurePreferences dockerNode = preferences.node("org.eclipse.linuxtools.docker.ui.accounts"); //$NON-NLS-1$
		String key = getServerAddress() + "," + getUsername() + "," + getEmail();
		try {
			password = dockerNode.get(key, null).toCharArray();
		} catch (StorageException e) {
		}
		return password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + Arrays.hashCode(password);
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RegistryAccountInfo other = (RegistryAccountInfo) obj;
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		} else if (!email.equals(other.email)) {
			return false;
		}
		if (!Arrays.equals(password, other.password)) {
			return false;
		}
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		return true;
	}

}
