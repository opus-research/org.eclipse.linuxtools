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
		return password;
	}

}
