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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.internal.tmf.ui.project.model.TmfImportHelper;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.AbstractTracePackageWizardPage;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageFilesElement;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.tracepkg.TracePackageTraceElement;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfNavigatorContentProvider;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.linuxtools.tmf.ui.project.model.TraceUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Wizard page for the import trace package wizard
 *
 * @author Marc-Andre Laperle
 */
public class ImportTracePackageWizardPage extends AbstractTracePackageWizardPage {

    private static final String ICON_PATH = "icons/wizban/trace_import_wiz.png"; //$NON-NLS-1$
    private static final String PAGE_NAME = "ImportTracePackagePage"; //$NON-NLS-1$
    private static final String STORE_PROJECT_NAME_ID = PAGE_NAME + ".STORE_PROJECT_NAME_ID"; //$NON-NLS-1$

    private String fValidatedFilePath;
    private TmfTraceFolder fTmfTraceFolder;
    private Text fProjectText;
    private List<IProject> fOpenedTmfProjects;

    /**
     * Constructor for the import trace package wizard page
     *
     * @param selection
     *            the current object selection
     */
    public ImportTracePackageWizardPage(IStructuredSelection selection) {
        super(PAGE_NAME, Messages.ImportTracePackageWizardPage_Title, Activator.getDefault().getImageDescripterFromPath(ICON_PATH), selection);

        if (getSelection().getFirstElement() instanceof TmfTraceFolder) {
            fTmfTraceFolder = (TmfTraceFolder) getSelection().getFirstElement();
        }
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.HORIZONTAL_ALIGN_FILL));
        composite.setFont(parent.getFont());

        createFilePathGroup(composite, Messages.ImportTracePackageWizardPage_FromArchive);
        createElementViewer(composite);
        createButtonsGroup(composite);
        if (fTmfTraceFolder == null) {
            createProjectSelectionGroup(composite);
        }

        restoreWidgetValues();
        setMessage(Messages.ImportTracePackageWizardPage_Message);
        updatePageCompletion();

        setControl(composite);
    }

    private void createProjectSelectionGroup(Composite parent) {

        Composite projectSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        projectSelectionGroup.setLayout(layout);
        projectSelectionGroup.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

        Label projectLabel = new Label(projectSelectionGroup, SWT.NONE);
        projectLabel.setText(Messages.ImportTracePackageWizardPage_Project);

        fProjectText = new Text(projectSelectionGroup, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL);
        data.grabExcessHorizontalSpace = true;
        fProjectText.setLayoutData(data);

        fOpenedTmfProjects = TraceUtils.getOpenedTmfProjects();

        // No project to import to, create a default project if it doesn't exist
        if (fOpenedTmfProjects.isEmpty()) {
            IProject defaultProject = ResourcesPlugin.getWorkspace().getRoot().getProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME);
            if (!defaultProject.exists()) {
                IProject project = TmfProjectRegistry.createProject(TmfCommonConstants.DEFAULT_TRACE_PROJECT_NAME, null, null);
                TmfImportHelper.forceFolderRefresh(project.getFolder(TmfTraceFolder.TRACE_FOLDER_NAME));
                fOpenedTmfProjects.add(project);
            }
        }

        if (!fOpenedTmfProjects.isEmpty()) {
            selectProject(fOpenedTmfProjects.get(0));
        }

        Button button = new Button(projectSelectionGroup,
                SWT.PUSH);
        button.setText(Messages.ImportTracePackageWizardPage_SelectProjectButton);
        button.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                ElementListSelectionDialog d = new ElementListSelectionDialog(getContainer().getShell(), new WorkbenchLabelProvider());

                d.setBlockOnOpen(true);
                d.setTitle(Messages.ImportTracePackageWizardPage_SelectProjectDialogTitle);

                d.setElements(fOpenedTmfProjects.toArray(new IProject[] {}));

                d.open();
                if (d.getFirstResult() != null) {
                    IProject project = (IProject) d.getFirstResult();
                    selectProject(project);
                }
            }
        });
        setButtonLayoutData(button);
    }

    @Override
    protected void restoreWidgetValues() {
        super.restoreWidgetValues();
        IDialogSettings settings = getDialogSettings();
        if (settings != null && fProjectText != null) {

            // Restore last selected project
            String projectName = settings.get(STORE_PROJECT_NAME_ID);
            if (projectName != null && !projectName.isEmpty()) {
                for (IProject project : fOpenedTmfProjects) {
                    if (project.getName().equals(projectName)) {
                        selectProject(project);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void saveWidgetValues() {
        super.saveWidgetValues();

        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            settings.put(STORE_PROJECT_NAME_ID, fTmfTraceFolder.getProject().getResource().getName());
        }
    }

    private void selectProject(IProject project) {
        fProjectText.setText(project.getName());
        new TmfNavigatorContentProvider().getChildren(project);
        fTmfTraceFolder = TmfProjectRegistry.getProject(project).getTracesFolder();
        updatePageCompletion();
    }

    @Override
    protected boolean determinePageCompletion() {
        return super.determinePageCompletion() && fTmfTraceFolder != null;
    }

    @Override
    protected Object createElementViewerInput() {

        final TracePackageExtractManifestOperation op = new TracePackageExtractManifestOperation(getFilePathValue());

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {

                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    monitor.beginTask(Messages.ImportTracePackageWizardPage_ReadingPackage, 10);
                    op.run(monitor);
                    monitor.done();
                }

            });

            IStatus status = op.getStatus();
            if (status.getSeverity() == IStatus.ERROR) {
                handleErrorStatus(status);
            }
        } catch (InvocationTargetException e1) {
            handleError(Messages.TracePackageExtractManifestOperation_ErrorReadingManifest, e1);
        } catch (InterruptedException e1) {
            // Canceled
        }

        TracePackageElement resultElement = op.getResultElement();
        if (resultElement == null) {
            return null;
        }

        for (TracePackageElement e : resultElement.getChildren()) {
            if (e instanceof TracePackageFilesElement) {
                e.setEnabled(false);
            }
        }

        return new TracePackageElement[] { resultElement };
    }

    @Override
    protected void createFilePathGroup(Composite parent, String label) {
        super.createFilePathGroup(parent, label);

        Combo filePathCombo = getFilePathCombo();
        filePathCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateWithFilePathSelection();
            }
        });

        // User can type-in path and press return to validate
        filePathCombo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == '\r') {
                    updateWithFilePathSelection();
                }
            }
        });
    }

    @Override
    protected void updateWithFilePathSelection() {
        if (!new File(getFilePathValue()).exists()) {
            setErrorMessage(Messages.ImportTracePackageWizardPage_ErrorFileNotFound);
            return;
        }
        setErrorMessage(null);

        getContainer().getShell().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                CheckboxTreeViewer elementViewer = getElementViewer();
                Object elementViewerInput = createElementViewerInput();
                elementViewer.setInput(elementViewerInput);
                if (elementViewerInput != null) {
                    elementViewer.expandToLevel(2);
                    setAllChecked(elementViewer, false, true);
                    fValidatedFilePath = getFilePathValue();
                }

                updatePageCompletion();
            }
        });
    }

    /**
     * Finish the wizard page
     *
     * @return true on success
     */
    public boolean finish() {
        if (!checkForOverwrite()) {
            return false;
        }

        saveWidgetValues();

        TracePackageElement[] input = (TracePackageElement[]) getElementViewer().getInput();
        TracePackageTraceElement exportTraceTraceElement = (TracePackageTraceElement) input[0];
        final TracePackageImportOperation exporter = new TracePackageImportOperation(fValidatedFilePath, exportTraceTraceElement, fTmfTraceFolder);

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    exporter.run(monitor);
                }
            });

            IStatus status = exporter.getStatus();
            if (status.getSeverity() == IStatus.ERROR) {
                handleErrorStatus(status);
            }

        } catch (InvocationTargetException e) {
            handleError(Messages.ImportTracePackageWizardPage_ErrorOperation, e);
        } catch (InterruptedException e) {
        }

        return exporter.getStatus().getSeverity() == IStatus.OK;
    }

    private boolean checkForOverwrite() {
        TracePackageTraceElement traceElement = (TracePackageTraceElement) ((TracePackageElement[]) getElementViewer().getInput())[0];
        String traceName = traceElement.getText();

        List<TmfTraceElement> traces = fTmfTraceFolder.getTraces();
        for (TmfTraceElement t : traces) {
            if (t.getName().equals(traceName)) {
                return MessageDialog.openQuestion(getContainer().getShell(), null, Messages.ImportTracePackageWizardPage_AlreadyExists);
            }
        }

        return true;
    }
}
