/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.structures;

import org.eclipse.linuxtools.systemtap.structures.TreeNode;


/**
 * A structure for containing extra information of SystemTap functions.
 * @since 3.0
 */
public class FunctionNodeData implements ISearchableNode, ISingleTypedNode {
	private final String line;
	private final String type;

	@Override
	public boolean isRegexSearch() {
		return false;
	}

	/**
	 * @return the text to search a file with for the definition of this function.
	 */
	@Override
	public String getSearchToken() {
		return line;
	}

	@Override
	public String toString() {
		return getSearchToken();
	}

	/**
	 * @return the <code>String</code> representation of the return type of the
	 * node's function (<code>null</code> for void functions).
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * Create a new instance of function node information. (Note that the name of a function
	 * or parameter is stored in a {@link TreeNode}, not here.)
	 * @param line Set this to the original script text that defines this function.
	 * @param type The <code>String</code> representation of the return type of the function.
	 */
	public FunctionNodeData(String line, String type) {
		this.line = line;
		this.type = type;
	}
}
