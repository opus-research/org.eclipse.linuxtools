/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;

public interface IValgrindMessage {

    IValgrindMessage getParent();

    IValgrindMessage[] getChildren();

    String getText();

    /** @since 1.1 */
	default ISourceLocator getSourceLocator() {
		if (getLaunch() != null) {
			return getLaunch().getSourceLocator();
		}
		return null;
	}

	/**
	 * @deprecated use getSourceLocation() to resolve locations,
	 * getLaunch may return null if log error message was imported from the log and not associated with a launch
	 */
    @Deprecated
	ILaunch getLaunch();

    void addChild(IValgrindMessage child);

}