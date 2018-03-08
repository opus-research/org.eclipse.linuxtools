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
package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import org.eclipse.linuxtools.internal.rpm.ui.editor.BuildType;

/**
 * Build Source RPM.
 *
 * @since 1.0.0
 */
public class SpecfileEditorBuildSRPMDelegate extends AbstractSpecfileEditorBuildDelegate {
	@Override
	protected BuildType getBuildType() {
		return BuildType.SOURCE;
	}
}