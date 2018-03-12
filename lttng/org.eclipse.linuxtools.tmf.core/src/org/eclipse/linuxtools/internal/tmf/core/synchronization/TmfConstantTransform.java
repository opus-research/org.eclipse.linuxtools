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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TmfTimestampTransformLinear;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

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
    private final Long fValue;
    private final Integer fScale;
    private final Integer fPrecision;

    private transient ITmfTimestamp fOffset;

    /**
     * Default constructor
     */
    public TmfConstantTransform() {
        this(new TmfNanoTimestamp(0));
    }

    /**
     * Constructor with offset
     *
     * @param offset
     *            The offset of the linear transform in nanoseconds
     */
    public TmfConstantTransform(long offset) {
        this(new TmfNanoTimestamp(offset));
    }

    /**
     * Constructor with offset timestamp
     *
     * @param offset
     *            The offset of the linear transform
     */
    public TmfConstantTransform(@NonNull ITmfTimestamp offset) {
        fOffset = offset;
        fValue = getOffset().getValue();
        fPrecision = getOffset().getPrecision();
        fScale = getOffset().getScale();
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return getOffset().normalize(timestamp.getValue(), timestamp.getScale());
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

        return getOffset().normalize(timestamp, ITmfTimestamp.NANOSECOND_SCALE).getValue();
    }

    /**
     * Gets the timestamp of the transform and restores it if need be
     *
     * @return the timestamp
     */
    protected ITmfTimestamp getOffset() {
        restoreTimestamp();
        return fOffset;
    }

    private void restoreTimestamp() {
        if (fOffset == null) {
            fOffset = new TmfTimestamp(fValue, fScale, fPrecision);
        }
    }

    @Override
    public ITmfTimestampTransform composeWith(ITmfTimestampTransform composeWith) {
        if (composeWith.equals(TmfTimestampTransform.IDENTITY)) {
            /* If composing with identity, just return this */
            return this;
        } else if (composeWith instanceof TmfConstantTransform) {
            TmfConstantTransform tct = (TmfConstantTransform) composeWith;
            return new TmfConstantTransform(getOffset().getValue() + tct.getOffset().getValue());
        } else if (composeWith instanceof TmfTimestampTransformLinear) {
            throw new UnsupportedOperationException("Cannot compose a constant and linear transform yet"); //$NON-NLS-1$
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
        builder.append(getOffset());
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }

}
