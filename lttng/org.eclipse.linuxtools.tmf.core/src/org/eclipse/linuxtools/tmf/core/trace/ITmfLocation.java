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
 *   Alexandre Montplaisir - Removed generic type, use ITmfLocationData
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;


/**
 * The generic trace location in TMF.
 * <p>
 * An ITmfLocation is the equivalent of a random-access file position, holding
 * enough information to allow the positioning of the trace 'pointer' to read an
 * arbitrary event.
 * <p>
 * This location information type is trace-specific and must implement
 * ITmfLocationData
 *
 * @version 2.0
 * @author Francois Chouinard
 */
public interface ITmfLocation {

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the location
     * @since 2.0
     */
    public ITmfLocationData getLocationData();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @return a clone of the location
     */
    public ITmfLocation clone();

}
