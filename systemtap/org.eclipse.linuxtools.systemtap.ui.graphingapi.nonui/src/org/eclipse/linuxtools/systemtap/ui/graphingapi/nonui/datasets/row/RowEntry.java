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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.row;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataEntry;

public class RowEntry implements IDataEntry {
	public RowEntry() {
		data = null;
	}
	
	public int getRowCount() {
		if(null != data)
			return 1;
		return 0;
	}
	
	public int getColCount() {
		return (null == data) ? 0 : data.length;
	}
	
	public Object get(String key, int col) {
		return (0 <= col && col < getColCount()) ? data[col] : null;
	}
	
	public Object[] getRow(int row) {
		if(0 == row)
			return data;
		return null;
	}
	
	public Object[] getRow(String key) {
		return data;
	}
	
	public Object[] getColumn(int col) {
		if(0 <= col && getColCount() > col) 
			return new Object[] {get(null, col)};
		return null;
	}
	
	public Object[][] getData() {
		return new Object[][] {getRow(null)};
	}
	
	public IDataEntry copy() {
		RowEntry entry = new RowEntry();
		if(null != data) {
			entry.data = new Object[data.length];
			System.arraycopy(data, 0, entry.data, 0, data.length);
		}
		
		return entry;
	}
	
	public void putRow(int row, Object[] data) {
		if(0 == row)
			this.data = data;
	}
	
	public boolean remove(int row) {
		if(row == 0) {
			data = null;
			return true;
		} else 
			return false;
	}
	
	private Object[] data;
}
