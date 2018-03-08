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

package org.eclipse.linuxtools.systemtap.graphing.core.datasets;

import org.eclipse.linuxtools.systemtap.graphing.core.filters.IDataSetFilter;

public interface IFilteredDataSet extends IDataSet {
    void addFilter(IDataSetFilter filter);
    IDataSetFilter[] getFilters();
    void clearFilters();
    boolean removeFilter(IDataSetFilter filter);
}
