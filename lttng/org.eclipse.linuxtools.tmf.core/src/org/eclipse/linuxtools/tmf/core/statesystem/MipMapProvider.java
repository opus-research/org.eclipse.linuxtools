/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jean-Christian Kouamé - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * This is an abstract provider that allows to realize mip mapping in a general
 * way
 *
 * Extend this one for a specific class
 *
 * @since 3.0
 */
public abstract class MipMapProvider extends AbstractTmfStateProvider {

    /**
     * The string for mipmap. By using this string, we can be make request to
     * access the mipmap node of a state system.
     */
    public static final String MIPMAP = "Mipmap"; //$NON-NLS-1$

    /**
     * The string for the number of levels of the "maximum" in the mipmap tree.
     * This string can be used to make request to the mipmap tree to get number
     * of levels in the tree for the "maximum" attribute.
     */
    public static final String NB_MAX_LEVELS = "maxNbLevels"; //$NON-NLS-1$

    /**
     * The string for the number of levels of the "minimum" in the mipmap tree.
     * This string can be used to make request to the mipmap tree to get number
     * of levels in the tree for the "minimum" attribute.
     */
    public static final String NB_MIN_LEVELS = "minNbLevels"; //$NON-NLS-1$

    /**
     * The string for the number of levels of the "average" in the mipmap tree.
     * This string can be used to make request to the mipmap tree to get number
     * of levels in the tree for the "average" attribute.
     */
    public static final String NB_AVG_LEVELS = "avgNbLevels"; //$NON-NLS-1$

    /**
     * The string for minimum. This string can be used to get the minimum value
     * in the mipmap tree at a specific level
     */
    public static final String MIN_STRING = "min"; //$NON-NLS-1$

    /**
     * The string for maximum. This string can be used to get the maximum value
     * in the mipmap tree at a specific level
     */
    public static final String MAX_STRING = "max"; //$NON-NLS-1$

    /**
     * The string for average. This string can be used to get the average value
     * in the mipmap tree at a specific level
     */
    public static final String AVG_STRING = "avg"; //$NON-NLS-1$

    Map<Integer, AttributeInfos> attributes = new HashMap<Integer, AttributeInfos>();
    private boolean computeManually;

    // ------------------------------------------------------------------------
    // public methods
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param trace
     *            The trace directory
     * @param eventType
     *            The specific class for the event type
     * @param id
     *            The name given to this state change input. Only used
     *            internally.
     * @param computeManually
     *            This determines if we want to update the mipmap levels
     *            automatically when we receive an event or not. It should be
     *            false when we want to update the levels automatically and true
     *            when we want it to be done manually
     */
    public MipMapProvider(ITmfTrace trace, Class<? extends ITmfEvent> eventType, String id, boolean computeManually) {
        super(trace, eventType, id);
        this.computeManually = computeManually;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // public methods
    // ------------------------------------------------------------------------
    /**
     * @param ts
     *            The timestamp of the event
     * @param value
     *            The value of the base attribute
     * @param quark
     *            The quark of the base attribute
     * @param mipmapAttribute
     *            The mipmap attribute we want to compute
     * @param mipmapResolution
     *            The resolution to use for this attribute
     */
    public void computeMipmapAttribute(long ts, ITmfStateValue value, int quark, String mipmapAttribute, int mipmapResolution) {
        if (mipmapResolution < 2) {
            return;
        }
        if (value.getType() == Type.LONG || value.getType() == Type.INTEGER) {
            checkAndAddNewEntry(quark, ts, value.getType(), value, mipmapAttribute, mipmapResolution);
            updateCounter(quark, mipmapAttribute);
            if (!computeManually) {
                updateLevels(quark, value, ts, mipmapAttribute);
            }
        }
    }

    /**
     * @param mipmapAttribute
     *            The mipmap attribute that we are computing.
     * @return The string of the "number of levels" node in the tree for this
     *         attribute.
     */
    public static String getMimapNbLevelString(String mipmapAttribute) {
        if (mipmapAttribute.equals(MAX_STRING)) {
            return NB_MAX_LEVELS;
        } else if (mipmapAttribute.equals(MIN_STRING)) {
            return NB_MIN_LEVELS;
        } else if (mipmapAttribute.equals(AVG_STRING)) {
            return NB_AVG_LEVELS;
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * This function is needed to update manually the mipmap and its levels from
     * an external class
     *
     * @param ts
     *            The timestamp of the event
     * @param value
     *            The new value of the corresponding attribute
     * @param quark
     *            The quark of the base attribute of the mipmap
     * @param mipmapAttribute
     *            The mipmap attribute to compute
     */
    public void updateMipMap(long ts, ITmfStateValue value, int quark, String mipmapAttribute) {
        if (computeManually) {
            try {
                ss.updateOngoingState(value, quark);
                updateLevels(quark, value, ts, mipmapAttribute);
            } catch (AttributeNotFoundException e) {
            }
        }
    }

    /**
     * Close the ongoing mipmap state attribute
     */
    public void closeMipMap() {
        waitForEmptyQueue();
        /*
         * for each attribute that has been received, it updates the minimum,
         * maximum and average at each level of the mipmap tree
         */
        int nextLevelAttribute, currentLevelAttribute;
        for (Integer quark : attributes.keySet()) {
            AttributeInfos ai = attributes.get(quark);
            for (int i = 0; i < ai.maxQuarks.size() - 1; i++) {
                nextLevelAttribute = getAttributeQuark(ai, MAX_STRING, i + 1);
                if (i == 0) {
                    currentLevelAttribute = quark;
                } else {
                    currentLevelAttribute = getAttributeQuark(ai, MAX_STRING, i);
                }
                updateMax(currentLevelAttribute, nextLevelAttribute);
            }
            for (int i = 0; i < ai.minQuarks.size() - 1; i++) {
                nextLevelAttribute = getAttributeQuark(ai, MIN_STRING, i + 1);
                if (i == 0) {
                    currentLevelAttribute = quark;
                } else {
                    currentLevelAttribute = getAttributeQuark(ai, MIN_STRING, i);
                }
                updateMin(currentLevelAttribute, nextLevelAttribute);
            }
            for (int i = 0; i < ai.avgQuarks.size() - 1; i++) {
                nextLevelAttribute = getAttributeQuark(ai, AVG_STRING, i + 1);
                updateAndCloseAvrg(quark, nextLevelAttribute, i);
            }
        }
    }

    // ------------------------------------------------------------------------
    // private methods
    // ------------------------------------------------------------------------

    private void checkAndAddNewEntry(int quark, long ts, Type type, ITmfStateValue value, String mipmapAttribute, int mipmapResolution) {
        if (!attributes.containsKey(quark)) {
            AttributeInfos ai = new AttributeInfos(quark, ts, type);
            attributes.put(quark, ai);
        }
        addNewMipmapAttribute(quark, value, ts, mipmapAttribute, attributes.get(quark), mipmapResolution);
    }

    private void updateCounter(int index, String mipmapAttribute) {
        /* increment the counter */
        AttributeInfos ai = attributes.get(index);
        ai.setCounter(mipmapAttribute, ai.getCounter(mipmapAttribute) + 1);
    }

    private void updateLevels(int quark, ITmfStateValue value, long ts, String mipmapAttribute) {
        if (mipmapAttribute.equals(MAX_STRING)) {
            updateMaxLevels(quark, value, ts);
        } else if (mipmapAttribute.equals(MIN_STRING)) {
            updateMinLevels(quark, value, ts);
        } else if (mipmapAttribute.equals(AVG_STRING)) {
            updateAvgLevels(quark, value, ts);
        }
    }

    private void updateMaxLevels(int quark, ITmfStateValue value, long ts) {
        try {
            AttributeInfos ai = attributes.get(quark);
            int numberOfLevel = ai.maxQuarks.size() - 1;
            for (int i = 0; i < numberOfLevel; i++) {
                /* update the upper level */
                if (ai.getCounter(MAX_STRING) % getCheckpoint(ai.getMipmapResolution(MAX_STRING), i) == 0) {
                    if (ai.getCounter(MAX_STRING) % getCheckpoint(ai.getMipmapResolution(MAX_STRING), i + 1) != 0) {
                        if (i == 0) {
                            /* update values at the first level */
                            int attributeQuark = getAttributeQuark(ai, MAX_STRING, i + 1);
                            if (ss.queryOngoingState(attributeQuark).compareTo(value) == -1) {
                                updateStateInfo(attributeQuark, value);
                            }
                        }
                    } else {
                        updateLevel(quark, i + 1, MAX_STRING);
                        int attributeQuark = getAttributeQuark(ai, MAX_STRING, i + 1);
                        modifyMipMapAttribute(attributeQuark, value, ts);
                    }
                }
            }
        } catch (AttributeNotFoundException e) {
        }
    }

    private void updateMinLevels(int quark, ITmfStateValue value, long ts) {
        try {
            AttributeInfos ai = attributes.get(quark);
            int numberOfLevel = ai.minQuarks.size() - 1;
            for (int i = 0; i < numberOfLevel; i++) {
                /* update the upper level */
                if (ai.getCounter(MIN_STRING) % getCheckpoint(ai.getMipmapResolution(MIN_STRING), i) == 0) {
                    if (ai.getCounter(MIN_STRING) % getCheckpoint(ai.getMipmapResolution(MIN_STRING), i + 1) != 0) {
                        if (i == 0) {
                            /* update values at the first level */
                            int attributeQuark = getAttributeQuark(ai, MIN_STRING, i + 1);
                            if (ss.queryOngoingState(attributeQuark).compareTo(value) == 1) {
                                updateStateInfo(attributeQuark, value);
                            }
                        }
                    } else {
                        updateLevel(quark, i + 1, MIN_STRING);
                        int attributeQuark = getAttributeQuark(ai, MIN_STRING, i + 1);
                        modifyMipMapAttribute(attributeQuark, value, ts);
                    }
                }
            }
        } catch (AttributeNotFoundException e) {
        }
    }

    private void updateAvgLevels(int quark, ITmfStateValue value, long ts) {
        try {
            AttributeInfos ai = attributes.get(quark);
            int numberOfLevel = ai.avgQuarks.size() - 1;
            for (int i = 0; i < numberOfLevel; i++) {
                /* update the upper level */
                if (ai.getCounter(AVG_STRING) % getCheckpoint(ai.getMipmapResolution(AVG_STRING), i) == 0) {
                    if (ai.getCounter(AVG_STRING) % getCheckpoint(ai.getMipmapResolution(AVG_STRING), i + 1) != 0) {
                        if (i == 0) {
                            /* update values at the first level */
                            int attributeQuark = getAttributeQuark(ai, AVG_STRING, i + 1);
                            ITmfStateValue sv = ss.queryOngoingState(attributeQuark).add(value);
                            updateStateInfo(attributeQuark, sv);
                        }
                    } else {
                        updateLevel(quark, i + 1, AVG_STRING);
                        int attributeQuark = getAttributeQuark(ai, AVG_STRING, i + 1);
                        if (i == 0) {
                            modifyMipMapAttribute(attributeQuark, value, ts);
                        } else {
                            modifyMipMapAttribute(attributeQuark, getNewStateValue(quark, 0L), ts);
                        }
                    }
                }
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateLevel(int quark, int level, String mipmapAttribute) {
        AttributeInfos ai = attributes.get(quark);
        /*
         * we don't want to update the upper levels when we have only received
         * the first event for this index
         */
        if (ai.getCounter(mipmapAttribute) == 0) {
            return;
        }
        /*
         * if the level doesn't exist, add a checkpoint for this level then
         * update the level
         */
        try {
            int currentLevelAttribute = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(mipmapAttribute), mipmapAttribute + String.valueOf(level));
            int nextLevelAttribute = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(mipmapAttribute), mipmapAttribute + String.valueOf(level + 1));

            if (!checkLevelExist(quark, level, mipmapAttribute)) {
                /*
                 * HACK : Create a new interval with a fake value in order to
                 * initialize the first interval at the timestamp where the
                 * attribute has been received.
                 */
                if (mipmapAttribute.equals(MAX_STRING)) {
                    ai.maxQuarks.add(nextLevelAttribute);
                    modifyMipMapAttribute(nextLevelAttribute, ss.queryOngoingState(currentLevelAttribute), ai.getTimestamp());
                } else if (mipmapAttribute.equals(MIN_STRING)) {
                    ai.minQuarks.add(nextLevelAttribute);
                    modifyMipMapAttribute(nextLevelAttribute, ss.queryOngoingState(currentLevelAttribute), ai.getTimestamp());
                } else if (mipmapAttribute.equals(AVG_STRING)) {
                    ai.avgQuarks.add(nextLevelAttribute);
                    modifyMipMapAttribute(nextLevelAttribute, getNewStateValue(quark, 0L), ai.getTimestamp());
                }

                int attributeQuark = ss.getQuarkRelative(quark, getMimapNbLevelString(mipmapAttribute));
                ss.updateOngoingState(ss.queryOngoingState(attributeQuark).increment(), attributeQuark);
            }

            /* update sub-Attribute of the new level */
            if (mipmapAttribute.equals(MAX_STRING)) {
                updateMax(currentLevelAttribute, nextLevelAttribute);
            } else if (mipmapAttribute.equals(MIN_STRING)) {
                updateMin(currentLevelAttribute, nextLevelAttribute);
            } else if (mipmapAttribute.equals(AVG_STRING)) {
                updateAvrg(quark, currentLevelAttribute, nextLevelAttribute, level);
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateStateInfo(int attributeQuark, ITmfStateValue value) {
        try {
            ss.updateOngoingState(value, attributeQuark);
        } catch (AttributeNotFoundException e) {
        }
    }

    private void modifyMipMapAttribute(int attributeQuark, ITmfStateValue value, long ts) {
        try {
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (TimeRangeException e) {
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private boolean checkLevelExist(int quark, int level, String mipmapAttribute) {
        if (mipmapAttribute.equals(MAX_STRING)) {
            if (level > attributes.get(quark).maxQuarks.size() - 2 || level < 0) {
                return false;
            }
            return true;
        } else if (mipmapAttribute.equals(MIN_STRING)) {
            if (level > attributes.get(quark).minQuarks.size() - 2 || level < 0) {
                return false;
            }
            return true;
        } else if (mipmapAttribute.equals(AVG_STRING)) {
            if (level > attributes.get(quark).avgQuarks.size() - 2 || level < 0) {
                return false;
            }
            return true;
        }
        return true;
    }

    private void updateMax(int currentLevelAttribute, int nextLevelAttribute) {
        try {
            if (ss.queryOngoingState(nextLevelAttribute).compareTo(ss.queryOngoingState(currentLevelAttribute)) == -1) {
                updateStateInfo(nextLevelAttribute, ss.queryOngoingState(currentLevelAttribute));
            }
        } catch (AttributeNotFoundException e) {
        }
    }

    private void updateMin(int currentLevelAttribute, int nextLevelAttribute) {
        try {
            if (ss.queryOngoingState(nextLevelAttribute).compareTo(ss.queryOngoingState(currentLevelAttribute)) == 1) {
                updateStateInfo(nextLevelAttribute, ss.queryOngoingState(currentLevelAttribute));
            }
        } catch (AttributeNotFoundException e) {
        }
    }

    private void updateAvrg(int quark, int currentLevelAttribute, int nextLevelAttribute, int level) {
        try {
            if (level != 0) {
                BigInteger average;
                average = getBigIntegerValueOf(quark, ss.queryOngoingState(currentLevelAttribute).add(ss.queryOngoingState(nextLevelAttribute)));
                /* update the sum in the upper level */
                updateStateInfo(nextLevelAttribute, getNewStateValue(quark, average.longValue()));
                /* update the average of the current level */
                average = getBigIntegerValueOf(quark, ss.queryOngoingState(currentLevelAttribute)).divide(BigInteger.valueOf(getCheckpoint(attributes.get(quark).getMipmapResolution(AVG_STRING), level)));
                updateStateInfo(currentLevelAttribute, getNewStateValue(quark, average.longValue()));
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateAndCloseAvrg(int quark, int currentLevelAttribute, int level) {
        try {
            BigInteger average;
            int nextLevelAttribute;
            AttributeInfos ai = attributes.get(quark);
            /* update the sum in the upper level */
            if (level + 2 <= ai.avgQuarks.size() - 1) {
                nextLevelAttribute = getAttributeQuark(ai, AVG_STRING, level + 2);
                average = getBigIntegerValueOf(quark, ss.queryOngoingState(nextLevelAttribute).add(ss.queryOngoingState(currentLevelAttribute)));
                updateStateInfo(nextLevelAttribute, getNewStateValue(quark, average.longValue()));
            }
            /* update the average */
            average = BigInteger.valueOf(((ai.getCounter(AVG_STRING) + 1) % getCheckpoint(ai.getMipmapResolution(AVG_STRING), level + 1)));
            if (average.equals(BigInteger.valueOf(0L))) {
                average = BigInteger.valueOf(getCheckpoint(ai.getMipmapResolution(AVG_STRING), level + 1));
            }
            average = getBigIntegerValueOf(quark, ss.queryOngoingState(currentLevelAttribute)).divide(average);
            updateStateInfo(currentLevelAttribute, getNewStateValue(quark, average.longValue()));
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private ITmfStateValue getNewStateValue(int quark, long value) {
        Type type = attributes.get(quark).getType();
        if (type == Type.INTEGER) {
            return TmfStateValue.newValueInt((int) value);
        } else if (type == Type.LONG) {
            return TmfStateValue.newValueLong(value);
        }
        return TmfStateValue.nullValue();
    }

    private BigInteger getBigIntegerValueOf(int quark, ITmfStateValue sv) {
        try {
            Type type = attributes.get(quark).getType();
            if (type == Type.INTEGER) {
                return BigInteger.valueOf(sv.unboxInt());
            } else if (type == Type.LONG) {
                return BigInteger.valueOf(sv.unboxLong());
            }
        } catch (StateValueTypeException e) {
        }
        return null;
    }

    private void addNewMipmapAttribute(int quark, ITmfStateValue value, long ts, String mipmapAttribute, AttributeInfos ai, int mipmapResolution) {
        /*
         * HACK : Create a new interval with a fake value in order to initialize
         * the first interval at the timestamp where the attribute has been
         * received.
         */
        try {
            if (mipmapAttribute.equals(MAX_STRING) && !ai.getMipmapAttributeExist(ai.MAX_INDEX)) {
                ai.setMipmapResolution(MAX_STRING, mipmapResolution);
                int nbLevelQuark = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(MAX_STRING));
                ss.modifyAttribute(ts, getNewStateValue(quark, 1L), nbLevelQuark);
                int attributeQuark = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(MAX_STRING), MAX_STRING + String.valueOf(1));
                ai.maxQuarks.add(attributeQuark);
                modifyMipMapAttribute(attributeQuark, value, ts);
                ai.setMipmapAttributeExist(ai.MAX_INDEX, true);
            } else if (mipmapAttribute.equals(MIN_STRING) && !ai.getMipmapAttributeExist(ai.MIN_INDEX)) {
                ai.setMipmapResolution(MIN_STRING, mipmapResolution);
                int nbLevelQuark = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(MIN_STRING));
                ss.modifyAttribute(ts, getNewStateValue(quark, 1L), nbLevelQuark);
                int attributeQuark = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(MIN_STRING), MIN_STRING + String.valueOf(1));
                ai.minQuarks.add(attributeQuark);
                modifyMipMapAttribute(attributeQuark, value, ts);
                ai.setMipmapAttributeExist(ai.MIN_INDEX, true);
            } else if (mipmapAttribute.equals(AVG_STRING) && !ai.getMipmapAttributeExist(ai.AVG_INDEX)) {
                ai.setMipmapResolution(AVG_STRING, mipmapResolution);
                int nbLevelQuark = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(AVG_STRING));
                ss.modifyAttribute(ts, getNewStateValue(quark, 1L), nbLevelQuark);
                int attributeQuark = ss.getQuarkRelativeAndAdd(quark, getMimapNbLevelString(AVG_STRING), AVG_STRING + String.valueOf(1));
                ai.avgQuarks.add(attributeQuark);
                modifyMipMapAttribute(attributeQuark, value, ts);
                ai.setMipmapAttributeExist(ai.AVG_INDEX, true);
            }
        } catch (TimeRangeException e) {
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private static int getCheckpoint(int resolution, int level) {
        int toReturn = 1;
        for (int i = 0; i < level; i++) {
            toReturn *= resolution;
        }
        return toReturn;
    }

    private static int getAttributeQuark(AttributeInfos ai, String mipmapAttribute, int level) {
        if (mipmapAttribute.equals(MAX_STRING)) {
            return ai.maxQuarks.get(level);
        } else if (mipmapAttribute.equals(MIN_STRING)) {
            return ai.minQuarks.get(level);
        } else if (mipmapAttribute.equals(AVG_STRING)) {
            return ai.avgQuarks.get(level);
        }
        return -1;
    }

    // ------------------------------------------------------------------------
    // Private class
    // ------------------------------------------------------------------------
    private class AttributeInfos {

        private long timestamp;
        private int mipmapResolution[] = { 0, 0, 0 };
        private int counter[] = { -1, -1, -1 };
        private boolean mipmapAttributeExist[] = { false, false, false };
        private final Type type;

        public final int MAX_INDEX = 0;
        public final int MIN_INDEX = 1;
        public final int AVG_INDEX = 2;

        public List<Integer> maxQuarks = new ArrayList<Integer>();
        public List<Integer> minQuarks = new ArrayList<Integer>();
        public List<Integer> avgQuarks = new ArrayList<Integer>();

        public AttributeInfos(int quark, long ts, Type type) {
            this.timestamp = ts;
            this.type = type;
            maxQuarks.add(quark);
            minQuarks.add(quark);
            avgQuarks.add(quark);
        }

        public Type getType() {
            return type;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public boolean getMipmapAttributeExist(int index) {
            return mipmapAttributeExist[index];
        }

        public int getCounter(String mipmapAttribute) {
            int index = getMipmapAttributeID(mipmapAttribute);
            if (index >= 0) {
                return counter[index];
            }
            return -1;
        }

        private int getMipmapAttributeID(String mipmapAttribute) {
            if (mipmapAttribute.equals(MAX_STRING)) {
                return MAX_INDEX;
            } else if (mipmapAttribute.equals(MIN_STRING)) {
                return MIN_INDEX;
            } else if (mipmapAttribute.equals(AVG_STRING)) {
                return AVG_INDEX;
            }
            return -1;
        }

        public int getMipmapResolution(String mipmapAttribute) {
            int index = getMipmapAttributeID(mipmapAttribute);
            if (index >= 0) {
                return mipmapResolution[index];
            }
            return -1;
        }

        public void setMipmapAttributeExist(int index, boolean mipmapFeatureExist) {
            this.mipmapAttributeExist[index] = mipmapFeatureExist;
        }

        public void setCounter(String mipmapAttribute, int counter) {
            int index = getMipmapAttributeID(mipmapAttribute);
            if (index >= 0) {
                this.counter[index] = counter;
            }
        }

        public void setMipmapResolution(String mipmapAttribute, int mipmapResolution) {
            int index = getMipmapAttributeID(mipmapAttribute);
            if (index >= 0) {
                this.mipmapResolution[index] = mipmapResolution;
            }
        }
    }
}
