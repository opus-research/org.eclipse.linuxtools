/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph;

import java.text.MessageFormat;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.systemtap.graphingapi.ui.Localization;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * @since 2.1
 */
public class SelectGraphAndSeriesWizardPage extends WizardPage implements Listener {
	public SelectGraphAndSeriesWizardPage() {
		super("selectGraphAndSeries"); //$NON-NLS-1$
		setTitle(Localization.getString("SelectGraphAndSeriesWizardPage.SelectGraphAndSeries")); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		wizard = (SelectGraphAndSeriesWizard)getWizard();
		model = wizard.model;
		edit = wizard.isEditing();

		//Set the layout data
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group cmpGraphOptsGraph = new Group(comp, SWT.SHADOW_ETCHED_IN);
		cmpGraphOptsGraph.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		RowLayout rowLayout = new RowLayout();
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.spacing = 10;
		cmpGraphOptsGraph.setLayout(rowLayout);
		cmpGraphOptsGraph.setText(Localization.getString("SelectGraphAndSeriesWizardPage.Graph")); //$NON-NLS-1$

		String[] graphIDs = GraphFactory.getAvailableGraphs(wizard.model.getDataSet());
		btnGraphs = new Button[graphIDs.length];
		for(int i=0; i<btnGraphs.length; i++) {
			btnGraphs[i] = new Button(cmpGraphOptsGraph, SWT.RADIO);
			btnGraphs[i].setImage(GraphFactory.getGraphImage(graphIDs[i]));
			btnGraphs[i].addListener(SWT.Selection, this);
			btnGraphs[i].setData(graphIDs[i]);
			btnGraphs[i].setToolTipText(GraphFactory.getGraphName(btnGraphs[i].getData().toString()) + "\n\n" + //$NON-NLS-1$
					GraphFactory.getGraphDescription(btnGraphs[i].getData().toString()));
			if (wizard.isEditing() && graphIDs[i].equals(wizard.model.getGraphID())) {
				btnGraphs[i].setSelection(true);
			}
		}

		Group cmpGraphOptsSeries = new Group(comp, SWT.SHADOW_ETCHED_IN);
		cmpGraphOptsSeries.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		cmpGraphOptsSeries.setLayout(layout);

		Label lblTitle = new Label(cmpGraphOptsSeries, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		lblTitle.setText(Localization.getString("SelectGraphAndSeriesWizardPage.Title")); //$NON-NLS-1$
		txtTitle = new Text(cmpGraphOptsSeries, SWT.BORDER);
		txtTitle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		if (edit) {
			txtTitle.setText(model.getGraphData().title);
		}
		txtTitle.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				getWizard().getContainer().updateButtons();
				checkErrors();
			}
		});

		//Add the data series widgets
		String[] labels = model.getSeries();

		cboYItems = new Combo[!edit ? labels.length : Math.max(labels.length, model.getYSeries().length)];
		lblYItems = new Label[cboYItems.length];
		deleted = new boolean[cboYItems.length + 1];

		Label lblXItem = new Label(cmpGraphOptsSeries, SWT.NONE);
		lblXItem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		lblXItem.setText(Localization.getString("SelectGraphAndSeriesWizardPage.XSeries")); //$NON-NLS-1$
		cboXItem = new Combo(cmpGraphOptsSeries, SWT.DROP_DOWN|SWT.READ_ONLY);
		cboXItem.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		cboXItem.addSelectionListener(new ComboSelectionListener());
		cboXItem.add(Localization.getString("SelectGraphAndSeriesWizardPage.RowID")); //$NON-NLS-1$

		for(int i=0; i<cboYItems.length; i++) {
			lblYItems[i] = new Label(cmpGraphOptsSeries, SWT.NONE);
			lblYItems[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
			lblYItems[i].setText(MessageFormat.format(Localization.getString("SelectGraphAndSeriesWizardPage.YSeries"), new Integer(i))); //$NON-NLS-1$
			cboYItems[i] = new Combo(cmpGraphOptsSeries, SWT.DROP_DOWN|SWT.READ_ONLY);
			cboYItems[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			cboYItems[i].addSelectionListener(new ComboSelectionListener());

			if(i>0) {
				cboYItems[i].add(Localization.getString("SelectGraphAndSeriesWizardPage.NA")); //$NON-NLS-1$
				cboYItems[i].setVisible(false);
				lblYItems[i].setVisible(false);
			}
		}

		for(int j,i=0; i<labels.length; i++) {
			cboXItem.add(labels[i]);
			for(j=0; j<lblYItems.length; j++)
				cboYItems[j].add(labels[i]);
		}

		int selected;
		boolean cvisible = edit;
		if (!edit) {
			cboXItem.select(0);
		} else {
			selected = model.getXSeries();
			if (selected < labels.length){
				cboXItem.select(selected + 1);
			} else {
				cboXItem.add(Localization.getString("SelectGraphAndSeriesWizardPage.Deleted"), 0); //$NON-NLS-1$
				cboXItem.select(0);
				deleted[0] = true;
			}
			selected = model.getYSeries()[0];
			if (selected < labels.length) {
				cboYItems[0].select(selected);
			} else {
				cboYItems[0].add(Localization.getString("SelectGraphAndSeriesWizardPage.Deleted"), 0); //$NON-NLS-1$
				cboYItems[0].select(0);
				deleted[1] = true;
			}
		}
		for(int i=1; i<cboYItems.length; i++) {
			if (!edit || model.getYSeries().length <= i) {
				cboYItems[i].select(selected = 0);
			} else {
				selected = model.getYSeries()[i];
				if (selected < labels.length){
					cboYItems[i].select(selected + 1);
				} else {
					cboYItems[i].add(Localization.getString("SelectGraphAndSeriesWizardPage.Deleted"), 0); //$NON-NLS-1$
					cboYItems[i].select(0);
					deleted[i+1] = true;
				}
			}
			cboYItems[i].setVisible(cvisible);
			lblYItems[i].setVisible(cvisible);
			cvisible = (selected > 0);
		}

		//Select one of the graph types by default, rather than blank choice
		if (!edit) {
			btnGraphs[0].setSelection(true);
			saveDataToModelGraph(graphIDs[0]);
		}
		else {
			saveDataToModelGraph(wizard.model.getGraphID());
		}

		setControl(comp);
		checkErrors();
	}

	@Override
	public void handleEvent(Event event) {
		if(event.widget instanceof Button) {
			saveDataToModelGraph(((Button)event.widget).getData().toString());
			wizard.getContainer().updateButtons();
		}
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public boolean isPageComplete() {
		return saveDataToModelSeries();
	}

	/**
	 * Saves the choice of graph type to the model.
	 * @param selected The ID of the selected graph.
	 */
	private void saveDataToModelGraph(String selected) {
		model.setGraph(selected);
	}

	/**
	 * Saves all information pertaining to series data & naming to the model.
	 * @return True if there are no conflicts in series data selection; false otherwise.
	 * In the case of the latter, no data is saved.
	 */
	private boolean saveDataToModelSeries() {
		if(isSeriesUnique()) {
			model.setTitle(txtTitle.getText());

			if(null != txtKey && txtKey.isEnabled())
				model.setKey(txtKey.getText());
			else
				model.setKey(null);

			model.setXSeries(cboXItem.getSelectionIndex()-1);

			int i, count;
			for(i=1, count=1; i<cboYItems.length && 0 != cboYItems[i].getSelectionIndex(); i++) {
				count++;
			}

			int[] ySeries = new int[count];
			ySeries[0] = cboYItems[0].getSelectionIndex();
			for(i=1; i<count; i++) {
				ySeries[i] = cboYItems[i].getSelectionIndex()-1;
			}
			model.setYSeries(ySeries);
			return true;
		}
		return false;
	}

	private void markAsDuplicate(Combo item, Boolean bad) {
		item.setForeground(item.getDisplay().getSystemColor(bad ? SWT.COLOR_RED : SWT.COLOR_BLACK));
	}

	private void findDuplicates() {
		// Undo duplicate marking, as it is to be updated.
		markAsDuplicate(cboXItem, false);
		for (int i = 0; i < cboYItems.length; i++) {
			markAsDuplicate(cboYItems[i], false);
		}

		for(int j,i=0; i<cboYItems.length; i++) {
			if(cboYItems[i].isVisible() && !deleted[i+1]) {
				for(j=i+1; j<cboYItems.length; j++) {
					try {
						if(!deleted[j+1] && cboYItems[j].isVisible() && cboYItems[i].getItem(cboYItems[i].getSelectionIndex())
								.equals(cboYItems[j].getItem(cboYItems[j].getSelectionIndex()))) {
								markAsDuplicate(cboYItems[i], true);
								markAsDuplicate(cboYItems[j], true);
						}
					} catch (Exception e) {
						// If a cboYItem has no item selected, don't mark any duplicates. Ignore.
					}
				}
				try {
					if(!deleted[0] && cboYItems[i].getItem(cboYItems[i].getSelectionIndex()).equals(cboXItem.getItem(cboXItem.getSelectionIndex()))) {
						markAsDuplicate(cboYItems[i], true);
						markAsDuplicate(cboXItem, true);
					}
				} catch (Exception e) {
					// Ignore for same reason as above.
				}
			}
		}
	}

	/**
	 * Checks for conflicts in data selection. (An example of a conflict
	 * is two Y-series fields set to the same output value.)
	 * @return True if there is no conflict, false otherwise.
	 */
	private boolean isSeriesUnique() {
		findDuplicates();
		if("".equals(txtTitle.getText().trim())) { //$NON-NLS-1$
			return false;
		}
		if(null != txtKey && txtKey.isEnabled() && txtKey.getText().length() <= 0) {
			return false;
		}

		for (int i = 0; i < deleted.length; i++) {
			if (deleted[i]) {
				return false;
			}
		}

		for(int j,i=0; i<cboYItems.length; i++) {
			if(cboYItems[i].isVisible()) {
				for(j=i+1; j<cboYItems.length; j++) {
					if(cboYItems[j].isVisible()) {
						try {
							if(cboYItems[i].getItem(cboYItems[i].getSelectionIndex())
									.equals(cboYItems[j].getItem(cboYItems[j].getSelectionIndex()))) {
								return false;
							}
						} catch (Exception e) {
							// Exception thrown if getting the selection of a cboItem failed.
							// This happens when nothing is selected, which is an invalid state.
							return false;
						}
					}
				}
				try {
					if(cboYItems[i].getItem(cboYItems[i].getSelectionIndex())
							.equals(cboXItem.getItem(cboXItem.getSelectionIndex()))) {
						return false;
					}
				} catch (Exception e) {
					// Exception thrown for same reason as above.
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void dispose() {
		super.dispose();
		if(null != btnGraphs)
			for(int i=0; i<btnGraphs.length; i++)
				btnGraphs[i].dispose();
		btnGraphs = null;

		if(null != txtTitle)
			txtTitle.dispose();
		txtTitle = null;

		if(null != txtKey)
			txtKey.dispose();
		txtKey = null;
		if(null != btnKey)
			btnKey.dispose();
		btnKey = null;
		if(null != lblKey)
			lblKey.dispose();
		lblKey = null;

		if(null != cboXItem)
			cboXItem.dispose();
		cboXItem = null;
		if(null != cboYItems) {
			for(int i=0; i<cboYItems.length; i++) {
				if(null != cboYItems[i])
					cboYItems[i].dispose();
				cboYItems[i] = null;
				if(null != lblYItems[i])
					lblYItems[i].dispose();
				lblYItems[i] = null;
			}
		}
		cboYItems = null;
		lblYItems = null;
		model = null;
	}

	/**
	 * This class is responsible for updating the menu elements whenever
	 * the user interacts with them. Namely, it checks for naming errors
	 * and invalid series selections, and handles display of Y-series combo boxes.
	 */
	private class ComboSelectionListener implements SelectionListener {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Combo source = (Combo) e.getSource();
			if(cboXItem.equals(source)) {
				if (deleted[0] && cboXItem.getSelectionIndex() != 0) {
					cboXItem.remove(0);
					deleted[0] = false;
				}
			}
			else {
				for (int i = 0; i < cboYItems.length; i++) {
					if (deleted[i+1] && cboYItems[i].equals(source) && cboYItems[i].getSelectionIndex() != 0) {
						cboYItems[i].remove(0);
						deleted[i+1] = false;
						break;
					}
				}
				boolean setVisible = true;
				if(GraphFactory.isMultiGraph(model.getGraphID())) {
					for(int i=1; i<cboYItems.length; i++) {
						cboYItems[i].setVisible(setVisible);
						lblYItems[i].setVisible(setVisible);
						if (!setVisible && deleted[i+1]) {
							cboYItems[i].remove(0);
							deleted[i+1] = false;
							cboYItems[i].select(0);
						}
						if(deleted[i+1] || (cboYItems[i].getSelectionIndex() > 0 && cboYItems[i].isVisible()))
							setVisible = true;
						else
							setVisible = false;
					}
				}
			}

			checkErrors();
			getWizard().getContainer().updateButtons();
		}
	}

	private void checkErrors(){
		if(!isSeriesUnique()) {
			setErrorMessage(Localization.getString("SelectGraphAndSeriesWizardPage.SeriesNotSelected")); //$NON-NLS-1$
			setMessage(null);
		} else {
			setErrorMessage(null);
			setMessage(""); //$NON-NLS-1$
		}
		if(txtTitle.getText().length() == 0) {
			setErrorMessage(Localization.getString("SelectGraphAndSeriesWizardPage.TitleNotSet")); //$NON-NLS-1$
			setMessage(null);
		}
	}

	private Button[] btnGraphs;
	private SelectGraphAndSeriesWizard wizard;

	private Text txtTitle;		//TODO: Move this to another page once graphs get more detail
	private Text txtKey;
	private Button btnKey;
	private Label lblKey;
	private Combo cboXItem;
	private Combo[] cboYItems;
	private Label[] lblYItems;
	private GraphModel model;
	private boolean[] deleted;
	private boolean edit;
}