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

import java.util.List;

public interface IRegistry {

	String[] DOCKERHUB_REGISTRY_ALIASES = new String[] {
				"registry.hub.docker.com", //$NON-NLS-1$
				"index.docker.io" //$NON-NLS-1$
		};
	String DOCKERHUB_REGISTRY = "https://index.docker.io/v1/";

	/**
	 * @return the server address URL, including the scheme
	 */
	String getServerAddress();

	/**
	 * 
	 * @return the server host (and optional port) to prepend to an image name
	 *         when pushing or pulling
	 */
	String getServerHost();

	List<IDockerImageSearchResult> getImages(String term) throws DockerException;

	List<IRepositoryTag> getTags(String repository) throws DockerException;

	boolean isVersion2();

	boolean isDockerHubRegistry();

}
