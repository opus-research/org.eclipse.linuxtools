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

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;

/**
 * @author @author Jean-Christian KouamÃ©
 *
 */
public class MinMipmapFeature extends TmfMipmapFeature{

    /**
     * The string for the number of levels of the "minimum" in the mipmap tree.
     * This string can be used to make request to the mipmap tree to get number
     * of levels in the tree for the "minimum" attribute.
     */
    public static final String NB_MIN_LEVELS = "minNbLevels"; //$NON-NLS-1$

    /**
     * The string for minimum. This string can be used to get the minimum value
     * in the mipmap tree at a specific level
     */
    public static final String MIN_STRING = "min"; //$NON-NLS-1$

    /**
     * @param quark
     *            The quark for the attribute we want to mipmap
     * @param mipmapResolution
     *            The resolution that will be use in the mipmap
     * @param ss
     *            The state system in which to insert the state changes
     * @param type
     *            The type of the state values
     * @param timestamp
     *            the timestamp when we receive the attribute
     */
    public MinMipmapFeature(final int quark, final int mipmapResolution, final ITmfStateSystemBuilder ss, final Type type, final long timestamp) {
        super(quark, mipmapResolution, ss, type, timestamp);
    }

    @Override
    public String getMimapNbLevelString() {
        return NB_MIN_LEVELS;
    }

    @Override
    public void updateAndCloseFeature() {
        int currentLevelAttribute;
        int nextLevelAttribute;
        for (int i = 0; i < getFeatureQuarks().size() - 1; i++) {
            nextLevelAttribute = getFeatureQuarks().get(i + 1);
            currentLevelAttribute = getFeatureQuarks().get(i);
            updateFeature(currentLevelAttribute, nextLevelAttribute);
        }
    }

    @Override
    public void updateFeature(int currentLevelAttribute, int nextLevelAttribute) {
        try {
            if (ss.queryOngoingState(nextLevelAttribute).compareTo(ss.queryOngoingState(currentLevelAttribute)) == 1) {
                ss.updateOngoingState(ss.queryOngoingState(currentLevelAttribute), nextLevelAttribute);
            }
        } catch (AttributeNotFoundException e) {
        }
    }

    @Override
    public void updateFeatureLevels(int quark, ITmfStateValue value, long ts) {
        try {
            int numberOfLevel = getFeatureQuarks().size() - 1;
            for (int i = 0; i < numberOfLevel; i++) {
                /* update the upper level */
                if (getCounter() % getCheckpoint(i) == 0) {
                    if (getCounter() % getCheckpoint(i + 1) != 0) {
                        if (i == 0) {
                            /* update values at the first level */
                            int attributeQuark = getFeatureQuarks().get(i + 1);
                            if (ss.queryOngoingState(attributeQuark).compareTo(value) == 1) {
                                ss.updateOngoingState(value, attributeQuark);
                            }
                        }
                    } else {
                        updateLevel(quark, i + 1, MIN_STRING);
                        int attributeQuark = getFeatureQuarks().get(i + 1);
                        ss.modifyAttribute(ts, value, attributeQuark);
                    }
                }
            }
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        } catch (StateValueTypeException e) {
        }
    }
}
