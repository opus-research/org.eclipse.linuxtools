/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.createrepo;

/**
 * Valid checksums that createrepo command uses.
 */
public interface ICreaterepoChecksums {

	/**
	 * Default checksum.
	 */
	String SHA256 = "compat"; //$NON-NLS-1$

	/**
	 * Old default. Actually "sha1" but "sha1" does not work
	 * with older (3.0.x) versions of yum. Must use "sha".
	 */
	String SHA1 = "sha"; //$NON-NLS-1$

	/**
	 * MD5.
	 */
	String MD5 = "md5"; //$NON-NLS-1$

	/**
	 * SHA512.
	 */
	String SHA512 = "sha512"; //$NON-NLS-1$

}
