package org.eclipse.linuxtools.internal.tmf.ui.dialogs;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.linuxtools.internal.tmf.ui.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A simple input dialog for soliciting a bookmark description from the user.
 *
 * Overrides InputDialog to support multiple line description.
 *
 * @author Patrick Tasse
 *
 */
public class AddBookmarkDialog extends InputDialog {

    /* flag to indicate if CR can be used to submit the dialog */
    private boolean submitOnCR = true;

    /**
     * Default constructor
     *
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level shell
     * @param initialValue
     *            the initial input value, or <code>null</code> if none (equivalent to the empty string)
     */
    public AddBookmarkDialog(final Shell parentShell, final String initialValue) {
        super(parentShell, Messages.AddBookmarkDialog_Title, Messages.AddBookmarkDialog_Message, initialValue, null);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.InputDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Control control = super.createDialogArea(parent);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.widthHint = convertHorizontalDLUsToPixels(250);
        gridData.heightHint = convertHeightInCharsToPixels(3);
        final Text text = getText();
        text.setLayoutData(gridData);
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.character == SWT.CR) {
                    if (submitOnCR) {
                        /* submit the dialog */
                        e.doit = false;
                        okPressed();
                        return;
                    }
                } else if (e.character == SWT.TAB) {
                    /* don't insert a tab character in the text */
                    e.doit = false;
                    text.traverse(SWT.TRAVERSE_TAB_NEXT);
                }
                /* don't allow CR to submit anymore */
                submitOnCR = false;
            }
        });
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                /* don't allow CR to submit anymore */
                submitOnCR = false;
            }
        });
        return control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.InputDialog#getInputTextStyle()
     */
    @Override
    protected int getInputTextStyle() {
        return SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.InputDialog#validateInput()
     */
    @Override
    protected void validateInput() {
        String newText = getText().getText();
        String errorMessage = (newText == null || newText.trim().length() == 0) ? " " : null;  //$NON-NLS-1$
        /* validation is ok if the error message is set to null */
        setErrorMessage(errorMessage);
    }

}
