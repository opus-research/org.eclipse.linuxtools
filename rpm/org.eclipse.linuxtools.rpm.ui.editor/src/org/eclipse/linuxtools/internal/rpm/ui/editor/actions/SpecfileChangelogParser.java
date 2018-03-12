/*******************************************************************************
 * Copyright (c) 2007, 2016 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.actions;

import org.eclipse.linuxtools.changelog.core.IParserChangeLogContrib;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class SpecfileChangelogParser implements IParserChangeLogContrib {

	@Override
	public String parseCurrentFunction(IEditorPart editor) {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String parseCurrentFunction(IEditorInput input, int offset) {
		return ""; //$NON-NLS-1$
	}

}
