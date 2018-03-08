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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

/**
 * This handler exports traces to text files.
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class ExportToTextCommandHandler extends AbstractHandler {

    /**
     * Constructor
     */
    public ExportToTextCommandHandler() {
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ITmfTrace t = TmfTraceManager.getInstance().getActiveTrace();
        if (t != null) {
            FileDialog fd = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
            fd.setFilterExtensions(new String[] { "*.csv", "*.*", "*" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            fd.setOverwrite(true);
            final String s = fd.open();
            if (s != null) {
                final ITmfFilter filter = TmfTraceManager.getInstance().getFilter(t);
                Job j = new ExportToTextJob(t, filter, s);
                j.setUser(true);
                j.schedule();
            }
        }
        return null;
    }

}
