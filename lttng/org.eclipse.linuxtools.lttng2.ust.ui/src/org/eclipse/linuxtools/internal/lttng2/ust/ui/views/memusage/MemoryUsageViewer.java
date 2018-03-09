/**********************************************************************
 * Copyright (c) 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Geneviève Bastien - Create and use base class for XY plots
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.ust.ui.views.memusage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.ust.core.stateprovider.UstMemoryStrings;
import org.eclipse.linuxtools.lttng2.ust.ui.analysis.memory.UstMemoryAnalysisModule;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfXYLinePlotViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Memory usage view
 *
 * @author Matthew Khouzam
 */
public class MemoryUsageViewer extends TmfXYLinePlotViewer {

    private TmfStateSystemAnalysisModule fModule = null;

    private final Map<Integer, double[]> fYValues = new HashMap<Integer, double[]>();
    private final Map<Integer, Integer> fMemoryQuarks = new HashMap<Integer, Integer>();
    private final Map<Integer, String> fSeriesName = new HashMap<Integer, String>();

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     */
    public MemoryUsageViewer(Composite parent) {
        super(parent, Messages.MemoryUsageViewer_Title, Messages.MemoryUsageViewer_XAxis, Messages.MemoryUsageViewer_YAxis);
    }

    @Override
    protected void initializeDataSource() {
        if (fTrace != null) {
            fModule = fTrace.getAnalysisModules(TmfStateSystemAnalysisModule.class).get(UstMemoryAnalysisModule.ID);
            if (fModule == null) {
                return;
            }
            fModule.schedule();
            fModule.waitForCompletion(new NullProgressMonitor());
        }
    }

    @Override
    protected void updateData(double[] xvalues) {
        try {
            if (fTrace == null || fModule == null) {
                return;
            }
            double maxy = 0.0;
            ITmfStateSystem ss = fModule.getStateSystem();
            /* Don't wait for the module completion, when it's ready, we'll know */
            if (ss == null) {
                return;
            }
            List<Integer> tidQuarks = ss.getQuarks("*"); //$NON-NLS-1$
            long traceStart = getStartTime();
            long traceEnd = getEndTime();
            long ts = getWindowStartTime();

            /* Initialize quarks and series names */
            for (int quark : tidQuarks) {
                fYValues.put(quark, new double[xvalues.length]);
                fMemoryQuarks.put(quark, ss.getQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE));
                int procNameQuark = ss.getQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_PROCNAME_ATTRIBUTE);
                try {
                    ITmfStateValue procnameValue = ss.querySingleState(ts, procNameQuark).getStateValue();
                    String procname = new String();
                    if (!procnameValue.isNull()) {
                        procname = procnameValue.unboxStr();
                    }
                    fSeriesName.put(quark, procname + ' ' + '(' + ss.getAttributeName(quark) + ')');
                } catch (TimeRangeException e) {
                    fSeriesName.put(quark, '(' + ss.getAttributeName(quark) + ')');
                }
            }

            /*
             * TODO: It should only show active threads in the time range. If a
             * tid does not have any memory value (only 1 interval in the time
             * range with value null or 0), then its series should not be
             * displayed.
             */
            double yvalue = 0.0;
            for (int i = 0; i < xvalues.length; i++) {
                double x = xvalues[i];
                long time = (long) x + ts;
                // make sure that time is in the trace range after double to
                // long conversion
                time = time < traceStart ? traceStart : time;
                time = time > traceEnd ? traceEnd : time;

                for (int quark : tidQuarks) {
                    try {
                        yvalue = ss.querySingleState(time, fMemoryQuarks.get(quark)).getStateValue().unboxLong() / 1000;
                        fYValues.get(quark)[i] = yvalue;
                        maxy = Math.max(maxy, yvalue);
                    } catch (TimeRangeException e) {
                        fYValues.get(quark)[i] = 0;
                    }
                }
            }
            for (int quark : tidQuarks) {
                setSeries(fSeriesName.get(quark), fYValues.get(quark), maxy);
            }
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            e.printStackTrace();
        }
    }

}
