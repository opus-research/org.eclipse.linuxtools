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
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import org.eclipse.linuxtools.internal.rpm.ui.editor.actions.Messages;

/**
 * The different types of builds and their execution messages.
 *
 * @since 1.0.0
 */
public enum BuildType {
	/**
	 * Everything - rpmbuild -ba.
	 */
	ALL(Messages.BuildAll_executeMessage),
	/**
	 * Binary - rpmbuild -bb.
	 */
	BINARY(Messages.BuildRPMS_executeMessage),
	/**
	 * Source RPM - rpmbuild -bs.
	 */
	SOURCE(Messages.BuildSRPM_executeMessage);

	private String executionMessage;

	/**
	 * Assign a message to each build type.
	 *
	 * @param executionMessage The execution message to store.
	 */
	private BuildType(String executionMessage) {
		this.executionMessage = executionMessage;
	}

	/**
	 * Returns the executions message for the build type.
	 *
	 * @return The execution message.
	 */
	public String getExecutionMessage() {
		return executionMessage;
	}
}
