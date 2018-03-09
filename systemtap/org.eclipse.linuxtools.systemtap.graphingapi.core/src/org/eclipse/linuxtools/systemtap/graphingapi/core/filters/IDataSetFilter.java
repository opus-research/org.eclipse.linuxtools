/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.core.filters;

import java.util.ArrayList;

public interface IDataSetFilter {
	public ArrayList<Object>[] filter(ArrayList<Object>[] data);
	public String getID();
	/**
	 * @since 2.0
	 */
	public String getInfo();
	/**
	 * @since 2.0
	 */
	public int getColumn();
}
