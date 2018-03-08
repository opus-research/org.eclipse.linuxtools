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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures;

import org.eclipse.linuxtools.systemtap.structures.listeners.IGobblerListener;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataEntry;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSetParser;



public class ChartStreamDaemon implements IGobblerListener {
	public ChartStreamDaemon(IDataSet d, IDataSetParser p) {
		data = d;
		outputData = new StringBuilder(""); //$NON-NLS-1$
		parser = p;
		disposed = false;
	}

	/**
	 * Takes one line from the output data and appends it to data object.
	 */
	protected void pushData() {
		if(null == data || null == parser)
			return;

		IDataEntry e = parser.parse(outputData);
		if(null != e)
			data.setData(e);
	}

	@Override
	public void handleDataEvent(String line) {
		outputData.append(line);
		this.pushData();
	}

	public boolean isDisposed() {
		return disposed;
	}

	public void dispose() {
		if(!disposed) {
			disposed = true;
			data = null;
			if(null != outputData)
				outputData.delete(0, outputData.length());
			outputData = null;
			parser = null;
		}
	}

	protected IDataSet data;
	protected StringBuilder outputData;
	protected IDataSetParser parser;
	private boolean disposed;
}
