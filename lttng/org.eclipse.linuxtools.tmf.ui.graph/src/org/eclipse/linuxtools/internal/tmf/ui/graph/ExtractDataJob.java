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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.tmf.ui.graph.expression.PlottingExpression;
import org.eclipse.linuxtools.internal.tmf.ui.graph.view.View;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.graph.Activator;

/**
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ExtractDataJob extends Job {

    public static final Object ExtractDataJobFamily = new Object();

    private final ITmfTrace fExperiment;
    private final ITmfFilterTreeNode fFilter;
    private final PlottingExpression fXExpr;
    private final PlottingExpression fYExpr;
    private final String xAxisName;
    private final String yAxisName;

    public ExtractDataJob(ITmfTrace experiment, ITmfFilterTreeNode filter, PlottingExpression xExpression,
            PlottingExpression yExpression, String xAxisName, String yAxisName) {
        super(Messages.ExtractDataJob_ExtractingDataMsg);
        this.fExperiment = experiment;
        this.fFilter = filter;
        this.fXExpr = xExpression;
        this.fYExpr = yExpression;
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(Messages.ExtractDataJob_ExtractingDataMsg, 110);
        IStatus ret = extractingDataImpl(monitor);
        monitor.done();
        return ret;
    }

    private IStatus extractingDataImpl(IProgressMonitor monitor) {
        ExtractDataRequest request = new ExtractDataRequest(ITmfEvent.class, fFilter, fXExpr, fYExpr);
        fExperiment.sendRequest(request);
        int currentIndex = 0;
        while (!request.isCompleted()) {
            if (monitor.isCanceled()) {
                request.cancel();
                return Status.CANCEL_STATUS;
            }
            int index = (int) (request.getNbRead() * 100 / fExperiment.getNbEvents());
            if (index > currentIndex) {
                int progress = index - currentIndex;
                monitor.worked(progress);
                currentIndex = index;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        if (request.isFailed()) {
            Status status = new Status(Status.ERROR, Activator.PLUGIN_ID,
                    Messages.ExtractDataJob_ExtractingDataErrorMsg);
            return status;
        }
        View.displayPlottingView(fExperiment.getName(), xAxisName, yAxisName, request.getLength(),
                request.getXSerieData(), request.getYSerieData());
        return Status.OK_STATUS;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
     */
    @Override
    public boolean belongsTo(Object family) {
        return (ExtractDataJobFamily.equals(family));
    }

}
