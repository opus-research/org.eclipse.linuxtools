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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class implementing fully incremental trace synchronization approach as
 * described in
 *
 * Masoume Jabbarifar, Michel Dagenais and Alireza Shameli-Sendi,
 * "Streaming Mode Incremental Clock Synchronization"
 *
 * @author gbastien
 * @since 2.0
 */
public class SyncAlgorithmFullyIncremental extends SynchronizationAlgorithm {

    /**
     *
     */
    private static final long serialVersionUID = -1782788842774838830L;
    /*
     * The list of meaningful points on the upper hull (received by the
     * reference trace, below in a graph)
     */
    private final LinkedList<SyncPoint> fUpperBoundList = new LinkedList<SyncPoint>();
    /*
     * The list of meaninful points on the lower hull (sent by the reference
     * trace, above in a graph)
     */
    private final LinkedList<SyncPoint> fLowerBoundList = new LinkedList<SyncPoint>();

    /* Points forming the line with maximum slope */
    private final SyncPoint[] fLmax;
    /* Points forming the line with minimum slope */
    private final SyncPoint[] fLmin;

    /*
     * Slopes and ordinate at origin of respectively fLmin, fLmax and the
     * bisector
     */
    private BigDecimal fAlphamin, fBetamax, fAlphamax, fBetamin, fAlpha, fBeta;

    private int fNbMatches, fNbAccurateMatches;
    private String fReferenceTrace = "", fOtherTrace = "";  //$NON-NLS-1$//$NON-NLS-2$
    private syncQuality fQuality;

    private Map<String, Object> fStats = new LinkedHashMap<String, Object>();

    private final MathContext fMc = MathContext.DECIMAL128;

    /**
     * Initialization of the attributes
     */
    public SyncAlgorithmFullyIncremental() {
        super();
        fLmax = new SyncPoint[2];
        fLmin = new SyncPoint[2];
        fAlpha = BigDecimal.ONE;
        fAlphamax = BigDecimal.ONE;
        fAlphamin = BigDecimal.ONE;
        fBeta =  BigDecimal.ZERO;
        fBetamax =  BigDecimal.ZERO;
        fBetamin =  BigDecimal.ZERO;
        fNbMatches = 0;
        fNbAccurateMatches = 0;
        fQuality = syncQuality.ABSENT;
    }

    /**
     * Function called after all matching has been done, to do any post-match
     * treatment. For this class, it calculates stats, while the data is
     * available
     */
    @Override
    public void matchingEnded() {
        getStats();
    }

    @Override
    protected void processMatch(TmfEventDependency match) {

        LinkedList<SyncPoint> boundList, otherBoundList;

        SyncPoint[] line, otherLine;
        SyncPoint p;
        int inversionFactor = 1;
        boolean qualify = false;
        fNbMatches++;

        /* Initialize data depending on the which hull the match is part of */
        if (match.getSourceEvent().getTrace().getName().compareTo(match.getDestinationEvent().getTrace().getName()) > 0) {
            fReferenceTrace = match.getDestinationEvent().getTrace().getName();
            fOtherTrace = match.getSourceEvent().getTrace().getName();
            boundList = fUpperBoundList;
            otherBoundList = fLowerBoundList;
            line = fLmin;
            otherLine = fLmax;
            p = new SyncPoint(match.getDestinationEvent(), match.getSourceEvent());
            inversionFactor = 1;
        } else {
            boundList = fLowerBoundList;
            otherBoundList = fUpperBoundList;
            line = fLmax;
            otherLine = fLmin;
            p = new SyncPoint(match.getSourceEvent(), match.getDestinationEvent());
            inversionFactor = -1;
        }

        /*
         * Does the message qualify for the hull, or is in on the wrong side of
         * the reference line
         */
        if ((line[0] == null) || (line[1] == null) || (p.crossProduct(line[0], line[1]) * inversionFactor > 0)) {
            /*
             * If message qualifies, verify if points need to be removed from
             * the hull and add the new point as the maximum reference point for
             * the line.
             * Also clear the stats that are not good anymore
             */
            fNbAccurateMatches++;
            qualify = true;
            removeUselessPoints(p, boundList, inversionFactor);
            line[1] = p;
            fStats.clear();
        }

        /*
         * Adjust the boundary of the reference line and if one of the reference
         * point of the other line was removed from the hull, also adjust the
         * other line
         */
        adjustBound(line, otherBoundList, inversionFactor);
        if ( (otherLine[1] != null ) && !boundList.contains(otherLine[0])) {
            adjustBound(otherLine, boundList, inversionFactor * -1);
        }

        if (qualify) {
            approximateSync();
        }

    }

    /*
     * Calculates slopes and ordinate at origin of fLmax and fLmin to obtain and
     * approximation of the synchronization at this time
     */
    private void approximateSync() {
        /*
         * Line slopes functions
         *
         * Lmax = alpha_max T + beta_min
         *
         * Lmin = alpha_min T + beta_max
         */
        if ((fLmax[0] != null) || (fLmin[0] != null)) {
            fAlphamax = fLmax[1].getAlpha(fLmax[0]);
            fBetamin = fLmax[1].getBeta(fAlphamax);
            fAlphamin = fLmin[1].getAlpha(fLmin[0]);
            fBetamax = fLmin[1].getBeta(fAlphamin);
            fAlpha = fAlphamax.add(fAlphamin).divide(BigDecimal.valueOf(2), fMc);
            fBeta = fBetamin.add(fBetamax).divide(BigDecimal.valueOf(2), fMc);
            if ( (fLmax[0] == null) || (fLmin[0] == null) ) {
                fQuality = syncQuality.APPROXIMATE;
            }
            else if (fAlphamax.compareTo(fAlphamin) > 0 ) {
                fQuality = syncQuality.ACCURATE;
            } else {
                /* Lines intersect */
                fQuality = syncQuality.FAIL;
            }
        } else if (((fLmax[0] == null) && (fLmin[1] == null))
                || ((fLmax[1] == null) && (fLmin[0] == null))) {
            /* Either there is no upper hull point or no lower hull */
            fQuality = syncQuality.INCOMPLETE;
        }
    }

    /*
     * Verify if the line should be adjusted to be more accurate give the hull
     */
    private static void adjustBound(SyncPoint[] line, LinkedList<SyncPoint> otherBoundList, int inversionFactor) {

        int i = 0;
        SyncPoint minPoint = null, nextPoint;
        boolean finishedSearch = false;

        /*
         * Find in the other bound, the origin point of the line, start from the
         * beginning if the point was lost
         */
        i = Math.max(0, otherBoundList.indexOf(line[0]));

        while ((i < otherBoundList.size() - 1) && !finishedSearch) {
            minPoint = otherBoundList.get(i);
            nextPoint = otherBoundList.get(i + 1);

            /*
             * If the rotation (cross-product) is not optimal, move to next
             * point as reference for the line (if available)
             *
             * Otherwise, the current minPoint is the minPoint of the line
             */
            if (minPoint.crossProduct(nextPoint, line[1]) * inversionFactor > 0) {
                if (nextPoint.getTimeX() < line[1].getTimeX()) {
                    i++;
                } else {
                    line[0] = null;
                    finishedSearch = true;
                }
            } else {
                line[0] = minPoint;
                finishedSearch = true;
            }
        }

        if (line[0] == null) {
            line[0] = minPoint;
        }

        /* Make sure point 0 is before point 1 */
        if ( (line[0] != null) && ( line[0].getTimeX() > line[1].getTimeX() ) ) {
            line[0] = null;
        }
    }

    /*
     * When a point qualifies to be in a hull, we verify if any of the existing
     * points need to be removed from the hull
     */
    private static void removeUselessPoints(final SyncPoint p, final LinkedList<SyncPoint> boundList, final int inversionFactor) {

        boolean checkRemove = true;

        while (checkRemove && boundList.size() >= 2) {
            if (p.crossProduct(boundList.get(boundList.size() - 2), boundList.getLast()) * inversionFactor > 0) {
                boundList.removeLast();
            } else {
                checkRemove = false;
            }
        }
        boundList.addLast(p);
    }

    @Override
    public ITmfTimestampTransform getTimestampTransform(ITmfTrace trace) {
        if (trace.getName().equals(fOtherTrace)) {
            return new TmfTimestampTransformLinear(fReferenceTrace, fOtherTrace, BigDecimal.ONE.divide(fAlpha, fMc), BigDecimal.valueOf(-1).multiply(fBeta).divide(fAlpha, fMc)); // 1 / fAlpha, -1 * fBeta / fAlpha);
        }
        return TmfTimestampTransform.IDENTITY;
    }

    @Override
    public ITmfTimestampTransform getTimestampTransform(String name) {
        if (name.equals(fOtherTrace)) {
            return new TmfTimestampTransformLinear(fReferenceTrace, fOtherTrace, BigDecimal.ONE.divide(fAlpha, fMc), BigDecimal.valueOf(-1).multiply(fBeta).divide(fAlpha, fMc)); // 1 / fAlpha, -1 * fBeta / fAlpha);
        }
        return TmfTimestampTransform.IDENTITY;
    }


    @Override
    public boolean isTraceSynced(String name) {
        return (name.equals(fOtherTrace));
    }

    /**
     * Rename one of the traces in the synchronization
     *
     * @param oldname The name of the original trace
     * @param newname The new name of the trace
     */
    @Override
    public void renameTrace(String oldname, String newname) {
        if (oldname.equals(fOtherTrace)) {
            fOtherTrace = newname;
        } else if (oldname.equals(fReferenceTrace)) {
            fReferenceTrace = newname;
        }
    }

    @Override
    public Map<String, Object> getStats() {
        if (fStats.size() == 0) {
            String syncQuality;
            switch (fQuality) {
            case ABSENT:
                syncQuality = Messages.SyncAlgorithmFullyIncremental_absent;
                break;
            case ACCURATE:
                syncQuality = Messages.SyncAlgorithmFullyIncremental_accurate;
                break;
            case APPROXIMATE:
                syncQuality = Messages.SyncAlgorithmFullyIncremental_approx;
                break;
            case INCOMPLETE:
                syncQuality = Messages.SyncAlgorithmFullyIncremental_incomplete;
                break;
            // $CASES-OMITTED$
            default:
                syncQuality = Messages.SyncAlgorithmFullyIncremental_fail;
                break;

            }

            fStats.put(Messages.SyncAlgorithmFullyIncremental_reftrace, fReferenceTrace);
            fStats.put(Messages.SyncAlgorithmFullyIncremental_othertrace, fOtherTrace);
            fStats.put(Messages.SyncAlgorithmFullyIncremental_quality, syncQuality);
            fStats.put(Messages.SyncAlgorithmFullyIncremental_alpha, fAlpha);
            fStats.put(Messages.SyncAlgorithmFullyIncremental_beta, fBeta);
            fStats.put(Messages.SyncAlgorithmFullyIncremental_ub, (fUpperBoundList.size() == 0) ? Messages.SyncAlgorithmFullyIncremental_NA : fUpperBoundList.size());
            fStats.put(Messages.SyncAlgorithmFullyIncremental_lb, (fLowerBoundList.size() == 0) ? Messages.SyncAlgorithmFullyIncremental_NA : fLowerBoundList.size());
            fStats.put(Messages.SyncAlgorithmFullyIncremental_accuracy, fAlphamax.subtract(fAlphamin).longValue()); // - fAlphamin);
            fStats.put(Messages.SyncAlgorithmFullyIncremental_nbmatch, (fNbMatches == 0) ? Messages.SyncAlgorithmFullyIncremental_NA : fNbMatches);
            fStats.put(Messages.SyncAlgorithmFullyIncremental_nbacc, (fNbAccurateMatches == 0) ? Messages.SyncAlgorithmFullyIncremental_NA : fNbAccurateMatches);
            fStats.put(Messages.SyncAlgorithmFullyIncremental_refformula, Messages.SyncAlgorithmFullyIncremental_T_ + fReferenceTrace);
            fStats.put(Messages.SyncAlgorithmFullyIncremental_otherformula, fAlpha + Messages.SyncAlgorithmFullyIncremental_mult + Messages.SyncAlgorithmFullyIncremental_T_ + fReferenceTrace + Messages.SyncAlgorithmFullyIncremental_add + fBeta);
        }
        return fStats;

    }

    /*
     * Private class representing a point to synchronize on a graph. The x axis
     * is the timestamp of the event from the reference trace while the y axis
     * is the timestamp of the event on the other trace
     */
    private class SyncPoint {
        private final ITmfTimestamp x, y;

        public SyncPoint(ITmfEvent ex, ITmfEvent ey) {
            x = ex.getTimestamp();
            y = ey.getTimestamp();
        }

        public long getTimeX() {
            return x.getValue();
        }

        /*
         * Calculate a cross product of 3 points:
         *
         * If the cross-product < 0, then p, pa, pb are clockwise
         *
         * If the cross-product > 0, then p, pa, pb are counter-clockwise
         *
         * If cross-product == 0, then they are in a line
         */
        public long crossProduct(SyncPoint pa, SyncPoint pb) {
            long cp = ((pa.x.getValue() - x.getValue()) * (pb.y.getValue() - y.getValue()) - (pa.y.getValue() - y.getValue()) * (pb.x.getValue() - x.getValue()));
            return cp;
        }

        /*
         * Gets the alpha (slope) between two points
         */
        public BigDecimal getAlpha(SyncPoint p1) {
            if (p1 == null) {
                return BigDecimal.ONE;
            }
            BigDecimal deltay = BigDecimal.valueOf(y.getValue() - p1.y.getValue());
            BigDecimal deltax = BigDecimal.valueOf(x.getValue() - p1.x.getValue());
            if (deltax.equals(BigDecimal.ZERO)){
                return BigDecimal.ONE;
            }
            return deltay.divide(deltax, fMc);
        }

        /*
         * Get the beta value (when x = 0) of the line given alpha
         */
        public BigDecimal getBeta(BigDecimal alpha) {
            return BigDecimal.valueOf(y.getValue()).subtract( alpha.multiply(BigDecimal.valueOf(x.getValue()), fMc)); // * x.getValue();
        }

    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("SyncAlgorithmFullyIncremental [ parent: ");
        b.append(super.toString());
        b.append(" alpha " + fAlpha + " beta " + fBeta + " ]");
        return b.toString();
    }

    private void writeObject(ObjectOutputStream s)
            throws IOException {
        /*
         * Remove calculation data because most of it is not serializable. We
         * have the statistics anyway
         */
        fUpperBoundList.clear();
        fLowerBoundList.clear();
        fLmin[0] = null;
        fLmin[1] = null;
        fLmax[0] = null;
        fLmax[1] = null;
        s.defaultWriteObject();

    }

}
