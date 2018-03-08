/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *     Patrick Tasse - Updates to mipmap feature
 ******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem;

import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;

/**
 * This interface includes additional statistical operations that can be
 * performed on attributes of the state system.
 */
public interface ITmfStateSystemOperations extends ITmfStateSystem {

    /**
     * Return the maximum value of an attribute over a time range
     *
     * @param t1
     *            The start time of the range
     * @param t2
     *            The end time of the range
     * @param quark
     *            The quark of the attribute
     * @return The maximum value of the attribute in this range
     */
    long queryRangeMax(long t1, long t2, int quark);

    /**
     * Return the minimum value of an attribute over a time range
     *
     * @param t1
     *            The start time of the range
     * @param t2
     *            The end time of the range
     * @param quark
     *            The quark of the attribute
     * @return The minimum value of the attribute in this range
     */
    long queryRangeMin(long t1, long t2, int quark);

    /**
     * Return the weighted average value of an attribute over a time range
     *
     * @param t1
     *            The start time of the range
     * @param t2
     *            The end time of the range
     * @param quark
     *            The quark of the attribute
     * @return The weighted average value of the attribute in this rangeO
     */
    double queryRangeAverage(long t1, long t2, int quark);

}
