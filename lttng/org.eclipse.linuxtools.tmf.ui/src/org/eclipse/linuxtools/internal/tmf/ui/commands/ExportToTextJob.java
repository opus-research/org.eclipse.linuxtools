/*******************************************************************************
 * Copyright (c) 2013 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Xavier Raynaud - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.commands;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * This job exports traces to text files.
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ExportToTextJob extends Job {

    /** the ExportToCSV job family */
    public static final Object ExportToCSVJobFamily = new Object();

    private final ITmfTrace trace;
    private final String destination;
    private final ITmfFilter filter;

    /**
     * Job constructor.
     *
     * @param trace
     *            the trace to export
     * @param filter
     *            the filter to apply when exporting the trace. may be null.
     * @param destination
     *            the path of the file where the data is exported.
     */
    public ExportToTextJob(ITmfTrace trace, ITmfFilter filter, String destination) {
        super(MessageFormat.format(Messages.ExportToTextJob_Export_to, destination));
        this.trace = trace;
        this.filter = filter;
        this.destination = destination;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {
        monitor.beginTask(Messages.ExportToTextJob_Export_trace_to + destination, 100);
        IStatus ret = saveImpl(monitor);
        monitor.done();
        return ret;
    }

    private IStatus saveImpl(IProgressMonitor monitor) {
        final BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(destination));
        } catch (IOException _) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    MessageFormat.format(Messages.ExportToTextJob_Unable_to_export_trace, destination),
                    _);
            return status;
        }
        try {
            bw.write(Messages.ExportToTextJob_Header);
            return saveImpl(bw, monitor);
        } catch (IOException _) {
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    MessageFormat.format(Messages.ExportToTextJob_Unable_to_export_trace, destination),
                    _);
            return status;
        } finally {
            try {
                bw.close();
            } catch (IOException e) {
            }
        }
    }

    private IStatus saveImpl(Writer bw, IProgressMonitor monitor) {
        ExportToTextRequest request = new ExportToTextRequest(bw, filter);
        trace.sendRequest(request);
        int currentIndex = 0;
        while (!request.isCompleted()) {
            if (monitor.isCanceled()) {
                request.cancel();
                return Status.CANCEL_STATUS;
            }
            int index = (int) (request.getNbRead() * 100 / trace.getNbEvents());
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
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                    MessageFormat.format(Messages.ExportToTextJob_Unable_to_export_trace, destination),
                    request.getIOException());
            return status;
        }
        return Status.OK_STATUS;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
     */
    @Override
    public boolean belongsTo(Object family) {
        return ExportToCSVJobFamily.equals(family);
    }

}
