/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.tests;

import java.util.Arrays;

import org.eclipse.linuxtools.internal.perf.model.PMStatEntry;

import junit.framework.TestCase;

public class StatsComparisonTest extends TestCase {
	PMStatEntry statEntry;
	PMStatEntry statEntry2;
	PMStatEntry statEntry3;
	PMStatEntry statEntry4;

	@Override
	protected void setUp() {
		String event = "event";
		String units = "unit";
		float samples = 1;
		float metrics = 2;
		float deviation = 3;
		float scaling = 4;

		statEntry = new PMStatEntry(samples, event, metrics, units, deviation,
				scaling);
		statEntry2 = new PMStatEntry(samples, event, metrics, units, deviation,
				scaling);
		statEntry3 = new PMStatEntry(samples++, event, metrics++, units,
				deviation++, scaling);
		statEntry4 = new PMStatEntry(samples--, "event2", metrics--, units,
				deviation--, scaling);
	}

	public void testPMStatEntryGetters() {
		assertEquals("event", statEntry.getEvent());
		assertEquals("unit", statEntry.getUnits());
		assertEquals((float)1, statEntry.getSamples());
		assertEquals((float)2, statEntry.getMetrics());
		assertEquals((float)3, statEntry.getDeviation());
		assertEquals((float)4, statEntry.getScaling());
	}

	public void testPMStatEntryEquality() {
		assertTrue(statEntry.equalEvents(statEntry3));
		assertFalse(statEntry.equalEvents(statEntry4));
		assertTrue(statEntry.equals(statEntry2));
	}

	public void testPMStatEntryArray() {
		String[] expectedList = new String[] {
				String.valueOf(statEntry.getSamples()), statEntry.getEvent(),
				String.valueOf(statEntry.getMetrics()), statEntry.getUnits(),
				String.valueOf(statEntry.getDeviation()) };

		String[] actualList = statEntry.toStringArray();

		// test string array representation
		assertTrue(Arrays.equals(expectedList, actualList));
	}

	public void testPMStatEntryComparison() {
		String expectedEvent = "event";
		String expectedUnits = "unit";
		float expectedSamples = statEntry.getSamples() - statEntry2.getSamples();
		float expectedMetrics = statEntry.getMetrics() - statEntry2.getMetrics();
		float expectedDeviation = statEntry.getDeviation() + statEntry2.getDeviation();
		float expectedScaling = statEntry.getScaling() + statEntry2.getScaling();

		PMStatEntry expectedDiff = new PMStatEntry(expectedSamples,
				expectedEvent, expectedMetrics, expectedUnits,
				expectedDeviation, expectedScaling);

		PMStatEntry actualDiff = statEntry.compare(statEntry2);

		// test stat entry comparison
		assertTrue(expectedDiff.equals(actualDiff));

	}

	public void testStatDataCollection() {
		// TODO: Test stat data comparison functionality.

	}
}
