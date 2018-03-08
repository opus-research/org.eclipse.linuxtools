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
 * Exemple implementation of ITmfLocationData, for use within TmfLongLocation's.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfLongLocationData implements ITmfLocationData {

    private final Long data;

    /**
     * Constructor.
     *
     * @param data The long value
     */
    public TmfLongLocationData(Long data) {
        this.data = data;
    }

    /**
     * @return The long value
     */
    public Long getLongValue() {
        return data;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public TmfLongLocationData clone() {
        TmfLongLocationData clone = null;
        try {
            clone = (TmfLongLocationData) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(ITmfLocationData other) {
        if (other instanceof TmfLongLocationData) {
            TmfLongLocationData o = (TmfLongLocationData) other;
            return data.compareTo(o.data);
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        if (data == null) {
            return 0;
        }
        return data.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof TmfLongLocationData) {
            TmfLongLocationData o = (TmfLongLocationData) obj;

            if (this.data == null) {
                return o.data == null;
            }
            /* Normal comparison of data objects */
            return data.equals(o.data);
        }
        return false;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}