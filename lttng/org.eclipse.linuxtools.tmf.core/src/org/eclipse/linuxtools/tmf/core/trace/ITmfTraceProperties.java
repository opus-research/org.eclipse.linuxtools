/*******************************************************************************
 * Copyright (c) 2013 Ericsson
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

import java.util.Iterator;

/**
 * Interface for trace types to implement when they can provide additional
 * trace-wide properties.
 *
 * This information will be displayed in the trace's Properties View, among
 * other things.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public interface ITmfTraceProperties extends Iterable<String> {

    /**
     * Retrieve the number of properties for this trace.
     *
     * @return The string value of the property
     */
    int getTracePropertiesSize();

    /**
     * Get a property related to this trace.
     *
     * @param key the name of the property to retrieve
     * @return The string value of the property
     */
    String getTraceProperty(String key);

    /**
     * Returns an iterator to the keys.
     */
    @Override
    public Iterator<String> iterator();
}
