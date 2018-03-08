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

package org.eclipse.linuxtools.systemtap.ui.structures;

import java.text.DateFormat;
import java.util.Date;


public class StringFormatter implements IFormattingStyles {
	public StringFormatter() {
		format = IFormattingStyles.UNFORMATED;
	}
	
	public int getFormat() {
		return format;
	}
	
	public void setFormat(int format) {
		this.format = format;
	}
	
	/**
	 * Potentially modifies a string value according to a certain format based on the current value
	 * of format.
	 * 
	 * @param s The string to potential modify.
	 *
	 * @return The modified string.
	 */
	public String format(String s) {
		switch (format) {
			case STRING:
				return s;
			case DATE:
				return DateFormat.getDateTimeInstance().format(new Date(Long.parseLong(s)));
			case HEX:
				return "0x"+Long.toHexString(Long.parseLong(s));
			case OCTAL:
				return "0x"+Long.toOctalString(Long.parseLong(s));
			case BINARY:
				return "0x" + Long.toBinaryString(Long.parseLong(s));
			case DOUBLE:
				return "" + Double.parseDouble(s);
		}
		return s;
	}
	
	private int format;
}
