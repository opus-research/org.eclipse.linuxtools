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
package org.eclipse.linuxtools.internal.perf.model;

import java.io.StreamCorruptedException;
import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * Representation of a single entry in a perf stat report.
 */
public class PMStatEntry {
	private float occurrence;
	private String event;
	private float metrics;
	private String units;
	private float deviation;
	private float scaling;

	public PMStatEntry(float occurrence, String event, float metrics,
			String units, float deviation, float scaling) {
		this.occurrence = occurrence;
		this.event = event;
		this.metrics = metrics;
		this.units = units;
		this.deviation = deviation;
		this.scaling = scaling;
	}

	public float getOccurrence() {
		return occurrence;
	}

	public String getEvent() {
		return (event == null) ? "" : event; //$NON-NLS-1$
	}


	public float getMetrics() {
		return metrics;
	}

	public String getUnits() {
		return (units == null) ? "" : units; //$NON-NLS-1$
	}

	public float getDeviation() {
		return deviation;
	}


	public float getScaling() {
		return scaling;
	}

	/**
	 * Check if PMStatEntry refer to the same event.
	 * @param entry PMStatEntry to check against
	 * @return true if events are equals, false otherwise.
	 */
	public boolean equals(PMStatEntry entry) {
		String event = entry.getEvent();
		if (this.event != null && event != null) {
			return this.event.equals(event);
		}
		return false;
	}

	/**
	 * Compare this PMStatEntry with the specified one, and return an object
	 * with the result.
	 *
	 * @param entry PMStatEntry to compare against
	 * @return a PMStatEntry representing the resulting comparison.
	 */
	public PMStatEntry compare(PMStatEntry entry){
		float occurrenceDiff = entry.getOccurrence() - this.occurrence;
		float metricsDiff = entry.getMetrics() - this.metrics;
		float deviationDiff = entry.getDeviation() + this.deviation;
		float scalingDiff = entry.getScaling() + this.scaling;

		return new PMStatEntry(occurrenceDiff, event, metricsDiff, units, deviationDiff, scalingDiff);
	}

	public String formattedFields(String format) {
		Object[] fields = new Object[] { occurrence, event, metrics, deviation };
		return String.format(format, fields);
	}

	public String[] toStringArray(){
		return new String[] { String.valueOf(occurrence),
				getEvent().trim(),
				String.valueOf(metrics),
				getUnits().trim(),
				String.valueOf(deviation) };
	}

}
