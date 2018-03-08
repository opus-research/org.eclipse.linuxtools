/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.ctfadaptor;

/**
 * CtfLocationData, the data in a ctf location.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class CtfLocationData implements Comparable<CtfLocationData> {

    /**
     * @param ts
     *            timestamp
     * @param off
     *            offset (if there are N elements with the same packet, which
     *            one is it.)
     */
    public CtfLocationData(long ts, long off) {
        timestamp = ts;
        offset = off;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return the offset of the element
     */
    public long getOffset() {
        return offset;
    }

    private final long timestamp;
    private final long offset;

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (int) (offset ^ (offset >>> 32));
        result = (prime * result) + (int) (timestamp ^ (timestamp >>> 32));
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CtfLocationData)) {
            return false;
        }
        CtfLocationData other = (CtfLocationData) obj;
        if (offset != other.offset) {
            return false;
        }
        if (timestamp != other.timestamp) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Element [" + timestamp + "/" + offset + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public int compareTo(CtfLocationData other) {
        if( this.timestamp > other.getTimestamp() ) {
            return 1;
        }
        if( this.timestamp < other.getTimestamp() ) {
            return -1;
        }
        if( this.offset > other.getOffset() ) {
            return 1;
        }
        if( this.offset < other.getOffset() ) {
            return -1;
        }
        return 0;
    }

}
