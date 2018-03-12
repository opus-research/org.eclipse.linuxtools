/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.graphing.ui.wizards.filter;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.systemtap.graphing.core.filters.IDataSetFilter;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

public abstract class FilterWizardPage extends WizardPage {
    public FilterWizardPage(String title) {
        super("selectFilterOptions"); //$NON-NLS-1$
        setTitle(title);
    }

    @Override
    public void createControl(Composite parent) {
        wizard = (SelectFilterWizard)super.getWizard();
    }

    @Override
    public boolean canFlipToNextPage() {
        return false;
    }

    abstract void createFilter();
    IDataSetFilter getFilter() {
        return filter;
    }

    @Override
    public void dispose() {
        wizard = null;
        super.dispose();
    }

    protected final SelectionListener selectionListener = SelectionListener.widgetSelectedAdapter(e -> update());

    protected final ModifyListener modifyListener = e -> update();

    private void update() {
        createFilter();
        wizard.getContainer().updateButtons();
    }

    /**
     * @since 3.0
     */
    protected IDataSetFilter filter;
    protected SelectFilterWizard wizard;
}
