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

import java.math.BigInteger;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;

/**
 * @author Jean-Christian KouamÃ©
 *
 */
public class AvgMipmapFeature extends TmfMipmapFeature {

    /**
     * The string for the number of levels of the "average" in the mipmap tree.
     * This string can be used to make request to the mipmap tree to get number
     * of levels in the tree for the "average" attribute.
     */
    public static final String NB_AVG_LEVELS = "avgNbLevels"; //$NON-NLS-1$

    /**
     * The string for average. This string can be used to get the average value
     * in the mipmap tree at a specific level
     */
    public static final String AVG_STRING = "avg"; //$NON-NLS-1$

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
    public AvgMipmapFeature(final int quark, final int mipmapResolution, final ITmfStateSystemBuilder ss, final Type type, final long timestamp) {
        super(quark, mipmapResolution, ss, type, timestamp);
    }

    @Override
    public String getMimapNbLevelString() {
        return NB_AVG_LEVELS;
    }

    @Override
    public void updateAndCloseFeature() {
        int currentLevelAttribute;
        int nextLevelAttribute;
        for (int i = 0; i < getFeatureQuarks().size() - 1; i++) {
            currentLevelAttribute = getFeatureQuarks().get(i + 1);
            try {
                BigInteger average;
                int level = getFeatureQuarks().indexOf(currentLevelAttribute);
                /* update the sum in the upper level */
                if (level + 2 <= getFeatureQuarks().size() - 1) {
                    nextLevelAttribute = getFeatureQuarks().get(level + 2);
                    average = getBigIntegerValueOf(ss.queryOngoingState(nextLevelAttribute).add(ss.queryOngoingState(currentLevelAttribute)));
                    ss.updateOngoingState(getNewStateValue(average.longValue()), nextLevelAttribute);
                }
                /* update the average */
                average = BigInteger.valueOf(((getCounter() + 1) % getCheckpoint(level + 1)));
                if (average.equals(BigInteger.valueOf(0L))) {
                    average = BigInteger.valueOf(getCheckpoint(level + 1));
                }
                average = getBigIntegerValueOf(ss.queryOngoingState(currentLevelAttribute)).divide(average);
                ss.updateOngoingState(getNewStateValue(average.longValue()), currentLevelAttribute);
            } catch (AttributeNotFoundException e) {
            } catch (StateValueTypeException e) {
            }
        }
    }

    @Override
    public void updateFeature(int currentLevelAttribute, int nextLevelAttribute) {
        try {
            if (getFeatureQuarks().indexOf(currentLevelAttribute) > 0) {
                BigInteger average;
                average = getBigIntegerValueOf(ss.queryOngoingState(currentLevelAttribute).add(ss.queryOngoingState(nextLevelAttribute)));
                /* update the sum in the upper level */
                ss.updateOngoingState(getNewStateValue(average.longValue()), nextLevelAttribute);
                /* update the average of the current level */
                average = getBigIntegerValueOf(ss.queryOngoingState(currentLevelAttribute)).
                        divide(BigInteger.valueOf(getCheckpoint(getFeatureQuarks().indexOf(currentLevelAttribute))));
                ss.updateOngoingState(getNewStateValue(average.longValue()), currentLevelAttribute);
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
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
                            ITmfStateValue sv = ss.queryOngoingState(attributeQuark).add(value);
                            ss.updateOngoingState(sv, attributeQuark);
                        }
                    } else {
                        updateLevel(quark, i + 1, AVG_STRING);
                        int attributeQuark = getFeatureQuarks().get(i + 1);
                        if (i == 0) {
                            ss.modifyAttribute(ts, value, attributeQuark);
                        } else {
                            ss.modifyAttribute(ts, getNewStateValue(0L), attributeQuark);
                        }
                    }
                }
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        } catch (TimeRangeException e) {
        }
    }

    @Override
    protected void updateLevel(int quark, int level, String mipmapFeatureString) {
        /*
         * we don't want to update the upper levels when we have only received
         * the first event for this index
         */
        if (getCounter() == 0) {
            return;
        }
        /*
         * if the level doesn't exist, add a checkpoint for this level then
         * update the level
         */
        try {
            int currentLevelAttribute = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(), mipmapFeatureString + String.valueOf(level));
            int nextLevelAttribute = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(), mipmapFeatureString + String.valueOf(level + 1));

            if (!checkLevelExist(level)) {
                /*
                 * HACK : Create a new interval with a fake value in order to
                 * initialize the first interval at the timestamp where the
                 * attribute has been received.
                 */
                getFeatureQuarks().add(nextLevelAttribute);
                ss.modifyAttribute(getTimestamp(), getNewStateValue(0L), nextLevelAttribute);

                int attributeQuark = ss.getQuarkRelative(quark, getMimapNbLevelString());
                ss.updateOngoingState(ss.queryOngoingState(attributeQuark).increment(), attributeQuark);
            }

            /* update sub-Attribute of the new level */
            updateFeature(currentLevelAttribute, nextLevelAttribute);
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        } catch (TimeRangeException e) {
        }
    }
}
