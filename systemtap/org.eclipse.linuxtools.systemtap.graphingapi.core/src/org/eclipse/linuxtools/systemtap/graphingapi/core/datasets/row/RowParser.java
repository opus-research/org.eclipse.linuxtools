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

package org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;


public class RowParser implements IDataSetParser {
	public RowParser(String regEx) {
		wholePattern = Pattern.compile(regEx);
	}

	@Override
	public IDataEntry parse(StringBuilder s) {
		if(null == s) {
			return null;
		}

		RowEntry e = null;
		Matcher wholeMatcher = wholePattern.matcher(s);

		if(wholeMatcher.find()) {
			e = new RowEntry();
			int groupCount = wholeMatcher.groupCount();
			Object[] data = new Object[groupCount];

			for(int i = 0; i < groupCount; i++) {
				data[i] = wholeMatcher.group(i+1);
			}
			e.putRow(0, data);
			s.delete(0, wholeMatcher.end());
		}

		return e;
	}

	private Pattern wholePattern;
}
