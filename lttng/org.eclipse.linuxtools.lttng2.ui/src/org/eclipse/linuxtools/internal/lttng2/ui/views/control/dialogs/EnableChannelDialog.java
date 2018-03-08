/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Simon Delisle - Updated for support of LTTng Tools 2.2
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.ChannelInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TargetNodeComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceDomainComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.service.LTTngControlUnusedValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * <p>
 * Dialog box for collecting channel information when enabling a channel (which will be created).
 * </p>
 *
 * @author Bernd Hufmann
 */
public class EnableChannelDialog extends Dialog implements IEnableChannelDialog {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The icon file for this dialog box.
     */
    public static final String ENABLE_CHANNEL_ICON_FILE = "icons/elcl16/add_button.gif"; //$NON-NLS-1$

    /**
     *  To indicate that the default value will be used for this field
     */
    private String DEFAULT_TEXT = "Default"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The text widget for the channel name
     */
    private Text fChannelNameText = null;
    /**
     * The discard mode of the channel.
     */
    private Button fDiscardModeButton = null;
    /**
     * The overwrite mode of the channel.
     */
    private Button fOverwriteModeButton = null;
    /**
     * The sub-buffer size of the channel.
     */
    private Text fSubBufferSizeText = null;
    /**
     * The number of sub-buffers of the channel.
     */
    private Text fNumberOfSubBuffersText = null;
    /**
     * The switch timer interval of the channel.
     */
    private Text fSwitchTimerText = null;
    /**
     * The read timer interval of the channel.
     */
    private Text fReadTimerText = null;
    /**
     * Radio button for selecting kernel domain.
     */
    private Button fKernelButton = null;
    /**
     * Radio button for selecting UST domain.
     */
    private Button fUstButton = null;
    /**
     * The parent domain component where the channel node should be added.
     * Null in case of creation on session level.
     */
    private TraceDomainComponent fDomain = null;
    /**
     * The target node component
     */
    private TargetNodeComponent fTargetNodeComponent = null;
    /**
     * Common verify listener for numeric text input.
     */
    private VerifyListener fVerifyListener = null;
    /**
     * Output channel information.
     */
    private IChannelInfo fChannelInfo = null;
    /**
     * Output domain information. True in case of Kernel domain. False for UST.
     */
    private boolean fIsKernel;
    /**
     *  Flag which indicates whether Kernel domain is available or not
     */
    private boolean fHasKernel;
    /**
     * Maximum size of trace files of the channel.
     */
    private Text fMaxSizeTraceText = null;
    /**
     * Maximum number of trace files of the channel.
     */
    private Text fMaxNumberTraceText = null;
    /**
     * CheckBox for selecting per UID buffers.
     */
    private Button fUIDBuffersButton = null;
    /**
     * CheckBox to configure metadata channel
     */
    private Button fMetadataChannelButton = null;
    /**
     * Previous channel name
     */
    private String fPreviousChannelName = null;


    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param shell - a shell for the display of the dialog
     */
    public EnableChannelDialog(Shell shell) {
       super(shell);
       fIsKernel = true;

        // Common verify listener
        fVerifyListener = new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                // only numbers and default are allowed.
                e.doit = e.text.matches("[0-9]*") || e.text.matches(DEFAULT_TEXT); //$NON-NLS-1$
            }
        };
        setShellStyle(SWT.RESIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public IChannelInfo getChannelInfo() {
        return fChannelInfo;
    }

    @Override
    public void setDomainComponent(TraceDomainComponent domain) {
        fDomain = domain;
        if (fDomain != null) {
            fIsKernel = fDomain.isKernel();
        } else {
            fIsKernel = true;
        }
    }

    @Override
    public boolean isKernel() {
        return fIsKernel;
    }

    @Override
    public void setHasKernel(boolean hasKernel) {
        if (fDomain != null) {
            fIsKernel = fDomain.isKernel();
        } else {
            fIsKernel = hasKernel;
        }

        fHasKernel = hasKernel;
    }

    @Override
    public void setTargetNodeComponent(TargetNodeComponent node) {
        fTargetNodeComponent = node;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.TraceControl_EnableChannelDialogTitle);
        newShell.setImage(Activator.getDefault().loadIcon(ENABLE_CHANNEL_ICON_FILE));
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        // Main dialog panel
        Composite dialogComposite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, true);
        dialogComposite.setLayout(layout);

        Label channelNameLabel = new Label(dialogComposite, SWT.RIGHT);
        channelNameLabel.setText(Messages.TraceControl_EnableChannelNameLabel);
        fChannelNameText = new Text(dialogComposite, SWT.NONE);
        fChannelNameText.setToolTipText(Messages.TraceControl_EnableChannelNameTooltip);

        Label subBufferSizeLabel = new Label(dialogComposite, SWT.RIGHT);
        subBufferSizeLabel.setText(Messages.TraceControl_SubBufferSizePropertyName);
        fSubBufferSizeText = new Text(dialogComposite, SWT.NONE);
        fSubBufferSizeText.setToolTipText(Messages.TraceControl_EnableChannelSubBufferSizeTooltip);
        fSubBufferSizeText.addVerifyListener(fVerifyListener);

        Label numSubBufferLabel = new Label(dialogComposite, SWT.RIGHT);
        numSubBufferLabel.setText(Messages.TraceControl_NbSubBuffersPropertyName);
        fNumberOfSubBuffersText = new Text(dialogComposite, SWT.NONE);
        fNumberOfSubBuffersText.setToolTipText(Messages.TraceControl_EnableChannelNbSubBuffersTooltip);
        fNumberOfSubBuffersText.addVerifyListener(fVerifyListener);

        Label switchTimerLabel = new Label(dialogComposite, SWT.RIGHT);
        switchTimerLabel.setText(Messages.TraceControl_SwitchTimerPropertyName);
        fSwitchTimerText = new Text(dialogComposite, SWT.NONE);
        fSwitchTimerText.setToolTipText(Messages.TraceControl_EnableChannelSwitchTimerTooltip);
        fSwitchTimerText.addVerifyListener(fVerifyListener);

        Label readTimerLabel = new Label(dialogComposite, SWT.RIGHT);
        readTimerLabel.setText(Messages.TraceControl_ReadTimerPropertyName);
        fReadTimerText = new Text(dialogComposite, SWT.NONE);
        fReadTimerText.setToolTipText(Messages.TraceControl_EnableChannelReadTimerTooltip);
        fReadTimerText.addVerifyListener(fVerifyListener);

        if (fTargetNodeComponent.isTraceFileRotationSupported()) {
            Label maxSizeTraceFilesLabel = new Label(dialogComposite, SWT.RIGHT);
            maxSizeTraceFilesLabel.setText(Messages.TraceControl_MaxSizeTraceFilesPropertyName);
            fMaxSizeTraceText = new Text(dialogComposite, SWT.NONE);
            fMaxSizeTraceText.setToolTipText(Messages.TraceControl_EnbleChannelMaxSizeTraceFilesTooltip);
            fMaxSizeTraceText.addVerifyListener(fVerifyListener);

            Label maxNumTraceFilesLabel = new Label(dialogComposite, SWT.RIGHT);
            maxNumTraceFilesLabel.setText(Messages.TraceControl_MaxNumTraceFilesPropertyName);
            fMaxNumberTraceText = new Text(dialogComposite, SWT.NONE);
            fMaxNumberTraceText.setToolTipText(Messages.TraceControl_EnbleChannelMaxNumTraceFilesTooltip);
            fMaxNumberTraceText.addVerifyListener(fVerifyListener);
        }

        if (fTargetNodeComponent.isPeriodicalMetadataFlushSupported()) {
            fMetadataChannelButton = new Button(dialogComposite, SWT.CHECK);
            fMetadataChannelButton.setText(Messages.TraceControl_ConfigureMetadataChannelName);
            fMetadataChannelButton.setSelection(false);

            fMetadataChannelButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fMetadataChannelButton.getSelection()) {
                        fPreviousChannelName = fChannelNameText.getText();
                        fChannelNameText.setText("metadata"); //$NON-NLS-1$
                        fChannelNameText.setEnabled(false);
                    } else {
                        fChannelNameText.setText(fPreviousChannelName);
                        fChannelNameText.setEnabled(true);
                    }
                }
            });
        }

        Group discardModeGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
        discardModeGroup.setText(Messages.TraceControl_EnableChannelDiscardModeGroupName);
        layout = new GridLayout(2, true);
        discardModeGroup.setLayout(layout);

        fDiscardModeButton = new  Button(discardModeGroup, SWT.RADIO);
        fDiscardModeButton.setText(Messages.TraceControl_EnableChannelDiscardModeLabel);
        fDiscardModeButton.setToolTipText(Messages.TraceControl_EnableChannelDiscardModeTooltip);
        fDiscardModeButton.setSelection(true);

        fOverwriteModeButton = new Button(discardModeGroup, SWT.RADIO);
        fOverwriteModeButton.setText(Messages.TraceControl_EnableChannelOverwriteModeLabel);
        fOverwriteModeButton.setToolTipText(Messages.TraceControl_EnableChannelOverwriteModeTooltip);
        fOverwriteModeButton.setSelection(false);

        Group domainGroup = new Group(dialogComposite, SWT.SHADOW_NONE);
        domainGroup.setText(Messages.TraceControl_DomainDisplayName);
        layout = new GridLayout(2, true);
        domainGroup.setLayout(layout);

        fKernelButton = new Button(domainGroup, SWT.RADIO);
        fKernelButton.setText(Messages.TraceControl_KernelDomainDisplayName);
        fKernelButton.setSelection(fIsKernel);
        fUstButton = new Button(domainGroup, SWT.RADIO);
        fUstButton.setText(Messages.TraceControl_UstDisplayName);
        fUstButton.setSelection(!fIsKernel);

        if (fTargetNodeComponent.isPerUIDBuffersSupported()) {
            Button fDummyButton = new Button(domainGroup, SWT.CHECK);
            fDummyButton.setEnabled(false);
            fDummyButton.setVisible(false);
            fUIDBuffersButton = new Button(domainGroup, SWT.CHECK);
            fUIDBuffersButton.setText(Messages.TraceControl_PerUidBuffersDisplayName);
            fUIDBuffersButton.setSelection(false);
            fUIDBuffersButton.setEnabled(!fIsKernel);

            fUstButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (fUstButton.getSelection()) {
                        fUIDBuffersButton.setEnabled(true);
                    } else {
                        fUIDBuffersButton.setEnabled(false);
                    }
                }
            });
        }

        if ((fDomain != null) || (!fHasKernel)) {
            fKernelButton.setEnabled(false);
            fUstButton.setEnabled(false);
        }

        // layout widgets
        GridData data = new GridData(GridData.FILL, GridData.CENTER, false, false, 3, 1);
        discardModeGroup.setLayoutData(data);
        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fDiscardModeButton.setLayoutData(data);
        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fOverwriteModeButton.setLayoutData(data);

        data = new GridData(GridData.FILL, GridData.CENTER, false, false, 3, 1);
        domainGroup.setLayoutData(data);

        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fKernelButton.setLayoutData(data);
        data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
        fUstButton.setLayoutData(data);
        if (fTargetNodeComponent.isPerUIDBuffersSupported()) {
            data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
            fUIDBuffersButton.setLayoutData(data);
        }
        if (fTargetNodeComponent.isPeriodicalMetadataFlushSupported()) {
            data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true);
            fMetadataChannelButton.setLayoutData(data);
        }

        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;

        fChannelNameText.setLayoutData(data);
        fSubBufferSizeText.setLayoutData(data);
        fNumberOfSubBuffersText.setLayoutData(data);
        fSwitchTimerText.setLayoutData(data);
        fReadTimerText.setLayoutData(data);
        if (fTargetNodeComponent.isTraceFileRotationSupported()) {
            fMaxNumberTraceText.setLayoutData(data);
            fMaxSizeTraceText.setLayoutData(data);
        }

        setDefaults();

        return dialogComposite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.DETAILS_ID, "&Default", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {
        // Set channel information
        fChannelInfo = new ChannelInfo(fChannelNameText.getText());
        fChannelInfo.setSubBufferSize(fSubBufferSizeText.getText().equals(DEFAULT_TEXT) ? LTTngControlUnusedValue.UNUSED_VALUE : Long.parseLong(fSubBufferSizeText.getText()));
        fChannelInfo.setNumberOfSubBuffers(fNumberOfSubBuffersText.getText().equals(DEFAULT_TEXT) ? LTTngControlUnusedValue.UNUSED_VALUE : Integer.parseInt(fNumberOfSubBuffersText.getText()));
        fChannelInfo.setSwitchTimer(fSwitchTimerText.getText().equals(DEFAULT_TEXT) ? LTTngControlUnusedValue.UNUSED_VALUE : Long.parseLong(fSwitchTimerText.getText()));
        fChannelInfo.setReadTimer(fReadTimerText.getText().equals(DEFAULT_TEXT) ? LTTngControlUnusedValue.UNUSED_VALUE : Long.parseLong(fReadTimerText.getText()));
        fChannelInfo.setOverwriteMode(fOverwriteModeButton.getSelection());
        if (fTargetNodeComponent.isTraceFileRotationSupported()) {
            fChannelInfo.setMaxSizeTraceFiles(fMaxSizeTraceText.getText().equals(DEFAULT_TEXT) ? LTTngControlUnusedValue.UNUSED_VALUE : Integer.parseInt(fMaxSizeTraceText.getText()));
            fChannelInfo.setMaxNumberTraceFiles(fMaxNumberTraceText.getText().equals(DEFAULT_TEXT) ? LTTngControlUnusedValue.UNUSED_VALUE : Integer.parseInt(fMaxNumberTraceText.getText()));
        }
        if (fTargetNodeComponent.isPerUIDBuffersSupported()) {
            fChannelInfo.setBuffersUID(fUIDBuffersButton.getSelection());
        }

        fIsKernel = fKernelButton.getSelection();

        // Check for invalid names
        if (!fChannelInfo.getName().matches("^[a-zA-Z0-9\\-\\_]{1,}$")) { //$NON-NLS-1$
            MessageDialog.openError(getShell(),
                  Messages.TraceControl_EnableChannelDialogTitle,
                  Messages.TraceControl_InvalidChannelNameError + " (" + fChannelInfo.getName() + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // Check for duplicate names
        if (fDomain != null && fDomain.containsChild(fChannelInfo.getName())) {
            MessageDialog.openError(getShell(),
                    Messages.TraceControl_EnableChannelDialogTitle,
                    Messages.TraceControl_ChannelAlreadyExistsError + " (" + fChannelInfo.getName() + ") \n");  //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        // validation successful -> call super.okPressed()
        super.okPressed();
    }

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.DETAILS_ID) {
            setDefaults();
            return;
        }
        super.buttonPressed(buttonId);
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    /**
     * Sets default value depending on Kernel or UST
     */
    private void setDefaults() {
        fSwitchTimerText.setText(DEFAULT_TEXT);
        fReadTimerText.setText(DEFAULT_TEXT);
        fOverwriteModeButton.setSelection(IChannelInfo.DEFAULT_OVERWRITE_MODE);
        if (fTargetNodeComponent.isTraceFileRotationSupported()) {
            fMaxSizeTraceText.setText(DEFAULT_TEXT);
            fMaxNumberTraceText.setText(DEFAULT_TEXT);
        }
        fSubBufferSizeText.setText(DEFAULT_TEXT);
        fNumberOfSubBuffersText.setText(DEFAULT_TEXT);
    }
}
