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

	// Name of matched symbols
	private String name;

	// Result
	private String result;

	// Event
	private String event;

	// Reference to newer dso
	private PMSymbol fresh;

	// Reference to older dso
	private PMSymbol stale;

	//TODO: Boolean??
	public static final  String SYMBOL_ADDED = "added";
	public static final String SYMBOL_REMOVED = "removed";
	public static final String SYMBOL_MATCHED = "matched";

	public PMSymbolMatch(PMSymbol freshPMSymbol, PMSymbol stalePMSymbol) {
		fresh = freshPMSymbol;
		stale = stalePMSymbol;
		setMatchInformation();
	}

	public String getResult(){
		return result;
	}

	public String getName() {
		return name;
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
	 * Set the information of this pair of matched PMSymbols:
	 *     - Name of PMSymbol this pair is associated to.
	 *     - Result of match comparison.
	 *     - Event this PMSymbol belongs to.
	 */
	public void setMatchInformation(){
		// if no stale PMSymbol given in this match, fresh is a newly added
		// PMSymbol to the event stale belonged to
		if ((fresh != null && stale == null)) {
			name = fresh.getName();
			result = SYMBOL_ADDED;
			setEvent(true);
		// if no fresh PMSymbol given, stale PMSymbol has been removed in the most recent run.
		} else if ((fresh == null && stale != null)) {
			name = stale.getName();
			result = SYMBOL_REMOVED;
			setEvent(false);
		// if both given, fresh or stale have the same PMEvent ancestor
		} else if (((fresh != null) && (stale != null))) {
			name = fresh.getName();
			result = SYMBOL_MATCHED;
			//result = String.valueOf(getOverheadDifference());
			setEvent(true);
		}
	}

	/**
	 * Set the name of the event that this PMSymbolMatch belongs to.
	 * @param freshEvent true if extracting the event name from the newer PMSymbol and
	 * false for the older PMSymbol.
	 */
	public void setEvent(boolean freshEvent) {
		TreeParent curElem = freshEvent ? fresh : stale;
		setEvent(curElem);
	}

	public void setEvent(TreeParent elem) {
		if (elem != null) {
			if (elem instanceof PMEvent) {
				event = elem.getName();
			} else {
				setEvent(elem.getParent());
			}
		}
	}

	/**
	 * Compare stale PMSymbol objects of a PMSymbolMatch pair
	 *
	 * @param p PMSymbols match
	 * @return int comparison of percentages if stale symbols exist in both
	 *  PMSymbolMatch objects,otherwise return -1 if stale PMSymbol does not
	 *  exist in the given PMSymbolMatch, 1 if vice versa and 0 if no stale
	 *  PMSymbol in either PMSymbolMatch objects.
	 */
	public int compareStaleSymbol(PMSymbolMatch p){
		return PMSymbol.comparePercentages(stale , p.getStale());
	}

	/**
	 * Compare fresh PMSymbol objects of a PMSymbolMatch pair
	 *
	 * @param p PMSymbols match
	 * @return int comparison of percentages if fresh symbols exist in both
	 *  PMSymbolMatch objects,otherwise return -1 if fresh PMSymbol does not
	 *  exist in the given PMSymbolMatch, 1 if vice versa and 0 if no fresh
	 *  PMSymbol in either PMSymbolMatch objects.
	 */
	public int compareFreshSymbol(PMSymbolMatch p){
		return PMSymbol.comparePercentages(fresh, p.getFresh());
	}

	public int compareResult(PMSymbolMatch p){
		int ret = 0;
		String res1 = result;
		String res2 = p.getResult();

		if ((res1 != null && res1.equals(SYMBOL_MATCHED))
				&& (res2 != null && res2.equals(SYMBOL_MATCHED))) {

			ret = (getOverheadDifference() < p.getOverheadDifference()) ? -1 : 1;
		} else{
			ret = res1.compareTo(res2);
		}

		return ret;
	}

	/**
	 * Get overhead difference of this PMSymbol matched pair.
	 * @return
	 */
	public float getOverheadDifference(){
		//TODO: Check nullity.
		return fresh.getPercent() - stale.getPercent();
	}
}