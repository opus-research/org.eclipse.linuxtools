/****************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - ongoing maintenance
 ****************************************************************
 */
package org.eclipse.linuxtools.systemtap.graphing.ui.charts;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.GraphingUIPlugin;
import org.eclipse.linuxtools.internal.systemtap.graphing.ui.preferences.GraphingPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphing.core.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.graphing.ui.charts.listeners.AbstractChartMouseMoveListener;
import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.ITitle;

/**
 * A {@link Composite} that provides the common members and the framework to build one chart.
 * @author Qi Liang
 */
public abstract class AbstractChartBuilder extends Composite implements IUpdateListener {

    /**
     * Font name for all titles, labels, and values.
     */
    protected final static String FONT_NAME = "MS Sans Serif"; //$NON-NLS-1$
    protected int maxItems;
    protected double scale = 1.0;
    /**
     * @since 3.0
     */
    protected double scaleY = 1.0;
    /**
     * @since 3.0
     */
    protected double scroll = 1.0;
    /**
     * @since 3.0
     */
    protected double scrollY = 1.0;

    /**
     * Provides data for chart.
     */
    protected IAdapter adapter = null;

    protected static final Color WHITE = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);
    protected static final Color BLACK = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    protected static final Color RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);

    protected static final Color[] COLORS = {
        RED,
        Display.getDefault().getSystemColor(SWT.COLOR_GREEN),
        Display.getDefault().getSystemColor(SWT.COLOR_BLUE),
        Display.getDefault().getSystemColor(SWT.COLOR_YELLOW),
        Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA),
        Display.getDefault().getSystemColor(SWT.COLOR_CYAN),
        BLACK,
        new Color(Display.getDefault(), 64, 128, 128),
        new Color(Display.getDefault(), 255, 165, 0),
        new Color(Display.getDefault(), 128, 128, 128),
    };
    /**
     * Chart instance.
     */
    protected Chart chart = null;

    /**
     * Chart title.
     */
    protected String title = null;

    private List<IUpdateListener> listeners = new ArrayList<>();

    /**
     * The mouse listener that watches for MouseMove events over a specified region.
     * It is null by default.
     * @since 3.0
     */
    protected AbstractChartMouseMoveListener chartMouseMoveListener = null;

    /**
     * If a mouse listener is registered, returns a message with details on the mouse's
     * current position on the chart. This method is primarily used for testing purposes.
     * @return A String message if a mouse listener is registered; null otherwise.
     * @since 3.2
     */
    public String getMouseMessage() {
        return chartMouseMoveListener != null ? chartMouseMoveListener.getMouseMessage() : null;
    }

    /**
     * A reference to the SystemTap Graphing preference store.
     * @since 3.0
     */
    protected IPreferenceStore store;

    /**
     * Updates the chart with properties read from user-set preferences. It is called automatically
     * whenever a change is made to SystemTap Graphing preferences.
     * @param event The update event containing details on the preference that was changed.
     * @since 3.0
     */
    protected void updateProperties(PropertyChangeEvent event) {
        if (event.getProperty().equals(GraphingPreferenceConstants.P_VIEWABLE_DATA_ITEMS)
                || event.getProperty().equals(GraphingPreferenceConstants.P_MAX_DATA_ITEMS)) {
            maxItems = Math.min(store.getInt(GraphingPreferenceConstants.P_VIEWABLE_DATA_ITEMS),
                    store.getInt(GraphingPreferenceConstants.P_MAX_DATA_ITEMS));
            updateDataSet();
        }
    }
    private IPropertyChangeListener propertyChangeListener;

    /**
     * Constructs a chart builder and associates it to one data set.
     * @param adapter An {@link IAdapter} for reading from the chart's data set.
     * @param parent The parent {@link Composite} that will contain this chart builder.
     * @param style The style of the chart to construct.
     * @param title The title of the chart to construct.
     */
    public AbstractChartBuilder(IAdapter adapter, Composite parent, int style, String title) {
        super(parent, style);
        this.adapter = adapter;
        this.title = title;
        this.setLayout(new FillLayout());

        store = GraphingUIPlugin.getDefault().getPreferenceStore();
        maxItems = Math.min(store.getInt(GraphingPreferenceConstants.P_VIEWABLE_DATA_ITEMS),
                store.getInt(GraphingPreferenceConstants.P_MAX_DATA_ITEMS));

        propertyChangeListener = event -> updateProperties(event);
        store.addPropertyChangeListener(propertyChangeListener);
    }

    @Override
    public void dispose() {
        store.removePropertyChangeListener(propertyChangeListener);
        propertyChangeListener = null;
        super.dispose();
    }

    /**
     * Builds one chart.
     */
    public void build() {
        createChart();
        buildPlot();
        buildLegend();
        buildTitle();
        buildXAxis();
        buildYAxis();
        buildXSeries();
        buildYSeries();
        updateDataSet();
    }

    /**
     * Creates chart instance.
     */
    protected void createChart() {
        this.chart = new Chart(this, getStyle());
    }

    /**
     * Builds plot.
     */
    private void buildPlot() {
        this.chart.setBackground(WHITE);
        this.chart.setBackgroundInPlotArea(WHITE);
    }

    /**
     * Builds X axis.
     */
    protected abstract void buildXAxis();

    /**
     * Builds Y axis.
     */
    protected abstract void buildYAxis();

    /**
     * Builds X series.
     */
    protected abstract void buildXSeries();

    /**
     * Builds Y series.
     */
    protected abstract void buildYSeries();

    public abstract void updateDataSet();

    /**
     * Builds legend.
     *
     */
    private void buildLegend() {
        chart.getLegend().setPosition(SWT.RIGHT);
    }

    /**
     * Builds the chart title.
     */
    private void buildTitle() {
        ITitle ctitle = chart.getTitle();
        ctitle.setForeground(BLACK);
        ctitle.setText(this.title);
    }

    /**
     * Returns the chart instance.
     *
     * @return the chart instance
     */
    public Chart getChart() {
        return chart;
    }

    public void setScale(double scale) {
        if (scale < 0) {
            this.scale = 0;
        } else if (scale > 1) {
            this.scale = 1;
        } else {
            this.scale = scale;
        }
        handleUpdateEvent();
    }

    /**
     * @since 3.0
     * @return The current horizontal scale of the chart.
     */
    public double getScale() {
        return this.scale;
    }

    /**
     * @param scale The desired vertical scale of the chart.
     * @since 3.0
     */
    public void setScaleY(double scale) {
        if (scale < 0) {
            this.scaleY = 0;
        } else if (scale > 1) {
            this.scaleY = 1;
        } else {
            this.scaleY = scale;
        }
        handleUpdateEvent();
    }

    /**
     * @since 3.0
     * @return The current vertical scale of the chart.
     */
    public double getScaleY() {
        return this.scaleY;
    }

    /**
     * @param scroll The desired horizontal scroll of the chart.
     * @since 3.0
     */
    public void setScroll(double scroll) {
        if (scroll < 0) {
            this.scroll = 0;
        } else if (scroll > 1) {
            this.scroll = 1;
        } else {
            this.scroll = scroll;
        }
        handleUpdateEvent();
    }

    /**
     * @since 3.0
     * @return The current horizontal scroll of the chart.
     */
    public double getScroll() {
        return this.scroll;
    }

    /**
     * @param scroll The desired vertical scroll of the chart.
     * @since 3.0
     */
    public void setScrollY(double scroll) {
        if (scroll < 0) {
            this.scrollY = 0;
        } else if (scroll > 1) {
            this.scrollY = 1;
        } else {
            this.scrollY = scroll;
        }
        handleUpdateEvent();
    }

    /**
     * @since 3.0
     * @return The current vertical scroll of the chart.
     */
    public double getScrollY() {
        return this.scrollY;
    }

    /**
     * Converts a value into its {@link Double} equivalent.
     * @param o The object to convert to a {@link Double}.
     * @return The object in the form of a {@link Double}. May be <code>null</code>
     * if conversion is not possible, or if the object was null in the first place.
     * @since 3.0
     */
    protected Double getDoubleOrNullValue(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return ((Integer)o).doubleValue();
        }
        if (o instanceof Double) {
            return (Double) o;
        }
        try {
            return new Double(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void handleUpdateEvent() {
        if (chart != null && !chart.isDisposed()) {
            repaint();
        }
    }

    /**
     * @param l A {@link IUpdateListener} to register with this chart.
     * @since 3.0
     */
    public void addUpdateListener(IUpdateListener l) {
        listeners.add(l);
    }

    /**
     * @param l A previously-registered {@link IUpdateListener} to remove.
     * @return <code>true</code> if the listener was removed,
     * <code>false</code> otherwise (such as when the provided
     * listener was not already registered).
     * @since 3.0
     */
    public boolean removeUpdateListener(IUpdateListener l) {
        return listeners.remove(l);
    }

    private void repaint() {
        getDisplay().asyncExec(() -> {
		    if (!chart.isDisposed()) {
		        updateDataSet();
		        for (IUpdateListener l : listeners) {
		            l.handleUpdateEvent();
		        }
		    }
		});
    }

    /**
     * Given an array of label name strings, returns a new array in which all duplicate labels
     * have been given unique names.
     * @param labels An array of label names.
     * @return A new array containing unique label names.
     * @since 3.0
     */
    protected String[] getUniqueNames(String[] labels) {
        Set<String> labelsUnique = new LinkedHashSet<>();
        for (String label : labels) {
            int count = 1;
            while (!labelsUnique.add(makeCountedLabel(label, count))) {
                count++;
            }
        }
        return labelsUnique.toArray(new String[labels.length]);
    }

    private String makeCountedLabel(String original, int count) {
        return count <= 1 ? original : original.concat(String.format(" (%d)", count)); //$NON-NLS-1$
    }

}
