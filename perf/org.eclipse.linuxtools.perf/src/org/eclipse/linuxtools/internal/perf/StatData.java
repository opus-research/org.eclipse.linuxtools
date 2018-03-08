/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatData extends AbstractDataManipulator {

	private String cmd;
	private String [] args;
	private int runCount;

	public StatData(String title, String cmd, String [] args, int runCount) {
		super(title);
		this.cmd = cmd;
		this.args = args;
		this.runCount = runCount;
	}

	@Override
	public void parse() {
		String [] cmd = getCommand(this.cmd, this.args);
		// perf stat prints the data to standard error
		performCommand(cmd, 2);
	}

	protected String [] getCommand(String command, String [] args) {
		String [] cmd = new String [] {"perf", "stat"}; //$NON-NLS-1$ //$NON-NLS-2$
		if (runCount > 1) {
			cmd = new String [] {"perf", "stat", "-r", String.valueOf(runCount)};
		}
		List<String> ret = new ArrayList<String> ();
		ret.addAll(Arrays.asList(cmd));
		ret.add(command);
		ret.addAll(Arrays.asList(args));
		return ret.toArray(new String [0]);
	}

}
