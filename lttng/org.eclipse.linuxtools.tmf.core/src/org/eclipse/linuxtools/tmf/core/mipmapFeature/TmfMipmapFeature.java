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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;

/**
 * @author Jean-christian KouamÃ©
 *
 */
public class TmfMipmapFeature implements ITmfMipmapFeature {

    private int counter;
    private int mipmapResolution;
    private List<Integer> featureQuarks = new ArrayList<Integer>();
    private Type type;
    private long timestamp;

    /**
     *
     */
    protected ITmfStateSystemBuilder ss;

    /**
     * @param mipmapResolution
     */
    TmfMipmapFeature(int quark, int mipmapResolution, ITmfStateSystemBuilder ss, Type type, long timestamp) {
        this.counter = -1;
        this.mipmapResolution = mipmapResolution;
        featureQuarks.add(quark);
        this.ss = ss;
        this.type = type;
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public int getMipmapResolution() {
        return mipmapResolution;
    }

    @Override
    public List<Integer> getFeatureQuarks() {
        return featureQuarks;
    }

    @Override
    public String getMimapNbLevelString() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public void setCounter(int counter) {
        this.counter = counter;
    }

    @Override
    public void setMipmapResolution(int mipmapResolution) {
        this.mipmapResolution = mipmapResolution;
    }

    @Override
    public void updateAndCloseFeature() {
    }

    @Override
    public void updateFeature(int currentLevelAttribute, int nextLevelAttribute) {
    }

    @Override
    public void updateFeatureLevels(int quark, ITmfStateValue value, long ts) {
    }

    @Override
    public boolean checkLevelExist(int level) {
        if (level > featureQuarks.size() - 2 || level < 0) {
            return false;
        }
        return true;
    }

    /**
     * @param level
     *            The level where we seek checkpoint
     * @return The checkpoint for this level
     */
    protected int getCheckpoint(final int level) {
        int toReturn = 1;
        for (int i = 0; i < level; i++) {
            toReturn *= mipmapResolution;
        }
        return toReturn;
    }

    @Override
    public ITmfStateValue getNewStateValue(final long val) {
        if (type == Type.INTEGER) {
            return TmfStateValue.newValueInt((int) val);
        } else if (type == Type.LONG) {
            return TmfStateValue.newValueLong(val);
        }
        return TmfStateValue.nullValue();
    }

    /**
     * @param sv
     *            The state value to convert
     * @return A bigInteger which value is equal to sv.unbox()
     */
    protected BigInteger getBigIntegerValueOf(final ITmfStateValue sv) {
        try {
            if (type == Type.INTEGER) {
                return BigInteger.valueOf(sv.unboxInt());
            } else if (type == Type.LONG) {
                return BigInteger.valueOf(sv.unboxLong());
            }
        } catch (StateValueTypeException e) {
        }
        return null;
    }

    /**
     * @param quark
     *            The quark of the attribute to mipmap
     * @param level
     *            The level to update
     * @param mipmapFeatureString
     *            The string that represent the mipmap feature
     */
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
                featureQuarks.add(nextLevelAttribute);
                ss.modifyAttribute(timestamp, ss.queryOngoingState(currentLevelAttribute), nextLevelAttribute);

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
