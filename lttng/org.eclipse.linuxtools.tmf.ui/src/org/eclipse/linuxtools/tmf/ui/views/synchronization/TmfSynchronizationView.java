/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.synchronization;

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSynchronizedSignal;
import org.eclipse.linuxtools.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;


/**
 * @author gbastien
 * @since 2.0
 */
public class TmfSynchronizationView extends TmfView {

    /**
     * The ID corresponds to the package in which this class is embedded.
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.synchronization"; //$NON-NLS-1$

    /**
     * The view name.
     */
    public static final String TMF_SYNCHRONIZATION_VIEW = "SynchronizationView"; //$NON-NLS-1$

    /**
     *
     */
    private SynchronizationAlgorithm fAlgoSync;
    private Tree fTree;

    /**
     * Default constructor
     */
    public TmfSynchronizationView() {
        super(TMF_SYNCHRONIZATION_VIEW);

    }

    @Override
    public void createPartControl(Composite parent) {
        fTree = new Tree(parent, SWT.NONE);
        TreeColumn nameCol = new TreeColumn(fTree, SWT.NONE, 0);
        TreeColumn valueCol = new TreeColumn(fTree, SWT.NONE, 1);
        nameCol.setText("Synchronization Information"); //$NON-NLS-1$
        valueCol.setText("Value"); //$NON-NLS-1$

        fTree.setItemCount(0);

        fTree.setHeaderVisible(true);
        nameCol.pack();
        valueCol.pack();

    }

    private void updateTable() {
        fTree.setItemCount(0);
        if (fAlgoSync == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : fAlgoSync.getStats().entrySet()) {
            TreeItem item = new TreeItem(fTree, SWT.NONE);
            item.setText(0, entry.getKey().toString());
            item.setText(1, entry.getValue().toString());

        }

        /* Expand the tree items */
        for (int i = 0; i < fTree.getItemCount(); i++) {
            fTree.getItem(i).setExpanded(true);
        }

        for (TreeColumn column : fTree.getColumns()) {
            column.pack();
        }
    }

    @Override
    public void setFocus() {
        fTree.setFocus();
    }

    /**
     * Handler called when traces are synchronized
     *
     * @param signal
     *            Contains the information about the selection.
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceSynchronized(TmfTraceSynchronizedSignal signal) {
        if (signal.getSyncAlgo() != fAlgoSync) {
            fAlgoSync = signal.getSyncAlgo();
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    updateTable();
                }
            });

        }
    }
}
