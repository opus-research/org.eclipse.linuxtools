/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;

/**
 * <b>Select trace types to import</b>, this page is the first of three pages
 * shown. This one selects the type of traces that are to be scanned.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class ImportTraceWizardPageSelectTraceType extends AbstractImportTraceWizardPage {

    private CheckboxTreeViewer fTreeView;
    private final TraceTypeContentProvider fProvider = new TraceTypeContentProvider();

    /**
     * Select trace types to import
     *
     * @param name
     *            The name of the page.
     * @param selection
     *            The current selection
     */
    protected ImportTraceWizardPageSelectTraceType(String name, IStructuredSelection selection) {
        super(name, selection);
    }

    /**
     * Select trace types to import
     *
     * @param workbench
     *            The workbench reference.
     * @param selection
     *            The current selection
     */
    public ImportTraceWizardPageSelectTraceType(IWorkbench workbench, IStructuredSelection selection) {
        super(workbench, selection);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite control = (Composite) this.getControl();

        final ICheckStateListener listener = new TraceTypeCheckListener();

        fTreeView = new CheckboxTreeViewer(control, SWT.BORDER);

        fTreeView.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fTreeView.setContentProvider(fProvider);
        fTreeView.setInput(fProvider);
        fTreeView.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return (String) element;
            }
        });
        fTreeView.addCheckStateListener(listener);

        // populateTree(treeView);

        Composite buttonArea = new Composite(control, SWT.NONE);
        buttonArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        buttonArea.setLayout(new GridLayout(2, false));

        Button selectAll = new Button(buttonArea, SWT.NONE);
        selectAll.setText(Messages.ImportTraceWizardPageSelectTraceType_0);
        selectAll.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        selectAll.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                String elements[] = (String[]) ((ITreeContentProvider) fTreeView.getContentProvider()).getElements(null);
                for (String key : elements) {
                    fTreeView.setSubtreeChecked(key, true);
                }
                getWizard().getContainer().updateButtons();
            }
        });

        Button selectNone = new Button(buttonArea, SWT.NONE);
        selectNone.setText(Messages.ImportTraceWizardPageSelectTraceType_1);
        selectNone.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        selectNone.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                String elements[] = (String[]) ((ITreeContentProvider) fTreeView.getContentProvider()).getElements(null);
                for (String key : elements) {
                    fTreeView.setSubtreeChecked(key, false);
                }
                getWizard().getContainer().updateButtons();
            }
        });
    }

    @Override
    public boolean canFlipToNextPage() {
        List<String> tracesToScan = new ArrayList<String>();
        String elements[] = (String[]) fProvider.getElements(null);
        for (String traceFamily : elements) {
            final String[] children = (String[]) fProvider.getChildren(traceFamily);
            if (children != null) {
                for (String traceType : children) {
                    if (fTreeView.getChecked(traceType)) {
                        tracesToScan.add(traceType);
                    }
                }
            }
        }
        ((BatchImportTraceWizard) getWizard()).setTraceTypesToScan(tracesToScan);
        if (tracesToScan.isEmpty()) {
            setErrorMessage(Messages.ImportTraceWizardPageSelectTraceType_2);
        }
        else {
            setErrorMessage(null);
        }
        return super.canFlipToNextPage() && !tracesToScan.isEmpty();
    }

    private final class TraceTypeCheckListener implements ICheckStateListener {
        @Override
        public void checkStateChanged(CheckStateChangedEvent event) {

            boolean checkStatus = event.getChecked();
            String checked = (String) event.getElement();
            fTreeView.setGrayed(checked, false);
            fTreeView.setSubtreeChecked(checked, checkStatus);
            ITreeContentProvider tcp = (ITreeContentProvider) fTreeView.getContentProvider();
            String parentElement = (String) tcp.getParent(checked);
            if (parentElement != null) {
                String[] siblings = (String[]) tcp.getChildren(parentElement);
                final String first = siblings[0];
                final boolean isFirstChecked = fTreeView.getChecked(first);
                boolean allSame = true;
                for (String peer : siblings) {
                    final boolean peerChecked = fTreeView.getChecked(peer);
                    if (peerChecked != isFirstChecked) {
                        allSame = false;
                    }
                }
                if (allSame) {
                    fTreeView.setGrayed(parentElement, false);
                    fTreeView.setChecked(parentElement, checkStatus);
                }
                else {
                    fTreeView.setChecked(parentElement, false);
                    fTreeView.setGrayed(parentElement, true);
                }
            }
            getWizard().getContainer().updateButtons();

        }
    }
}
