/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

public interface IDockerImageSearchResult {

	/**
	 * @return the image description
	 */
	String getDescription();

	/**
	 * @return the official image flag
	 */
	boolean isOfficial();

	/**
	 * @return the automated build flag
	 */
	boolean isAutomated();

	/**
	 * @return the image name
	 */
	String getName();

	/**
	 * @return the star count
	 */
	int getStarCount();

}