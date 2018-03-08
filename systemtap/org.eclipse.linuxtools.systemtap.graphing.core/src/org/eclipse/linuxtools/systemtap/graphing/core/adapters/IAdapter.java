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

package org.eclipse.linuxtools.systemtap.graphing.core.adapters;

public interface IAdapter {
    Number getYSeriesMax(int series, int start, int end);
    Number getSeriesMax(int series, int start, int end);

    String[] getLabels();
    int getRecordCount();
    int getSeriesCount();
    Object[][] getData();
    Object[][] getData(int start, int end);
}
