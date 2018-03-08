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
 * Data containers that are to be used with (I)TmfLocation's.
 *
 * It should contain one (or more) objects to be used as a unique location
 * identifier.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public interface ITmfLocationData extends Comparable<ITmfLocationData>, Cloneable {

    /**
     * Deep-copy this object
     *
     * @return A copy of this object
     */
    public ITmfLocationData clone();
}
