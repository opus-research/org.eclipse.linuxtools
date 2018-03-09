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

package org.eclipse.linuxtools.systemtap.graphingapi.core.tests.filters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.graphingapi.core.aggregates.MaxAggregate;
import org.eclipse.linuxtools.systemtap.graphingapi.core.aggregates.SumAggregate;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.UniqueFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.tests.MockDataSet;
import org.eclipse.ui.XMLMemento;
import org.junit.Before;
import org.junit.Test;

public class UniqueFilterTest  {
	@Before
	public void setUp() {
		filter = new UniqueFilter(0, new SumAggregate(), 0);
	}
	@Test
	public void testUniqueFilter() {
		filter = new UniqueFilter(-1, new MaxAggregate(), 0);
		assertNotNull(filter);
	}
	@Test
	public void testFilter() {
		int width = 4;
		int height = 10;
		int wrap = height / 3;
		ArrayList<Object>[] data = MockDataSet.buildArray(width, height, wrap);

		assertEquals(width, data.length);
		assertEquals(height, data[0].size());

		ArrayList<Object>[] data2 = filter.filter(data);
		
		assertEquals(width, data.length);
		assertEquals(height, data[0].size());
		assertEquals(width, data2.length);
		assertEquals(wrap, data2[0].size());
		assertEquals("1", data2[0].get(0));
		assertEquals("2", data2[0].get(1));
		assertEquals("0", data2[0].get(2));

		assertEquals(0, ((Number)data2[2].get(0)).intValue());
		assertEquals(3, ((Number)data2[2].get(1)).intValue());
		assertEquals(8, ((Number)data2[2].get(2)).intValue());
		
		filter = new UniqueFilter(-1, new SumAggregate(), 0);
		assertNull(filter.filter(null));

		data = MockDataSet.createArrayList(2, new Object());
		data[0] = new ArrayList<Object>();
		data[1] = new ArrayList<Object>();
		
		data[0].add("a");
		data[0].add("a");
		data[1].add("b");
		data[1].add("c");
		filter = new UniqueFilter(0, new SumAggregate(), 0);
		assertNotNull(filter.filter(data));
	}
	@Test
	public void testGetID() {
		assertTrue(UniqueFilter.ID.equals(filter.getID()));
	}
	@Test
	public void testWriteXML() {
		filter.writeXML(XMLMemento.createWriteRoot("test"));
	}
	
	UniqueFilter filter;
}
