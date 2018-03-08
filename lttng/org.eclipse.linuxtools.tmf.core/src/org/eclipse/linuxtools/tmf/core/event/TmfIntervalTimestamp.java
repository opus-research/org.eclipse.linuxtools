/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.event;

/**
 * A generic timestamp implementation for intervals.
 * The toString() method takes negative values into consideration.
 *
 * @author Bernd Hufmann
 * @since 2.0
 */
public class TmfIntervalTimestamp extends TmfTimestamp {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public TmfIntervalTimestamp() {
        super();
    }

    /**
     * Simple constructor (scale = precision = 0)
     *
     * @param value the timestamp value
     */

    public TmfIntervalTimestamp(long value) {
        super(value);
    }

    /**
     * Simple constructor (precision = 0)
     *
     * @param value the timestamp value
     * @param scale the timestamp scale
     */
    public TmfIntervalTimestamp(long value, int scale) {
        super(value, scale);
    }


    /**
     * Copy constructor
     *
     * @param timestamp the timestamp to copy
     */
    public TmfIntervalTimestamp(ITmfTimestamp timestamp) {
        super(timestamp);
    }

    /**
     * Full constructor
     *
     * @param value the timestamp value
     * @param scale the timestamp scale
     * @param precision the timestamp precision
     */
    public TmfIntervalTimestamp(long value, int scale, int precision) {
        super(value, scale, precision);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (getValue() < 0) {
            TmfIntervalTimestamp tmpTs = new TmfIntervalTimestamp(-getValue(), getScale(), getPrecision());
            return "-" + tmpTs.toString(TmfTimestampFormat.getDefaulIntervalFormat()); //$NON-NLS-1$
        }
        return toString(TmfTimestampFormat.getDefaulIntervalFormat());
    }
}
