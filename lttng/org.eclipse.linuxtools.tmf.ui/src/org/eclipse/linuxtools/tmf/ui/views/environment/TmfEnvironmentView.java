/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Bernd Hufmann - Updated to use Tree with columns to be able to group traces
 *   Alexandre Montplaisir - Display info for any ITmfTraceProperties trace
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.environment;

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTraceProperties;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Displays the trace's properties.
 *
 * @version 1.1
 * @author Matthew Khouzam
 */
public class TmfEnvironmentView extends TmfView {

    /** The Environment View's ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.environment"; //$NON-NLS-1$

    private Tree fTree;

    /**
     * Default constructor
     */
    public TmfEnvironmentView() {
        super("EnvironmentVariables"); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        fTree = new Tree(parent, SWT.NONE);
        TreeColumn nameCol = new TreeColumn(fTree, SWT.NONE, 0);
        TreeColumn valueCol = new TreeColumn(fTree, SWT.NONE, 1);
        nameCol.setText("Environment Variable"); //$NON-NLS-1$
        valueCol.setText("Value"); //$NON-NLS-1$

        fTree.setItemCount(0);

        fTree.setHeaderVisible(true);
        nameCol.pack();
        valueCol.pack();

        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
    }

    @Override
    protected void loadTrace() {
        fTree.setItemCount(0);
        if (fTrace == null) {
            return;
        }

        for (ITmfTrace trace : TmfTraceManager.getTraceSet(fTrace)) {
            if (trace instanceof ITmfTraceProperties) {
                TreeItem item = new TreeItem(fTree, SWT.NONE);
                item.setText(0, trace.getName());

                ITmfTraceProperties propTrace = (ITmfTraceProperties) trace;
                Map <String, String> properties = propTrace.getTraceProperties();
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    TreeItem subItem = new TreeItem(item, SWT.NONE);
                    subItem.setText(0, entry.getKey()); // Variable name
                    subItem.setText(1, entry.getValue()); // Variable value
                }
            }
        }

        // Expand the tree items
        for (int i = 0; i < fTree.getItemCount(); i++) {
            fTree.getItem(i).setExpanded(true);
        }

        for (TreeColumn column : fTree.getColumns()) {
            column.pack();
        }
    }

    @Override
    protected void closeTrace() {
        fTree.setItemCount(0);
    }

    @Override
    public void setFocus() {
        fTree.setFocus();
    }
}

