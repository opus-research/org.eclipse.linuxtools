/*******************************************************************************
 * Copyright (c) 2012 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Sami Wagiaalla
 *     Red Hat - Andrew Ferrazzutti
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.LineParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset.DataSetFactory;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.GraphFactory;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.SelectGraphAndSeriesWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SystemTapScriptGraphOptionsTab extends
		AbstractLaunchConfigurationTab {

	// Note: any non-private String key with a trailing underscore is to be appended with an integer when looking up values.
	static final String RUN_WITH_CHART = "runWithChart"; //$NON-NLS-1$
	static final String NUMBER_OF_REGEXS = "numberOfRegexs"; //$NON-NLS-1$
	static final String REGEX_TITLE= "regexTitle_"; //$NON-NLS-1$
	static final String NUMBER_OF_COLUMNS = "numberOfColumns_"; //$NON-NLS-1$
	static final String REGEX_BOX = "regexBox_"; //$NON-NLS-1$
	static final String NUMBER_OF_EXTRAS = "numberOfExtras_"; //$NON-NLS-1$
	static final String EXTRA_BOX = "extraBox_"; //$NON-NLS-1$
	static final String REGULAR_EXPRESSION = "regularExpression_"; //$NON-NLS-1$
	static final String SAMPLE_OUTPUT = "sampleOutput_"; //$NON-NLS-1$

	// Note: all graph-related keys point to 2D lists (regular expression & graph number).
	private static final String NUMBER_OF_GRAPHS = "numberOfGraphs"; //$NON-NLS-1$
	private static final String GRAPH_TITLE = "graphTitle"; //$NON-NLS-1$
	private static final String GRAPH_KEY = "graphKey"; //$NON-NLS-1$
	private static final String GRAPH_X_SERIES = "graphXSeries"; //$NON-NLS-1$
	private static final String GRAPH_ID = "graphID"; //$NON-NLS-1$
	private static final String GRAPH_Y_SERIES_LENGTH = "graphYSeriesLength"; //$NON-NLS-1$
	private static final String GRAPH_Y_SERIES = "graphYSeries"; //$NON-NLS-1$
	protected Pattern pattern;
	protected Matcher matcher;

	private ModifyListener regExListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			if (!textListenersEnabled) {
				return;
			}
			regexAndOutputList.set(selectedRegex * 2, regularExpressionText.getText());
			regexAndOutputList.set(selectedRegex * 2 + 1, sampleOutputText.getText());
			refreshRegexRows();
			updateLaunchConfigurationDialog();
		}
	};

	private ModifyListener columnNameListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent event) {
			if (!textListenersEnabled) {
				return;
			}

			ArrayList<String> columnNames = new ArrayList<String>();
			Control[] children = textFieldsComposite.getChildren();
			for (int i = 0; i < numberOfVisibleColumns; i++) {
				columnNames.add(((Text)children[i*2]).getText());
			}
			columnNamesList.set(selectedRegex, columnNames);
			updateLaunchConfigurationDialog();
		}
	};

	private Combo regexComboBox;
	private Button addRegexButton, editRegexButton, removeRegexButton;

	private Text regularExpressionText;
	private Text sampleOutputText;
	private Composite textFieldsComposite;

	/**
	 * This value controls whether or not the ModifyListeners associated with
	 * the Texts will perform when dispatched. Sometimes the listeners should
	 * be disabled to prevent needless/unsafe operations.
	 */
	private boolean textListenersEnabled = true;

	private ScrolledComposite regexTextScrolledComposite;
	private Group outputParsingGroup;
	private Button runWithChartCheckButton;

	private Table graphsTable;
	private Button addGraphButton, duplicateGraphButton, editGraphButton, removeGraphButton;
	private TableItem selectedTableItem;
	private Group graphsGroup;
	private int numberOfVisibleColumns = 0;
	private boolean graphingEnabled = true;
	private String regexErrorMessage;

	/**
	 * The index of the selected regular expression.
	 */
	private int selectedRegex = -1;

	/**
	 * A list containing pairs of regular expressions and their associated sample outputs,
	 * in the format (regex0, output0, regex1, output1, ...).
	 */
	private ArrayList<String> regexAndOutputList = new ArrayList<String>();

	/**
	 * A name is given to each group captured by a regular expression. This stack contains
	 * the names of all of a regex's groups that have been deleted, so each name may be
	 * restored (without having to retype it) when a group is added again.
	 */
	private Stack<String> cachedNames;

	/**
	 * A list of cachedNames stacks, containing one entry for each regular expression stored.
	 */
	private ArrayList<Stack<String>> cachedNamesList = new ArrayList<Stack<String>>();

	/**
	 * A two-dimensional list that holds references to the names given to each regular expression's captured groups.
	 */
	private ArrayList<ArrayList<String>> columnNamesList = new ArrayList<ArrayList<String>>();

	/**
	 * A list holding the data of every graph for the selected regular expression.
	 */
	private LinkedList<GraphData> graphsData = new LinkedList<GraphData>();

	/**
	 * A list of graphsData lists. This is needed because each regular expression has its own set of graphs.
	 */
	private ArrayList<LinkedList<GraphData>> graphsDataList = new ArrayList<LinkedList<GraphData>>();

	/**
	 * A list of GraphDatas that rely on series information that has been deleted from their relying regex.
	 */
	private LinkedList<GraphData> badGraphs = new LinkedList<GraphData>();

	private ArrayList<Integer> oldNumColumns = new ArrayList<Integer>();
	private ArrayList<Integer> oldNumExtras = new ArrayList<Integer>();

	/**
	 * Returns the list of the names given to reach regular expression.
	 * @param configuration
	 * @return
	 */
	public static ArrayList<String> createDatasetNames(ILaunchConfiguration configuration) {
		try {
			int numberOfRegexs = configuration.getAttribute(NUMBER_OF_REGEXS, 0);
			ArrayList<String> names = new ArrayList<String>(numberOfRegexs);
			for (int r = 0; r < numberOfRegexs; r++) {
				names.add(configuration.getAttribute(REGEX_TITLE + r, "")); //$NON-NLS-1$
			}
			return names;
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_0, e);
		}
		return null;
	}

	/**
	 * Creates a list of parsers, one for each regular expression created, that will be used
	 * to parse the output of a running script.
	 * @param configuration The desired run configuration.
	 * @return A list of parsers.
	 */
	public static ArrayList<IDataSetParser> createDatasetParsers(ILaunchConfiguration configuration) {
		try {
			int numberOfRegexs = configuration.getAttribute(NUMBER_OF_REGEXS, 0);
			ArrayList<IDataSetParser> parsers = new ArrayList<IDataSetParser>(numberOfRegexs);
			for (int r = 0; r < numberOfRegexs; r++) {
				parsers.add(new LineParser("^" + configuration.getAttribute(REGULAR_EXPRESSION + r, "") + "$")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return parsers;
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_0, e);
		}
		return null;
	}

	/**
	 * Creates a data set corresponding to the titles given to each output column
	 * from each of a run configuration's regular expressions.
	 * @param configuration
	 * @return
	 */
	public static ArrayList<IDataSet> createDataset(ILaunchConfiguration configuration) {
		try {
			int numberOfRegexs = configuration.getAttribute(NUMBER_OF_REGEXS, 0);
			ArrayList<IDataSet> datasets = new ArrayList<IDataSet>(numberOfRegexs);

			for (int r = 0; r < numberOfRegexs; r++) {
				int numberOfColumns = configuration.getAttribute(NUMBER_OF_COLUMNS + r, 0);
				ArrayList<String> labels = new ArrayList<String>(numberOfColumns);

				for (int c = 0; c < numberOfColumns; c++) {
					labels.add(configuration.getAttribute(get2DConfigData(REGEX_BOX, r, c), (String) null));
				}
				datasets.add(DataSetFactory.createDataSet(RowDataSet.ID, labels.toArray(new String[] {})));
			}

			return datasets;
		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_1, e);
		}
		return null;
	}

	/**
	 * Creates graph data corresponding to the graphs that will plot a script's parsed output data.
	 * @param configuration The desired run configuration.
	 * @return A data set.
	 */
	public static ArrayList<LinkedList<GraphData>> createGraphsFromConfiguration (ILaunchConfiguration configuration) throws CoreException {
		int numberOfRegexs = configuration.getAttribute(NUMBER_OF_REGEXS, 0);
		ArrayList<LinkedList<GraphData>> graphsList = new ArrayList<LinkedList<GraphData>>(numberOfRegexs);

		for (int r = 0; r < numberOfRegexs; r++) {
			int numberOfGraphs = configuration.getAttribute(NUMBER_OF_GRAPHS + r, 0);
			LinkedList<GraphData> graphs = new LinkedList<GraphData>();
			for (int i = 0; i < numberOfGraphs; i++) {
				GraphData graphData = new GraphData();
				graphData.title = configuration.getAttribute(get2DConfigData(GRAPH_TITLE, r, i), ""); //$NON-NLS-1$

				graphData.key = configuration.getAttribute(get2DConfigData(GRAPH_KEY, r, i), ""); //$NON-NLS-1$
				graphData.xSeries = configuration.getAttribute(get2DConfigData(GRAPH_X_SERIES, r, i), 0);
				graphData.graphID = configuration.getAttribute(get2DConfigData(GRAPH_ID, r, i), ""); //$NON-NLS-1$

				int ySeriesLength = configuration.getAttribute(get2DConfigData(GRAPH_Y_SERIES_LENGTH, r, i), 0);
				int[] ySeries = new int[ySeriesLength];
				for (int j = 0; j < ySeriesLength; j++) {
					ySeries[j] = configuration.getAttribute(get2DConfigData(GRAPH_Y_SERIES, r, i + "_" + j), 0); //$NON-NLS-1$
				}
				graphData.ySeries = ySeries;

				graphs.add(graphData);
			}
			graphsList.add(graphs);
		}

		return graphsList;
	}

	/**
	 * Returns the key associated with the i'th data item of the r'th regular expression.
	 * @param configDataName The type of data to access from the configuration.
	 * @param r The index of the regular expression.
	 * @param i The index of the data item to access.
	 */
	private static String get2DConfigData(String configDataName, int r, int i) {
		return configDataName + r + "_" + i; //$NON-NLS-1$
	}

	/**
	 * Returns the key associated with the data item of the r'th regular expression, tagged by string s.
	 * @param configDataName The type of data to access from the configuration.
	 * @param r The index of the regular expression.
	 * @param s
	 */
	private static String get2DConfigData(String configDataName, int r, String s) {
		return configDataName + r + "_" + s; //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		GridLayout layout = new GridLayout();
		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(layout);
		top.setLayoutData( new GridData(SWT.FILL, SWT.FILL, true, true));

		this.runWithChartCheckButton = new Button(top, SWT.CHECK);
		runWithChartCheckButton.setText(Messages.SystemTapScriptGraphOptionsTab_2);
		runWithChartCheckButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setGraphingEnabled(runWithChartCheckButton.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				setGraphingEnabled(runWithChartCheckButton.getSelection());
			}
		});

		runWithChartCheckButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_3);

		this.outputParsingGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
		outputParsingGroup.setText(Messages.SystemTapScriptGraphOptionsTab_4);
		outputParsingGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		this.createColumnSelector(outputParsingGroup);

		this.graphsGroup = new Group(top, SWT.SHADOW_ETCHED_IN);
		graphsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createGraphCreateArea(graphsGroup);

		setGraphingEnabled(false);
		runWithChartCheckButton.setSelection(false);
	}

	protected void createColumnSelector(Composite parent) {

		GridLayout layout = new GridLayout();
		parent.setLayout(layout);

		GridLayout twoColumns = new GridLayout();
		twoColumns.numColumns = 2;

		GridLayout fiveColumns = new GridLayout();
		fiveColumns.numColumns = 5;

		Composite regexButtonLayout = new Composite(parent, SWT.NONE);
		regexButtonLayout.setLayout(fiveColumns);
		regexButtonLayout.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label selectedRegexLabel = new Label(regexButtonLayout, SWT.NONE);
		selectedRegexLabel.setText(Messages.SystemTapScriptGraphOptionsTab_selectedRegexLabel);
		selectedRegexLabel.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_selectedRegexTooltip);
		regexComboBox = new Combo(regexButtonLayout, SWT.DROP_DOWN);
		regexComboBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		regexComboBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateRegexSelection();
			}
		});

		addRegexButton = new Button(regexButtonLayout, SWT.PUSH);
		editRegexButton = new Button(regexButtonLayout, SWT.PUSH);
		removeRegexButton = new Button(regexButtonLayout, SWT.PUSH);
		addRegexButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		editRegexButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		removeRegexButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		addRegexButton.setText(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton);
		editRegexButton.setText(Messages.SystemTapScriptGraphOptionsTab_EditGraphButton);
		removeRegexButton.setText(Messages.SystemTapScriptGraphOptionsTab_RemoveGraphButton);

		final IInputValidator regexInputValidator = new IInputValidator() {
			@Override
			public String isValid(String newText) {
				if (newText.length() == 0) {
					return "Please enter a non-empty title."; //$NON-NLS-1$
				}
				for (String rname : regexComboBox.getItems()) {
					if (newText.contentEquals(rname)) {
						return "Name \"" + rname + "\" is the title of another regular expression. " //$NON-NLS-1$ //$NON-NLS-2$
								+ "Please choose a unique title."; //$NON-NLS-1$
					}
				}
				return null;
			}
		};

		addRegexButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				InputDialog dialog = new InputDialog(workbench
						.getActiveWorkbenchWindow().getShell(), Messages.SystemTapScriptGraphOptionsTab_setRegexTitleMessage,
						Messages.SystemTapScriptGraphOptionsTab_setRegexTitleAdd,
						MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultRegexTitleBase, new Integer(regexComboBox.getItemCount())),
						regexInputValidator);

				dialog.open();
				String rname = dialog.getValue();
				if (rname == null) {
					return;
				}

				// Add two (empty) slots for a new regular expression & sample output.
				regexAndOutputList.add(""); //$NON-NLS-1$
				regexAndOutputList.add(""); //$NON-NLS-1$
				columnNamesList.add(new ArrayList<String>());
				cachedNamesList.add(new Stack<String>());
				graphsDataList.add(new LinkedList<GraphData>());
				oldNumColumns.add(0);
				oldNumExtras.add(0);

				// Add and select the new item.
				regexComboBox.add(rname);
				regexComboBox.select(regexComboBox.getItemCount() - 1);
				updateRegexSelection();

				// Enable the "remove" button if only one item was present before. (Don't do this _every_ time something is added.) 
				if (regexComboBox.getItemCount() == 2) {
					removeRegexButton.setEnabled(true);
				}
				updateLaunchConfigurationDialog();
			}
		});

		editRegexButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				InputDialog dialog = new InputDialog(workbench
						.getActiveWorkbenchWindow().getShell(), Messages.SystemTapScriptGraphOptionsTab_setRegexTitleEdit,
						Messages.SystemTapScriptGraphOptionsTab_setRegexTitleMessage,
						regexComboBox.getItem(selectedRegex), regexInputValidator);

				dialog.open();
				String rname = dialog.getValue();
				if (rname == null) {
					return;
				}
				regexComboBox.setItem(selectedRegex, rname);
				regexComboBox.select(selectedRegex); //Just to update the text of the combo box.
				updateLaunchConfigurationDialog();
			}
		});

		removeRegexButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				MessageDialog dialog = new MessageDialog(workbench
						.getActiveWorkbenchWindow().getShell(), Messages.SystemTapScriptGraphOptionsTab_setRegexTitleDelete, null,
						MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_setRegexTitleAskdelete, regexComboBox.getItem(selectedRegex)),
						MessageDialog.QUESTION, new String[]{"Yes", "No"}, 0); //$NON-NLS-1$ //$NON-NLS-2$
				int result = dialog.open();
				if (result != 0) { //No
					return;
				}

				int removedRegex = selectedRegex;
				// The current selection is to be removed, so select something else that will be available.
				regexComboBox.select(selectedRegex != 0 ? selectedRegex - 1 : 1);
				updateRegexSelection();

				regexComboBox.remove(removedRegex);
				regexAndOutputList.remove(removedRegex * 2);
				regexAndOutputList.remove(removedRegex * 2);
				columnNamesList.remove(removedRegex);
				cachedNamesList.remove(removedRegex);
				graphsDataList.remove(removedRegex);
				oldNumColumns.add(removedRegex);
				oldNumExtras.add(removedRegex);

				// Make sure the index of the selection is accurate.
				selectedRegex = regexComboBox.getSelectionIndex();

				// Disable the "remove" button if only one selection is left; never want zero items.
				if (regexComboBox.getItemCount() == 1) {
					removeRegexButton.setEnabled(false);
				}
				updateLaunchConfigurationDialog();
			}
		});

		Composite regexSummaryComposite = new Composite(parent, SWT.NONE);
		regexSummaryComposite.setLayout(twoColumns);
		regexSummaryComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label regularExpressionLabel = new Label(regexSummaryComposite, SWT.NONE);
		regularExpressionLabel.setText(Messages.SystemTapScriptGraphOptionsTab_regexLabel);
		regularExpressionLabel.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_regexTooltip);
		regularExpressionText = new Text(regexSummaryComposite, SWT.BORDER);
		regularExpressionText.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_regexTooltip);
		regularExpressionText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		regularExpressionText.addModifyListener(regExListener);

		Label sampleOutputLabel = new Label(regexSummaryComposite, SWT.NONE);
		sampleOutputLabel.setText(Messages.SystemTapScriptGraphOptionsTab_sampleOutputLabel);
		sampleOutputLabel.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_sampleOutputTooltip);
		this.sampleOutputText = new Text(regexSummaryComposite, SWT.BORDER);
		this.sampleOutputText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.sampleOutputText.addModifyListener(regExListener);
		sampleOutputText.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_sampleOutputTooltip);

		Composite expressionTableLabels = new Composite(parent, SWT.NONE);
		expressionTableLabels.setLayout(twoColumns);
		expressionTableLabels.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label label = new Label(expressionTableLabels, SWT.NONE);
		label.setText(Messages.SystemTapScriptGraphOptionsTab_columnTitle);
		label.setAlignment(SWT.CENTER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = 200;

		label.setLayoutData(data);

		label = new Label(expressionTableLabels, SWT.NONE);
		label.setText(Messages.SystemTapScriptGraphOptionsTab_extractedValueLabel);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		this.regexTextScrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.FILL, true, false);
		data.heightHint = 200;
		regexTextScrolledComposite.setLayoutData(data);

		textFieldsComposite = new Composite(regexTextScrolledComposite, SWT.NONE);
		textFieldsComposite.setLayout(twoColumns);
		textFieldsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		regexTextScrolledComposite.setContent(textFieldsComposite);
		regexTextScrolledComposite.setExpandHorizontal(true);
		regexTextScrolledComposite.setExpandVertical(false);
	}

	private IDataSet getCurrentDataset() {
		return DataSetFactory.createDataSet(RowDataSet.ID, columnNamesList.get(selectedRegex).toArray(new String[] {}));
	}

	/**
	 * Given an existing entry in the table of graph items, provides the entry with
	 * graph data and an appropriate title.
	 * @param item The table item to inject data into.
	 * @param gd The graph data to give to the table item.
	 */
	private void setGraphTableItemData(TableItem item, GraphData gd) {
		item.setText(GraphFactory.getGraphName(gd.graphID) + ":" //$NON-NLS-1$
				+ gd.title);
		item.setData(gd);
	}

	private void createGraphCreateArea(Composite comp){
		GridLayout twoColumnsLayout = new GridLayout();
		comp.setLayout(twoColumnsLayout);
		twoColumnsLayout.numColumns = 2;

		graphsTable = new Table(comp, SWT.SINGLE | SWT.BORDER);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		graphsTable.setLayoutData(layoutData);

		// Button to add another graph
		Composite buttonComposite = new Composite(comp, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;

		buttonComposite.setLayout(gridLayout);
		// Button to add a new graph
		addGraphButton = new Button(buttonComposite, SWT.PUSH);
		addGraphButton.setText(Messages.SystemTapScriptGraphOptionsTab_AddGraphButton);
		addGraphButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_AddGraphButtonToolTip);
		addGraphButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Button to copy an existing graph
		duplicateGraphButton = new Button(buttonComposite, SWT.PUSH);
		duplicateGraphButton.setText(Messages.SystemTapScriptGraphOptionsTab_DuplicateGraphButton);
		duplicateGraphButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_DuplicateGraphButtonToolTip);
		duplicateGraphButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Button to edit an existing graph
		editGraphButton = new Button(buttonComposite, SWT.PUSH);
		editGraphButton.setText(Messages.SystemTapScriptGraphOptionsTab_EditGraphButton);
		editGraphButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_EditGraphButtonToolTip);
		editGraphButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Button to remove the selected graph/filter
		removeGraphButton = new Button(buttonComposite, SWT.PUSH);
		removeGraphButton.setText(Messages.SystemTapScriptGraphOptionsTab_RemoveGraphButton);
		removeGraphButton.setToolTipText(Messages.SystemTapScriptGraphOptionsTab_RemoveGraphButtonToolTip);
		removeGraphButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Action to notify the buttons when to enable/disable themselves based
		// on list selection
		graphsTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedTableItem = (TableItem) e.item;
				setSelectionControlsEnabled(true);
			}
		});

		// Brings up a new dialog box when user clicks the add button. Allows
		// selecting a new graph to display.
		addGraphButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectGraphAndSeriesWizard wizard = new SelectGraphAndSeriesWizard(getCurrentDataset(), null);
				IWorkbench workbench = PlatformUI.getWorkbench();
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench
						.getActiveWorkbenchWindow().getShell(), wizard);
				dialog.create();
				dialog.open();

				GraphData gd = wizard.getGraphData();

				if (null != gd) {
					TableItem item = new TableItem(graphsTable, SWT.NONE);
					setGraphTableItemData(item, gd);
					graphsData.add(gd);
					markGraphTableItem(item, false);
					updateLaunchConfigurationDialog();
				}
			}
		});

		// Adds a new entry to the list of graphs that is a copy of the one selected.
		duplicateGraphButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphData gd = (GraphData) selectedTableItem.getData();

				TableItem item = new TableItem(graphsTable, SWT.NONE);
				setGraphTableItemData(item, gd);
				graphsData.add(gd);
				if (badGraphs.contains(selectedTableItem)) {
					badGraphs.add(gd);
					markGraphTableItem(item, true);
				}
				updateLaunchConfigurationDialog();
			}
		});

		// When button is clicked, brings up same wizard as the one for adding
		// a graph. Data in the wizard is filled out to match the properties
		// of the selected graph.
		editGraphButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectGraphAndSeriesWizard wizard = new SelectGraphAndSeriesWizard(getCurrentDataset(),
						(GraphData) selectedTableItem.getData());
				IWorkbench workbench = PlatformUI.getWorkbench();
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench
						.getActiveWorkbenchWindow().getShell(), wizard);
				dialog.create();
				dialog.open();

				GraphData gd = wizard.getGraphData();

				if (null != gd) {
					setGraphTableItemData(selectedTableItem, gd);
					graphsData.set(graphsTable.indexOf(selectedTableItem), gd);
					if (badGraphs.contains(selectedTableItem)) {
						findBadGraphs();
					}
					updateLaunchConfigurationDialog();
				}
			}
		});

		// Removes the selected graph/filter from the table
		removeGraphButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				GraphData gd = (GraphData) selectedTableItem.getData();
				graphsData.remove(gd);
				badGraphs.remove(gd);
				selectedTableItem.dispose();
				setSelectionControlsEnabled(false);
				updateLaunchConfigurationDialog();
			}
		});
	}

	private void updateRegexSelection() {
		updateRegexSelection(false);
	}

	/**
	 * This handles UI & list updating whenever a different regular expression is selected.
	 * @param force If true, the UI will update even if the index of the selected regex did not change.
	 */
	private void updateRegexSelection(boolean force) {
		// Quit if the selection didn't change anything.
		int newSelection = regexComboBox.getSelectionIndex();
		if (!force && selectedRegex == newSelection) {
			return;
		}
		selectedRegex = newSelection;

		// Disable the Texts' ModifyListener here, as it would perform repeated operations.
		textListenersEnabled = false;

		regularExpressionText.setText(regexAndOutputList.get(selectedRegex * 2));
		sampleOutputText.setText(regexAndOutputList.get(selectedRegex * 2 + 1));

		cachedNames = cachedNamesList.get(selectedRegex);

		// Update the number of columns and their titles here, and not in refreshRegexRows,
		// using the list of saved active names instead of a cachedNames stack.
		ArrayList<String> columnNames = columnNamesList.get(selectedRegex);
		int desiredNumberOfColumns = columnNames.size();
		// Remove all columns to easily update them all immediately afterwards.
		while (numberOfVisibleColumns > 0) {
			removeColumn(true);
		}
		while (numberOfVisibleColumns < desiredNumberOfColumns) {
			addColumn(columnNames.get(numberOfVisibleColumns));
		}

		refreshRegexRows();

		// Now, only display graphs that are associated with the selected regex.
		graphsData = graphsDataList.get(selectedRegex);
		//badGraphs = badGraphsList.get(selectedRegex);
		graphsTable.removeAll();
		selectedTableItem = null;
		setSelectionControlsEnabled(false);

		for (GraphData gd : graphsData) {
			TableItem item = new TableItem(graphsTable, SWT.NONE);
			setGraphTableItemData(item, gd);
			markGraphTableItem(item, badGraphs.contains(gd));
		}
		graphsGroup.setText(MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_graphsTitle, regexComboBox.getItem(selectedRegex)));

		textListenersEnabled = true;
	}

	private void refreshRegexRows() {

		try{
			pattern = Pattern.compile(regularExpressionText.getText());
			matcher = pattern.matcher(sampleOutputText.getText());
			this.regexErrorMessage = ""; //$NON-NLS-1$
		}catch (PatternSyntaxException e){
			this.regexErrorMessage = e.getMessage();
			return;
		}
		if (regularExpressionText.getText().contains("()")){ //$NON-NLS-1$
			this.regexErrorMessage = Messages.SystemTapScriptGraphOptionsTab_6;
			return;
		}

		int desiredNumberOfColumns = matcher.groupCount();

		while (numberOfVisibleColumns < desiredNumberOfColumns){
			addColumn();
		}

		while (numberOfVisibleColumns > desiredNumberOfColumns){
			removeColumn();
		}

		// Set values
		Control[] children = textFieldsComposite.getChildren();
		for (int i = 0; i < numberOfVisibleColumns; i++) {
			if (!matcher.matches()){
				((Label)children[i*2+1]).setText(""); //$NON-NLS-1$
			} else {
				((Label)children[i*2+1]).setText(" " +matcher.group(i+1)); //$NON-NLS-1$
			}
		}

		// May only add graphs if there is output data being captured.
		addGraphButton.setEnabled(numberOfVisibleColumns > 0);
	}

	private void addColumn() {
		addColumn(null);
	}

	private void addColumn(String nameToAdd) {
		Text text = new Text(textFieldsComposite, SWT.BORDER);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.minimumWidth = 200;
		data.widthHint = 200;
		text.setLayoutData(data);

		this.numberOfVisibleColumns++;
		text.addModifyListener(columnNameListener);
		if (nameToAdd == null) {
			// Restore a deleted name by popping from the stack.
			if (cachedNames.size() > 0) {
				text.setText(cachedNames.pop());
			} else {
				text.setText(""); //$NON-NLS-1$
			}
			findBadGraphs();
		} else {
			text.setText(nameToAdd);
		}

		Label label = new Label(textFieldsComposite, SWT.BORDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		textFieldsComposite.layout();
		textFieldsComposite.pack();
	}

	private void removeColumn() {
		removeColumn(false);
	}
	private void removeColumn(Boolean graphSwitch) {
		Control[] children = textFieldsComposite.getChildren();
		int i = this.numberOfVisibleColumns*2 -1;

		if (!graphSwitch) {
			// Push the removed name on a stack.
			String name = ((Text)children[i-1]).getText();
			if (name != null && name != "") { //$NON-NLS-1$
				cachedNames.push(name);
			}
			columnNamesList.get(selectedRegex).remove(numberOfVisibleColumns - 1);
		}

		children[i].dispose();
		children[i-1].dispose();

		this.numberOfVisibleColumns--;
		if (!graphSwitch) {
			findBadGraphs();
		}

		textFieldsComposite.layout();
		textFieldsComposite.pack();
	}

	private void findBadGraphs() {
		int numberOfRegexs = regexComboBox.getItemCount();
		for (int r = 0; r < numberOfRegexs; r++) {
			for (GraphData gd : graphsDataList.get(r)) {
				boolean removed = false;
				int numberOfColumns = columnNamesList.get(r).size();
				if (gd.xSeries >= numberOfColumns) {
					removed = true;
				}
				for (int s = 0; s < gd.ySeries.length && !removed; s++) {
					if (gd.ySeries[s] >= numberOfColumns) {
						removed = true;
					}
				}
	
				// Remember, only need to mark TableItems that are actually visible.
				if (removed) {
					if (!badGraphs.contains(gd)) {
						badGraphs.add(gd);
						markGraphTableItem(findGraphTableItem(gd), true);
					}
				} else if (badGraphs.contains(gd)) {
					badGraphs.remove(gd);
					markGraphTableItem(findGraphTableItem(gd), false);
				}
			}
		}
	}

	private TableItem findGraphTableItem(GraphData gd) {
		for (TableItem item : graphsTable.getItems()) {
			if (item.getData().equals(gd)) {
				return item;
			}
		}
		return null;
	}

	private void markGraphTableItem(TableItem item, boolean bad) {
		// Include a null check to avoid accidentally marking non-visible items.
		if (item == null) {
			return;
		}
		item.setForeground(item.getDisplay().getSystemColor(bad ? SWT.COLOR_RED : SWT.COLOR_BLACK));
	}

	public boolean canFlipToNextPage() {
		return false;
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RUN_WITH_CHART, false);
		configuration.setAttribute(NUMBER_OF_REGEXS, 1);
		configuration.setAttribute(REGEX_TITLE + 0, MessageFormat.format(Messages.SystemTapScriptGraphOptionsTab_defaultRegexTitleBase, new Integer(regexComboBox.getItemCount())));
		configuration.setAttribute(NUMBER_OF_COLUMNS + 0, 0);
		configuration.setAttribute(REGULAR_EXPRESSION + 0, ""); //$NON-NLS-1$
		configuration.setAttribute(SAMPLE_OUTPUT + 0, ""); //$NON-NLS-1$
		configuration.setAttribute(NUMBER_OF_GRAPHS + 0, 0);
		selectedRegex = -1;
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			boolean chart = configuration.getAttribute(RUN_WITH_CHART, false);
			setGraphingEnabled(chart);
			this.runWithChartCheckButton.setSelection(chart);

			// Reset lists & settings to keep things idempotent.
			regexComboBox.removeAll();
			regexAndOutputList.clear();
			columnNamesList.clear();
			cachedNamesList.clear();
			oldNumColumns.clear();
			oldNumExtras.clear();
			graphsTable.removeAll();
			badGraphs.clear();

			int numberOfRegexs = configuration.getAttribute(NUMBER_OF_REGEXS, 0);

			// Only allow removing regexs if there are more than one.
			removeRegexButton.setEnabled(numberOfRegexs > 1);

			for (int r = 0; r < numberOfRegexs; r++) {
				regexComboBox.add(configuration.getAttribute(REGEX_TITLE + r, "")); //$NON-NLS-1$

				// Save all of the configuration's regular expressions & sample outputs in a list.
				regexAndOutputList.add(configuration.getAttribute(REGULAR_EXPRESSION + r, "")); //$NON-NLS-1$
				regexAndOutputList.add(configuration.getAttribute(SAMPLE_OUTPUT + r, "")); //$NON-NLS-1$

				oldNumColumns.add(configuration.getAttribute(NUMBER_OF_COLUMNS + 0, 0));

				// Save each regex's list of group names.
				int numberOfColumns = configuration.getAttribute(NUMBER_OF_COLUMNS + r, 0);
				ArrayList<String> namelist = new ArrayList<String>(numberOfColumns);
				for (int i = 0; i < numberOfColumns; i++) {
					namelist.add(configuration.getAttribute(get2DConfigData(REGEX_BOX, r, i), (String)null));
				}
				columnNamesList.add(namelist);

				//Reclaim missing column data that was required for existing graphs at the time of the previous "apply".
				int numberOfExtras = configuration.getAttribute(NUMBER_OF_EXTRAS + r, 0);
				oldNumExtras.add(numberOfExtras);
				Stack<String> oldnames = new Stack<String>();
				for (int i = 0; i < numberOfExtras; i++) {
					oldnames.push(configuration.getAttribute(get2DConfigData(EXTRA_BOX, r, i), (String)null));
				}
				cachedNamesList.add(oldnames);
			}

			// When possible, preserve the selection on subsequent initializations, for user convenience.
			int defaultSelectedRegex = 0 <= selectedRegex && selectedRegex < numberOfRegexs ? selectedRegex : 0;
			regexComboBox.select(defaultSelectedRegex);

			// Add graphs
			graphsDataList = createGraphsFromConfiguration(configuration);
			graphsData = graphsDataList.get(defaultSelectedRegex);
			for (GraphData graphData : graphsData) {
				TableItem item = new TableItem(graphsTable, SWT.NONE);
				setGraphTableItemData(item, graphData);
			}

			updateRegexSelection(true); // Handles all remaining updates.
			findBadGraphs();

		} catch (CoreException e) {
			ExceptionErrorDialog.openError(Messages.SystemTapScriptGraphOptionsTab_5, e);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RUN_WITH_CHART, this.runWithChartCheckButton.getSelection());

		int numberOfRegexs = regexComboBox.getItemCount();
		configuration.setAttribute(NUMBER_OF_REGEXS, numberOfRegexs);

		for (int r = 0; r < numberOfRegexs; r++) {
			// Save data sets.
			configuration.setAttribute(REGEX_TITLE + r, regexComboBox.getItem(r));
			configuration.setAttribute(REGULAR_EXPRESSION + r, regexAndOutputList.get(r * 2));
			configuration.setAttribute(SAMPLE_OUTPUT + r, regexAndOutputList.get(r * 2 + 1));

			ArrayList<String> columnNames = columnNamesList.get(r);
			int numberOfColumns = columnNames.size();
			configuration.setAttribute(NUMBER_OF_COLUMNS + r, numberOfColumns);
			for (int i = 0; i < numberOfColumns; i++) {
				configuration.setAttribute(get2DConfigData(REGEX_BOX, r, i), columnNames.get(i));
			}
			// Clear what's unused.
			int oldNumCols = oldNumColumns.get(r);
			for (int i = numberOfColumns; i < oldNumCols; i++) {
				configuration.setAttribute(get2DConfigData(REGEX_BOX, r, i), (String) null);
			}
			oldNumColumns.set(r, numberOfColumns);

			// If there are graphs with missing data, store all cached names in the configuration
			// so that they will be easily restorable for next time.
			Stack<String> extranames = cachedNamesList.get(r);
			int numberOfExtras = (badGraphs.size() == 0) ? 0 : extranames.size();
			configuration.setAttribute(NUMBER_OF_EXTRAS + r, numberOfExtras);
			for (int i = 0; i < numberOfExtras; i++) {
				configuration.setAttribute(get2DConfigData(EXTRA_BOX, r, i), extranames.get(i));
			}
			int oldNumberOfExtras = oldNumExtras.get(r);
			for (int i = numberOfExtras; i < oldNumberOfExtras; i++) {
				configuration.setAttribute(get2DConfigData(EXTRA_BOX, r, i), (String) null);
			}

			// Save graphs.
			LinkedList<GraphData> list = graphsDataList.get(r);
			int numberOfGraphs = list.size();
			configuration.setAttribute(NUMBER_OF_GRAPHS + r, numberOfGraphs);
			for (int i = 0; i < numberOfGraphs; i++) {
				GraphData graphData = list.get(i);
				configuration.setAttribute(get2DConfigData(GRAPH_TITLE, r, i), graphData.title);

				configuration.setAttribute(get2DConfigData(GRAPH_KEY, r, i), graphData.key);
				configuration.setAttribute(get2DConfigData(GRAPH_X_SERIES, r, i), graphData.xSeries);
				configuration.setAttribute(get2DConfigData(GRAPH_ID, r, i), graphData.graphID);

				configuration.setAttribute(get2DConfigData(GRAPH_Y_SERIES_LENGTH, r, i), graphData.ySeries.length);
				for (int j = 0; j < graphData.ySeries.length; j++) {
					configuration.setAttribute(get2DConfigData(GRAPH_Y_SERIES, r, i + "_" + j), graphData.ySeries[j]); //$NON-NLS-1$
				}
			}
		}
	}


	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);

		// If graphic is disabled then everything is valid.
		if (!this.graphingEnabled){
			return true;
		}

		if (!this.regexErrorMessage.equals("")){ //$NON-NLS-1$
			setErrorMessage(regexErrorMessage);
			return false;
		}
		if (badGraphs.size() > 0){
			setErrorMessage(Messages.SystemTapScriptGraphOptionsTab_8);
			return false;
		}
		for (ArrayList<String> columnNames : columnNamesList) {
			if (columnNames.size() == 0){
				setErrorMessage(Messages.SystemTapScriptGraphOptionsTab_9);
				return false;
			}
		}

		return true;
	}

	@Override
	public String getName() {
		return Messages.SystemTapScriptGraphOptionsTab_7;
	}

	@Override
	public Image getImage() {
		return AbstractUIPlugin.imageDescriptorFromPlugin(IDEPlugin.PLUGIN_ID,
				"icons/graphing_tab.gif").createImage(); //$NON-NLS-1$
	}

	private void setGraphingEnabled(boolean enabled){
		if (this.graphingEnabled == enabled){
			return;
		}
		this.graphingEnabled = enabled;
		this.setControlEnabled(outputParsingGroup, enabled);
		this.setControlEnabled(graphsGroup, enabled);
		// Disable buttons that rely on a selected graph if no graph is selected.
		this.setSelectionControlsEnabled(selectedTableItem != null);
		this.addGraphButton.setEnabled(enabled && numberOfVisibleColumns > 0);
		this.removeRegexButton.setEnabled(enabled && regexComboBox.getItemCount() > 1);
		updateLaunchConfigurationDialog();
	}

	private void setControlEnabled(Composite composite, boolean enabled){
		composite.setEnabled(enabled);
		for (Control child : composite.getChildren()) {
				child.setEnabled(enabled);
			if(child instanceof Composite){
				setControlEnabled((Composite)child, enabled);
			}
		}
	}

	/**
	 * Call this to enable/disable all buttons whose actions depend on a selected graph.
	 * @param enabled Set to true to enable the buttons; set to false to disable them.
	 */
	private void setSelectionControlsEnabled(boolean enabled) {
		duplicateGraphButton.setEnabled(enabled);
		editGraphButton.setEnabled(enabled);
		removeGraphButton.setEnabled(enabled);
	}
}
