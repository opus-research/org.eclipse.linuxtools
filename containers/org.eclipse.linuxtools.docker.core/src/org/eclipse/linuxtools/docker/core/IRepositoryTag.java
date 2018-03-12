/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.docker.core;

/**
 * A tag (or version) for a given repository.
 */
public interface IRepositoryTag extends Comparable<IRepositoryTag> {

	/**
	 * @return Name of the tag.
	 */
	String getName();

	/**
	 * @return The corresponding image layer for this specific tag
	 */
	String getLayer();

	/**
	 * Compares by the tag 'name' in reverse order.
	 */
	@Override
	default int compareTo(final IRepositoryTag other) {
		// tries to compare versions in the x.y.z.qualifer format, otherwise,
		// just do a lexicographical comparison
		try {
			final String[] thisParts = this.getName().split("\\.");
			final String[] thatParts = other.getName().split("\\.");
			int length = Math.max(thisParts.length, thatParts.length);
			for (int i = 0; i < length; i++) {
				int thisPart = i < thisParts.length
						? Integer.parseInt(thisParts[i]) : 0;
				int thatPart = i < thatParts.length
						? Integer.parseInt(thatParts[i]) : 0;
				if (thisPart < thatPart) {
					return 1;
				}
				if (thisPart > thatPart) {
					return -1;
				}
			}
			return 0;
		} catch (NumberFormatException e) {
			// if one of the name was not a valid version, just do this:
			return other.getName().compareTo(this.getName());
		}
	}
}
