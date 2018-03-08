/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Added call-site interface
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * The generic event structure in TMF. In its canonical form, an event has:
 * <ul>
 * <li> a parent trace
 * <li> a rank (order within the trace)
 * <li> a timestamp
 * <li> a source (reporting component)
 * <li> a type
 * <li> a content (payload)
 * <li> a reference field (optional)
 * <li> a call site (source code location, optional)
 * </ul>
 * For convenience, a free-form reference field is also provided.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfTimestamp
 * @see ITmfEventType
 * @see ITmfEventField
 * @see ITmfCallsite
 * @see TmfEvent
 */
public interface ITmfEvent extends IAdaptable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Pre-defined event timestamp attribute (for searching &filtering purposes)
     */
    public static final String EVENT_FIELD_TIMESTAMP = ":timestamp:"; //$NON-NLS-1$

    /**
     * Pre-defined event source attribute (for searching &filtering purposes)
     */
    public static final String EVENT_FIELD_SOURCE = ":source:"; //$NON-NLS-1$

    /**
     * Pre-defined event type attribute (for searching &filtering purposes)
     */
    public static final String EVENT_FIELD_TYPE = ":type:"; //$NON-NLS-1$

    /**
     * Pre-defined event content attribute (for searching &filtering purposes)
     */
    public static final String EVENT_FIELD_CONTENT = ":content:"; //$NON-NLS-1$

    /**
     * Pre-defined event reference attribute (for searching &filtering purposes)
     */
    public static final String EVENT_FIELD_REFERENCE = ":reference:"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the trace that 'owns' the event
     */
    public ITmfTrace getTrace();

    /**
     * @return the event rank within the parent trace
     */
    public long getRank();

    /**
     * @return the event timestamp
     * @since 2.0
     */
    public ITmfTimestamp getTimestamp();

    /**
     * @return the event source
     */
    public String getSource();

    /**
     * @return the event type
     */
    public ITmfEventType getType();

    /**
     * @return the event content
     */
    public ITmfEventField getContent();

    /**
     * @return the event reference
     */
    public String getReference();

    /**
     * @return call site information or null if not available
     * @since 2.0
     */
    public ITmfCallsite getCallsite();

}
