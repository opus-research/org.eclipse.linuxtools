/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocationData;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * TmfLocation implementation using a ITmfTimestamp as a location data. Used for
 * tests.
 *
 * @author Alexandre Montplaisir
 */
public class TimestampLocationStub extends TmfLocation {

    /**
     * Constructor.
     *
     * @param timestamp
     *            The timestamp to use as a location data
     */
    public TimestampLocationStub(ITmfTimestamp timestamp) {
        super(new TimestampLocationDataStub(timestamp));
    }

    /**
     * Copy constructor
     *
     * @param loc
     *            Location to copy
     */
    public TimestampLocationStub(TimestampLocationStub loc) {
        super(loc.getLocationData());
    }

    @Override
    public TimestampLocationDataStub getLocationData() {
        return (TimestampLocationDataStub) super.getLocationData();
    }

    /**
     * Directly retrieve the timestamp representing this location.
     *
     * @return The timestamp object
     */
    public ITmfTimestamp getTimestamp() {
        return getLocationData().getTimestamp();
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public TimestampLocationStub clone() {
        return (TimestampLocationStub) super.clone();
    }

}


class TimestampLocationDataStub implements ITmfLocationData {

    private ITmfTimestamp data;

    TimestampLocationDataStub(ITmfTimestamp data) {
        this.data = data;
    }

    ITmfTimestamp getTimestamp() {
        return data;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public TimestampLocationDataStub clone() {
        TimestampLocationDataStub clone = null;
        try {
            clone = (TimestampLocationDataStub) super.clone();
            clone.data = data.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(ITmfLocationData other) {
        if (other instanceof TimestampLocationDataStub) {
            TimestampLocationDataStub o = (TimestampLocationDataStub) other;
            return data.compareTo(o.data);
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        if (data == null) {
            return 0;
        }
        return data.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof TimestampLocationDataStub) {
            TimestampLocationDataStub o = (TimestampLocationDataStub) obj;

            if (this.data == null) {
                return o.data == null;
            }
            /* Normal comparison of data objects */
            return data.equals(o.data);
        }
        return false;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}