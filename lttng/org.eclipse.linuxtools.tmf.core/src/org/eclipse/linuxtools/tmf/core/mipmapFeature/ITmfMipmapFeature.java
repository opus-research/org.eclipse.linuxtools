/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian KouamÃ© - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.mipmapFeature;

import java.util.List;

import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

/**
 * @author Jean-Christian KouamÃ©
 *
 */
public interface ITmfMipmapFeature {

    /**
     * @return The timestamp where the concerned event is received for the first
     *         time
     */
    public long getTimestamp();

    /**
     * @return The value of the counter of state changes
     */
    public int getCounter();

    /**
     * @return The mipmap resolution for this feature
     */
    public int getMipmapResolution();

    /**
     * @return The list of existing quarks for this feature. This list can be
     *         use to know the depth of the mipmap
     */
    public List<Integer> getFeatureQuarks();

    /**
     * @return The string for the "numer of level" node for this feature
     */
    public String getMimapNbLevelString();

    /**
     * @param val
     *            The value to contain
     * @return a new state value whose val
     */
    public ITmfStateValue getNewStateValue(final long val);

    /**
     * @param counter
     *            The new value for the counter of state changes
     */
    public void setCounter(int counter);

    /**
     * @param mipmapResolution
     *            The resolution to give at this feature
     */
    public void setMipmapResolution(int mipmapResolution);

    /**
     * Update the values at all the level of the mipmap for this feature before
     * closing
     */
    public void updateAndCloseFeature();

    /**
     * Update the value of the feature for the next level
     *
     * @param currentLevelAttribute
     *            The quark of the current level
     * @param nextLevelAttribute
     *            The quark of the next level
     */
    public void updateFeature(int currentLevelAttribute, int nextLevelAttribute);

    /**
     * @param quark
     *            The quark of the attribute to mipmap
     * @param value
     *            The value to compute
     * @param ts
     *            The timestamp of the concerned event
     */
    public void updateFeatureLevels(int quark, ITmfStateValue value, long ts);

    /**
     * @param level
     *            The level to check
     * @return True if this level exist False otherwise
     */
    public boolean checkLevelExist(int level);
}
