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
package org.eclipse.linuxtools.docker.core;

public interface IDockerNetworkCreation {

	/**
	 * Get network id
	 * 
	 * @return the id string for the network
	 */
	public String id();

	/**
	 * Get network creation warnings
	 * 
	 * @return String containing warnings
	 */
	public String warnings();

}
