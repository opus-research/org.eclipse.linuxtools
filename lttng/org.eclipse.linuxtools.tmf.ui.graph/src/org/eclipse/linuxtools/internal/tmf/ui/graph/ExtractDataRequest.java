/*******************************************************************************
 * Copyright (c) 2013 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xavier Raynaud - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.tmf.ui.graph;

import org.eclipse.linuxtools.internal.tmf.ui.graph.expression.PlottingExpression;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;

/**
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ExtractDataRequest extends TmfDataRequest {

    private final ITmfFilter fFilter;
    private final PlottingExpression fXExpr;
    private final PlottingExpression fYexpr;

    private int index = 0;
    private ITmfEvent previousEvent = null;

    private double[] fXSerieData = new double[4096];
    private double[] fYSerieData = new double[4096];

    /**
     * Constructor
     */
    public ExtractDataRequest(Class<? extends ITmfEvent> dataType, ITmfFilter filter, PlottingExpression xExpr,
            PlottingExpression yExpr) {
        super(dataType);
        this.fFilter = filter;
        this.fXExpr = xExpr;
        this.fYexpr = yExpr;
    }

    @Override
    public void handleData(ITmfEvent event) {
        super.handleData(event);
        if (isCancelled()) {
            return;
        }
        if (fFilter == null || fFilter.matches(event)) {
            try {
                fXSerieData[index] = fXExpr.getValue(index, event, previousEvent);
                fYSerieData[index] = fYexpr.getValue(index, event, previousEvent);
            } catch (ArrayIndexOutOfBoundsException _) {
                double[] oldX = fXSerieData;
                double[] oldY = fYSerieData;
                int newCapacity = (index * 3) / 2 + 1;
                fXSerieData = new double[newCapacity];
                fYSerieData = new double[newCapacity];
                System.arraycopy(oldX, 0, fXSerieData, 0, oldX.length);
                System.arraycopy(oldY, 0, fYSerieData, 0, oldY.length);
                fXSerieData[index] = fXExpr.getValue(index, event, previousEvent);
                fYSerieData[index] = fYexpr.getValue(index, event, previousEvent);
            }
            index++;
            previousEvent = event;
        }
    }

    /**
     * @return the X SerieData
     */
    public double[] getXSerieData() {
        return fXSerieData;
    }

    /**
     * @return the Y SerieData
     */
    public double[] getYSerieData() {
        return fYSerieData;
    }

    public int getLength() {
        return index;
    }
}
