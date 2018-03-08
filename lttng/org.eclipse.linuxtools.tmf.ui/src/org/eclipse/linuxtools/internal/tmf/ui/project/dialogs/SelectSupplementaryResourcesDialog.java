/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Copied and adapted from NewFolderDialog
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.dialogs;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * <b><u>SelectSupplementaryResourcesDialog</u></b>
 * <p>
 */
public class SelectSupplementaryResourcesDialog extends Dialog {

    // ------------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------------
    private CheckboxTreeViewer fTreeViewer;
    private final IResource[] fAvailableResources;
    private IResource[] fReturndResources;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public SelectSupplementaryResourcesDialog(Shell shell, IResource[] resources) {
        super(shell);
        fAvailableResources = Arrays.copyOf(resources, resources.length);
        setShellStyle(SWT.RESIZE);
    }

    // ------------------------------------------------------------------------
    // Getters/Setters
    // ------------------------------------------------------------------------
    
    public IResource[] getResources() {
        return Arrays.copyOf(fReturndResources, fReturndResources.length);
    }
    
    // ------------------------------------------------------------------------
    // Dialog
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.SelectSpplementaryResources_DialogTitle);
        newShell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ETOOL_DELETE));
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group contextGroup = new Group(composite, SWT.SHADOW_NONE);
        contextGroup.setText(Messages.SelectSpplementaryResources_ResourcesGroupTitle);
        contextGroup.setLayout(new GridLayout());
        contextGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        fTreeViewer = new CheckboxTreeViewer(contextGroup, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData data = new GridData(GridData.FILL_BOTH);
        Tree tree = fTreeViewer.getTree();
        tree.setLayoutData(data);
        fTreeViewer.setContentProvider(new ITreeContentProvider() {

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public boolean hasChildren(Object element) {
                if (element instanceof IResource[]) {
                    return true;
                }
                return false;
            }

            @Override
            public Object getParent(Object element) {
                return null;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return getChildren(inputElement);
            }

            @Override
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof IResource[]) {
                    return (Object[]) parentElement;
                }
                return null;            
            }
        });

//        fTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
        
      fTreeViewer.setLabelProvider(new LabelProvider() {
          @Override
          public String getText(Object element) {
              if (element instanceof IResource) {
                  IResource resource = (IResource) element;
                  // show also trace name 
                  return resource.getParent().getName() + File.separator + resource.getName();
              }
              return super.getText(element);
          } 
      });
        fTreeViewer.setInput(fAvailableResources);

        getShell().setMinimumSize(new Point(300, 150));

        return composite;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "&Cancel", true); //$NON-NLS-1$
        createButton(parent, IDialogConstants.OK_ID, "&Ok", true); //$NON-NLS-1$
    }

    @Override
    protected void okPressed() {
        Object[] checked = fTreeViewer.getCheckedElements();

        fReturndResources = new IResource[checked.length];
        for (int i = 0; i < checked.length; i++) {
            fReturndResources[i] = (IResource) checked[i];
        }
        super.okPressed();
    }


}
