/*******************************************************************************
 * Copyright (c) 2013 Kalray
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xavier Raynaud - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.graph;

import java.io.StringReader;
import java.text.MessageFormat;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.linuxtools.internal.tmf.ui.graph.ExtractDataJob;
import org.eclipse.linuxtools.internal.tmf.ui.graph.expression.PlottingExpression;
import org.eclipse.linuxtools.internal.tmf.ui.graph.language.ParseException;
import org.eclipse.linuxtools.internal.tmf.ui.graph.language.PlottingLanguage;
import org.eclipse.linuxtools.internal.tmf.ui.graph.language.TokenMgrError;
import org.eclipse.linuxtools.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class PlottingDialog extends Dialog {

    private final static String XEXPR = "XEXPR"; //$NON-NLS-1$
    private final static String YEXPR = "YEXPR"; //$NON-NLS-1$

    private final ITmfTrace fTrace;
    private final ITmfFilterTreeNode fFilter;

    private Text fXSerieText;
    private Text fYSerieText;
    private Label fErrorLabel;
    private PlottingExpression fXPlottingExpression;
    private PlottingExpression fYPlottingExpression;
    private String xExpression;
    private String yExpression;

    public PlottingDialog(Shell s, ITmfTrace trace, ITmfFilterTreeNode filter) {
        super(s);
        this.fTrace = trace;
        this.fFilter = filter;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control c = super.createContents(parent);
        this.getShell().setText(Messages.PlottingDialog_Title);
        validate();
        return c;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.dialogs.ListDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite c) {
        Composite container = (Composite) super.createDialogArea(c);
        Composite parent = new Composite(container, SWT.NONE);
        parent.setLayout(new GridLayout(1, false));
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label label = new Label(parent, SWT.WRAP);
        label.setText(Messages.PlottingDialog_HelpMsg);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Group xGroup = new Group(parent, SWT.NONE);
        xGroup.setText(Messages.PlottingDialog_ValuesOnXAxis);
        xGroup.setLayout(new GridLayout(1, false));
        xGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fXSerieText = new Text(xGroup, SWT.BORDER | SWT.MULTI);
        String xText = getDialogBoundsSettings().get(XEXPR);
        if (xText == null)
            xText = "event.timestamp"; //$NON-NLS-1$
        fXSerieText.setText(xText);
        fXSerieText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fXSerieText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                validate();
            }
        });

        Group yGroup = new Group(parent, SWT.NONE);
        yGroup.setText(Messages.PlottingDialog_ValuesOnYAxis);
        yGroup.setLayout(new GridLayout(1, false));
        yGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fYSerieText = new Text(yGroup, SWT.BORDER | SWT.MULTI);
        String yText = getDialogBoundsSettings().get(YEXPR);
        if (yText == null)
            yText = "1"; //$NON-NLS-1$
        fYSerieText.setText(yText);
        fYSerieText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fYSerieText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                validate();
            }
        });

        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        fErrorLabel = new Label(parent, SWT.WRAP);
        fErrorLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        fErrorLabel.setForeground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED));
        return container;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings rootSettings = Activator.getDefault().getDialogSettings();
        IDialogSettings settings = rootSettings.getSection(PlottingDialog.class.toString());
        if (settings == null) {
            settings = rootSettings.addNewSection(PlottingDialog.class.toString());
        }
        return settings;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#isResizable()
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    private void validate() {
        getButton(IDialogConstants.OK_ID).setEnabled(true);
        fErrorLabel.setText(""); //$NON-NLS-1$
        fXPlottingExpression = null;
        fYPlottingExpression = null;

        xExpression = fXSerieText.getText();
        try {
            PlottingLanguage l = new PlottingLanguage(new StringReader(xExpression));
            fXPlottingExpression = l.Expression();
        } catch (ParseException _) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            fErrorLabel.setText(MessageFormat.format(Messages.PlottingDialog_InvalidValueOnXAxis, _.getMessage()));
            return;
        } catch (TokenMgrError _) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            fErrorLabel.setText(MessageFormat.format(Messages.PlottingDialog_InvalidValueOnXAxis, _.getMessage()));
            return;
        }

        yExpression = fYSerieText.getText();
        try {
            PlottingLanguage l = new PlottingLanguage(new StringReader(yExpression));
            fYPlottingExpression = l.Expression();
        } catch (ParseException _) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            fErrorLabel.setText(MessageFormat.format(Messages.PlottingDialog_InvalidValueOnYAxis, _.getMessage()));
            return;
        } catch (TokenMgrError _) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            fErrorLabel.setText(MessageFormat.format(Messages.PlottingDialog_InvalidValueOnYAxis, _.getMessage()));
            return;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        getDialogBoundsSettings().put(XEXPR, fXSerieText.getText());
        getDialogBoundsSettings().put(YEXPR, fYSerieText.getText());
        super.okPressed();
        PlottingExpression xExpr = getPlottingExpressionOnXAxis();
        PlottingExpression yExpr = getPlottingExpressionOnYAxis();
        String xAxisName = getXExpression();
        String yAxisName = getYExpression();
        Job j = new ExtractDataJob(fTrace, fFilter, xExpr, yExpr, xAxisName, yAxisName);
        j.setUser(true);
        j.schedule();
    }

    /**
     * @return the plottingExpression for X axis
     */
    public PlottingExpression getPlottingExpressionOnXAxis() {
        return fXPlottingExpression;
    }

    /**
     * @return the plottingExpression for Y axis
     */
    public PlottingExpression getPlottingExpressionOnYAxis() {
        return fYPlottingExpression;
    }

    /**
     * @return the xExpression
     */
    public String getXExpression() {
        return xExpression;
    }

    /**
     * @return the yExpression
     */
    public String getYExpression() {
        return yExpression;
    }
}
