/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.ui.wizards.graph;

import java.util.LinkedList;

import org.eclipse.linuxtools.internal.systemtap.graphing.ui.GraphingUIPlugin;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.Localization;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.AreaChartBuilder;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.BarChartBuilder;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.LineChartBuilder;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.PieChartBuilder;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.ScatterChartBuilder;
import org.eclipse.linuxtools.systemtap.graphing.core.adapters.BlockAdapter;
import org.eclipse.linuxtools.systemtap.graphing.core.adapters.ScrollAdapter;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IBlockDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IHistoricalDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphing.ui.charts.AbstractChartBuilder;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.GraphComposite;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.GraphContinuousXControl;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.GraphContinuousYControl;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.GraphDiscreteXControl;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;


public final class GraphFactory {
    private static final String[] graphNames = new String[] {
        Localization.getString("GraphFactory.ScatterGraph"), //$NON-NLS-1$
        Localization.getString("GraphFactory.LineGraph"), //$NON-NLS-1$
        Localization.getString("GraphFactory.BarGraph"), //$NON-NLS-1$
        Localization.getString("GraphFactory.AreaGraph"), //$NON-NLS-1$
        Localization.getString("GraphFactory.PieChart"), //$NON-NLS-1$
    };

    private static final String[] graphDescriptions = new String[] {
        Localization.getString("GraphFactory.ScatterDescription"), //$NON-NLS-1$
        Localization.getString("GraphFactory.LineDescription"), //$NON-NLS-1$
        Localization.getString("GraphFactory.BarDescription"), //$NON-NLS-1$
        Localization.getString("GraphFactory.AreaDescription"), //$NON-NLS-1$
        Localization.getString("GraphFactory.PieDescription"), //$NON-NLS-1$
    };

    private static final Image[] graphImages = new Image[] {
        GraphingUIPlugin.getImageDescriptor("icons/graphs/ScatterGraph.gif").createImage(), //$NON-NLS-1$
        GraphingUIPlugin.getImageDescriptor("icons/graphs/LineGraph.gif").createImage(), //$NON-NLS-1$
        GraphingUIPlugin.getImageDescriptor("icons/graphs/BarGraph.gif").createImage(), //$NON-NLS-1$
        GraphingUIPlugin.getImageDescriptor("icons/graphs/AreaChart.gif").createImage(), //$NON-NLS-1$
        GraphingUIPlugin.getImageDescriptor("icons/graphs/PieChart.gif").createImage(), //$NON-NLS-1$
    };

    private static final String[] graphIDs = new String[] {
        ScatterChartBuilder.ID,
        LineChartBuilder.ID,
        BarChartBuilder.ID,
        AreaChartBuilder.ID,
        PieChartBuilder.ID,
    };

    public static String[] getAvailableGraphs(IDataSet data) {
        LinkedList<String> ids = new LinkedList<>();
        if(data instanceof IHistoricalDataSet) {
            ids.add(ScatterChartBuilder.ID);
            ids.add(LineChartBuilder.ID);
            ids.add(AreaChartBuilder.ID);
            ids.add(BarChartBuilder.ID);
            ids.add(PieChartBuilder.ID);
        }
        if(data instanceof IBlockDataSet) {
            if(!ids.contains(BarChartBuilder.ID)) {
                ids.add(BarChartBuilder.ID);
            }
            ids.add(PieChartBuilder.ID);
        }

        String[] id = new String[0];
        return ids.toArray(id);
    }

    public static String getGraphName(String id) {
        int index = getIndex(id);
        if(index >= 0) {
            return graphNames[index];
        }
        return null;
    }

    public static String getGraphDescription(String id) {
        int index = getIndex(id);
        if(index >= 0) {
            return graphDescriptions[index];
        }
        return null;
    }

    public static Image getGraphImage(String id) {
        int index = getIndex(id);
        if(index >= 0) {
            return graphImages[index];
        }
        return null;
    }

    // TODO leave in id paramter to cover possible future support of more graph types.
    public static boolean isMultiGraph(@SuppressWarnings("unused") String id) {
        return true;
    }

    public static AbstractChartBuilder createGraph(GraphComposite comp,
            int style, GraphData gd, IDataSet data) {
        AbstractChartBuilder builder = null;

        switch (getIndex(gd.graphID)) {
            case 0:
                builder = new ScatterChartBuilder(comp, style, gd.title,
                        new ScrollAdapter((IHistoricalDataSet) data, gd.xSeries,
                                gd.ySeries, gd.key));
                break;
            case 1:
                builder = new LineChartBuilder(comp, style, gd.title,
                        new ScrollAdapter((IHistoricalDataSet) data, gd.xSeries,
                                gd.ySeries, gd.key));
                break;
            case 2:
                if (!(data instanceof IBlockDataSet) || (null != gd.key)) {
                    builder = new BarChartBuilder(comp, style, gd.title,
                            new ScrollAdapter((IHistoricalDataSet) data,
                                    gd.xSeries, gd.ySeries, gd.key));

                } else {
                    builder = new BarChartBuilder(comp, style, gd.title,
                            new BlockAdapter((IBlockDataSet) data, gd.xSeries,
                                    gd.ySeries));

                }
                break;
            case 3:
                builder = new AreaChartBuilder(comp, style, gd.title,
                        new ScrollAdapter((IHistoricalDataSet) data, gd.xSeries,
                                gd.ySeries, gd.key));
                break;
            case 4:
                builder = new PieChartBuilder(comp, style, gd.title,
                        new ScrollAdapter((IHistoricalDataSet) data, gd.xSeries,
                                gd.ySeries, gd.key));

                break;
        }
        return builder;
    }

    /**
     * @param comp Parent composite.
     * @param style Style of widget to create.
     * @return The newly created X axis control.
     * @since 3.0
     */
    public static final Composite createGraphXControl(GraphComposite comp, int style) {
        AbstractChartBuilder builder = comp.getCanvas();
        if (builder instanceof BarChartBuilder || builder instanceof PieChartBuilder) {
            return new GraphDiscreteXControl(comp, style);
        }
        return new GraphContinuousXControl(comp, style);
    }

    /**
     * @param comp Parent composite.
     * @param style Style of widget to create.
     * @return The newly created Y axis control
     * @since 3.0
     */
    public static final Composite createGraphYControl(GraphComposite comp, int style) {
        AbstractChartBuilder builder = comp.getCanvas();
        if (builder instanceof PieChartBuilder) {
            return null;
        }
        return new GraphContinuousYControl(comp, style);
    }

    private static int getIndex(String id) {
        for(int i=0; i<graphIDs.length; i++) {
            if(id.equals(graphIDs[i])) {
                return i;
            }
        }
        return -1;
    }
}
