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

package org.eclipse.linuxtools.systemtap.graphingapi.core.structures;

public class GraphData {
	public int xSeries;
	public int[] ySeries;
	public String key;
	public String graphID;
	public String title;

	/**
	 * Creates and returns a copy of this GraphData instance.
	 * @return A copy of this GraphData.
	 */
	public GraphData getCopy() {
		GraphData gd = new GraphData();
		gd.xSeries = this.xSeries;
		gd.ySeries = this.ySeries == null ? null : this.ySeries.clone();
		gd.key = this.key == null ? null : this.key.substring(0);
		gd.graphID = this.graphID == null ? null : this.graphID.substring(0);
		gd.title = this.title == null ? null : this.title.substring(0);
		return gd;
	}

	/**
	 * Indicates whether all properties of this GraphData are the same as another GraphData.
	 * @param gd The GraphData to compare with.
	 * @return <code>true</code> if all properties are equal, or <code>false</code> otherwise.
	 */
	public boolean equals(GraphData gd) {
		try {
			if (gd.ySeries.length != this.ySeries.length) {
				return false;
			}
		} catch (Exception e) {
			if (gd.ySeries != this.ySeries) { //If both null, continue checking.
				return false;
			}
		}
		for (int i = 0; i < this.ySeries.length; i++) {
			if (gd.ySeries[i] != this.ySeries[i]) {
				return false;
			}
		}

		try {
			if (!gd.key.equals(this.key)) {
				return false;
			}
		} catch (Exception e) {
			if (gd.key != this.key) {
				return false;
			}
		}

		try {
			if (!gd.graphID.equals(this.graphID)) {
				return false;
			}
		} catch (Exception e) {
			if (gd.graphID != this.graphID) {
				return false;
			}
		}

		try {
			if (!gd.title.equals(this.title)) {
				return false;
			}
		} catch (Exception e) {
			if (gd.title != this.title) {
				return false;
			}
		}

		return gd.xSeries == this.xSeries;
	}
}
