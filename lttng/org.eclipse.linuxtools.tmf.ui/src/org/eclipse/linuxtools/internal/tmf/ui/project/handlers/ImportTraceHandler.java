/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>ImportTraceHandler</u></b>
 * <p>
 * Starts an ImportTraceWizard that will handle the lowly details.
 */
public class ImportTraceHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        TmfTrace t = (TmfTrace)activeTrace;
        SimpleDateFormat d = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SSS");
        try {
            Date parse = d.parse("01/01/1985 12:34:56:789"); //$NON-NLS-1$
            TmfTimestamp ts = new TmfTimestamp(parse.getTime(), ITmfTimestamp.MILLISECOND_SCALE);
            t.getIndexer().seekIndex(ts);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//        TmfTraceFolder traceFolder = getTraceFolder();
//        if (traceFolder == null) {
//            return null;
//        }
//        // Fire the Import Trace Wizard
//        IWorkbench workbench = PlatformUI.getWorkbench();
//        Shell shell = workbench.getActiveWorkbenchWindow().getShell();
//
//        ImportTraceWizard wizard = new ImportTraceWizard();
//        wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(traceFolder));
//        WizardDialog dialog = new WizardDialog(shell, wizard);
//        dialog.open();
//
//        traceFolder.refresh();

        return null;
    }

    /**
     * @return the trace folder or null
     */
    protected TmfTraceFolder getTraceFolder() {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return null;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return null;
        }
        ISelection selection = selectionProvider.getSelection();

        TmfTraceFolder traceFolder = null;
        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfTraceFolder) {
                traceFolder = (TmfTraceFolder) element;
            }
        }
        return traceFolder;
    }

}
