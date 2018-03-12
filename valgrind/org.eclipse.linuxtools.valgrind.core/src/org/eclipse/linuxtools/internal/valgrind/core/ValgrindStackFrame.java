/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;

public class ValgrindStackFrame extends AbstractValgrindMessage {
	protected String file;
	protected int line;
	private ISourceLocator locator;

	public ValgrindStackFrame(IValgrindMessage message, String text, ILaunch launch, String file, int line) {
		super(message, text, launch);
		this.file = file;
		this.line = line;
	}

	public String getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}

	@Override
	public ISourceLocator getSourceLocator() {
		if (locator != null)
			return locator;
		if (getLaunch() != null) {
			return getLaunch().getSourceLocator();
		}
		return null;
	}

	public void setSourceLocator(ISourceLocator locator) {
		this.locator = locator;
	}
}
