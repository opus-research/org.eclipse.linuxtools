/*******************************************************************************
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.model;

public class PMLineRef extends TreeParent {
	protected int ln;

	public PMLineRef(int lineNum, float percent) {
		super(""+lineNum, percent);
		ln = lineNum;
	}

	public void addPercent(Float addpc) {
		setPercent(getPercent() + addpc);
	}

	@Override
	public String toString() {
		return getPercent() + "% (" + getNumberOfSamples() + " samples) on line " + ln;
	}
}
