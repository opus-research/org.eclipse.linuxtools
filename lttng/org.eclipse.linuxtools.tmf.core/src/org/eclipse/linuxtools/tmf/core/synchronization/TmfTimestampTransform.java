/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.synchronization;

import java.io.Serializable;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * Abstract class to implemet ITmfTimestampTransform
 *
 * @author gbastien
 * @since 2.0
 */
public class TmfTimestampTransform implements ITmfTimestampTransform, Serializable {

    /**
     * Generated serial UID
     */
    private static final long serialVersionUID = -1480581417493073304L;
    private final String fReferenceTrace, fOtherTrace;

    /**
     * The unique instance of this transform, since it is always the same
     */
    public static final TmfTimestampTransform IDENTITY = new TmfTimestampTransform();

    /**
     * Default constructor
     */
    private TmfTimestampTransform() {
        fReferenceTrace = ""; //$NON-NLS-1$
        fOtherTrace = ""; //$NON-NLS-1$
    }

    /**
     * @param refTrace
     *            The reference trace for this transform (the one for which f(t)
     *            = t
     * @param otherTrace
     *            The trace whose time will be transformed
     */
    protected TmfTimestampTransform(final String refTrace, final String otherTrace) {
        if (refTrace != null) {
            fReferenceTrace = refTrace;
        } else {
            fReferenceTrace = ""; //$NON-NLS-1$
        }
        if (otherTrace != null) {
            fOtherTrace = otherTrace;
        } else {
            fOtherTrace = ""; //$NON-NLS-1$
        }
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return timestamp;
    }


    @Override
    public long transform(long timestamp) {
        return timestamp;
    }

    /**
     * @return The reference trace for this transform (the one for which f(t) =
     *         t
     */
    public String getReferenceTrace() {
        return fReferenceTrace;
    }

    /**
     * @return The trace whose time will be transformed
     */
    public String getOtherTrace() {
        return fOtherTrace;
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other.getClass().equals(TmfTimestampTransform.class) ) {
            TmfTimestampTransform that = (TmfTimestampTransform) other;
            result = that.getReferenceTrace().equals(getReferenceTrace());
        }
        return result;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + getReferenceTrace().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TmfTimestampTransform [ IDENTITY ]"; //$NON-NLS-1$
    }

}
