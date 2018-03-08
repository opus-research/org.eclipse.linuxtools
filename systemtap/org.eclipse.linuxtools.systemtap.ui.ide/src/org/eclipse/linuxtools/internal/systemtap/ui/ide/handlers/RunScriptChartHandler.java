/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPerspective;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.ChartStreamDaemon;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphing.ui.views.GraphSelectorEditor;
import org.eclipse.linuxtools.systemtap.graphing.ui.views.GraphSelectorEditorInput;
import org.eclipse.linuxtools.systemtap.graphing.ui.widgets.ExceptionErrorDialog;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * Action used to run the systemTap script in the active editor.  This action will start stap
 * and send the output to both the <code>ScriptConsole</code> window and a <code>DataSet</code>.
 * @author Ryan Morse
 */
public class RunScriptChartHandler extends RunScriptHandler {

    private List<IDataSetParser> parsers;
    private List<IFilteredDataSet> dataSets;
    private List<String> names;
    private List<LinkedList<GraphData>> graphs;

    public RunScriptChartHandler(List<IDataSetParser> parsers, List<IFilteredDataSet> dataSet, List<String> names, List<LinkedList<GraphData>> graphs) {
        super();
        this.parsers = parsers;
        this.dataSets = dataSet;
        this.names = names;
        this.graphs = graphs;
    }

    @Override
    protected void scriptConsoleInitialized(ScriptConsole console){
        int n = parsers.size();
        for (int i = 0; i < n; i++) {
            console.getCommand().addInputStreamListener(new ChartStreamDaemon(dataSets.get(i), parsers.get(i)));
        }
        try {
            String name = console.getName();
            String title = name.substring(name.lastIndexOf('/') + 1);

            IWorkbenchPage p = PlatformUI.getWorkbench().showPerspective(IDEPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            GraphSelectorEditor ivp = (GraphSelectorEditor) p.openEditor(new GraphSelectorEditorInput(title), GraphSelectorEditor.ID);

            String scriptName = console.getName();
            ivp.createScriptSets(scriptName, names, dataSets);

            for (int i = 0; i < n; i++) {
                for (GraphData graph : graphs.get(i)) {
                    ivp.getDisplaySet(i).addGraph(graph);
                }
            }
        } catch (WorkbenchException we) {
            ExceptionErrorDialog.openError(Messages.RunScriptChartHandler_couldNotSwitchToGraphicPerspective, we);
        }
        super.scriptConsoleInitialized(console);
    }

}
