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

package org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.aggregates;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.aggregates.CountAggregate;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.tests.MockDataSet;

import junit.framework.TestCase;

public class CountAggregateTest extends TestCase {
	public CountAggregateTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAggregate() {
		CountAggregate aa = new CountAggregate();
		Number num;

		num = aa.aggregate(null);
		assertNull(num);

		num = aa.aggregate(new Number[] {});
		assertNull(num);

		num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {0,0,0}));
		assertEquals(3, num.intValue());
		
		num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {-1,0}));
		assertEquals(2, num.intValue());

		num = aa.aggregate(MockDataSet.buildIntegerArray(new int[] {0,0,1,2,3}));
		assertEquals(5, num.intValue());


		num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {0,0,0}));
		assertEquals(3, num.doubleValue(), 0.0);
		
		num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {-1,1}));
		assertEquals(2, num.doubleValue(), 0.0);

		num = aa.aggregate(MockDataSet.buildDoubleArray(new double[] {0,0,1,4,5}));
		assertEquals(5, num.doubleValue(), 0.0);
	}
	
	public void testGetID() {
		CountAggregate aa = new CountAggregate();
		assertTrue(CountAggregate.ID.equals(aa.getID()));
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
