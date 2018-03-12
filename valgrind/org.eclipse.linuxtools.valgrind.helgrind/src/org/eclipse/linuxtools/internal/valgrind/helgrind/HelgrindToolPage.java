/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.helgrind;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.valgrind.helgrind.HelgrindPlugin;
import org.eclipse.linuxtools.internal.valgrind.launch.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.osgi.framework.Version;

public class HelgrindToolPage extends AbstractLaunchConfigurationTab implements IValgrindToolPage {
    // HELGRIND controls
    private Button lockordersButton;
    private Combo historyCombo;
    private Spinner cacheSizeSpinner;


    private boolean isInitializing = false;

    private SelectionListener selectListener = new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
            updateLaunchConfigurationDialog();
        }
    };
    private ModifyListener modifyListener = e -> updateLaunchConfigurationDialog();

    @Override
    public void createControl(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        GridLayout helgrindLayout = new GridLayout(2, true);
        top.setLayout(helgrindLayout);
        top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        lockordersButton = new Button(top, SWT.CHECK);
        lockordersButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        lockordersButton.setText(Messages.getString("HelgrindToolPage.track_lockorders")); //$NON-NLS-1$
        lockordersButton.addSelectionListener(selectListener);

        Composite historyTop = new Composite(top, SWT.NONE);
        historyTop.setLayout(new GridLayout(2, false));
        historyTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label historyLabel = new Label(historyTop, SWT.NONE);
        historyLabel.setText(Messages.getString("HelgrindToolPage.history_level")); //$NON-NLS-1$
        historyCombo = new Combo(historyTop, SWT.READ_ONLY);
        String[] historyOpts = { HelgrindLaunchConstants.HISTORY_FULL, HelgrindLaunchConstants.HISTORY_APPROX, HelgrindLaunchConstants.HISTORY_NONE };
        historyCombo.setItems(historyOpts);
        historyCombo.addSelectionListener(selectListener);

        Composite conflictCacheSizeTop = new Composite(top, SWT.NONE);
        conflictCacheSizeTop.setLayout(new GridLayout(2, false));
        conflictCacheSizeTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label cacheSizeLabel = new Label(conflictCacheSizeTop, SWT.NONE);
        cacheSizeLabel.setText(Messages.getString("HelgrindToolPage.cache_size")); //$NON-NLS-1$
        cacheSizeSpinner = new Spinner(conflictCacheSizeTop, SWT.BORDER);
        cacheSizeSpinner.setMaximum(Integer.MAX_VALUE);
        cacheSizeSpinner.addModifyListener(modifyListener);
    }

    @Override
    public String getName() {
        return Messages.getString("HelgrindToolPage.Helgrind_Options"); //$NON-NLS-1$
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        isInitializing = true;
        try {
            lockordersButton.setSelection(configuration.getAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_LOCKORDERS, HelgrindLaunchConstants.DEFAULT_HELGRIND_LOCKORDERS));
            historyCombo.setText(configuration.getAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_HISTORYLEVEL, HelgrindLaunchConstants.DEFAULT_HELGRIND_HISTORYLEVEL));
            cacheSizeSpinner.setSelection(configuration.getAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_CACHESIZE, HelgrindLaunchConstants.DEFAULT_HELGRIND_CACHESIZE));
        } catch (CoreException e) {
        }
        isInitializing = false;
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_LOCKORDERS, lockordersButton.getSelection());
        configuration.setAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_HISTORYLEVEL, historyCombo.getText());
        configuration.setAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_CACHESIZE, cacheSizeSpinner.getSelection());
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        setErrorMessage(null);
        return true;
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, HelgrindPlugin.TOOL_ID);
        configuration.setAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_LOCKORDERS, HelgrindLaunchConstants.DEFAULT_HELGRIND_LOCKORDERS);
        configuration.setAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_HISTORYLEVEL, HelgrindLaunchConstants.DEFAULT_HELGRIND_HISTORYLEVEL);
        configuration.setAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_CACHESIZE, HelgrindLaunchConstants.DEFAULT_HELGRIND_CACHESIZE);
    }

    @Override
    public void setValgrindVersion(Version ver) {
        // no constraints
    }

    @Override
    protected void updateLaunchConfigurationDialog() {
        if (!isInitializing) {
            super.updateLaunchConfigurationDialog();
        }
    }

    public Button getLockordersButton() {
        return lockordersButton;
    }

    public Spinner getCacheSizeSpinner() {
        return cacheSizeSpinner;
    }

    public Combo getHistoryCombo() {
        return historyCombo;
    }
}
