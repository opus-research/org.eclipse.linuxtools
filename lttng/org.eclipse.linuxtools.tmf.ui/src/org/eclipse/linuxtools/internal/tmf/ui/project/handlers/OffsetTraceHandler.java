package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.OffsetDialog;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Offset Handler
 *
 * @author Matthew Khouzam
 */
public class OffsetTraceHandler extends AbstractHandler {

    private static final String CR = System.getProperty("line.separator"); //$NON-NLS-1$
    private TreeSelection fSelection = null;

    // ------------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------------

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return null;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return Boolean.FALSE;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return Boolean.FALSE;
        }
        ISelection selection = selectionProvider.getSelection();

        // Make sure selection contains only traces
        fSelection = null;
        final ArrayList<TmfTraceElement> tl = new ArrayList<>();
        final ArrayList<TmfExperimentElement> uiexperiment = new ArrayList<>();
        if (selection instanceof TreeSelection) {
            fSelection = (TreeSelection) selection;
            Iterator<Object> iterator = fSelection.iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof TmfTraceElement) {
                    tl.add((TmfTraceElement) element);
                } else if (element instanceof TmfExperimentElement) {
                    TmfExperimentElement exp = (TmfExperimentElement) element;
                    uiexperiment.add(exp);
                    for (TmfTraceElement trace : exp.getTraces()) {
                        tl.add(trace);
                    }
                }
            }
        }

        if ((uiexperiment.size() != 1) || (tl.isEmpty())) {
            TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_WrongTraceNumber);
            return null;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {

                final ITmfTrace[] traces = new ITmfTrace[tl.size()];
                final TmfExperimentElement exp = uiexperiment.get(0);
                for (int i = 0; i < tl.size(); i++) {
                    ITmfTrace trace = tl.get(i).instantiateTrace();
                    ITmfEvent traceEvent = tl.get(i).instantiateEvent();
                    if (trace == null) {
                        TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_WrongType + tl.get(i).getName());
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        return;
                    }
                    try {
                        trace.initTrace(tl.get(i).getResource(), tl.get(i).getLocation().getPath(), traceEvent.getClass());
                        TmfTraceManager.refreshSupplementaryFiles(trace);
                    } catch (TmfTraceException e) {
                        TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_InitError + CR + CR + e);
                        trace.dispose();
                        for (int j = 0; j < i; j++) {
                            traces[j].dispose();
                        }
                        return;
                    }
                    traces[i] = trace;
                }
                exp.refreshSupplementaryFolder();
                final TmfExperiment experiment = new TmfExperiment(ITmfEvent.class, exp.getName(), traces, exp.getResource());

                TmfTraceManager.refreshSupplementaryFiles(experiment);

                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        OffsetDialog od = new OffsetDialog(Display.getDefault().getActiveShell(), SWT.NONE);
                        Map<ITmfTrace, Long> transforms = new HashMap<>();
                        for (ITmfTrace trace : traces) {
                            long transform = trace.getTimestampTransform().transform(0);
                            transforms.put(trace, transform);
                        }
                        od.open(transforms);
                        for (ITmfTrace trace : traces) {
                            Long offset = transforms.get(trace);
                            ITmfTimestampTransform transform = TimestampTransformFactory.create(offset);
                            trace.setTimestampTransform(transform);
                        }
                    }
                });

            }
        };
        thread.start();

        return null;
    }
}
