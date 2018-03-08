/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.importexport;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorLabelProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A wizard page for selecting the trace to export when no trace was previously
 * selected.
 *
 * @author Marc-Andre Laperle
 */
public class ExportTracePackageSelectTraceWizardPage extends WizardPage {

    private static final String PAGE_NAME = "ExportTracePackageSelectTraceWizardPage"; //$NON-NLS-1$

    /**
     * Construct the select trace page
     */
    public ExportTracePackageSelectTraceWizardPage() {
        super(PAGE_NAME);
    }

    private IProject fSelectedProject;
    private Table fTraceTable;

    @Override
    public void createControl(Composite parent) {
        Composite projectSelectionGroup = new Composite(parent, SWT.NONE);
        projectSelectionGroup.setLayout(new GridLayout(2, true));
        projectSelectionGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        projectSelectionGroup.setFont(parent.getFont());

        Label projectLabel = new Label(projectSelectionGroup, SWT.NONE);
        projectLabel.setText(Messages.ExportTracePackageSelectTraceWizardPage_ProjectSelection);
        projectLabel.setLayoutData(new GridData());

        Label configLabel = new Label(projectSelectionGroup, SWT.NONE);
        configLabel.setText(Messages.ExportTracePackageSelectTraceWizardPage_TraceSelection);
        configLabel.setLayoutData(new GridData());

        final Table projectTable = new Table(projectSelectionGroup, SWT.SINGLE | SWT.BORDER);
        projectTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableViewer projectViewer = new TableViewer(projectTable);
        projectViewer.setContentProvider(new TmfNavigatorContentProvider() {

            @Override
            public Object[] getElements(Object inputElement) {
                return (IProject[]) inputElement;
            }
        });
        projectViewer.setLabelProvider(new WorkbenchLabelProvider());
        projectViewer.setInput(TraceUtils.getOpenedTmfProjects().toArray(new IProject[] {}));

        fTraceTable = new Table(projectSelectionGroup, SWT.SINGLE | SWT.BORDER);
        fTraceTable.setLayoutData(new GridData(GridData.FILL_BOTH));

        final TableViewer traceViewer = new TableViewer(fTraceTable);
        traceViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof TmfTraceElement[]) {
                    return (TmfTraceElement[]) inputElement;
                }
                return null;
            }
        });
        traceViewer.setLabelProvider(new TmfNavigatorLabelProvider());
        fTraceTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = fTraceTable.getSelection();
                TmfTraceElement trace = (TmfTraceElement) items[0].getData();
                ExportTracePackageWizardPage page = (ExportTracePackageWizardPage) getWizard().getPage(ExportTracePackageWizardPage.PAGE_NAME);
                ArrayList<TmfTraceElement> traces = new ArrayList<TmfTraceElement>();
                traces.add(trace);
                page.setSelectedTraces(traces);
            }
        });

        projectTable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TableItem[] items = projectTable.getSelection();
                fSelectedProject = (IProject) items[0].getData();

                // Make sure all the elements are created
                new TmfNavigatorContentProvider().getChildren(fSelectedProject);
                TmfProjectElement project = TmfProjectRegistry.getProject(fSelectedProject);

                TmfTraceFolder tracesFolder = project.getTracesFolder();
                List<TmfTraceElement> traces = tracesFolder.getTraces();
                TmfTraceElement[] array = traces.toArray(new TmfTraceElement[] {});
                traceViewer.setInput(array);
                traceViewer.refresh();
                fTraceTable.select(0);
                fTraceTable.notifyListeners(SWT.Selection, new Event());
                getWizard().getContainer().updateButtons();
            }
        });

        setControl(projectSelectionGroup);
        setTitle(Messages.ExportTracePackageWizardPage_Title);
        setMessage(Messages.ExportTracePackageSelectTraceWizardPage_ChooseTrace);
    }

    @Override
    public boolean canFlipToNextPage() {
        return fTraceTable.getSelectionCount() > 0;
    }
}
