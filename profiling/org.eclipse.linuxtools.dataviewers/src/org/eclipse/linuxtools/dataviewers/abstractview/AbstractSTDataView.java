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

package org.eclipse.linuxtools.dataviewers.abstractview;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTTreeViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField;
import org.eclipse.linuxtools.dataviewers.actions.STCollapseAllTreeAction;
import org.eclipse.linuxtools.dataviewers.actions.STCollapseSelectionAction;
import org.eclipse.linuxtools.dataviewers.actions.STCopyAction;
import org.eclipse.linuxtools.dataviewers.actions.STDataViewersSortAction;
import org.eclipse.linuxtools.dataviewers.actions.STExpandAllTreeAction;
import org.eclipse.linuxtools.dataviewers.actions.STExpandSelectionAction;
import org.eclipse.linuxtools.dataviewers.actions.STExportToCSVAction;
import org.eclipse.linuxtools.dataviewers.actions.STHideShowColAction;
import org.eclipse.linuxtools.dataviewers.findreplace.ISTFindReplaceTarget;
import org.eclipse.linuxtools.dataviewers.findreplace.STFindReplaceAction;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.preferences.ViewPreferencesAction;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;



/**
 * The AbstractSTDataView is a view that generically implements data result views. 
 */
public abstract class AbstractSTDataView extends ViewPart {

	private AbstractSTViewer stViewer = null;

	private IAction sortAction = null;

	private IAction preferencesAction = null;
	
	private IAction hideShowColAction = null;
	
	private IAction expandAllAction = null;

	private IAction collapseAllAction = null;
	
	private IAction expandSelectionAction = null;

	private IAction collapseSelectionAction = null;
	
	private IAction exportToCSVAction = null;
	
	private IAction copyToAction = null;
	
	private ISTFindReplaceTarget fTarget = null;
	
	/**
	 * Create optional arbitrary Controls, on top of viewer.
	 * Does nothing by default.
	 * Can be used to display a title, or some other informations.
	 * @param parent the parent composite, with a gridlayout (1 column)
	 */
	protected void createTitle(Composite parent) {
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		GridLayout gridLayout = new GridLayout(1,true);
		parent.setLayout(gridLayout);
		createTitle(parent);
		stViewer = createAbstractSTViewer(parent);
			
		// create the actions before the input is set on the viewer but after
		// the sorter and filter are set so the actions will be enabled correctly.
		createActions();

		final MenuManager mgr = initContextMenu();
		final Menu menu = mgr.createContextMenu(parent);
		stViewer.getViewer().getControl().setMenu(menu);
	
		getSite().registerContextMenu(mgr, getSelectionProvider());

		//the selection provider registered
		getSite().setSelectionProvider(getSelectionProvider());

		IActionBars actionBars = getViewSite().getActionBars();

		initMenu(actionBars.getMenuManager());
		initToolBar(actionBars.getToolBarManager());
		
		registerGlobalActions(getViewSite().getActionBars());
	}
	/**
	 * Creates the actions for the receiver.
	 */
	protected void createActions() {
		// menu bar
		expandAllAction = createExpandAllAction();
		collapseAllAction = createCollapseAllAction();
		
		sortAction = createSortAction();

		hideShowColAction = createHideShowColumnAction();
	
		exportToCSVAction = createExportToCSVAction();
		
		// context menu (right-click)
		expandSelectionAction = createExpandSelectionAction();
		collapseSelectionAction = createCollapseSelectionAction();
		copyToAction = createCopyToAction();
	}
	
	/**
	 * Creates the copy actions.
	 * @return IAction
	 */
	protected IAction createCopyToAction() {
		return new STCopyAction(getSTViewer());
	}
	
	/**
	 * Creates the export To CSV actions.
	 * @return IAction
	 */
	protected IAction createExportToCSVAction() {
		return new STExportToCSVAction(getSTViewer());
	}

	/**
	 * Creates the collapse selection actions.
	 * @return IAction
	 */
	protected IAction createCollapseSelectionAction() {
		if (getSTViewer() instanceof AbstractSTTreeViewer) {
			AbstractSTTreeViewer stTreeViewer =
				(AbstractSTTreeViewer)getSTViewer();
			return new STCollapseSelectionAction(stTreeViewer);
		}
		return null;
	}

	/**
	 * Creates the expand selection actions.
	 * @return IAction
	 */
	protected IAction createExpandSelectionAction() {
		if (getSTViewer() instanceof AbstractSTTreeViewer) {
			AbstractSTTreeViewer stTreeViewer =
				(AbstractSTTreeViewer)getSTViewer();
			return new STExpandSelectionAction(stTreeViewer);
		}
		return null;
	}

	/**
	 * Creates the Hide and/or Show action.
	 * @return IAction
	 */
	protected IAction createHideShowColumnAction() {
		return new STHideShowColAction(getSTViewer());
	}

	/**
	 * Creates the sort action.
	 * @return IAction
	 */
	protected IAction createSortAction() {
		return new STDataViewersSortAction(getSTViewer());
	}

	/**
	 * Creates the collapse all action.
	 * @return IAction
	 */
	protected IAction createCollapseAllAction() {
		if (getSTViewer() instanceof AbstractSTTreeViewer) {
			AbstractSTTreeViewer stTreeViewer =
				(AbstractSTTreeViewer)getSTViewer();
			return new STCollapseAllTreeAction(stTreeViewer);
		}
		return null;
	}

	/**
	 * Creates the expand all action.
	 * @return IAction
	 */
	protected IAction createExpandAllAction() {
		if (getSTViewer() instanceof AbstractSTTreeViewer) {
			AbstractSTTreeViewer stTreeViewer =
				(AbstractSTTreeViewer)getSTViewer();
			return new STExpandAllTreeAction(stTreeViewer);
		}
		return null;
	}

	/**
	 * Init the context menu
	 * <p>
	 * If you intend to add an action in the context menu
	 * you may override the <code>fillContextMenu</code> method.
	 * </p>
	 * 
	 * @see #fillContextMenu(IMenuManager manager)
	 */
	protected MenuManager initContextMenu() {
		MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				getSTViewer().getViewer().cancelEditing();
				fillContextMenu(mgr);
			}
		});
		return mgr;
	}

	/**
	 * Init the toolbar for the receiver
	 * <p>
	 * You can override this method if you want - but remember to call <code>super.initToolBar()</code>.
	 * </p>
	 * @param manager the tool bar manager of this view
	 */
	protected void initToolBar(IToolBarManager manager) {
		if (expandAllAction != null)   manager.add(expandAllAction);
		if (collapseAllAction != null) manager.add(collapseAllAction);
		if (hideShowColAction != null) manager.add(hideShowColAction);
		if (exportToCSVAction != null) manager.add(exportToCSVAction);
		if (sortAction != null) manager.add(sortAction);
		contributeToToolbar(manager);
		manager.update(true);
	}

	/**
	 * Init the menu for the receiver.
	 * 
	 * @param menu
	 */
	protected void initMenu(IMenuManager menu) {
		if (sortAction != null) {
			menu.add(sortAction);
		}
		if (hideShowColAction != null) {
			menu.add(hideShowColAction);
		}
		if (preferencesAction != null) {
			menu.add(preferencesAction);
		}
		if (exportToCSVAction != null){
			menu.add(exportToCSVAction);
		}
		
		contributeToDropDownMenu(menu);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		Viewer viewer = getSTViewer().getViewer();
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getControl().setFocus();
		}
	}
	
	/**
	 * Return the viewer.
	 * 
	 * @return AbstractSTViewer
	 */
	public AbstractSTViewer getSTViewer() {
		return stViewer;
	}

	/**
	 * Return the tree for the receiver.
	 * 
	 * @return Control
	 */
	public Control getControl() {
		return stViewer.getViewer().getControl();
	}
	
	/**
	 * Shortcut for getViewer().setInput(input)
	 * See {@link TreeViewer#setInput(Object)}
	 * @param input
	 */
	public void setInput(Object input) {
		stViewer.getViewer().setInput(input);
	}

	/**
	 * Get the IWorkbenchSiteProgressService for the receiver.
	 * 
	 * @return IWorkbenchSiteProgressService or <code>null</code>.
	 */
	protected IWorkbenchSiteProgressService getProgressService() {
		IWorkbenchSiteProgressService service = null;
		Object siteService = getSite().getAdapter(
				IWorkbenchSiteProgressService.class);
		if (siteService != null) {
			service = (IWorkbenchSiteProgressService) siteService;
		}
		return service;
	}

	/**
	 * Return the preferences action.
	 * 
	 * @return IAction
	 */
	protected IAction getPreferencesAction() {
		return preferencesAction;
	}

	/**
	 * Set the preferences action.
	 * 
	 * @param preferencesAction
	 */
	protected void setPreferencesAction(ViewPreferencesAction preferencesAction) {
		this.preferencesAction = preferencesAction;
	}

	/**
	 * Gets the selection provider.
	 * 
	 * @return the selection provider
	 */
	protected ISelectionProvider getSelectionProvider() {
		return stViewer.getViewer();
	}
	
	/**
	 * Return the input of the viewer.
	 * 
	 * @return Object
	 */
	public Object getViewerInput() {
		return stViewer.getViewer().getInput();
	}

	/**
	 * Set the selection of the receiver.
	 * 
	 * @param selection
	 */
	protected void setSelection(IStructuredSelection selection) {
		getSelectionProvider().setSelection(selection);
	}
	
	
	/**
	 * Fills the context menu
	 * (mouse right-click)
	 * <p>
	 * Subclasses may extend it. don't forget to call
	 * <code>super.fillContextMenu(...)</code>
	 * </p>
	 * 
	 * @param manager
	 */
	protected void fillContextMenu(IMenuManager manager) {
		if (getControl() != null
				&& getControl() instanceof Tree) {
			Tree tree = (Tree)getControl();
			TreeItem[] selection = tree.getSelection();
			if (selection != null
				&& selection.length > 0) {
				if (collapseSelectionAction != null) manager.add(collapseSelectionAction);
				if (expandSelectionAction != null)   manager.add(expandSelectionAction);
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
				if (copyToAction != null) manager.add(copyToAction);
			}
		}
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * Add actions to the drop down menu.
	 * (top-right corner drop down menu)
	 * <p>
	 * Subclasses may override it.
	 * </p>
	 * 
	 * @param menu
	 */
	protected void contributeToDropDownMenu(IMenuManager menu) {
		//nothing, intended to be overridden
	}
	
	/**
	 * Add actions to the toolbar
	 * (top-right toolbar)
	 * <p>
	 * Subclasses may override it.
	 * </p>
	 * 
	 * @param manager
	 */
	protected void contributeToToolbar(IToolBarManager manager){
		//nothing, intended to be overridden
	}
	
	/**
	 * Register contributions to the global actions bar.
	 * (global actions toolbar)
	 * <p>
	 * Subclasses may override it.
	 * </p>
	 * 
	 * @param actionBars
	 */
	protected void registerGlobalActions(IActionBars actionBars) {
		fTarget = createSTFindReplaceTarget();
		if (fTarget == null) return;
		STFindReplaceAction action= new STFindReplaceAction(getSite().getShell(),fTarget); //$NON-NLS-1$
		actionBars.setGlobalActionHandler(ITextEditorActionConstants.FIND, action);
	}
	
	/**
	 * Creates an instance of ISTFindReplaceTarget. It has to return an object not null
	 * when the Find action is enabled 
	 * @return ISTFindReplaceTarget
	 */
	public ISTFindReplaceTarget createSTFindReplaceTarget(){
		return null;
	}
	
	
	
	/**
	 * Gets the fields that are able to be sorted
	 * <p>
	 * By default it returns 
	 * <code>getAllFields()</code>. If you want to 
	 * change that behavior you'll have to override
	 * this method.
	 * </p>
	 * 
	 * <p>
	 * Subclasses may override it.
	 * </p>
	 * @return the fields that are able to be sorted
	 */
	protected ISTDataViewersField[] getSortingFields() {
		return null;
	}

	/**
	 * Creates a wrapper handling a TreeTable or Table Viewer
	 * 
	 * <p>
	 * Subclasses may override it.
	 * </p>
	 * 
	 * @param parent
	 * @return an AbstractSTViewer
	 */
	abstract protected AbstractSTViewer createAbstractSTViewer(Composite parent);
	
	public void dispose(){
		super.dispose();
	}
}
