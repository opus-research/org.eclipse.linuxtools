/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

/**
 * A convenience implementation on of ITmfLocation. The generic class (L) must
 * be comparable.
 *
 *
 * @version 2.0
 * @author Francois Chouinard
 */
public class TmfLocation implements ITmfLocation, Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private ITmfLocationData fLocationData;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor.
     *
     * @param locationData the trace location
     * @since 2.0
     */
    public TmfLocation(final ITmfLocationData locationData) {
        fLocationData = locationData;
    }

    /**
     * Copy constructor
     *
     * @param location the original location
     */
    public TmfLocation(final TmfLocation location) {
        fLocationData = location.fLocationData;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public ITmfLocationData getLocationData() {
        return fLocationData;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TmfLocation clone() {
        TmfLocation clone = null;
        try {
            clone = (TmfLocation) super.clone();
            clone.fLocationData = this.fLocationData.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fLocationData != null) ? fLocationData.hashCode() : 0);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TmfLocation other = (TmfLocation) obj;
        if (fLocationData == null) {
            if (other.fLocationData != null) {
                return false;
            }
        } else if (!fLocationData.equals(other.fLocationData)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        String loc = fLocationData == null ? "null" : fLocationData.toString();
        return "TmfLocation [fLocation=" + loc + "]";
    }

}
