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

package org.eclipse.linuxtools.tmf.core.trace;

/**
 * Example implementation of TmfLocation that uses a Long as its location data.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfLongLocation extends TmfLocation {

    /**
     * Constructor.
     *
     * @param longValue
     *            The Long to use as a location data.
     */
    public TmfLongLocation(Long longValue) {
        super(new TmfLongLocationData(longValue));
    }

    /**
     * Copy constructor
     *
     * @param loc
     *            Other location to copy
     */
    public TmfLongLocation(TmfLongLocation loc) {
        super(loc.getLocationData());
    }

    @Override
    public TmfLongLocationData getLocationData() {
        return (TmfLongLocationData) super.getLocationData();
    }

    /**
     * Directly retrieve the long representing this location.
     *
     * @return The Long value
     */
    public Long getLongValue() {
        return getLocationData().getLongValue();
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public TmfLongLocation clone() {
        return (TmfLongLocation) super.clone();
    }

}