/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Camilo Bernal <cabernal@redhat.com> - Initial Implementation.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.model;

public class PMSymbolMatch {

	// Name of matched dsos
	private String name;

	// Path of matched dsos in the tree model
	private String path;

	// Reference to newer dso
	private PMSymbol fresh;

	// Reference to older dso
	private PMSymbol stale;

	// Result
	private String result;

	// Event
	private String event;

	public PMSymbolMatch(PMSymbol fresh, PMSymbol stale) {
		this.fresh = fresh;
		this.stale = stale;

		if ((fresh != null && stale == null)) {
			name = fresh.getName();
			result = "added";
			setEvent(true);
		} else if ((fresh == null && stale != null)) {
			name = stale.getName();
			result = "removed";
			setEvent(false);
		} else if (((fresh != null) && (stale != null))) {
			name = fresh.getName();
			this.result = String.valueOf(fresh.getPercent()
					- stale.getPercent());
			setEvent(true);
		} else {
			name = "";
			path = "";
			event = "";
			result = "";
		}
	}

	public void setEvent(boolean freshEvent) {
		try {
			if (freshEvent) {
				event = fresh.getParent().getParent().getParent().getParent()
						.getName();
			} else {
				event = stale.getParent().getParent().getParent().getParent()
						.getName();
			}
		} catch (Exception e) {
			e.printStackTrace();
			event = "";
		}
	}

	public String getResult() {
		try {
			float res = Float.parseFloat(result);
			if (res > 0) {
				return "+" + result;
			} else {
				return result;
			}
		} catch (Exception e) {
			return result;
		}
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public PMSymbol getFresh() {
		return fresh;
	}

	public PMSymbol getStale() {
		return stale;
	}

	public String getEvent() {
		return event;
	}

	/**
	 * Compare PMSymbols percentages against our pair
	 * 
	 * @param p
	 *            PMSymbols match
	 * @param compFresh
	 *            true if fresh symbols are to be compared, false otherwise
	 * @return comparison of percentages
	 */
	public int compareSymbol(PMSymbolMatch p, boolean compFresh) {
		PMSymbol thisSym;
		PMSymbol otherSym;
		int ret = 0;
		if (compFresh) {
			thisSym = fresh;
			otherSym = p.getFresh();
		} else {
			thisSym = stale;
			otherSym = p.getStale();
		}

		if (thisSym == null && otherSym != null) {
			ret = -1;
		} else if (thisSym != null && otherSym == null) {
			ret = 1;
		} else if (thisSym == null && otherSym == null) {
			ret = 0;
		} else {
			ret = (thisSym.getPercent() < otherSym.getPercent()) ? -1 : 1;
		}
		return ret;
	}

	/**
	 * Compare result of given PMSymbols against our pair
	 * 
	 * @param p
	 *            PMSymbols match
	 * @return comparison of results
	 */
	public int compareResult(PMSymbolMatch p) {
		int ret = 0;
		String oldRes1 = result;
		String oldRes2 = p.getResult();
		try {
			float oldRes1f = Float.parseFloat(oldRes1);
			float oldRes2f = Float.parseFloat(oldRes2);
			ret = (oldRes1f < oldRes2f) ? -1 : 1;
		} catch (NumberFormatException e) {
			ret = oldRes1.compareTo(oldRes2);
		}
		return ret;
	}
}