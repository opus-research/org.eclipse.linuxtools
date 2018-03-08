/*******************************************************************************
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.linuxtools.internal.perf.launch.PerfEventsTab;
import org.eclipse.linuxtools.internal.perf.launch.PerfOptionsTab;
import org.eclipse.linuxtools.internal.perf.model.PMCommand;
import org.eclipse.linuxtools.internal.perf.model.PMDso;
import org.eclipse.linuxtools.internal.perf.model.PMEvent;
import org.eclipse.linuxtools.internal.perf.model.PMFile;
import org.eclipse.linuxtools.internal.perf.model.PMSymbol;
import org.eclipse.linuxtools.internal.perf.model.TreeParent;
import org.eclipse.linuxtools.internal.perf.ui.PerfDoubleClickAction;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.osgi.framework.FrameworkUtil;

public class ModelTest extends AbstractTest {
	protected ILaunchConfiguration config;
	protected Stack<Class<?>> stack;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild(FrameworkUtil.getBundle(this.getClass()), "fibTest"); //$NON-NLS-1$
		config = createConfiguration(proj.getProject());

		Class<?>[] klassList = new Class<?>[] { PMSymbol.class, PMFile.class,
				PMDso.class, PMCommand.class, PMEvent.class };
		stack = new Stack<Class<?>>();
		stack.addAll(Arrays.asList(klassList));
	}

	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(PerfPlugin.LAUNCHCONF_ID);
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) {
		PerfEventsTab eventsTab = new PerfEventsTab();
		PerfOptionsTab optionsTab = new PerfOptionsTab();
		wc.setAttribute(PerfPlugin.ATTR_SourceLineNumbers, false);
		eventsTab.setDefaults(wc);
		optionsTab.setDefaults(wc);
	}

	public void testModelDefaultGenericStructure() {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		checkChildrenStructure(invisibleRoot, stack);
	}

	public void testModelMultiEventGenericStructure() {
		TreeParent invisibleRoot = buildModel(
				"resources/multievent-data/perf.data",
				"resources/multievent-data/perf.data.txt",
				"resources/multievent-data/perf.data.err.log");

		checkChildrenStructure(invisibleRoot, stack);
	}

	public void testPercentages() {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		checkChildrenPercentages(invisibleRoot, invisibleRoot.getPercent());
	}

	public void testDoubleClickAction () {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		PerfPlugin.getDefault().setModelRoot(invisibleRoot);
		// update the model root for the view
		PerfCore.RefreshView();

		// number of parents excluding invisibleRoot
		int numOfParents = getNumberOfParents(invisibleRoot) - 1;

		// create a double click action to act on the tree viewer
		TreeViewer tv = PerfPlugin.getDefault().getProfileView().getTreeViewer();
		PerfDoubleClickAction dblClick = new PerfDoubleClickAction(tv);

		// double click every element
		doubleClickAllChildren(invisibleRoot, tv, dblClick);

		// If all elements are expanded, this is the number of elements
		// in our model that have children.
		assertEquals(numOfParents, tv.getExpandedElements().length);
	}

	public void testParserMultiEvent() {
		TreeParent invisibleRoot = buildModel(
				"resources/multievent-data/perf.data",
				"resources/multievent-data/perf.data.txt",
				"resources/multievent-data/perf.data.err.log");

		assertEquals(invisibleRoot.getChildren().length, 5);

		String cur = null;

		for (TreeParent event : invisibleRoot.getChildren()) {

			cur = event.getName();

			// Assert specific properties extracted by the parser.
			if ("cpu-clock".equals(cur)) {
				assertTrue(event.hasChildren());
				assertEquals(event.getChildren().length, 1);

				TreeParent cmd = event.getChildren()[0];
				assertEquals(cmd.getChildren().length, 1);

				String[] cmdLabels = { "hellotest" };
				checkCommadLabels(cmdLabels, cmd);
			} else if ("task-clock".equals(cur)) {
				assertTrue(event.hasChildren());
				assertEquals(event.getChildren().length, 1);

				TreeParent cmd = event.getChildren()[0];
				assertEquals(cmd.getChildren().length, 1);

				String[] cmdLabels = { "hellotest" };
				checkCommadLabels(cmdLabels, cmd);
			} else if ("page-faults".equals(cur)) {
				assertTrue(event.hasChildren());
				assertEquals(event.getChildren().length, 1);

				TreeParent cmd = event.getChildren()[0];
				assertEquals(cmd.getChildren().length, 3);

				String[] cmdLabels = { "ld-2.14.90.so", "[kernel.kallsyms]",
						"libc-2.14.90.so" };
				checkCommadLabels(cmdLabels, cmd);
			} else if ("minor-faults".equals(cur)) {
				assertTrue(event.hasChildren());
				assertEquals(event.getChildren().length, 1);

				TreeParent cmd = event.getChildren()[0];
				assertEquals(cmd.getChildren().length, 3);

				String[] cmdLabels = { "ld-2.14.90.so", "[kernel.kallsyms]",
						"libc-2.14.90.so" };
				checkCommadLabels(cmdLabels, cmd);
			} else if ("major-faults".equals(cur)) {
				assertTrue(!event.hasChildren());
			}

		}
	}

	public void testParserDefaultEvent() {
		TreeParent invisibleRoot = buildModel(
				"resources/defaultevent-data/perf.data",
				"resources/defaultevent-data/perf.data.txt",
				"resources/defaultevent-data/perf.data.err.log");

		// Assert specific properties extracted by the parser.
		assertEquals(invisibleRoot.getChildren().length, 1);

		TreeParent event = invisibleRoot.getChildren()[0];
		assertEquals(event.getName(), "cycles");
		assertTrue(event.hasChildren());
		assertEquals(event.getChildren().length, 1);

		TreeParent cmd = event.getChildren()[0];
		assertTrue(cmd.hasChildren());
		assertEquals(cmd.getChildren().length, 4);

		String[] cmdLabels = { "hellotest", "[kernel.kallsyms]",
				"ld-2.14.90.so", "perf" };
		checkCommadLabels(cmdLabels, cmd);
	}

	public void testParseEventList() {
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader("resources/simple-perf-event-list"));
		} catch (FileNotFoundException e) {
			fail();
		}

		HashMap<String, ArrayList<String>> eventList = PerfCore.parseEventList(input);
		for(String key : eventList.keySet()){
			if ("Raw hardware event descriptor".equals(key)) {
				assertTrue(eventList.get(key).contains("rNNN"));
				assertTrue(eventList.get(key).contains("cpu/t1=v1"));
			} else if ("Hardware breakpoint".equals(key)) {
				assertTrue(eventList.get(key).contains("mem:<addr>"));
			} else if ("Software event".equals(key)) {
				assertTrue(eventList.get(key).contains("cpu-clock"));
				assertTrue(eventList.get(key).contains("task-clock"));
			} else if ("Hardware cache event".equals(key)) {
				assertTrue(eventList.get(key).contains("L1-dcache-loads"));
				assertTrue(eventList.get(key).contains("L1-dcache-load-misses"));
			} else if ("Tracepoint event".equals(key)) {
				assertTrue(eventList.get(key).contains("mac80211:drv_return_void"));
				assertTrue(eventList.get(key).contains("mac80211:drv_return_int"));
			} else if ("Hardware event".equals(key)) {
				assertTrue(eventList.get(key).contains("cpu-cycles"));
				assertTrue(eventList.get(key).contains("stalled-cycles-frontend"));
			}
		}
	}

	public void testRecordString() {
		ILaunchConfigurationWorkingCopy tempConfig = null;
		try {
			tempConfig = config.copy("test-config");
			tempConfig.setAttribute(PerfPlugin.ATTR_Record_Realtime, true);
			tempConfig.setAttribute(PerfPlugin.ATTR_Record_Verbose, true);
			tempConfig.setAttribute(PerfPlugin.ATTR_Multiplex, true);

			ArrayList<String> selectedEvents = new ArrayList<String>();
			selectedEvents.add("cpu-cycles");
			selectedEvents.add("cache-misses");
			selectedEvents.add("cpu-clock");
			tempConfig.setAttribute(PerfPlugin.ATTR_SelectedEvents,	selectedEvents);

			tempConfig.setAttribute(PerfPlugin.ATTR_DefaultEvent, false);

		} catch (CoreException e) {
			fail();
		}

		String[] recordString = PerfCore.getRecordString(tempConfig);
		assertNotNull(recordString);

		String[] expectedString = { PerfPlugin.PERF_COMMAND,
				"record",
				"-f",
				"-r",
				"-v",
				"-M",
				"-e",
				"cpu-cycles",
				"-e",
				"cache-misses",
				"-e",
				"cpu-clock" };
		assertTrue(recordString.length == expectedString.length);

		for (int i = 0; i < recordString.length; i++) {
			assertTrue(recordString[i].equals(expectedString[i]));
		}
	}

	public void testReportString(){ILaunchConfigurationWorkingCopy tempConfig = null;
		try {
			tempConfig = config.copy("test-config");
			tempConfig.setAttribute(PerfPlugin.ATTR_Kernel_Location,
					"/boot/kernel");
			tempConfig.setAttribute(PerfPlugin.ATTR_ModuleSymbols, true);
		} catch (CoreException e) {
			fail();
		}

		String[] reportString = PerfCore.getReportString(tempConfig,
				"resources/defaultevent-data/perf.data");
		assertNotNull(reportString);

		String[] expectedString = { PerfPlugin.PERF_COMMAND,
				"report",
				"--sort",
				"comm,dso,sym",
				"-n",
				"-t",
				"" + (char) 1,
				"--vmlinux",
				"/boot/kernel",
				"-m",
				"-i",
				"resources/defaultevent-data/perf.data" };
		assertTrue(reportString.length == expectedString.length);

		for (int i = 0; i < reportString.length; i++) {
			assertTrue(reportString[i].equals(expectedString[i]));
		}

	}

	/**
	 * @param root some element that will serve as the root
	 * @param sum the expected sum of the percentages of this root's
	 * immediate children
	 */
	public void checkChildrenPercentages (TreeParent root, float sum) {
		float actualSum = 0;
		// If a root has no children we're done
		if (root.getChildren().length != 0) {
			for (TreeParent child : root.getChildren()) {
				actualSum += child.getPercent();
				checkChildrenPercentages(child, child.getPercent());
			}
			// some top-level elements have an undefined percentage but
			// their children have defined percentages
			// eg. the invisible root, and PMCommand
			if (actualSum != 100 && sum != -1){
				assertTrue(actualSum/sum <= 1.0 && actualSum/sum >= 0.99);
			}
		}
	}

	/**
	 * @param root some element that will serve as the root
	 * @param stack a stack of classes
	 */
	public void checkChildrenStructure (TreeParent root, Stack<Class<?>> stack){
		if (stack.isEmpty()){
			return;
		}else{
			// children of root must be instances of the top class on the stack
			Class<?> klass = stack.pop();
			for (TreeParent tp : root.getChildren()){
				// tp.getClass() instanceof klass
				assertTrue(klass.isAssignableFrom(tp.getClass()));
				// each sibling needs its own stack
				Stack<Class<?>> newStack = new Stack<Class<?>>();
				newStack.addAll(Arrays.asList(stack.toArray(new Class<?> [] {})));
				checkChildrenStructure(tp, newStack);
			}
		}
	}

	/**
	 * Performs a Perf double-click action on every element in the
	 * TreeViewer model.
	 *
	 * @param root some element that will serve as the root
	 * @param tv a TreeViewer containing elements from the Perf model
	 * @param dblClick the double-click action to perform on every
	 * element of the TreeViewer.
	 */
	private void doubleClickAllChildren(TreeParent root, TreeViewer tv,
			PerfDoubleClickAction dblClick) {

		for (TreeParent child : root.getChildren()) {
			// see PerfDoubleClickAction for IStructuredSelection
			tv.setSelection(new StructuredSelection(child));
			dblClick.run();
			doubleClickAllChildren(child, tv, dblClick);
		}
	}

	/**
	 * Find the number of ancestors of the given root that have children.
	 * This includes the given root in the computation.
	 *
	 * @param root some element that will serve as the root
	 * @return the number of elements under, and including the
	 * given root, that have children elements.
	 */
	private int getNumberOfParents(TreeParent root) {
		int ret = root.hasChildren() ? 1 : 0;
		for (TreeParent child : root.getChildren()) {
			ret += getNumberOfParents(child);
		}
		return ret;
	}

	/**
	 * Build model based on perf data file report.
	 * @param perfDataLoc location of perf data file
	 * @param perfTextDataLoc location of perf data text file
	 * @param perfErrorDataLoc location of error log file
	 * @return tree model based on perf data report.
	 */
	public TreeParent buildModel(String perfDataLoc, String perfTextDataLoc,
			String perfErrorDataLoc) {
		TreeParent invisibleRoot = new TreeParent("");
		BufferedReader input = null;
		BufferedReader error = null;

		try {
			input = new BufferedReader(new FileReader(perfTextDataLoc));
			error = new BufferedReader(new FileReader(perfErrorDataLoc));
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		PerfCore.parseReport(config, null, null, perfDataLoc, null,
				invisibleRoot, false, input, error);
		return invisibleRoot;
	}

	/**
	 * Check whether the command labels in model rooted at cmd exist in
	 * list of labels cmdLabels.
	 * @param cmdLabels list of command labels
	 * @param cmd root of tree model
	 */
	public void checkCommadLabels(String[] cmdLabels, TreeParent cmd) {
		List<String> cmdList = new ArrayList<String>(Arrays.asList(cmdLabels));

		for (TreeParent dso : cmd.getChildren()) {
			assertTrue(cmdList.get(0).equals(dso.getName()));
			cmdList.remove(0);
		}
	}
}
