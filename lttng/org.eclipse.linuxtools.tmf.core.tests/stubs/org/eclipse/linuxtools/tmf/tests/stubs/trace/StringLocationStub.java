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

package org.eclipse.linuxtools.tmf.tests.stubs.trace;

import org.eclipse.linuxtools.tmf.core.trace.ITmfLocationData;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * TmfLocation implementation that contains a String as data. Used for tests.
 *
 * @author Alexandre Montplaisir
 */
public class StringLocationStub extends TmfLocation {

    /**
     * Constructor. Uses a String object directly.
     *
     * @param strValue
     *            The string to use as location data
     */
    public StringLocationStub(String strValue) {
        super(new StringLocationDataStub(strValue));
    }

    /**
     * Copy constructor
     *
     * @param loc
     *            The other location to copy
     */
    public StringLocationStub(StringLocationStub loc) {
        super(loc.getLocationData());
    }

    @Override
    public StringLocationDataStub getLocationData() {
        return (StringLocationDataStub) super.getLocationData();
    }

    /**
     * Directly get the string representing the location data.
     *
     * @return The string value
     */
    public String getStringValue() {
        return getLocationData().getString();
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public StringLocationStub clone() {
        return (StringLocationStub) super.clone();
    }

}

class StringLocationDataStub implements ITmfLocationData {

    private final String data;

    StringLocationDataStub(String data) {
        this.data = data;
    }

    String getString() {
        return data;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    @Override
    public StringLocationDataStub clone() {
        StringLocationDataStub clone = null;
        try {
            clone = (StringLocationDataStub) super.clone();
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
        if (other instanceof StringLocationDataStub) {
            StringLocationDataStub o = (StringLocationDataStub) other;
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

        if (obj instanceof StringLocationDataStub) {
            StringLocationDataStub o = (StringLocationDataStub) obj;

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
        return data;
    }
}