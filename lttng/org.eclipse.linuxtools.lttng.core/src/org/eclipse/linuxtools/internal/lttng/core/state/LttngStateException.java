/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.core.state;

/**
 * 
 * @author alvaro
 * 
 */
public class LttngStateException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7122881233964952441L;

	/**
	 * Constructs an {@code LttngStateException} with {@code null} as its error detail
	 * message.
	 */
	public LttngStateException() {
		super();
	}

	/**
	 * Constructs an {@code LttngStateException} with the specified detail message.
	 * 
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 */
	public LttngStateException(String message) {
		super(message);
	}

	/**
	 * Constructs an {@code LttngStateException} with the specified detail message and
	 * cause.
	 * 
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated into this exception's detail message.
	 * 
	 * @param message
	 *            The detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method)
	 * 
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 * 
	 * @since 1.6
	 */
	public LttngStateException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an {@code LttngStateException} with the specified cause and a detail
	 * message of {@code (cause==null ? null : cause.toString())} (which
	 * typically contains the class and detail message of {@code cause}). This
	 * constructor is useful for exceptions that are little more than
	 * wrappers for other throwables.
	 * 
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 * 
	 * @since 1.6
	 */
	public LttngStateException(Throwable cause) {
		super(cause);
	}
}
