/*******************************************************************************
 * Copyright (c) 2011-2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Johnston <jjohnstn@redhat.com> - initial API and implementation
 *    Eric Williams <ericwill@redhat.com> - modification for Javadocs
 *******************************************************************************/
package org.eclipse.linuxtools.internal.javadocs.ui.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * The label class for the preferences page.
 */
class LabelFieldEditor extends FieldEditor {
    private Composite parent;

    public LabelFieldEditor( Composite parent, String title ) {
        this.parent = parent;
        init("org.eclipse.linuxtools.javadocs.dummy", title); //$NON-NLS-1$
        createControl(parent);
    }

    @Override
    protected void adjustForNumColumns( int numColumns ) {
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        // We only grab excess space if we have to
        // If another field editor has more columns then
        // we assume it is setting the width.
        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
        getLabelControl(parent).setLayoutData(gd);
    }

    @Override
    protected void doFillIntoGrid( Composite parent, int numColumns ) {
        getLabelControl(parent);
    }

    @Override
    public int getNumberOfControls() {    return 1; }
    /**
     * The label field editor is only used to present a text label on a preference page.
     */
    @Override
    protected void doLoad() {}
    @Override
    protected void doLoadDefault() {
    }
    @Override
    protected void doStore() {}
}

