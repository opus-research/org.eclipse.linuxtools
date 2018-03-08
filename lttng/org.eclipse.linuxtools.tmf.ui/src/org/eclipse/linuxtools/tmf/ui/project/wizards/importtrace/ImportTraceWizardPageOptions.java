/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam- Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

/**
 * @author Matthew Khouzam
 * @since 2.0
 */
public class ImportTraceWizardPageOptions extends AbstractImportTraceWizardPage {


    private Button fLink;
    private Button fCopy;

    private Button fOverwrite;

    /**
     * Constructor
     * @param workbench The reference workbench
     * @param selection the current selection
     */
    public ImportTraceWizardPageOptions(IWorkbench workbench, IStructuredSelection selection) {
        super(workbench, selection);
    }

    @Override
    public void createControl(Composite parent) {

        super.createControl(parent);
        init();

    }

    private void init() {
        Composite optionPane = (Composite) this.getControl();

        optionPane.setLayout(new GridLayout());
        optionPane.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

        final SelectionListener linkedListener = new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getBatchWizard().setLinked(((Button) e.widget).getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };

        fLink = new Button(optionPane, SWT.RADIO);
        fLink.setText(Messages.ImportTraceWizardPageOptions_0);
        fLink.setSelection(true);
        fLink.setLayoutData(new GridData());
        fLink.addSelectionListener(linkedListener);

        fCopy = new Button(optionPane, SWT.RADIO);
        fCopy.setText(Messages.ImportTraceWizardPageOptions_1);
        fCopy.setLayoutData(new GridData());
        fCopy.addSelectionListener(linkedListener);

        fOverwrite = new Button(optionPane, SWT.CHECK);
        fOverwrite.setText(Messages.ImportTraceWizardPageOptions_2);
        fOverwrite.setLayoutData(new GridData());
        fOverwrite.setSelection(true);
        fOverwrite.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                getBatchWizard().setOverwrite(((Button) e.widget).getSelection());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }
}
