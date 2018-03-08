/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.findreplace;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.linuxtools.dataviewers.STDataViewersActivator;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * Find/Replace dialog. The dialog is opened on a particular target but can be re-targeted. Internally used by the
 * <code>FindReplaceAction</code>
 */
class STFindReplaceDialog extends Dialog {
    private ViewerCell index;

    private ViewerCell findReplacePosition;

    /**
     * Updates the find replace dialog on activation changes.
     */
    class ActivationListener extends ShellAdapter {
        /*
         * @see ShellListener#shellActivated(ShellEvent)
         */
        public void shellActivated(ShellEvent e) {
            fActiveShell = (Shell) e.widget;
            updateButtonState();

            if (fGiveFocusToFindField && getShell() == fActiveShell && okToUse(fFindField))
                fFindField.setFocus();

        }

        /*
         * @see ShellListener#shellDeactivated(ShellEvent)
         */
        public void shellDeactivated(ShellEvent e) {
            fGiveFocusToFindField = false;

            storeSettings();

            fGlobalRadioButton.setSelection(true);
            fSelectedRangeRadioButton.setSelection(false);
            fUseSelectedLines = false;

            fActiveShell = null;
            updateButtonState();
        }
    }

    /**
     * Modify listener to update the search result in case of incremental search.
     * 
     * @since 2.0
     */
    private class FindModifyListener implements ModifyListener {

        /*
         * @see ModifyListener#modifyText(ModifyEvent)
         */
        public void modifyText(ModifyEvent e) {
            if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked()) {
                if (fFindField.getText().equals("") && fTarget != null) { //$NON-NLS-1$
                    // empty selection at base location
                    ViewerCell start = fIncrementalBaseLocation;

                    if (isForwardSearch() && !fNeedsInitialFindBeforeReplace || !isForwardSearch()
                            && fNeedsInitialFindBeforeReplace) {
                        if (start.getNeighbor(ViewerCell.RIGHT, true) == null)
                            start.getNeighbor(ViewerCell.BELOW, true);
                    }
                    fNeedsInitialFindBeforeReplace = false;
                    findAndSelect(
                            start,
                            "", isForwardSearch(), isCaseSensitiveSearch(), isWholeWordSearch(), isRegExSearchAvailableAndChecked(), isWrapSearch()); //$NON-NLS-1$
                } else {
                    performSearch(false);
                }
            }

            updateButtonState(!isIncrementalSearch());
        }
    }

    /** The size of the dialogs search history. */
    private static final int HISTORY_SIZE = 5;

    private ViewerCell fIncrementalBaseLocation;
    private boolean fWrapInit, fCaseInit, fWholeWordInit, fForwardInit, fGlobalInit, fIncrementalInit;
    /**
     * Tells whether an initial find operation is needed before the replace operation.
     * 
     * @since 3.0
     */
    private boolean fNeedsInitialFindBeforeReplace;
    /**
     * Initial value for telling whether the search string is a regular expression.
     * 
     * @since 3.0
     */
    boolean fIsRegExInit;

    private List<String> fFindHistory;
    private List<String> fReplaceHistory;

    private boolean fIsTargetEditable;
    private ISTFindReplaceTarget fTarget;
    private Shell fParentShell;
    private Shell fActiveShell;

    private final ActivationListener fActivationListener = new ActivationListener();
    private final ModifyListener fFindModifyListener = new FindModifyListener();

    private Label fReplaceLabel, fStatusLabel;
    private Button fForwardRadioButton, fGlobalRadioButton, fSelectedRangeRadioButton;
    private Button fCaseCheckBox, fWrapCheckBox, fWholeWordCheckBox, fIncrementalCheckBox;

    /**
     * Checkbox for selecting whether the search string is a regular expression.
     * 
     * @since 3.0
     */
    private Button fIsRegExCheckBox;

    private Button fReplaceSelectionButton, fReplaceFindButton, fFindNextButton, fReplaceAllButton;
    private Combo fFindField, fReplaceField;

    /**
     * Find and replace command adapters.
     * 
     * @since 3.3
     */
    private ContentAssistCommandAdapter fContentAssistFindField, fContentAssistReplaceField;

    private Rectangle fDialogPositionInit;

    private IDialogSettings fDialogSettings;
    /**
     * Tells whether the target supports regular expressions. <code>true</code> if the target supports regular
     * expressions
     * 
     * @since 3.0
     */
    private boolean fIsTargetSupportingRegEx;
    /**
     * Tells whether fUseSelectedLines radio is checked.
     * 
     * @since 3.0
     */
    private boolean fUseSelectedLines;
    /**
     * <code>true</code> if the find field should receive focus the next time the dialog is activated,
     * <code>false</code> otherwise.
     * 
     * @since 3.0
     */
    private boolean fGiveFocusToFindField = true;

    /**
     * Creates a new dialog with the given shell as parent.
     * 
     * @param parentShell
     *            the parent shell
     */
    public STFindReplaceDialog(Shell parentShell) {
        super(parentShell);

        fParentShell = null;
        fTarget = null;

        fDialogPositionInit = null;
        fFindHistory = new ArrayList<String>(HISTORY_SIZE - 1);
        fReplaceHistory = new ArrayList<String>(HISTORY_SIZE - 1);

        fWrapInit = false;
        fCaseInit = false;
        fIsRegExInit = false;
        fWholeWordInit = false;
        fIncrementalInit = false;
        fGlobalInit = true;
        fForwardInit = true;

        readConfiguration();

        setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE | SWT.RESIZE);
        setBlockOnOpen(false);
    }

    /**
     * Returns this dialog's parent shell.
     * 
     * @return the dialog's parent shell
     */
    public Shell getParentShell() {
        return super.getParentShell();
    }

    /**
     * Returns <code>true</code> if control can be used.
     * 
     * @param control
     *            the control to be checked
     * @return <code>true</code> if control can be used
     */
    private boolean okToUse(Control control) {
        return control != null && !control.isDisposed();
    }

    /*
     * @see org.eclipse.jface.window.Window#create()
     */
    public void create() {

        super.create();

        Shell shell = getShell();
        shell.addShellListener(fActivationListener);

        // set help context
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IAbstractTextEditorHelpContextIds.FIND_REPLACE_DIALOG);

        // fill in combo contents
        fFindField.removeModifyListener(fFindModifyListener);
        updateCombo(fFindField, fFindHistory);
        fFindField.addModifyListener(fFindModifyListener);
        updateCombo(fReplaceField, fReplaceHistory);

        // get find string
        initFindStringFromSelection();

        // set dialog position
        if (fDialogPositionInit != null)
            shell.setBounds(fDialogPositionInit);

        shell.setText(EditorMessages.FindReplace_title);
        // shell.setImage(null);
    }

    /**
     * Create the button section of the find/replace dialog.
     * 
     * @param parent
     *            the parent composite
     * @return the button section
     */
    private Composite createButtonSection(Composite parent) {

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = -2; // this is intended
        panel.setLayout(layout);

        fFindNextButton = makeButton(panel, EditorMessages.FindReplace_FindNextButton_label, 102, true,
                new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked())
                            initIncrementalBaseLocation();

                        fNeedsInitialFindBeforeReplace = false;
                        performSearch();
                        updateFindHistory();
                        fFindNextButton.setFocus();
                    }
                });
        setGridData(fFindNextButton, SWT.FILL, true, SWT.FILL, false);

        fReplaceFindButton = makeButton(panel, EditorMessages.FindReplace_ReplaceFindButton_label, 103, false,
                new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        /*
                         * if (fNeedsInitialFindBeforeReplace) performSearch(); if (performReplaceSelection())
                         * performSearch(); updateFindAndReplaceHistory(); fReplaceFindButton.setFocus();
                         */
                    }
                });
        setGridData(fReplaceFindButton, SWT.FILL, false, SWT.FILL, false);

        fReplaceSelectionButton = makeButton(panel, EditorMessages.FindReplace_ReplaceSelectionButton_label, 104,
                false, new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        /*
                         * if (fNeedsInitialFindBeforeReplace) performSearch(); performReplaceSelection();
                         * updateFindAndReplaceHistory(); fFindNextButton.setFocus();
                         */
                    }
                });
        setGridData(fReplaceSelectionButton, SWT.FILL, false, SWT.FILL, false);

        fReplaceAllButton = makeButton(panel, EditorMessages.FindReplace_ReplaceAllButton_label, 105, false,
                new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        /*
                         * performReplaceAll(); updateFindAndReplaceHistory(); fFindNextButton.setFocus();
                         */
                    }
                });
        setGridData(fReplaceAllButton, SWT.FILL, true, SWT.FILL, false);

        // Make the all the buttons the same size as the Remove Selection button.
        fReplaceAllButton.setEnabled(isEditable());

        return panel;
    }

    /**
     * Creates the options configuration section of the find replace dialog.
     * 
     * @param parent
     *            the parent composite
     * @return the options configuration section
     */
    private Composite createConfigPanel(Composite parent) {

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = true;
        panel.setLayout(layout);

        Composite directionGroup = createDirectionGroup(panel);
        setGridData(directionGroup, SWT.FILL, true, SWT.FILL, false);

        Composite scopeGroup = createScopeGroup(panel);
        setGridData(scopeGroup, SWT.FILL, true, SWT.FILL, false);

        Composite optionsGroup = createOptionsGroup(panel);
        setGridData(optionsGroup, SWT.FILL, true, SWT.FILL, true);
        ((GridData) optionsGroup.getLayoutData()).horizontalSpan = 2;

        return panel;
    }

    /*
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {

        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.makeColumnsEqualWidth = true;
        panel.setLayout(layout);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite inputPanel = createInputPanel(panel);
        setGridData(inputPanel, SWT.FILL, true, SWT.TOP, false);

        Composite configPanel = createConfigPanel(panel);
        setGridData(configPanel, SWT.FILL, true, SWT.TOP, true);

        Composite buttonPanelB = createButtonSection(panel);
        setGridData(buttonPanelB, SWT.RIGHT, true, SWT.BOTTOM, false);

        Composite statusBar = createStatusAndCloseButton(panel);
        setGridData(statusBar, SWT.FILL, true, SWT.BOTTOM, false);

        updateButtonState();

        applyDialogFont(panel);

        return panel;
    }

    private void setContentAssistsEnablement(boolean enable) {
        fContentAssistFindField.setEnabled(enable);
        fContentAssistReplaceField.setEnabled(enable);
    }

    /**
     * Creates the direction defining part of the options defining section of the find replace dialog.
     * 
     * @param parent
     *            the parent composite
     * @return the direction defining part
     */
    private Composite createDirectionGroup(Composite parent) {

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        panel.setLayout(layout);

        Group group = new Group(panel, SWT.SHADOW_ETCHED_IN);
        group.setText(EditorMessages.FindReplace_Direction);
        GridLayout groupLayout = new GridLayout();
        group.setLayout(groupLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        SelectionListener selectionListener = new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                calcolateIndex(fForwardRadioButton.getSelection());
                if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked())
                    initIncrementalBaseLocation();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };

        fForwardRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
        fForwardRadioButton.setText(EditorMessages.FindReplace_ForwardRadioButton_label);
        setGridData(fForwardRadioButton, SWT.LEFT, false, SWT.CENTER, false);
        fForwardRadioButton.addSelectionListener(selectionListener);

        Button backwardRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
        backwardRadioButton.setText(EditorMessages.FindReplace_BackwardRadioButton_label);
        setGridData(backwardRadioButton, SWT.LEFT, false, SWT.CENTER, false);
        backwardRadioButton.addSelectionListener(selectionListener);

        backwardRadioButton.setSelection(!fForwardInit);
        backwardRadioButton.setEnabled(true);
        fForwardRadioButton.setSelection(fForwardInit);

        return panel;
    }

    /**
     * Creates the scope defining part of the find replace dialog.
     * 
     * @param parent
     *            the parent composite
     * @return the scope defining part
     * @since 2.0
     */
    private Composite createScopeGroup(Composite parent) {

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        panel.setLayout(layout);

        Group group = new Group(panel, SWT.SHADOW_ETCHED_IN);
        group.setText(EditorMessages.FindReplace_Scope);
        GridLayout groupLayout = new GridLayout();
        group.setLayout(groupLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        fGlobalRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
        fGlobalRadioButton.setText(EditorMessages.FindReplace_GlobalRadioButton_label);
        setGridData(fGlobalRadioButton, SWT.LEFT, false, SWT.CENTER, false);
        fGlobalRadioButton.setSelection(fGlobalInit);
        fGlobalRadioButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                if (!fGlobalRadioButton.getSelection() || !fUseSelectedLines)
                    return;
                fUseSelectedLines = false;
                useSelectedLines(false);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        fSelectedRangeRadioButton = new Button(group, SWT.RADIO | SWT.LEFT);
        fSelectedRangeRadioButton.setText(EditorMessages.FindReplace_SelectedRangeRadioButton_label);
        setGridData(fSelectedRangeRadioButton, SWT.LEFT, false, SWT.CENTER, false);
        fSelectedRangeRadioButton.setSelection(!fGlobalInit);
        fUseSelectedLines = !fGlobalInit;
        fSelectedRangeRadioButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                if (!fSelectedRangeRadioButton.getSelection() || fUseSelectedLines)
                    return;
                fUseSelectedLines = true;
                useSelectedLines(true);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        return panel;
    }

    /**
     * Tells the dialog to perform searches only in the scope given by the actually selected lines.
     * 
     * @param selectedLines
     *            <code>true</code> if selected lines should be used
     * @since 2.0
     */
    private void useSelectedLines(boolean selectedLines) {
        if (isIncrementalSearch() && !isRegExSearchAvailableAndChecked())
            initIncrementalBaseLocation();

        if (fTarget == null)
            return;

        fTarget.useSelectedLines(selectedLines);

    }

    /**
     * Creates the panel where the user specifies the text to search for and the optional replacement text.
     * 
     * @param parent
     *            the parent composite
     * @return the input panel
     */
    private Composite createInputPanel(Composite parent) {

        ModifyListener listener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateButtonState();
            }
        };

        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        panel.setLayout(layout);

        Label findLabel = new Label(panel, SWT.LEFT);
        findLabel.setText(EditorMessages.FindReplace_Find_label);
        setGridData(findLabel, SWT.LEFT, false, SWT.CENTER, false);

        // Create the find content assist field
        ComboContentAdapter contentAdapter = new ComboContentAdapter();
        RegExContentProposalProvider findProposer = new RegExContentProposalProvider(true);
        fFindField = new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
        fContentAssistFindField = new ContentAssistCommandAdapter(fFindField, contentAdapter, findProposer,
                ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new char[] { '\\', '[', '(' }, true);
        setGridData(fFindField, SWT.FILL, true, SWT.CENTER, false);
        addDecorationMargin(fFindField);
        fFindField.addModifyListener(fFindModifyListener);

        fReplaceLabel = new Label(panel, SWT.LEFT);
        fReplaceLabel.setText(EditorMessages.FindReplace_Replace_label);
        setGridData(fReplaceLabel, SWT.LEFT, false, SWT.CENTER, false);

        // Create the replace content assist field
        RegExContentProposalProvider replaceProposer = new RegExContentProposalProvider(false);
        fReplaceField = new Combo(panel, SWT.DROP_DOWN | SWT.BORDER);
        fContentAssistReplaceField = new ContentAssistCommandAdapter(fReplaceField, contentAdapter, replaceProposer,
                ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new char[] { '$' }, true);
        setGridData(fReplaceField, SWT.FILL, true, SWT.CENTER, false);
        addDecorationMargin(fReplaceField);
        fReplaceField.addModifyListener(listener);

        return panel;
    }

    /**
     * Creates the functional options part of the options defining section of the find replace dialog.
     * 
     * @param parent
     *            the parent composite
     * @return the options group
     */
    private Composite createOptionsGroup(Composite parent) {

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        panel.setLayout(layout);

        Group group = new Group(panel, SWT.SHADOW_NONE);
        group.setText(EditorMessages.FindReplace_Options);
        GridLayout groupLayout = new GridLayout();
        groupLayout.numColumns = 2;
        groupLayout.makeColumnsEqualWidth = true;
        group.setLayout(groupLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        SelectionListener selectionListener = new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                storeSettings();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        };

        fCaseCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
        fCaseCheckBox.setText(EditorMessages.FindReplace_CaseCheckBox_label);
        setGridData(fCaseCheckBox, SWT.LEFT, false, SWT.CENTER, false);
        fCaseCheckBox.setSelection(fCaseInit);
        fCaseCheckBox.addSelectionListener(selectionListener);

        fWrapCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
        fWrapCheckBox.setText(EditorMessages.FindReplace_WrapCheckBox_label);
        setGridData(fWrapCheckBox, SWT.LEFT, false, SWT.CENTER, false);
        fWrapCheckBox.setSelection(fWrapInit);
        fWrapCheckBox.addSelectionListener(selectionListener);

        fWholeWordCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
        fWholeWordCheckBox.setText(EditorMessages.FindReplace_WholeWordCheckBox_label);
        setGridData(fWholeWordCheckBox, SWT.LEFT, false, SWT.CENTER, false);
        fWholeWordCheckBox.setSelection(fWholeWordInit);
        fWholeWordCheckBox.addSelectionListener(selectionListener);

        fIncrementalCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
        fIncrementalCheckBox.setText(EditorMessages.FindReplace_IncrementalCheckBox_label);
        setGridData(fIncrementalCheckBox, SWT.LEFT, false, SWT.CENTER, false);
        fIncrementalCheckBox.setSelection(fIncrementalInit);
        fIncrementalCheckBox.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                if (isIncrementalSearch() && !isRegExSearch())
                    initIncrementalBaseLocation();

                storeSettings();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        fIncrementalCheckBox.setEnabled(false);

        fIsRegExCheckBox = new Button(group, SWT.CHECK | SWT.LEFT);
        fIsRegExCheckBox.setText(EditorMessages.FindReplace_RegExCheckbox_label);
        setGridData(fIsRegExCheckBox, SWT.LEFT, false, SWT.CENTER, false);
        ((GridData) fIsRegExCheckBox.getLayoutData()).horizontalSpan = 2;
        fIsRegExCheckBox.setSelection(fIsRegExInit);
        fIsRegExCheckBox.addSelectionListener(new SelectionAdapter() {
            /*
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                boolean newState = fIsRegExCheckBox.getSelection();
                fIncrementalCheckBox.setEnabled(!newState);
                updateButtonState();
                storeSettings();
                setContentAssistsEnablement(newState);
            }
        });

        fWholeWordCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());
        fWholeWordCheckBox.addSelectionListener(new SelectionAdapter() {
            /*
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                updateButtonState();
            }
        });
        fIncrementalCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());
        return panel;
    }

    /**
     * Creates the status and close section of the dialog.
     * 
     * @param parent
     *            the parent composite
     * @return the status and close button
     */
    private Composite createStatusAndCloseButton(Composite parent) {

        Composite panel = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        panel.setLayout(layout);

        fStatusLabel = new Label(panel, SWT.LEFT);
        setGridData(fStatusLabel, SWT.FILL, true, SWT.CENTER, false);

        String label = EditorMessages.FindReplace_CloseButton_label;
        Button closeButton = createButton(panel, 101, label, false);
        setGridData(closeButton, SWT.RIGHT, false, SWT.BOTTOM, false);

        return panel;
    }

    /*
     * @see Dialog#buttonPressed
     */
    protected void buttonPressed(int buttonID) {
        if (buttonID == 101)
            close();
    }

    // ------- action invocation ---------------------------------------

    /**
     * Returns the position of the specified search string, or <code>-1</code> if the string can not be found when
     * searching using the given options.
     * 
     * @param findString
     *            the string to search for
     * @param startPosition
     *            the position at which to start the search
     * @param forwardSearch
     *            the direction of the search
     * @param caseSensitive
     *            should the search be case sensitive
     * @param wrapSearch
     *            should the search wrap to the start/end if arrived at the end/start
     * @param wholeWord
     *            does the search string represent a complete word
     * @param regExSearch
     *            if <code>true</code> findString represents a regular expression
     * @return the occurrence of the find string following the options or <code>-1</code> if nothing found
     * @since 3.0
     */
    private ViewerCell findIndex(String findString, ViewerCell startPosition, boolean forwardSearch,
            boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean regExSearch) {

        if (forwardSearch) {
            if (wrapSearch) {
                ViewerCell index = findAndSelect(startPosition, findString, true, caseSensitive, wholeWord,
                        regExSearch, wrapSearch);
                if (index == null) {
                    if (okToUse(getShell()) && !isIncrementalSearch())
                        getShell().getDisplay().beep();

                    index = findAndSelect(fTarget.getFirstCell(startPosition, ViewerRow.ABOVE), findString, true,
                            caseSensitive, wholeWord, regExSearch, wrapSearch);
                }
                return index;
            }
            return findAndSelect(startPosition, findString, true, caseSensitive, wholeWord, regExSearch, wrapSearch);
        }

        // backward
        if (wrapSearch) {
            ViewerCell index = findAndSelect(startPosition, findString, false, caseSensitive, wholeWord, regExSearch,
                    wrapSearch);
            if (index == null) {
                if (okToUse(getShell()) && !isIncrementalSearch())
                    getShell().getDisplay().beep();
                index = findAndSelect(fTarget.getFirstCell(startPosition, ViewerRow.BELOW), findString, false,
                        caseSensitive, wholeWord, regExSearch, wrapSearch);
            }
            return index;
        }
        return findAndSelect(startPosition, findString, false, caseSensitive, wholeWord, regExSearch, wrapSearch);
    }

    /**
     * Searches for a string starting at the given offset and using the specified search directives. If a string has
     * been found it is selected and its start offset is returned.
     * 
     * @param offset
     *            the offset at which searching starts
     * @param findString
     *            the string which should be found
     * @param forwardSearch
     *            the direction of the search
     * @param caseSensitive
     *            <code>true</code> performs a case sensitive search, <code>false</code> an insensitive search
     * @param wholeWord
     *            if <code>true</code> only occurrences are reported in which the findString stands as a word by itself
     * @param regExSearch
     *            if <code>true</code> findString represents a regular expression
     * @return the position of the specified string, or -1 if the string has not been found
     * @since 3.0
     */
    private ViewerCell findAndSelect(ViewerCell offset, String findString, boolean forwardSearch,
            boolean caseSensitive, boolean wholeWord, boolean regExSearch, boolean wrapSearch) {
        if (fTarget != null)
            return fTarget.findAndSelect(offset, findString, forwardSearch, caseSensitive, wholeWord, wrapSearch,
                    regExSearch);
        return null;
    }

    /**
     * Replaces the selection with <code>replaceString</code>. If <code>regExReplace</code> is <code>true</code>,
     * <code>replaceString</code> is a regex replace pattern which will get expanded if the underlying target supports
     * it. Returns the region of the inserted text; note that the returned selection covers the expanded pattern in case
     * of regex replace.
     * 
     * @param replaceString
     *            the replace string (or a regex pattern)
     * @param regExReplace
     *            <code>true</code> if <code>replaceString</code> is a pattern
     * @return the selection after replacing, i.e. the inserted text
     * @since 3.0
     */
    ViewerCell replaceSelection(String replaceString, boolean regExReplace) {
        return fTarget.getSelection(index);
    }

    /**
     * Returns whether the specified search string can be found using the given options.
     * 
     * @param findString
     *            the string to search for
     * @param forwardSearch
     *            the direction of the search
     * @param caseSensitive
     *            should the search be case sensitive
     * @param wrapSearch
     *            should the search wrap to the start/end if arrived at the end/start
     * @param wholeWord
     *            does the search string represent a complete word
     * @param incremental
     *            is this an incremental search
     * @param regExSearch
     *            if <code>true</code> findString represents a regular expression
     * @return <code>true</code> if the search string can be found using the given options
     * 
     * @since 3.0
     */
    private boolean findNext(String findString, boolean forwardSearch, boolean caseSensitive, boolean wrapSearch,
            boolean wholeWord, boolean incremental, boolean regExSearch) {
        if (fTarget == null)
            return false;

        ViewerCell r = null;

        if (incremental)
            r = fIncrementalBaseLocation;
        else {
            r = fTarget.getSelection(index);
        }

        // restores the original value
        if (findReplacePosition != null) {
            AbstractSTViewer target = (AbstractSTViewer) fTarget;
            if (target.getAllFields()[findReplacePosition.getColumnIndex()].getSpecialDrawer(findReplacePosition
                    .getElement()) != null) {
                findReplacePosition.setBackground(target.getAllFields()[findReplacePosition.getColumnIndex()]
                        .getBackground(findReplacePosition.getElement()));
            } else
                fTarget.getViewer().refresh(findReplacePosition.getElement());
        }

        findReplacePosition = r;

        findReplacePosition = findIndex(findString, findReplacePosition, forwardSearch, caseSensitive, wrapSearch,
                wholeWord, regExSearch);

        if (findReplacePosition != null) {
            findReplacePosition.setBackground(findReplacePosition.getControl().getShell().getDisplay()
                    .getSystemColor(SWT.COLOR_GREEN));

            calcolateIndex(forwardSearch);

            fNeedsInitialFindBeforeReplace = false;

            return true;
        }

        return false;
    }

    private void calcolateIndex(boolean forwardSearch) {
        if (forwardSearch) {
            if (findReplacePosition.getNeighbor(ViewerCell.RIGHT, true) != null)
                index = findReplacePosition.getNeighbor(ViewerCell.RIGHT, true);
            else {
                ViewerRow row = findReplacePosition.getViewerRow();
                index = row.getNeighbor(ViewerRow.BELOW, true).getCell(0);
            }
        } else {
            if (findReplacePosition.getNeighbor(ViewerCell.LEFT, true) != null)
                index = findReplacePosition.getNeighbor(ViewerCell.LEFT, true);
            else {
                ViewerRow row = findReplacePosition.getViewerRow();
                index = row.getNeighbor(ViewerRow.ABOVE, true).getCell(0);
            }
        }
    }

    /**
     * Returns the dialog's boundaries.
     * 
     * @return the dialog's boundaries
     */
    private Rectangle getDialogBoundaries() {
        if (okToUse(getShell()))
            return getShell().getBounds();
        return fDialogPositionInit;
    }

    /**
     * Returns the dialog's history.
     * 
     * @return the dialog's history
     */
    private List<String> getFindHistory() {
        return fFindHistory;
    }

    // ------- accessors ---------------------------------------

    /**
     * Retrieves the string to search for from the appropriate text input field and returns it.
     * 
     * @return the search string
     */
    private String getFindString() {
        if (okToUse(fFindField)) {
            return fFindField.getText();
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the dialog's replace history.
     * 
     * @return the dialog's replace history
     */
    private List<String> getReplaceHistory() {
        return fReplaceHistory;
    }

    /**
     * Retrieves the replacement string from the appropriate text input field and returns it.
     * 
     * @return the replacement string
     */
    private String getReplaceString() {
        if (okToUse(fReplaceField)) {
            return fReplaceField.getText();
        }
        return ""; //$NON-NLS-1$
    }

    // ------- init / close ---------------------------------------

    /**
     * Returns the actual selection of the find replace target.
     * 
     * @return the selection of the target
     */
    private String getSelectionString() {
        String selection = fTarget.getSelectionText(index);
        if (selection != null && selection.length() > 0) {
            int[] info = TextUtilities.indexOf(TextUtilities.DELIMITERS, selection, 0);
            if (info[0] > 0)
                return selection.substring(0, info[0]);
            else if (info[0] == -1)
                return selection;
        }
        return null;
    }

    /**
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        handleDialogClose();
        return super.close();
    }

    /**
     * Removes focus changed listener from browser and stores settings for re-open.
     */
    private void handleDialogClose() {

        // remove listeners
        if (okToUse(fFindField)) {
            fFindField.removeModifyListener(fFindModifyListener);
        }

        if (fParentShell != null) {
            fParentShell.removeShellListener(fActivationListener);
            fParentShell = null;
        }

        getShell().removeShellListener(fActivationListener);

        // store current settings in case of re-open
        storeSettings();

        // prevent leaks
        fActiveShell = null;
        fTarget = null;

    }

    /**
     * Writes the current selection to the dialog settings.
     * 
     * @since 3.0
     */
    private void writeSelection() {
        if (fTarget == null)
            return;
        String selection = fTarget.getSelectionText(index);
        if (selection == null)
            selection = ""; //$NON-NLS-1$

        IDialogSettings s = getDialogSettings();
        s.put("selection", selection); //$NON-NLS-1$
    }

    /**
     * Stores the current state in the dialog settings.
     * 
     * @since 2.0
     */
    private void storeSettings() {
        fDialogPositionInit = getDialogBoundaries();
        fWrapInit = isWrapSearch();
        fWholeWordInit = isWholeWordSetting();
        fCaseInit = isCaseSensitiveSearch();
        fIsRegExInit = isRegExSearch();
        fIncrementalInit = isIncrementalSearch();
        fForwardInit = isForwardSearch();

        writeConfiguration();
    }

    /**
     * Initializes the string to search for and the appropriate text in the Find field based on the selection found in
     * the action's target.
     */
    private void initFindStringFromSelection() {
        if (fTarget != null && okToUse(fFindField)) {
            String selection = getSelectionString();
            fFindField.removeModifyListener(fFindModifyListener);
            if (selection != null) {
                fFindField.setText(selection);
                if (!selection.equals(fTarget.getSelectionText(index))) {
                    useSelectedLines(true);
                    fGlobalRadioButton.setSelection(false);
                    fSelectedRangeRadioButton.setSelection(true);
                    fUseSelectedLines = true;
                }
            } else {
                if ("".equals(fFindField.getText())) { //$NON-NLS-1$
                    if (fFindHistory.size() > 0)
                        fFindField.setText((String) fFindHistory.get(0));
                    else
                        fFindField.setText(""); //$NON-NLS-1$
                }
            }
            fFindField.setSelection(new Point(0, fFindField.getText().length()));
            fFindField.addModifyListener(fFindModifyListener);
        }
    }

    /**
     * Initializes the anchor used as starting point for incremental searching.
     * 
     * @since 2.0
     */
    private void initIncrementalBaseLocation() {
        if (fTarget != null && isIncrementalSearch() && !isRegExSearchAvailableAndChecked()) {
            fIncrementalBaseLocation = fTarget.getSelection(index);
        } else {
            fIncrementalBaseLocation = null;
        }

    }

    // ------- history ---------------------------------------

    /**
     * Retrieves and returns the option case sensitivity from the appropriate check box.
     * 
     * @return <code>true</code> if case sensitive
     */
    private boolean isCaseSensitiveSearch() {
        if (okToUse(fCaseCheckBox)) {
            return fCaseCheckBox.getSelection();
        }
        return fCaseInit;
    }

    /**
     * Retrieves and returns the regEx option from the appropriate check box.
     * 
     * @return <code>true</code> if case sensitive
     * @since 3.0
     */
    private boolean isRegExSearch() {
        if (okToUse(fIsRegExCheckBox)) {
            return fIsRegExCheckBox.getSelection();
        }
        return fIsRegExInit;
    }

    /**
     * If the target supports regular expressions search retrieves and returns regEx option from appropriate check box.
     * 
     * @return <code>true</code> if regEx is available and checked
     * @since 3.0
     */
    private boolean isRegExSearchAvailableAndChecked() {
        if (okToUse(fIsRegExCheckBox)) {
            return fIsTargetSupportingRegEx && fIsRegExCheckBox.getSelection();
        }
        return fIsRegExInit;
    }

    /**
     * Retrieves and returns the option search direction from the appropriate check box.
     * 
     * @return <code>true</code> if searching forward
     */
    private boolean isForwardSearch() {
        if (okToUse(fForwardRadioButton)) {
            return fForwardRadioButton.getSelection();
        }
        return fForwardInit;
    }

    /**
     * Retrieves and returns the option search whole words from the appropriate check box.
     * 
     * @return <code>true</code> if searching for whole words
     */
    private boolean isWholeWordSetting() {
        if (okToUse(fWholeWordCheckBox)) {
            return fWholeWordCheckBox.getSelection();
        }
        return fWholeWordInit;
    }

    /**
     * Returns <code>true</code> if searching should be restricted to entire words, <code>false</code> if not. This is
     * the case if the respective checkbox is turned on, regex is off, and the checkbox is enabled, i.e. the current
     * find string is an entire word.
     * 
     * @return <code>true</code> if the search is restricted to whole words
     */
    private boolean isWholeWordSearch() {
        return isWholeWordSetting() && !isRegExSearchAvailableAndChecked()
                && (okToUse(fWholeWordCheckBox) ? fWholeWordCheckBox.isEnabled() : true);
    }

    /**
     * Retrieves and returns the option wrap search from the appropriate check box.
     * 
     * @return <code>true</code> if wrapping while searching
     */
    private boolean isWrapSearch() {
        if (okToUse(fWrapCheckBox)) {
            return fWrapCheckBox.getSelection();
        }
        return fWrapInit;
    }

    /**
     * Retrieves and returns the option incremental search from the appropriate check box.
     * 
     * @return <code>true</code> if incremental search
     * @since 2.0
     */
    private boolean isIncrementalSearch() {
        if (okToUse(fIncrementalCheckBox)) {
            return fIncrementalCheckBox.getSelection();
        }
        return fIncrementalInit;
    }

    /**
     * Creates a button.
     * 
     * @param parent
     *            the parent control
     * @param label
     *            the button label
     * @param id
     *            the button id
     * @param dfltButton
     *            is this button the default button
     * @param listener
     *            a button pressed listener
     * @return the new button
     */
    private Button makeButton(Composite parent, String label, int id, boolean dfltButton, SelectionListener listener) {
        Button b = createButton(parent, id, label, dfltButton);
        b.addSelectionListener(listener);
        return b;
    }

    /**
     * Returns the status line manager of the active editor or <code>null</code> if there is no such editor.
     * 
     * @return the status line manager of the active editor
     */
    private IEditorStatusLine getStatusLineManager() {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null)
            return null;

        IWorkbenchPage page = window.getActivePage();
        if (page == null)
            return null;

        IEditorPart editor = page.getActiveEditor();
        if (editor == null)
            return null;

        return (IEditorStatusLine) editor.getAdapter(IEditorStatusLine.class);
    }

    /**
     * Sets the given status message in the status line.
     * 
     * @param error
     *            <code>true</code> if it is an error
     * @param message
     *            the error message
     */
    private void statusMessage(boolean error, String message) {
        fStatusLabel.setText(message);

        if (error)
            fStatusLabel.setForeground(JFaceColors.getErrorText(fStatusLabel.getDisplay()));
        else
            fStatusLabel.setForeground(null);

        IEditorStatusLine statusLine = getStatusLineManager();
        if (statusLine != null)
            statusLine.setMessage(error, message, null);

        if (error)
            getShell().getDisplay().beep();
    }

    /**
     * Sets the given error message in the status line.
     * 
     * @param message
     *            the message
     */
    private void statusError(String message) {
        statusMessage(true, message);
    }

    /**
     * Sets the given message in the status line.
     * 
     * @param message
     *            the message
     */
    private void statusMessage(String message) {
        statusMessage(false, message);
    }

    /**
     * Locates the user's findString in the text of the target.
     */
    private void performSearch() {
        performSearch(isIncrementalSearch() && !isRegExSearchAvailableAndChecked());
    }

    /**
     * Locates the user's findString in the text of the target.
     * 
     * @param mustInitIncrementalBaseLocation
     *            <code>true</code> if base location must be initialized
     * @since 3.0
     */
    private void performSearch(boolean mustInitIncrementalBaseLocation) {

        if (mustInitIncrementalBaseLocation)
            initIncrementalBaseLocation();

        String findString = getFindString();

        if (fIncrementalBaseLocation != null) {
            findString = findString + fIncrementalBaseLocation.getText();
        }

        boolean somethingFound = false;

        if (findString != null && findString.length() > 0) {

            try {
                somethingFound = findNext(findString, isForwardSearch(), isCaseSensitiveSearch(), isWrapSearch(),
                        isWholeWordSearch(), isIncrementalSearch() && !isRegExSearchAvailableAndChecked(),
                        isRegExSearchAvailableAndChecked());
                if (somethingFound) {
                    statusMessage(""); //$NON-NLS-1$
                } else {
                    statusMessage(EditorMessages.FindReplace_Status_noMatch_label);
                }
            } catch (PatternSyntaxException ex) {
                statusError(ex.getLocalizedMessage());
            } catch (IllegalStateException ex) {
                // we don't keep state in this dialog
            }
        }
        writeSelection();
        updateButtonState(!somethingFound);
    }

    // ------- UI creation ---------------------------------------

    /**
     * Attaches the given layout specification to the <code>component</code>.
     * 
     * @param component
     *            the component
     * @param horizontalAlignment
     *            horizontal alignment
     * @param grabExcessHorizontalSpace
     *            grab excess horizontal space
     * @param verticalAlignment
     *            vertical alignment
     * @param grabExcessVerticalSpace
     *            grab excess vertical space
     */
    private void setGridData(Control component, int horizontalAlignment, boolean grabExcessHorizontalSpace,
            int verticalAlignment, boolean grabExcessVerticalSpace) {
        GridData gd;
        if (component instanceof Button && (((Button) component).getStyle() & SWT.PUSH) != 0) {
            gd = (GridData) component.getLayoutData();
            gd.horizontalAlignment = GridData.FILL;
        } else {
            gd = new GridData();
            component.setLayoutData(gd);
            gd.horizontalAlignment = horizontalAlignment;
            gd.grabExcessHorizontalSpace = grabExcessHorizontalSpace;
        }
        gd.verticalAlignment = verticalAlignment;
        gd.grabExcessVerticalSpace = grabExcessVerticalSpace;
    }

    /**
     * Adds enough space in the control's layout data margin for the content assist decoration.
     * 
     * @param control
     *            the control that needs a margin
     * @since 3.3
     */
    private void addDecorationMargin(Control control) {
        Object layoutData = control.getLayoutData();
        if (!(layoutData instanceof GridData))
            return;
        GridData gd = (GridData) layoutData;
        FieldDecoration dec = FieldDecorationRegistry.getDefault().getFieldDecoration(
                FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
        gd.horizontalIndent = dec.getImage().getBounds().width;
    }

    /**
     * Updates the enabled state of the buttons.
     */
    private void updateButtonState() {
        updateButtonState(false);
    }

    /**
     * Updates the enabled state of the buttons.
     * 
     * @param disableReplace
     *            <code>true</code> if replace button must be disabled
     * @since 3.0
     */
    private void updateButtonState(boolean disableReplace) {
        if (okToUse(getShell()) && okToUse(fFindNextButton)) {
            boolean enable = fTarget != null && (fActiveShell == fParentShell || fActiveShell == getShell());
            String str = getFindString();
            boolean findString = str != null && str.length() > 0;

            fWholeWordCheckBox.setEnabled(isWord(str) && !isRegExSearchAvailableAndChecked());

            fFindNextButton.setEnabled(enable && findString);
            fReplaceSelectionButton.setEnabled(false);
            fReplaceFindButton.setEnabled(false);
            fReplaceAllButton.setEnabled(false);
        }
    }

    /**
     * Tests whether each character in the given string is a letter.
     * 
     * @param str
     * @return <code>true</code> if the given string is a word
     * @since 3.0
     */
    private boolean isWord(String str) {
        if (str == null || str.length() == 0)
            return false;

        for (int i = 0; i < str.length(); i++) {
            if (!Character.isJavaIdentifierPart(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Updates the given combo with the given content.
     * 
     * @param combo
     *            combo to be updated
     * @param content
     *            to be put into the combo
     */
    private void updateCombo(Combo combo, List<String> content) {
        combo.removeAll();
        for (int i = 0; i < content.size(); i++) {
            combo.add(content.get(i).toString());
        }
    }

    // ------- open / reopen ---------------------------------------

    /**
     * Called after executed find action to update the history.
     */
    private void updateFindHistory() {
        if (okToUse(fFindField)) {
            fFindField.removeModifyListener(fFindModifyListener);
            updateHistory(fFindField, fFindHistory);
            fFindField.addModifyListener(fFindModifyListener);
        }
    }

    /**
     * Updates the combo with the history.
     * 
     * @param combo
     *            to be updated
     * @param history
     *            to be put into the combo
     */
    private void updateHistory(Combo combo, List<String> history) {
        String findString = combo.getText();
        int index = history.indexOf(findString);
        if (index != 0) {
            if (index != -1) {
                history.remove(index);
            }
            history.add(0, findString);
            updateCombo(combo, history);
            combo.setText(findString);
        }
    }

    /**
     * Returns whether the target is editable.
     * 
     * @return <code>true</code> if target is editable
     */
    private boolean isEditable() {
        boolean isEditable = (fTarget == null ? false : fTarget.isEditable());
        return fIsTargetEditable && isEditable;
    }

    /**
     * Updates this dialog because of a different target.
     * 
     * @param target
     *            the new target
     * @param isTargetEditable
     *            <code>true</code> if the new target can be modified
     * @param initializeFindString
     *            <code>true</code> if the find string of this dialog should be initialized based on the viewer's
     *            selection
     * @since 2.0
     */
    public void updateTarget(ISTFindReplaceTarget target, boolean isTargetEditable, boolean initializeFindString) {

        fIsTargetEditable = isTargetEditable;
        fNeedsInitialFindBeforeReplace = true;

        if (target != fTarget) {
            // TO ADAPT with ISTFindReplaceTarget
            /*
             * if (fTarget != null && fTarget instanceof IFindReplaceTargetExtension) ((IFindReplaceTargetExtension)
             * fTarget).endSession();
             */

            fTarget = target;
            if (fTarget != null) {
                fIsTargetSupportingRegEx = true;
                // ((IFindReplaceTargetExtension) fTarget).beginSession();

                fGlobalInit = true;
                fGlobalRadioButton.setSelection(fGlobalInit);
                fSelectedRangeRadioButton.setSelection(!fGlobalInit);
                fUseSelectedLines = !fGlobalInit;
            }
        }

        if (okToUse(fIsRegExCheckBox))
            fIsRegExCheckBox.setEnabled(fIsTargetSupportingRegEx);

        if (okToUse(fWholeWordCheckBox))
            fWholeWordCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());

        /*
         * if (okToUse(fIncrementalCheckBox)) fIncrementalCheckBox.setEnabled(!isRegExSearchAvailableAndChecked());
         */

        if (okToUse(fReplaceLabel)) {
            fReplaceLabel.setEnabled(isEditable());
            fReplaceField.setEnabled(isEditable());
            if (initializeFindString) {
                initFindStringFromSelection();
                fGiveFocusToFindField = true;
            }
            initIncrementalBaseLocation();
            updateButtonState();
        }

        setContentAssistsEnablement(isRegExSearchAvailableAndChecked());
    }

    /**
     * Sets the parent shell of this dialog to be the given shell.
     * 
     * @param shell
     *            the new parent shell
     */
    public void setParentShell(Shell shell) {
        if (shell != fParentShell) {

            if (fParentShell != null)
                fParentShell.removeShellListener(fActivationListener);

            fParentShell = shell;
            fParentShell.addShellListener(fActivationListener);
        }

        fActiveShell = shell;
    }

    // --------------- configuration handling --------------

    /**
     * Returns the dialog settings object used to share state between several find/replace dialogs.
     * 
     * @return the dialog settings to be used
     */
    private IDialogSettings getDialogSettings() {
        IDialogSettings settings = STDataViewersActivator.getDefault().getDialogSettings();
        fDialogSettings = settings.getSection(getClass().getName());
        if (fDialogSettings == null)
            fDialogSettings = settings.addNewSection(getClass().getName());
        return fDialogSettings;
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     * 
     * @since 3.2
     */
    protected IDialogSettings getDialogBoundsSettings() {
        String sectionName = getClass().getName() + "_dialogBounds"; //$NON-NLS-1$
        IDialogSettings settings = STDataViewersActivator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(sectionName);
        if (section == null)
            section = settings.addNewSection(sectionName);
        return section;
    }

    /*
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsStrategy()
     * 
     * @since 3.2
     */
    protected int getDialogBoundsStrategy() {
        return DIALOG_PERSISTLOCATION | DIALOG_PERSISTSIZE;
    }

    /**
     * Initializes itself from the dialog settings with the same state as at the previous invocation.
     */
    private void readConfiguration() {
        IDialogSettings s = getDialogSettings();

        fWrapInit = s.getBoolean("wrap"); //$NON-NLS-1$
        fCaseInit = s.getBoolean("casesensitive"); //$NON-NLS-1$
        fWholeWordInit = s.getBoolean("wholeword"); //$NON-NLS-1$
        fIncrementalInit = s.getBoolean("incremental"); //$NON-NLS-1$
        fIsRegExInit = s.getBoolean("isRegEx"); //$NON-NLS-1$

        String[] findHistory = s.getArray("findhistory"); //$NON-NLS-1$
        if (findHistory != null) {
            List<String> history = getFindHistory();
            history.clear();
            for (int i = 0; i < findHistory.length; i++)
                history.add(findHistory[i]);
        }

        String[] replaceHistory = s.getArray("replacehistory"); //$NON-NLS-1$
        if (replaceHistory != null) {
            List<String> history = getReplaceHistory();
            history.clear();
            for (int i = 0; i < replaceHistory.length; i++)
                history.add(replaceHistory[i]);
        }
    }

    /**
     * Stores its current configuration in the dialog store.
     */
    private void writeConfiguration() {
        IDialogSettings s = getDialogSettings();

        s.put("wrap", fWrapInit); //$NON-NLS-1$
        s.put("casesensitive", fCaseInit); //$NON-NLS-1$
        s.put("wholeword", fWholeWordInit); //$NON-NLS-1$
        s.put("incremental", fIncrementalInit); //$NON-NLS-1$
        s.put("isRegEx", fIsRegExInit); //$NON-NLS-1$

        List<String> history = getFindHistory();
        String findString = getFindString();
        if (findString.length() > 0)
            history.add(0, findString);
        writeHistory(history, s, "findhistory"); //$NON-NLS-1$

        history = getReplaceHistory();
        String replaceString = getReplaceString();
        if (replaceString.length() > 0)
            history.add(0, replaceString);
        writeHistory(history, s, "replacehistory"); //$NON-NLS-1$
    }

    /**
     * Writes the given history into the given dialog store.
     * 
     * @param history
     *            the history
     * @param settings
     *            the dialog settings
     * @param sectionName
     *            the section name
     * @since 3.2
     */
    private void writeHistory(List<String> history, IDialogSettings settings, String sectionName) {
        int itemCount = history.size();
        Set<String> distinctItems = new HashSet<String>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            String item = history.get(i);
            if (distinctItems.contains(item)) {
                history.remove(i--);
                itemCount--;
            } else {
                distinctItems.add(item);
            }
        }

        while (history.size() > 8)
            history.remove(8);

        String[] names = new String[history.size()];
        history.toArray(names);
        settings.put(sectionName, names);

    }
}
