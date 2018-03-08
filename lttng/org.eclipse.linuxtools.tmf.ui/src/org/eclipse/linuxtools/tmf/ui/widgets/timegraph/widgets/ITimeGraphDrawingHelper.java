package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

/**
 * @author gbastien
 * @since 2.0
 *
 */
public interface ITimeGraphDrawingHelper {

    /**
     * Return the x coordinate corresponding to a time
     *
     * @param time the time
     * @return the x coordinate corresponding to the time
     *
     * @since 2.0
     */
    public int getXForTime(long time);

    /**
     * Return the time corresponding to an x coordinate
     *
     * @param x the x coordinate
     * @return the time corresponding to the x coordinate
     *
     * @since 2.0
     */
    public long getTimeAtX(int x);
}
