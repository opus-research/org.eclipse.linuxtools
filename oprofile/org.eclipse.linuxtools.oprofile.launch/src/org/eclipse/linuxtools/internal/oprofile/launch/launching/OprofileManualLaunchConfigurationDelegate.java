/*******************************************************************************
 * Copyright (c) 2009, 2017 Red Hat, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.launch.launching;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonEvent;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchMessages;
import org.eclipse.linuxtools.internal.oprofile.launch.configuration.LaunchOptions;
import org.eclipse.linuxtools.internal.oprofile.ui.view.OprofileViewSaveDefaultSessionAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class OprofileManualLaunchConfigurationDelegate extends AbstractOprofileLaunchConfigurationDelegate {
    @Override
    protected boolean preExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, ILaunch launch) {
        // Set current project to allow using the oprofile path that
        // was chosen for the project
        Oprofile.OprofileProject.setProject(getProject());
        return true;
    }

    @Override
	protected void postExec(LaunchOptions options, OprofileDaemonEvent[] daemonEvents, Process process) {
		Display.getDefault().syncExec(() -> {
			// TODO: have a initialization dialog to do reset and setupDaemon?
			// using a progress dialog, can't abort the launch if there's an exception..
			if (!oprofileStatus()) {
				return;
			}

			// manual oprofile control dialog
			final OprofiledControlDialog dlg = new OprofiledControlDialog();

			// This was earlier in an if (!fLaunch.isTerminated()) {} block. From a
			// usability perspective I think it's better to show the oprofile control
			// dialog regardless, since it might not show up at all if the launch
			// has already be terminated. Anyhow, I think it's better this way.
			// --Severin, 2011-02-11
			dlg.setBlockOnOpen(false);
			dlg.open();

		});
	}

    /**
     * A custom dialog box to control the oprofile daemon.
     */
    private class OprofiledControlDialog extends MessageDialog {
        private Button startDaemonButton;
        private Button stopDaemonButton;
        private Button refreshViewButton;
        private Button resetSessionButton;
        private Button saveSessionButton;
        private List feedbackList;

        public OprofiledControlDialog () {
            super(new Shell(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()), OprofileLaunchMessages.getString("oprofiledcontroldialog.title"), null, null, MessageDialog.NONE, new String[] { IDialogConstants.OK_LABEL }, 0); //$NON-NLS-1$

            //override styles; makes the dialog non-modal
            setShellStyle(SWT.CLOSE | SWT.TITLE );
        }

        @Override
        protected Control createCustomArea(Composite parent) {
            Composite area = new Composite(parent, 0);
            Layout layout = new GridLayout(5, true);
            GridData gd = new GridData();

            area.setLayout(layout);
            area.setLayoutData(gd);

            startDaemonButton = new Button(area, SWT.PUSH);
            startDaemonButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            startDaemonButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.startdaemon")); //$NON-NLS-1$
            startDaemonButton.addSelectionListener(new SelectionAdapter() {
                @Override
				public void widgetSelected(SelectionEvent e) {
					startDaemonButton.setEnabled(false);
					stopDaemonButton.setEnabled(true);
					refreshViewButton.setEnabled(true);
					resetSessionButton.setEnabled(true);
					saveSessionButton.setEnabled(true);
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.startdaemon")); //$NON-NLS-1$
				}
			});

            stopDaemonButton = new Button(area, SWT.PUSH);
            stopDaemonButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            stopDaemonButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.stopdaemon")); //$NON-NLS-1$
            stopDaemonButton.setEnabled(false);        //disabled at start
			stopDaemonButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					startDaemonButton.setEnabled(true);
					stopDaemonButton.setEnabled(false);
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.stopdaemon")); //$NON-NLS-1$
				}
			});

            saveSessionButton = new Button(area, SWT.PUSH);
            saveSessionButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            saveSessionButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.savesession")); //$NON-NLS-1$
            saveSessionButton.setEnabled(false);        //disabled at start
            saveSessionButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.save")); //$NON-NLS-1$
                    OprofileViewSaveDefaultSessionAction hack = new OprofileViewSaveDefaultSessionAction();
                    hack.run();
                }});

            resetSessionButton = new Button(area, SWT.PUSH);
            resetSessionButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            resetSessionButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.resetsession")); //$NON-NLS-1$
            resetSessionButton.setEnabled(false);        //disabled at start
			resetSessionButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					refreshOprofileView(); // without refresh can lead to inconsistencies for save session
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.reset")); //$NON-NLS-1$
				}
			});

            refreshViewButton = new Button(area, SWT.PUSH);
            refreshViewButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            refreshViewButton.setText(OprofileLaunchMessages.getString("oprofiledcontroldialog.buttons.refreshview")); //$NON-NLS-1$
            refreshViewButton.setEnabled(false);        //disabled at start
			refreshViewButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.dumpsamples")); //$NON-NLS-1$
					refreshOprofileView();
					addToFeedbackList(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.refreshed")); //$NON-NLS-1$
				}
			});


            List feedback = new List(area, SWT.READ_ONLY | SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
            feedback.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1));
            feedback.add(OprofileLaunchMessages.getString("oprofiledcontroldialog.feedback.init")); //$NON-NLS-1$
            feedbackList = feedback;

            return area;
        }

        //a little hack to get the list to auto scroll to the newly added item
        private void addToFeedbackList(String s) {
            feedbackList.add(s,0);
            feedbackList.setTopIndex(0);
        }
    }

}
