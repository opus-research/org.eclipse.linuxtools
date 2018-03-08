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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.adapters.BlockAdapterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.adapters.ScrollAdapterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.AverageAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.CountAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.MaxAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.MinAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates.SumAggregateTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row.FilteredRowDataSetTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row.RowDataSetTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row.RowEntryTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.row.RowParserTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.table.FilteredTableDataSetTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.table.TableDataSetTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.table.TableEntryTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.datasets.table.TableParserTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.filters.MatchFilterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.filters.RangeFilterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.filters.SortFilterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.filters.UniqueFilterTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.ChartStreamDaemonTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.DataPointTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.GraphDataTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.NumberTypeTest;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.structures.UpdateManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({// Adapters
		BlockAdapterTest.class,
		ScrollAdapterTest.class,

		// Aggregates
		AverageAggregateTest.class,
		CountAggregateTest.class,
		MaxAggregateTest.class,
		MinAggregateTest.class,
		SumAggregateTest.class,

		// DataSets

		// DataSets.Row
		FilteredRowDataSetTest.class,
		RowDataSetTest.class,
		RowEntryTest.class,
		RowParserTest.class,

		// DataSets.Table
		FilteredTableDataSetTest.class, TableDataSetTest.class,
		TableEntryTest.class,
		TableParserTest.class,

		// Filters
		MatchFilterTest.class, RangeFilterTest.class, SortFilterTest.class,
		UniqueFilterTest.class,

		// Structures
		ChartStreamDaemonTest.class,
		// ChartStreamDaemonTest2.class,
		DataPointTest.class, GraphDataTest.class, NumberTypeTest.class,
		UpdateManagerTest.class })
public class AllTests {
}
