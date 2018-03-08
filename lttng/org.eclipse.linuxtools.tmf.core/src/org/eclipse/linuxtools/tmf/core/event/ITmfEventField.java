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
 *   Alexandre Montplaisir - Consolidated API methods
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import java.util.Map;

/**
 * The generic event payload in TMF. Each field can be either a terminal or
 * further decomposed into subfields.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfEventType
 */
public interface ITmfEventField {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The root field id (the main container)
     */
    public static final String ROOT_FIELD_ID = ":root:"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the field name
     */
    public String getName();

    /**
     * @return the field value
     */
    public Object getValue();

    /**
     * @return the value formatted as string
     * @since 2.0
     */
    public String getFormattedValue();

    /**
     * Return the sub-fields contained in this field. This is an unmodifiable
     * map, so do not try to modify it! The order of iteration should remain the
     * same as the order of insertion.
     *
     * @return The map of subfields (empty map if none)
     * @throws UnsupportedOperationException
     *             If an attempt to modify the map is made
     * @since 2.0
     */
    public Map<String, ITmfEventField> getFields();

    /**
     * Convenience method for getting fields by name, equivalent to
     * .getFields().get(name).
     *
     * @param name
     *            The name of the field
     * @return A specific subfield by name (null if absent or inexistent)
     */
    public ITmfEventField getField(String name);
}
