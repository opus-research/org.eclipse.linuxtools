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
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;

/**
 * Select trace types to import (1/4)
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class ImportTraceWizardPageSelectTraceType extends AbstractImportTraceWizardPage {

    private Tree treeView;

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

        treeView = new Tree(control, SWT.BORDER | SWT.CHECK);
        treeView.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        final SelectionListener listener = new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.detail == SWT.CHECK) {
                    Tree tv = (Tree) e.widget;
                    TreeItem[] tis = tv.getSelection();
                    if (tis.length != 0) {
                        propagateChecked(tis);

                        for (TreeItem item : tis) {
                            TreeItem itemParent = item.getParentItem();
                            if (itemParent != null) {
                                TreeItem[] children = itemParent.getItems();
                                boolean allChecked = true;
                                boolean allNotUnchecked = false;
                                if (children.length > 0) {
                                    for (TreeItem child : children) {
                                        boolean checked = child.getChecked();
                                        allChecked &= checked;
                                        allNotUnchecked |= checked;
                                    }
                                    final boolean grayed = !(allChecked || !allNotUnchecked);
                                    itemParent.setGrayed(grayed);
                                    itemParent.setChecked(allChecked);
                                }

                            }
                        }
                        getWizard().getContainer().updateButtons();
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };
        treeView.addSelectionListener(listener);// (SWT.CHECK, listener);
        populateTree(treeView);

        Composite buttonArea = new Composite(control, SWT.NONE);
        buttonArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        buttonArea.setLayout(new GridLayout(2, false));

        Button selectAll = new Button(buttonArea, SWT.NONE);
        selectAll.setText("Select All");
        selectAll.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        selectAll.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                for (TreeItem item : treeView.getItems()) {
                    item.setChecked(true);
                }
                propagateChecked(treeView.getItems());
                getWizard().getContainer().updateButtons();
            }
        });

        Button selectNone = new Button(buttonArea, SWT.NONE);
        selectNone.setText("Select None");
        selectNone.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        selectNone.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                for (TreeItem item : treeView.getItems()) {
                    item.setChecked(false);
                }
                propagateChecked(treeView.getItems());
                getWizard().getContainer().updateButtons();
            }
        });
    }

    @Override
    public boolean canFlipToNextPage() {
        List<String> tracesToScan = new ArrayList<String>();
        for (TreeItem traceFamily : treeView.getItems()) {
            for (TreeItem traceType : traceFamily.getItems()) {
                if (traceType.getChecked()) {
                    tracesToScan.add(traceType.getText());
                }
            }
        }
        ((BatchImportTraceWizard) getWizard()).setTracesToScan(tracesToScan);
        if( tracesToScan.isEmpty()){
            setErrorMessage("Please Select at least one trace type");
        }
        else{
            setErrorMessage(null);
        }
        return super.canFlipToNextPage() && !tracesToScan.isEmpty();
    }

    /**
     * @param treeView
     */
    private static void populateTree(final Tree treeView) {
        final String[] elems = ImportUtils.getTraceTypeNames().toArray(new String[0]);
        String s2[] = ImportUtils.getAvailableTraceTypes();
        for (String s : elems) {
            TreeItem iItem = new TreeItem(treeView, SWT.CHECK);
            iItem.setText(s);
            for (String tt : ImportUtils.getTraceType(s)) {
                TreeItem child = new TreeItem(iItem, SWT.CHECK);
                child.setText(tt);
            }
        }
    }

    /**
     * @param treeItems
     */
    private static void propagateChecked(TreeItem[] treeItems) {
        for (TreeItem ti : treeItems) {
            boolean checked = ti.getChecked();
            for (TreeItem child : ti.getItems()) {
                child.setChecked(checked);
            }
        }

    }

}
