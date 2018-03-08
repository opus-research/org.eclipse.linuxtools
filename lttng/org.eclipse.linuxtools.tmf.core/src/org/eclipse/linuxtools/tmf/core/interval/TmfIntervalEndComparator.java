package org.eclipse.linuxtools.tmf.core.interval;

import java.util.Comparator;

/**
 * Comparator for ITmfStateInterval, using their *end times*. Making intervals
 * Comparable wouldn't be clear if it's using their start or end times (or maybe
 * even values), so separate comparators are provided.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfIntervalEndComparator implements Comparator<ITmfStateInterval> {

    @Override
    public int compare(ITmfStateInterval o1, ITmfStateInterval o2) {
        long e1 = o1.getEndTime();
        long e2 = o2.getEndTime();

        if (e1 < e2) {
            return -1;
        } else if (e1 > e2) {
            return 1;
        } else {
            return 0;
        }
    }

}
