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

/**
 * A specialized version of the IDockerImageHierarchyNode targeting
 * {@link IDockerImage}.
 */
public interface IDockerImageHierarchyImageNode
		extends IDockerImageHierarchyNode {

	@Override
	IDockerImage getElement();
}
