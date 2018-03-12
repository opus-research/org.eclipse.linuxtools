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
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;

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
    private TmfNanoTimestamp fOffset;

    /**
     * Default constructor
     */
    public TmfConstantTransform() {
        fOffset = new TmfNanoTimestamp(0);
    }

    /**
     * Constructor with offset
     *
     * @param offset
     *            The offset of the linear transform in nanoseconds
     */
    public TmfConstantTransform(long offset) {
        fOffset = new TmfNanoTimestamp(offset);
    }

    /**
     * Constructor with offset timestamp
     *
     * @param offset
     *            The offset of the linear transform
     */
    public TmfConstantTransform(ITmfTimestamp offset) {
        fOffset = new TmfNanoTimestamp(offset);
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return timestamp.normalize(fOffset.getValue(), ITmfTimestamp.NANOSECOND_SCALE);
    }

    /**
     * {@inheritDoc}
     *
     * @param timestamp
     *            the timestamp in nanoseconds
     * @return the timestamp in nanoseconds
     */
    @Override
    public long transform(long timestamp) {
        return fOffset.getValue() + timestamp;
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        if (composeWith.equals(TmfTimestampTransform.IDENTITY)) {
            /* If composing with identity, just return this */
            return this;
        } else if (composeWith instanceof TmfConstantTransform) {
            TmfConstantTransform tct = (TmfConstantTransform) composeWith;
            return new TmfConstantTransform(fOffset.getValue() + tct.fOffset.getValue());
        } else if (composeWith instanceof TmfTimestampTransformLinear) {
            /*
             * If composeWith is a linear transform, add the two together, we
             * hope the linear transform is in nanoseconds, because there is no
             * way to determine what its scale is. At the time of the writing,
             * it is always in ns though.
             */
            TmfTimestampTransformLinear ttl = (TmfTimestampTransformLinear) composeWith;
            BigDecimal newBeta = ttl.getOffset().add(new BigDecimal(fOffset.getValue()));
            return new TmfTimestampTransformLinear(ttl.getSlope(), newBeta);
        } else {
            /*
             * We do not know what to do with this kind of transform, just
             * return this
             */
            return this;
        }
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
