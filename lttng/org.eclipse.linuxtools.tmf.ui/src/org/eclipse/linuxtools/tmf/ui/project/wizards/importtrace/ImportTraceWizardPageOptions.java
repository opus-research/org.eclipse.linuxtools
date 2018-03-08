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
 * @author ematkho
 * @since 2.0
 */
public class ImportTraceWizardPageOptions extends AbstractImportTraceWizardPage {

    public ImportTraceWizardPageOptions(IWorkbench workbench, IStructuredSelection selection) {
        super(workbench, selection);
    }

    private Button fLink;
    private Button fCopy;

    private Button fOverwrite;

    @Override
    public void createControl(Composite parent) {

        super.createControl(parent);
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
        fLink.setText("Link traces (Recommended)"); //$NON-NLS-1$
        fLink.setSelection(true);
        fLink.setLayoutData(new GridData());
        fLink.addSelectionListener(linkedListener);

        fCopy = new Button(optionPane, SWT.RADIO);
        fCopy.setText("Copy traces"); //$NON-NLS-1$
        fCopy.setLayoutData(new GridData());
        fCopy.addSelectionListener(linkedListener);

        fOverwrite = new Button(optionPane, SWT.CHECK);
        fOverwrite.setText("Overwrite existing traces (recommended)"); //$NON-NLS-1$
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

        createDestinationGroup(optionPane);
        getBatchWizard().setResourcePath(getContainerFullPath());

    }
}
