/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tracing.examples.ui;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.linuxtools.internal.tmf.ui.project.wizards.importtrace.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * An example of extending ImportTraceWizardPage. This page extends ImportTraceWizardPage
 * so that it can only support importing from zip archives without user options.
 */
@SuppressWarnings("restriction")
public class MyImportTraceWizardPage extends ImportTraceWizardPage {

    private static final String MY_IMPORT_WIZARD_PAGE_NAME = "MyImportTraceWizardPage"; //$NON-NLS-1$
    private static final String[] FILE_IMPORT_MASK = { "*.zip" }; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param selection
     *            The current selection
     */
    public MyImportTraceWizardPage(IStructuredSelection selection) {
        super(MY_IMPORT_WIZARD_PAGE_NAME, selection);
    }

    @Override
    protected void createSourceGroup(Composite parent) {
        createMySourceSelectionGroup(parent);
        createFileSelectionGroup(parent);
        validateSourceGroup();
    }

    private void createMySourceSelectionGroup(Composite parent) {
        Composite sourceGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 0;
        sourceGroup.setLayout(layout);
        sourceGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label importFromArchiveLabel = new Label(sourceGroup, SWT.NONE);
        importFromArchiveLabel.setText(Messages.ImportTraceWizard_ArchiveLocation);

        fArchiveNameField = createPathSelectionCombo(sourceGroup);
        fArchiveBrowseButton = createPathSelectionBrowseButton(sourceGroup);
        fArchiveBrowseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleArchiveBrowseButtonPressed(FILE_IMPORT_MASK);
            }
        });
    }

    @Override
    protected String getImportTraceTypeId() {
        return TRACE_TYPE_AUTO_DETECT;
    }

    @Override
    protected void createOptionsGroup(Composite parent) {
        // No options
    }

    @Override
    protected void setSourcePath(String path) {
        super.setSourcePath(path);
        // When the user selects a path, select the all files by default
        setFileSelectionGroupChecked(true);
    }

    @Override
    protected int getImportOptionFlags() {
        return OPTION_PRESERVE_FOLDER_STRUCTURE | OPTION_IMPORT_UNRECOGNIZED_TRACES | OPTION_OVERWRITE_EXISTING_RESOURCES;
    }
}
