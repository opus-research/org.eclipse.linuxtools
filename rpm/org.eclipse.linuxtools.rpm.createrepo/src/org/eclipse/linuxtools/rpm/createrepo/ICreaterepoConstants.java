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

import java.util.ArrayList;
import java.util.List;

/**
 * Common constants used in createrepo.
 */
public interface ICreaterepoConstants {

	/**
	 * The folder that contains all the repository metadata.
	 */
	String REPODATA_FOLDER = "repodata"; //$NON-NLS-1$

	/**
	 * The index file that describes the other files within the repository.
	 */
	String REPO_METADATA_FILE = "repomd.xml"; //$NON-NLS-1$

	/**
	 * The file extension of RPM files.
	 */
	String RPM_FILE_EXTENSION = "rpm"; //$NON-NLS-1$

	/**
	 * An empty string.
	 */
	String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Delimiter to be used when storing values into preference store.
	 */
	String DELIMITER = ","; //$NON-NLS-1$

	/**
	 * An empty list to be used for initializing/default createrepo command.
	 */
	List<String> EMPTY_LIST = new ArrayList<String>();

}
