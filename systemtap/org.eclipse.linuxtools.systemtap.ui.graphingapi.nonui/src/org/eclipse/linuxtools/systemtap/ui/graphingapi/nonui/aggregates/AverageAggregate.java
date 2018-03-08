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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures.NumberType;

public class AverageAggregate implements IDataAggregate {
	
	/**
	 * Ensure column isn't empty, then get the average of all the column's values.
	 * 
	 * @param column The column to average.
	 * 
	 * @return Average of all the column's values.
	 */
	@Override
	public Number aggregate(Number[] column) {
		if(column == null || column.length == 0)
			return null;
		
		double num = 0;
		
		for(int i=0; i<column.length; i++)
			num += column[i].doubleValue();
		
		num /= column.length;
		return NumberType.getNumber(column[0], num);
	}
	
	@Override
	public String getID() {
		return ID;
	}
	
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates.AverageAggregate";
}
