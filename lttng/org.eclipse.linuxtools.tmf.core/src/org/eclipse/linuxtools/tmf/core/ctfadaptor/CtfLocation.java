/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

/**
 * The nugget of information that is unique to a location in a CTF trace.
 *
 * It can be copied and used to restore a position in a given trace.
 *
 * @version 1.0
 * @author Matthew Khouzam
 */
public class CtfLocation implements ITmfLocation<CtfLocationData>, Cloneable {

    /**
     * An invalid location
     */
    public static final CtfLocationData INVALID_LOCATION = new CtfLocationData(-1, -1);

    /**
     * Constructor for CtfLocation.
     * @param location Long
     * @since 2.0
     */
    public CtfLocation(CtfLocationData location) {
        setLocation(location);
    }

    /**
     * Constructor for CtfLocation.
     * @param timestamp ITmfTimestamp
     * @param offset the offset
     * @since 2.0
     */
    public CtfLocation(ITmfTimestamp timestamp, long offset) {
        setLocation(new CtfLocationData(timestamp.getValue(), offset));
    }
    /**
     * Constructor for CtfLocation.
     * @param timestamp ITmfTimestamp
     */
    @Deprecated
    public CtfLocation(ITmfTimestamp timestamp) {
        setLocation(new CtfLocationData(timestamp.getValue(), 0));
    }

    private CtfLocationData fLocation;

    /**
     * Method setLocation.
     * @param location the location
     * @since 2.0
     */
    public void setLocation(CtfLocationData location) {
        this.fLocation = location;
    }

    /**
     * @param timestampValue the timestamp
     * @param offset the offset
     * @since 2.0
     */
    public void setLocation(long timestampValue, long offset) {
       this.fLocation = new CtfLocationData(timestampValue, offset);
    }


    /**
     * Method getLocation.
     * @return Long
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfLocation#getLocation()
     * @since 2.0
     */
    @Override
    public CtfLocationData getLocation() {
        return fLocation;
    }

    /**
     * Method clone.
     * @return CtfLocation
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfLocation#clone()
     */
    @Override
    public CtfLocation clone() {
        return new CtfLocation(new CtfLocationData(fLocation.getTimestamp(), fLocation.getIndex()));
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((fLocation == null) ? 0 : fLocation.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CtfLocation)) {
            return false;
        }
        CtfLocation other = (CtfLocation) obj;
        if (fLocation == null) {
            if (other.fLocation != null) {
                return false;
            }
        } else if (!fLocation.equals(other.fLocation)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if( this.getLocation().equals(CtfLocation.INVALID_LOCATION )) {
            return "CtfLocation: INVALID"; //$NON-NLS-1$
        }
        return "CtfLocation: " + getLocation().toString(); //$NON-NLS-1$
    }


}
