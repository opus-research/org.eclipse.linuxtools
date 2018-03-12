/*******************************************************************************
 * Copyright (c) 2015, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class OSIOLabelAttributeEditor extends AbstractAttributeEditor {

	private List list;
	
	private TaskAttribute attrRemoveLabel;

	protected boolean suppressRefresh;

	public OSIOLabelAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
		super(manager, taskAttribute);
		setLayoutHint(new LayoutHint(RowSpan.MULTIPLE, ColumnSpan.MULTIPLE));
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		list = new List(parent, SWT.FLAT | SWT.MULTI | SWT.V_SCROLL);
		toolkit.adapt(list, true, true);
		list.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		list.setFont(JFaceResources.getDefaultFont());
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(list);
		list.setToolTipText(getDescription());

		populateFromAttribute();

		attrRemoveLabel = getModel().getTaskData()
				.getRoot()
				.getMappedAttribute(OSIORestTaskSchema.getDefault().REMOVE_LABEL.getKey());

		selectValuesToRemove();

		list.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					suppressRefresh = true;
					for (String cc : list.getItems()) {
						int index = list.indexOf(cc);
						if (list.isSelected(index)) {
							java.util.List<String> remove = attrRemoveLabel.getValues();
							if (!remove.contains(cc)) {
								attrRemoveLabel.addValue(cc);
							}
						} else {
							attrRemoveLabel.removeValue(cc);
						}
					}
					getModel().attributeChanged(attrRemoveLabel);
				} finally {
					suppressRefresh = false;
				}
			}
		});

		list.showSelection();

		setControl(list);
	}

	private void populateFromAttribute() {
		TaskAttribute attrLabel = getTaskAttribute();
		if (attrLabel != null) {
			for (String value : attrLabel.getValues()) {
				list.add(value);
			}
		}
	}

	private void selectValuesToRemove() {
		for (String item : attrRemoveLabel.getValues()) {
			int i = list.indexOf(item);
			if (i != -1) {
				list.select(i);
			}
		}
	}

	@Override
	public void refresh() {
		if (list != null && !list.isDisposed()) {
			list.removeAll();
			populateFromAttribute();
			selectValuesToRemove();
		}
	}

	@Override
	public boolean shouldAutoRefresh() {
		return !suppressRefresh;
	}

}
