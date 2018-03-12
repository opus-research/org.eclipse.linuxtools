/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.offset.OffsetDialog;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Offset Handler
 *
 * @author Matthew Khouzam
 */
public class OffsetTraceHandler extends AbstractHandler {

    // ------------------------------------------------------------------------
    // Execution
    // ------------------------------------------------------------------------

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {

        ISelection selection = HandlerUtil.getCurrentSelection(event);

        // Get the set of selected trace elements
        final Set<TmfTraceElement> traceElements = new HashSet<>();
        if (selection instanceof StructuredSelection) {
            Iterator<Object> iterator = ((StructuredSelection) selection).iterator();
            while (iterator.hasNext()) {
                Object element = iterator.next();
                if (element instanceof TmfTraceElement) {
                    TmfTraceElement trace = (TmfTraceElement) element;
                    traceElements.add(trace.getElementUnderTraceFolder());
                } else if (element instanceof TmfExperimentElement) {
                    TmfExperimentElement exp = (TmfExperimentElement) element;
                    for (TmfTraceElement trace : exp.getTraces()) {
                        traceElements.add(trace.getElementUnderTraceFolder());
                    }
                } else if (element instanceof TmfTraceFolder) {
                    TmfTraceFolder folder = (TmfTraceFolder) element;
                    traceElements.addAll(folder.getTraces());
                }
            }
        }

        if (traceElements.isEmpty()) {
            TraceUtils.displayErrorMsg(Messages.SynchronizeTracesHandler_Title, Messages.SynchronizeTracesHandler_WrongTraceNumber);
            return null;
        }

        Map<TmfTraceElement, Long> previousOffsets = new LinkedHashMap<>();
        for (TmfTraceElement trace : traceElements) {
            long offset = TimestampTransformFactory.getTimestampTransform(trace.getResource()).transform(0);
            previousOffsets.put(trace, offset);
        }

        Map<TmfTraceElement, Long> offsets = new LinkedHashMap<>(previousOffsets);
        OffsetDialog dialog = new OffsetDialog(HandlerUtil.getActiveShellChecked(event), offsets);
        dialog.open();
        if (dialog.getReturnCode() != Window.OK) {
            return null;
        }

        for (TmfTraceElement trace : offsets.keySet()) {
            Long offset = offsets.get(trace);
            ITmfTimestampTransform transform = TimestampTransformFactory.createWithOffset(offset);
            if (offset != previousOffsets.get(trace)) {
                trace.closeEditors();
                trace.deleteSupplementaryResources();
                // make sure the supplementary folder exists
                trace.refreshSupplementaryFolder();
                TimestampTransformFactory.setTimestampTransform(trace.getResource(), transform);
                trace.refreshSupplementaryFolder();
            }
        }

        return null;
    }
}
