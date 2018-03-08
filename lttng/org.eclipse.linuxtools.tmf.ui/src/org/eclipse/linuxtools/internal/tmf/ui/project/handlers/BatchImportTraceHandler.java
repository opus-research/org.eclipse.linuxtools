package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.BatchImportTraceWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Batch import handler, spawn a wizard
 * @author Matthew Khouzam
 * @since 2.0
 *
 */
public class BatchImportTraceHandler extends ImportTraceHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        TmfTraceFolder traceFolder = prepWork();
        if (traceFolder == null) {
            return null;
        }

        // Fire the Import Trace Wizard
        IWorkbench workbench = PlatformUI.getWorkbench();
        Shell shell = workbench.getActiveWorkbenchWindow().getShell();

        BatchImportTraceWizard wizard = new BatchImportTraceWizard();
        wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(traceFolder));
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.open();

        traceFolder.refresh();

        return null;
    }

}
