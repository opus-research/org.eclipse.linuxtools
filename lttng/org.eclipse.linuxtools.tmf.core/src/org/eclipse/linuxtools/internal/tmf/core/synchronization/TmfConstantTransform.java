/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.synchronization;

import java.math.BigDecimal;

import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TmfTimestampTransformLinear;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * Constant transform, just offset your timestamp with another.
 *
 * @author Matthew Khouzam
 */
public class TmfConstantTransform implements ITmfTimestampTransform {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 417299521984404532L;
    private long fOffset;

    /**
     * Default constructor
     */
    public TmfConstantTransform() {
        fOffset = 0L;
    }

    /**
     * Constructor with offset
     *
     * @param offset
     *            The offset of the linear transform
     */
    public TmfConstantTransform(long offset) {
        fOffset = offset;
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return timestamp.normalize(fOffset, ITmfTimestamp.NANOSECOND_SCALE);
    }

    @Override
    public long transform(long timestamp) {
        return fOffset + timestamp;
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        if (composeWith.equals(TmfTimestampTransform.IDENTITY)) {
            /* If composing with identity, just return this */
            return this;
        } else if (composeWith instanceof TmfConstantTransform) {
            TmfConstantTransform tct = (TmfConstantTransform) composeWith;
            return new TmfConstantTransform(fOffset + tct.getOffset());
        } else if (composeWith instanceof TmfTimestampTransformLinear) {
            /* If composeWith is a linear transform, add the two together */
            TmfTimestampTransformLinear ttl = (TmfTimestampTransformLinear) composeWith;
            BigDecimal newBeta = ttl.getOffset().add(new BigDecimal(fOffset));
            return new TmfTimestampTransformLinear(ttl.getSlope(), newBeta);
        } else {
            /*
             * We do not know what to do with this kind of transform, just
             * return this
             */
            return this;
        }
    }

    private long getOffset() {
        return fOffset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fOffset ^ (fOffset >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfConstantTransform other = (TmfConstantTransform) obj;
        if (fOffset != other.fOffset) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TmfConstantTransform [fOffset="); //$NON-NLS-1$
        builder.append(fOffset);
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }

}
