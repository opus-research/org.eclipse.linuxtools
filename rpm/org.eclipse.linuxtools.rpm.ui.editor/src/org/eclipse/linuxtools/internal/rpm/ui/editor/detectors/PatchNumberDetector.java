/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.detectors;



public class PatchNumberDetector implements IStrictWordDetector {

	public boolean isWordPart(char c) {
		return Character.isDigit(c);
	}

	public boolean isWordStart(char c) {
		return Character.isDigit(c);
	}
	
	public boolean isEndingCharacter(char c) {
		return Character.isWhitespace(c);
	}

}
