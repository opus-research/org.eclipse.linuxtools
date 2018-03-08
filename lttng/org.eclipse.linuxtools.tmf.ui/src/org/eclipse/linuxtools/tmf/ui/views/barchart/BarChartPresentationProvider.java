/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for bar charts
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.barchart;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.ui.views.barchart.BarChartEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Presentation provider for the control flow view
 * @since 2.0
 */
public abstract class BarChartPresentationProvider extends TimeGraphPresentationProvider {

    final private String fStateTypeName;
    /**
     * @author gbastien
     *
     */
    protected enum State implements IEnumState {
        ;

        @Override
        public RGB rgb() {
            return null;
        }

    }

    /**
     * Default Constructor
     */
    public BarChartPresentationProvider() {
        this(""); //$NON-NLS-1$
    }

    /**
     * Constructor
     * @param stateTypeName  The state type name
     */
    public BarChartPresentationProvider(String stateTypeName) {
        super();
        fStateTypeName = stateTypeName;
    }

    @Override
    public String getStateTypeName() {
        return fStateTypeName;
    }

    /**
     * Get the array of state values for the provider class
     *
     * @return An array of states for this provider
     */
    protected abstract IEnumState[] getStateValues();

    @Override
    public StateItem[] getStateTable() {
        IEnumState[] states = getStateValues();
        StateItem[] stateTable = new StateItem[states.length];
        for (int i = 0; i < stateTable.length; i++) {
            IEnumState state = states[i];
            stateTable[i] = new StateItem(state.rgb(), state.toString());
        }
        return stateTable;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof BarChartEvent) {
            int status = ((BarChartEvent) event).getValue();
            return getMatchingState(status).ordinal();
        }
        return TRANSPARENT;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof BarChartEvent) {
            int status = ((BarChartEvent) event).getValue();
            return getMatchingState(status).toString();
        }
        return Messages.BarChartPresentationProvider_multipleStates;
    }

    /**
     * @param status The model status to get the state for
     * @return The enum state value
     */
    protected abstract IEnumState getMatchingState(int status);

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        Map<String, String> retMap = new LinkedHashMap<String, String>();
        return retMap;
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {

    }

}
