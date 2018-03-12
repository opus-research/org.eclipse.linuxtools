package org.eclipse.linuxtools.internal.tmf.core.synchronization;

import java.math.BigDecimal;
import java.math.MathContext;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Fast linear timestamp transform.
 *
 * Reduce the use of BigDecimal for an interval of time where the transform
 * can be computed only with integer math. By rearranging the linear equation to:
 *
 * alphaLong = alpha * m
 * f(t) = (alphaLong * (t - ts)) / m + beta
 *
 * The slope applies to a relative time instead of absolute timestamp from
 * epoch, that reduce the width of the factors and allow using standard
 * integer arithmetic, while preserving precision. Because of rounding at each
 * step, there may be a difference of +3ns between the value computed by the
 * fast transform compared to BigDecimal.
 *
 * @author Francis Giraldeau <francis.giraldeau@gmail.com>
 *
 */
public class TmfTimestampTransformLinearFast extends TmfTimestampTransformLinear {

    private static final long serialVersionUID = 2398540405078949739L;
    private long scaleOffset;
    private long start;
    private long fAlphaLong;
    private long fBetaLong;
    private long fScaleMiss;
    private long fScaleHit;
    private int hc;
    private static final int tsBitWidth = 30;
    private static final HashFunction hf = Hashing.goodFastHash(32);

    private static final MathContext fMc = MathContext.DECIMAL128;

    /**
     * @param xform construct a fast transform for the linear transform
     */
    public TmfTimestampTransformLinearFast(TmfTimestampTransformLinear xform) {
        super(xform.getAlpha(), xform.getBeta());
        fAlphaLong = xform.getAlpha().multiply(BigDecimal.valueOf(1 << tsBitWidth)).longValue() ;
        fBetaLong = xform.getBeta().longValue();
        start = 0L;
        scaleOffset = 0L;
        fScaleMiss = 0;
        fScaleHit = 0;
        hc = hf.newHasher()
                .putLong(getAlpha().longValue())
                .putLong(getBeta().longValue())
                .hash()
                .asInt();
    }

    private long apply(long ts) {
        // rescale if we exceed the safe range
        long absDelta = Math.abs(ts - start);
        if (absDelta > (1 << tsBitWidth)) {
            scaleOffset = BigDecimal.valueOf(ts).multiply(getAlpha(), fMc).longValue() + fBetaLong;
            start = ts;
            absDelta = 0;
            fScaleMiss++;
        } else {
            fScaleHit++;
        }
        long x = (fAlphaLong * absDelta) >>> tsBitWidth;
        if (ts < start) {
             x = -x;
        }
        return x + scaleOffset;
    }

    @Override
    public ITmfTimestamp transform(ITmfTimestamp timestamp) {
        return new TmfTimestamp(timestamp, apply(timestamp.getValue()));
    }

    @Override
    public long transform(long timestamp) {
        return apply(timestamp);
    }

    /**
     * A cache miss occurs when the timestamp is out of the range for integer
     * computation, and therefore requires using BigDecimal for re-scaling.
     *
     * @return number of misses
     */
    public long getCacheMisses() {
        return fScaleMiss;
    }

    /**
     * A scale hit occurs if the timestamp is in the range for fast transform.
     *
     * @return number of hits
     */
    public long getCacheHits() {
        return fScaleHit;
    }

    /**
     * Reset scale misses to zero
     */
    public void resetScaleStats() {
        fScaleMiss = 0;
        fScaleHit = 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TmfTimestampTransformLinearFast) {
            TmfTimestampTransformLinearFast that = (TmfTimestampTransformLinearFast) other;
            return this.getAlpha().equals(that.getAlpha()) &&
                    this.getBeta().equals(that.getBeta());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hc;
    }


}