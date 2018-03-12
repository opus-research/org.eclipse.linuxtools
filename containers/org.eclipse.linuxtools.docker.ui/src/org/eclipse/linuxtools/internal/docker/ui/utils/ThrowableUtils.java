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

package org.eclipse.linuxtools.internal.docker.ui.utils;

/**
 * 
 */
public class ThrowableUtils {

	/**
	 * @param throwable
	 *            the throwable to analyze
	 * @return the root cause of the given {@link Throwable}
	 */
	public static Throwable getRootCause(final Throwable throwable) {
		while (throwable.getCause() != null) {
			return getRootCause(throwable.getCause());
		}
		return throwable;
	}
}
