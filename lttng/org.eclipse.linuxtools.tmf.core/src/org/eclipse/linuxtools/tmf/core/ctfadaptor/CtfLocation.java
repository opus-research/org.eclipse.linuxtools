/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Alexandre Montplaisir - Extend TmfLocation, make immutable
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * The nugget of information that is unique to a location in a CTF trace.
 *
 * It can be copied and used to restore a position in a given trace.
 *
 * @version 1.0
 * @author Matthew Khouzam
 */
public final class CtfLocation extends TmfLocation<CtfLocationData> {

    /**
     * An invalid location
     */
    final static CtfLocationData INVALID_LOCATION_DATA = new CtfLocationData(-1, -1);

    /**
     * Constructor for CtfLocation. Uses a default index of 0.
     *
     * @param timestamp
     *            The timestamp of this location
     */
    public CtfLocation(ITmfTimestamp timestamp) {
        super(new CtfLocationData(timestamp.getValue(), 0));
    }

    /**
     * Full constructor using ITmfTimestamp for the timestamp
     *
     * @param timestamp
     *            The timestamp of this location
     * @param index
     *            The index of this location for this timestamp
     * @since 2.0
     */
    public CtfLocation(ITmfTimestamp timestamp, long index) {
        super(new CtfLocationData(timestamp.getValue(), index));
    }

    /**
     * Full constructor using a long for the timestamp
     *
     * @param timestamp
     *            The timestamp of this location
     * @param index
     *            The index of this location for this timestamp
     * @since 2.0
     */
    public CtfLocation(long timestamp, long index) {
        super(new CtfLocationData(timestamp, index));
    }

    /**
     * Constructor with a CtfLocationData object
     *
     * @param locationData
     *            Location Data object to use
     * @since 2.0
     */
    public CtfLocation(CtfLocationData locationData) {
        super(locationData);
    }

    @Override
    public CtfLocation clone() {
        /* Shallow-copying the CtfLocationData, which we know to be immutable */
        return new CtfLocation(this.getLocationData());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if( this.getLocationData().equals(CtfLocation.INVALID_LOCATION_DATA )) {
            return "CtfLocation: INVALID"; //$NON-NLS-1$
        }
        return "CtfLocation: " + getLocationData().toString(); //$NON-NLS-1$
    }
}
