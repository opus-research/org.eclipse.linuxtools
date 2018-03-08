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

import java.math.BigDecimal;
import java.math.MathContext;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

/**
 * Class implementing a linear timestamp transform, with a slope and/or offset
 *
 * @author gbastien
 * @since 2.0
 */
public class TmfTimestampTransformLinear extends TmfTimestampTransform {

    /**
     * Generated serial UID
     */
    private static final long serialVersionUID = -4756608071358979461L;

    /**
     * Respectively the slope and offset and this linear equation
     */
    private final BigDecimal fAlpha, fBeta;

    private static final MathContext fMc = MathContext.DECIMAL128;

    /**
     * Default constructor
     *
     * @param refTrace
     *            The reference trace
     * @param otherTrace
     *            The other trace
     */
    public TmfTimestampTransformLinear(final String refTrace, final String otherTrace) {
        super(refTrace, otherTrace);
        fAlpha = BigDecimal.ONE;
        fBeta = BigDecimal.ZERO;
    }

    /**
     * Constructor with alpha and beta
     *
     * @param refTrace
     *            The reference trace
     * @param otherTrace
     *            The trace for which to transform the time
     * @param alpha
     *            The slope of the linear transform
     * @param beta
     *            The initial offset of the linear transform
     */
    public TmfTimestampTransformLinear(final String refTrace, final String otherTrace, final double alpha, final double beta) {
        super(refTrace, otherTrace);
        fAlpha = BigDecimal.valueOf(alpha);
        fBeta = BigDecimal.valueOf(beta);
    }

    /**
     * Constructor with alpha and beta in big decimal
     *
     * @param refTrace
     *            The reference trace
     * @param otherTrace
     *            The trace for which to transform the time
     * @param fAlpha2
     *            The slope of the linear transform
     * @param fBeta2
     *            The initial offset of the linear transform
     */
    public TmfTimestampTransformLinear(String refTrace, String otherTrace, BigDecimal fAlpha2, BigDecimal fBeta2) {
        super(refTrace, otherTrace);
        fAlpha = fAlpha2;
        fBeta = fBeta2;
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        BigDecimal newvalue = BigDecimal.valueOf(timestamp.getValue()).multiply(fAlpha, fMc).add(fBeta);
        return new TmfTimestamp(timestamp, newvalue.longValue());
    }

    @Override
    public long transform(long timestamp) {
        BigDecimal t = BigDecimal.valueOf(timestamp).multiply(fAlpha, fMc).add(fBeta);
        return t.longValue();
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof TmfTimestampTransformLinear) {
            TmfTimestampTransformLinear that = (TmfTimestampTransformLinear) other;
            result = (that.getReferenceTrace().equals(getReferenceTrace()) &&
                    that.getOtherTrace().equals(getOtherTrace()) &&
                    (that.fAlpha.equals(fAlpha)) && (that.fBeta.equals(fBeta)) );
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (fBeta.multiply(fAlpha).intValue());
        result = (prime * result) + getReferenceTrace().hashCode();
        result = (prime * result) + getOtherTrace().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TmfTimestampLinear [ alpha = " + fAlpha.toString() + //$NON-NLS-1$
                ", beta = " + fBeta.toString() + //$NON-NLS-1$
                " ]"; //$NON-NLS-1$
    }

}
