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
 * The basic trace context structure in TMF. The purpose of the context is to
 * associate a trace location to an event at a specific rank (order).
 * <p>
 * The context should be sufficient to allow the trace to position itself so
 * that performing a trace read operation will yield the corresponding 'nth'
 * event.
 * 
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfLocation
 */
public interface ITmfContext {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The unknown event rank
     */
    public long UNKNOWN_RANK = -1L;

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the rank of the event at the context location
     */
    public long getRank();

    /**
     * @return the location of the event at the context rank
     */
    public ITmfLocation<? extends Comparable<?>> getLocation();

    /**
     * @return indicates if the context rank is valid (!= UNKNOWN_RANK)
     */
    public boolean hasValidRank();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * @param location the new location
     */
    public void setLocation(ITmfLocation<? extends Comparable<?>> location);

    /**
     * @param rank the new rank
     */
    public void setRank(long rank);

    /**
     * Increment the context rank
     */
    public void increaseRank();

    /**
     * Cleanup hook
     */
    public void dispose();

    /**
     * @return a clone of the context
     */
    public ITmfContext clone();

}
