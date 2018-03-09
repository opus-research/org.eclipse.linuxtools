/*******************************************************************************
 * Copyright (c) 2013 Etienne Bergeron
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends;

import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;

/**
 * The listener interface for receiving intervals from an intervals
 * publisher.
 *
 * This class is provided to ease the use of the observer pattern over
 * data-structure holding intervals. A query on a data-structure could
 * enumerate the results through a listener instead of producing the
 * full results into a 'specific' collection.
 *
 * @author Etienne Bergeron
 *
 */
public interface ITmfStateIntervalListener {

    /**
     * Receives an interval from the intervals publisher
     *
     * @param interval
     *            the interval provided by the publisher
     */
    void addInterval(ITmfStateInterval interval);

}