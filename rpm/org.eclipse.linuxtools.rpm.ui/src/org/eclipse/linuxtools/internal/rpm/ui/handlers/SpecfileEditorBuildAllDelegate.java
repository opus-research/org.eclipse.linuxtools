/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.handlers;

import org.eclipse.linuxtools.internal.rpm.ui.BuildType;

/**
 * Build both Binary and Source RPMs.
 *
 * @since 1.0.0
 */
public class SpecfileEditorBuildAllDelegate extends AbstractSpecfileEditorBuildDelegate {
	@Override
	protected BuildType getBuildType() {
		return BuildType.ALL;
	}
}
