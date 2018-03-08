/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *     Patrick Tasse - Updates to mipmap feature
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.statesystem.mipmap.AbstractTmfMipmapStateProvider;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.util.Pair;

/**
 * This class implements additional statistical operations that can be
 * performed on attributes of the state system.
 *
 * @author Patrick Tassé
 */
public final class TmfStateSystemOperations {

    private TmfStateSystemOperations() {}

    /**
     * Return the maximum value of an attribute over a time range
     *
     * @param ss
     *            The state system to query
     * @param t1
     *            The start time of the range
     * @param t2
     *            The end time of the range
     * @param quark
     *            The quark of the attribute
     * @return The maximum value of the attribute in this range
     * @throws TimeRangeException
     *             If an invalid time range is specified
     * @throws AttributeNotFoundException
     *             If the specified quark doesn't match an attribute
     */
    public static ITmfStateValue queryRangeMax(ITmfStateSystem ss, long t1, long t2, int quark) throws AttributeNotFoundException, TimeRangeException {
        ITmfStateValue max = TmfStateValue.nullValue();
        try {
            List<ITmfStateInterval> intervals = queryAttributeRange(ss, t1, t2, quark, AbstractTmfMipmapStateProvider.MAX_STRING);
            if (intervals.size() == 0) {
                return TmfStateValue.nullValue();
            }
            for (ITmfStateInterval si : intervals) {
                ITmfStateValue value = si.getStateValue();

                switch (value.getType()) {
                case DOUBLE:
                    if (max.isNull() || si.getStateValue().unboxDouble() > max.unboxDouble()) {
                        max = si.getStateValue();
                    }
                    break;

                case INTEGER:
                case LONG:
                    if (max.isNull() || si.getStateValue().unboxLong() > max.unboxLong()) {
                        max = si.getStateValue();
                    }
                    break;

                case NULL:
                case STRING:
                default:
                    Activator.logError(unsupOperationMsg(value, "Max")); //$NON-NLS-1$
                    break;
                }
            }

        } catch (StateValueTypeException e) {
            /* Should not happen since we handle all the types in the switch */
            throw new IllegalStateException();
        }
        return max;
    }

    /**
     * Return the minimum value of an attribute over a time range
     *
     * @param ss
     *            The state system to query
     * @param t1
     *            The start time of the range
     * @param t2
     *            The end time of the range
     * @param quark
     *            The quark of the attribute
     * @return The minimum value of the attribute in this range
     * @throws TimeRangeException
     *             If an invalid time range is specified
     * @throws AttributeNotFoundException
     *             If the specified quark doesn't match an attribute
     */
    public static ITmfStateValue queryRangeMin(ITmfStateSystem ss,
            long t1, long t2, int quark)
                    throws AttributeNotFoundException, TimeRangeException {
        ITmfStateValue min = TmfStateValue.nullValue();
        try {
            List<ITmfStateInterval> intervals = queryAttributeRange(ss, t1, t2, quark, AbstractTmfMipmapStateProvider.MIN_STRING);
            if (intervals.size() == 0) {
                return TmfStateValue.nullValue();
            }
            for (ITmfStateInterval si : intervals) {
                ITmfStateValue value = si.getStateValue();

                switch (value.getType()) {
                case DOUBLE:
                    if (min.isNull() || si.getStateValue().unboxDouble() < min.unboxDouble()) {
                        min = si.getStateValue();
                    }
                    break;

                case INTEGER:
                case LONG:
                    if (min.isNull() || si.getStateValue().unboxLong() < min.unboxLong()) {
                        min = si.getStateValue();
                    }
                    break;

                case NULL:
                case STRING:
                default:
                    Activator.logError(unsupOperationMsg(value, "Min")); //$NON-NLS-1$
                }
            }

        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
        return min;
    }

    /**
     * Return the weighted average value of an attribute over a time range
     *
     * @param ss
     *            The state system to query
     * @param t1
     *            The start time of the range
     * @param t2
     *            The end time of the range
     * @param quark
     *            The quark of the attribute
     * @return The weighted average value of the attribute in this range
     * @throws TimeRangeException
     *             If an invalid time range is specified
     * @throws AttributeNotFoundException
     *             If the specified quark doesn't match an attribute
     */
    public static double queryRangeAverage(ITmfStateSystem ss, long t1, long t2, int quark)
            throws AttributeNotFoundException, TimeRangeException {
        double avg = 0.0;
        List<ITmfStateInterval> intervals = queryAttributeRange(ss, t1, t2, quark, AbstractTmfMipmapStateProvider.AVG_STRING);
        if (intervals.size() == 0) {
            return 0;
        } else if (t1 == t2) {
            ITmfStateValue value = intervals.get(0).getStateValue();
            try {
                if (value.getType() == Type.DOUBLE) {
                    return value.unboxDouble();
                }
                return value.unboxLong();
            } catch (StateValueTypeException e) {
                Activator.logError(unsupOperationMsg(value, "Average")); //$NON-NLS-1$
                return -1;
            }
        }
        for (ITmfStateInterval si : intervals) {
            long startTime = Math.max(t1, si.getStartTime());
            long endTime = Math.min(t2, si.getEndTime() + 1);
            long delta = endTime - startTime;
            if (delta > 0) {
                ITmfStateValue value = si.getStateValue();
                try {
                    if (value.getType() == Type.DOUBLE) {
                        avg += si.getStateValue().unboxDouble() * ((double) delta / (double) (t2 - t1));
                    } else {
                        avg += si.getStateValue().unboxLong() * ((double) delta / (double) (t2 - t1));
                    }
                } catch (StateValueTypeException e) {
                    Activator.logError(unsupOperationMsg(value, "Average")); //$NON-NLS-1$
                }
            }
        }
        return avg;
}

    private static List<ITmfStateInterval> queryAttributeRange(ITmfStateSystem ss,
            long t1, long t2, int baseQuark, String featureString)
                    throws AttributeNotFoundException, TimeRangeException {
        Pair<Long, Long> timeRange = new Pair<Long, Long>(t1, t2);
        int mipmapQuark = -1;
        List<ITmfStateInterval> intervals = new ArrayList<ITmfStateInterval>();
        try {
            try {
                mipmapQuark = ss.getQuarkRelative(baseQuark, featureString);
            } catch (AttributeNotFoundException e) {
                /* Not a mipmap attribute, query the base attribute */
                if (t1 == t2) {
                    ITmfStateInterval interval = ss.querySingleState(t1, baseQuark);
                    if (!interval.getStateValue().isNull()) {
                        intervals.add(interval);
                    }
                } else {
                    for (ITmfStateInterval interval : ss.queryHistoryRange(baseQuark, t1, t2)) {
                        if (!interval.getStateValue().isNull()) {
                            intervals.add(interval);
                        }
                    }
                }
                return intervals;
            }
            ITmfStateInterval maxLevelInterval = ss.querySingleState(timeRange.getSecond(), mipmapQuark);
            int levelMax = maxLevelInterval.getStateValue().unboxInt();
            queryMipmapAttributeRange(ss, 0, levelMax, baseQuark, mipmapQuark, timeRange, intervals);
            return intervals;

        } catch (StateValueTypeException e) {
            Activator.logError("State system advertises mipmaps," + //$NON-NLS-1$
                    " but doesn't actually have them."); //$NON-NLS-1$
        } catch (StateSystemDisposedException e) {
            /* We are shutting down, ignore the operation */
        }
        return intervals;
    }

    private static void queryMipmapAttributeRange(ITmfStateSystem ss,
            int currentLevel, int levelMax, int baseQuark, int mipmapQuark,
            Pair<Long, Long> timeRange, List<ITmfStateInterval> intervals)
                    throws AttributeNotFoundException, TimeRangeException {
        int level = currentLevel;
        Pair<Long, Long> range = timeRange;
        ITmfStateInterval currentLevelInterval = null, nextLevelInterval = null;
        if (range == null || range.getFirst() > range.getSecond()) {
            return;
        }
        if (level > levelMax || level < 0) {
            return;
        }
        try {
            if (range.getFirst().longValue() == range.getSecond().longValue()) {
                level = 0;
                currentLevelInterval = ss.querySingleState(range.getFirst(), baseQuark);
                if (!currentLevelInterval.getStateValue().isNull()) {
                    intervals.add(currentLevelInterval);
                }
                return;
            }
            if (level < levelMax) {
                int levelQuark = ss.getQuarkRelative(mipmapQuark, String.valueOf(level + 1));
                nextLevelInterval = ss.querySingleState(range.getFirst(), levelQuark);
            }

            if (nextLevelInterval != null && isFullyOverlapped(range, nextLevelInterval)) {
                if (nextLevelInterval.getStateValue().isNull()) {
                    range = updateTimeRange(range, nextLevelInterval);
                } else {
                    level++;
                }
                queryMipmapAttributeRange(ss, level, levelMax, baseQuark, mipmapQuark, range, intervals);
                return;
            }

            if (level == 0) {
                currentLevelInterval = ss.querySingleState(range.getFirst(), baseQuark);
            } else {
                int levelQuark = ss.getQuarkRelative(mipmapQuark, String.valueOf(level));
                currentLevelInterval = ss.querySingleState(range.getFirst(), levelQuark);
            }

            if (currentLevelInterval != null && isFullyOverlapped(range, currentLevelInterval)) {
                if (!currentLevelInterval.getStateValue().isNull()) {
                    intervals.add(currentLevelInterval);
                }
                range = updateTimeRange(range, currentLevelInterval);
            } else {
                if (level == 0) {
                    if (currentLevelInterval == null) {
                        return;
                    }
                    if (!currentLevelInterval.getStateValue().isNull()) {
                        intervals.add(currentLevelInterval);
                    }
                    range = updateTimeRange(range, currentLevelInterval);
                } else {
                    level--;
                }
            }

            queryMipmapAttributeRange(ss, level, levelMax, baseQuark,
                    mipmapQuark, range, intervals);

        } catch (StateSystemDisposedException e) {
            /* We are shutting down, ignore the operation */
        }
    }

    private static Pair<Long, Long> updateTimeRange(Pair<Long, Long> timeRange,
            ITmfStateInterval currentLevelInterval) {
        if (currentLevelInterval.getEndTime() >= timeRange.getSecond()) {
            return null;
        }
        long startTime = Math.max(timeRange.getFirst(),
                Math.min(currentLevelInterval.getEndTime() + 1, timeRange.getSecond()));
        return new Pair<Long, Long>(startTime, timeRange.getSecond());
    }

    private static boolean isFullyOverlapped(Pair<Long, Long> range,
            ITmfStateInterval interval) {
        if (range.getFirst() >= range.getSecond() ||
                interval.getStartTime() >= interval.getEndTime()) {
            return false;
        }
        if (range.getFirst() <= interval.getStartTime() &&
                range.getSecond() >= interval.getEndTime()) {
            return true;
        }
        return false;
    }

    /**
     * Error message for unsupported operations
     */
    private static String unsupOperationMsg(ITmfStateValue value, String operation) {
        return "State value " + value.toString() + //$NON-NLS-1$
                " of type " + value.getType().name() + //$NON-NLS-1$
                " does not support operation " + operation + '.'; //$NON-NLS-1$
    }

}
