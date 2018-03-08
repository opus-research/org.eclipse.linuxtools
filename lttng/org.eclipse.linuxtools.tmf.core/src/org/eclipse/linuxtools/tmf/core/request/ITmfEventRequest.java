/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * The TMF event request
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public interface ITmfEventRequest extends ITmfDataRequest {

    /**
     * @return the requested time range
     */
    public TmfTimeRange getRange();

    /**
     * this method is called by the event provider to set the index corresponding to the time range start time
     * @param index the start time index
     */
	public void setStartIndex(int index);

}
