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

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * This is an abstract provider that allows to realize mip mapping in a general
 * way
 *
 * Extend this one for a specific class
 *
 * @since 3.0
 */
public abstract class MipMapProvider extends AbstractTmfStateProvider {

    private static final String LTTNG_MIPMAP_ID = "Mipmap_ID"; //$NON-NLS-1$
    private final String ERROR_ATTRIBUTE_NOT_FOUND = "Error : Impossible to find the attribute"; //$NON-NLS-1$
    private final String ERROR_INVALID_STATE_VALUE = "Error : Invalid state value"; //$NON-NLS-1$
    private final String ERROR_INVALID_TIMESTAMP = "Error : Invalid timestamp"; //$NON-NLS-1$
    private final long DEFAULT_NEW_ATTRIBUTE_VALUE = -2L;

    /**
     * The string for level. This string can be used to make request to reach a
     * specific level in the mipmap tree.
     */
    public static final String LEVEL = "level"; //$NON-NLS-1$

    /**
     * The string for mipmap. By using this string, we can be make request to
     * access the mipmap node of a state system.
     */
    public static final String MIPMAP = "Mipmap"; //$NON-NLS-1$

    /**
     * The string for the number of levels in the mipmap tree. This string can
     * be used to make request to the mipmap tree to get number of levels in the
     * tree.
     */
    public static final String NB_LEVELS = "Nlevels"; //$NON-NLS-1$

    /**
     * The string for minimum. This string can be used to get the minimum value
     * in the mipmap tree at a specific level
     */
    public static final String MIN = "min"; //$NON-NLS-1$

    /**
     * The string for maximum. This string can be used to get the maximum value
     * in the mipmap tree at a specific level
     */
    public static final String MAX = "max"; //$NON-NLS-1$

    /**
     * The string for average. This string can be used to get the average value
     * in the mipmap tree at a specific level
     */
    public static final String AVRG = "avrg"; //$NON-NLS-1$

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
     *            the name given to this state change input. Only used
     *            internally.
     * @param computeManually
     *            this determines if we want to update the mipmap levels when
     *            automatically when we received an event or not. It should be
     *            false when we want to updates the levels automatically and
     *            true when we want it will be done manually
     */
    public MipMapProvider(TmfTrace trace, Class<? extends ITmfEvent> eventType, String id, boolean computeManually) {
        super(trace, eventType, id);
        this.computeManually = computeManually;
    }

    /**
     * @param trace
     *            The trace directory
     * @param computeManually
     *            this determines if we want to update the mipmap levels when
     *            automatically when we received an event or not. It should be
     *            false when we want to updates the levels automatically and
     *            true when we want it willbe done manually
     */
    public MipMapProvider(TmfTrace trace, boolean computeManually) {
        super(trace, TmfEvent.class, LTTNG_MIPMAP_ID);
        this.computeManually = computeManually;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void dispose() {
        closeMipMap();
        super.dispose();

    }

    // ------------------------------------------------------------------------
    // protected methods
    // ------------------------------------------------------------------------

    @Override
    protected void eventHandle(ITmfEvent ev) {
        TmfEvent event = (TmfEvent) ev;
        final long ts = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        try {
            final Integer currentParentNode = ss.getQuarkAbsoluteAndAdd(MIPMAP);
            for (Map.Entry<String[], ITmfStateValue> entry : getEventMap().entrySet()) {
                if (entry.getValue().getType() == Type.LONG || entry.getValue().getType() == Type.INTEGER) {
                    int fieldQuark = ss.getQuarkRelativeAndAdd(currentParentNode, entry.getKey());
                    checkAndAddNewEntry(fieldQuark, ts, entry.getValue().getType());
                    updateCounter(ts, entry.getValue(), fieldQuark);
                    ss.modifyAttribute(ts, entry.getValue(), fieldQuark);
                }
            }
        } catch (TimeRangeException e) {
            Activator.logError(ERROR_INVALID_TIMESTAMP, e);
        } catch (AttributeNotFoundException e) {
            Activator.logError(ERROR_ATTRIBUTE_NOT_FOUND, e);
        } catch (StateValueTypeException e) {
            Activator.logError(ERROR_INVALID_STATE_VALUE, e);
        }
    }

    // ------------------------------------------------------------------------
    // private methods
    // ------------------------------------------------------------------------

    private void checkAndAddNewEntry(int index, long ts, Type type) {
        try {
            checkValidAttribute(index);
            if (!attributes.containsKey(index)) {
                AttributeInfos ai = new AttributeInfos(ts, type);
                attributes.put(index, ai);

                /*
                 * HACK : Create a new interval with a fake value in order to
                 * initialize the fist interval at the timestamp where the
                 * attribute has been received.
                 */
                modifyMipMapAttribute(index, getNewStateValue(index, String.valueOf(DEFAULT_NEW_ATTRIBUTE_VALUE)), 1, MAX, ts);
                modifyMipMapAttribute(index, getNewStateValue(index, String.valueOf(DEFAULT_NEW_ATTRIBUTE_VALUE)), 1, MIN, ts);
                modifyMipMapAttribute(index, getNewStateValue(index, String.valueOf(DEFAULT_NEW_ATTRIBUTE_VALUE)), 1, AVRG, ts);

                int quark = ss.getQuarkRelativeAndAdd(index, NB_LEVELS);
                ss.modifyAttribute(ts, getNewStateValue(index, String.valueOf(1L)), quark);

            }
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateCounter(long ts, ITmfStateValue value, int index) {
        /* increment the counter */
        AttributeInfos ai = attributes.get(index);
        ai.setCounter(ai.getCounter() + 1);

        if (ai.getType() == Type.NULL) {
            ai.setType(value.getType());
        }

        if (!computeManually) {
            updateLevels(index, value, ts);
        }
    }

    private void updateLevels(int index, ITmfStateValue value, long ts) {
        try {
            ITmfStateValue toCompare = value;
            AttributeInfos ai = attributes.get(index);
            int numberOfLevel = ai.getCheckPoints().size() - 1;
            for (int i = 0; i < numberOfLevel; i++) {
                /* update the upper level */
                if (ai.getCounter() % ai.getCheckPoints().get(i) == 0) {
                    if (ai.getCounter() % ai.getCheckPoints().get(i + 1) != 0) {
                        if (i == 0) {
                            /* update values at the first level */
                            int quark = ss.getQuarkRelative(index, NB_LEVELS, LEVEL + String.valueOf(i + 1), MAX);
                            if (ss.queryOngoingState(quark).compareTo(toCompare) == -1) {
                                updateStateInfo(index, toCompare, i + 1, MAX);
                            }
                            quark = ss.getQuarkRelative(index, NB_LEVELS, LEVEL + String.valueOf(i + 1), MIN);
                            if (ss.queryOngoingState(quark).compareTo(toCompare) == 1 || ss.queryOngoingState(quark).compareTo(getNewStateValue(index, String.valueOf(DEFAULT_NEW_ATTRIBUTE_VALUE))) == 0) {
                                updateStateInfo(index, toCompare, i + 1, MIN);
                            }
                            quark = ss.getQuarkRelative(index, NB_LEVELS, LEVEL + String.valueOf(i + 1), AVRG);
                            ITmfStateValue sv = ss.queryOngoingState(quark).add(toCompare);
                            updateStateInfo(index, sv, i + 1, AVRG);
                        }
                    } else {
                        updateLevel(index, i + 1);
                        modifyMipMapAttribute(index, toCompare, i + 1, MAX, ts);
                        modifyMipMapAttribute(index, toCompare, i + 1, MIN, ts);
                        if (i == 0) {
                            modifyMipMapAttribute(index, toCompare, i + 1, AVRG, ts);
                        } else {
                            modifyMipMapAttribute(index, getNewStateValue(index, String.valueOf(0)), i + 1, AVRG, ts);
                        }
                    }
                }
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateLevel(int index, int level) {
        AttributeInfos ai = attributes.get(index);
        /*
         * we don't want to update the upper levels when we have only received the
         * first event for this index
         */
        if (ai.getCounter() == 0) {
            return;
        }
        /*
         * if the level doesn't exist, add a checkpoint for this level then
         * update the level
         */
        try {
            int currentLevel = ss.getQuarkRelativeAndAdd(index, NB_LEVELS, LEVEL + String.valueOf(level));
            int nextLevel = ss.getQuarkRelativeAndAdd(index, NB_LEVELS, LEVEL + String.valueOf(level + 1));

            if (!checkLevelExist(index, level)) {
                ai.getCheckPoints().add((int) Math.pow(getMipMapRange(), level + 1));
                int quark = ss.getQuarkRelative(index, NB_LEVELS);
                ss.updateOngoingState(ss.queryOngoingState(quark).increment(), quark);

                /*
                 * HACK : Create a new interval with a fake value in order to
                 * initialize the fist interval at the timestamp where the
                 * attribute has been received.
                 */
                modifyMipMapAttribute(index, getNewStateValue(index, String.valueOf(DEFAULT_NEW_ATTRIBUTE_VALUE)), level + 1, MAX, ai.getTimestamp());
                modifyMipMapAttribute(index, getNewStateValue(index, String.valueOf(DEFAULT_NEW_ATTRIBUTE_VALUE)), level + 1, MIN, ai.getTimestamp());
                modifyMipMapAttribute(index, getNewStateValue(index, String.valueOf(DEFAULT_NEW_ATTRIBUTE_VALUE)), level + 1, AVRG, ai.getTimestamp());
            }

            /* update sub-Attribute of the new level */
            updateMax(index, currentLevel, nextLevel, level);
            updateMin(index, currentLevel, nextLevel, level);
            updateAvrg(index, currentLevel, nextLevel, level);
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateStateInfo(int index, ITmfStateValue value, int level, String mipmapLevelAttribute) {
        try {
            int quark = ss.getQuarkRelativeAndAdd(index, NB_LEVELS, LEVEL + String.valueOf(level), mipmapLevelAttribute);
            ss.updateOngoingState(value, quark);
        } catch (AttributeNotFoundException e) {
        }
    }

    private void modifyMipMapAttribute(int index, ITmfStateValue value, int level, String mipmapLevelAttribute, long ts) {
        try {
            int quark = ss.getQuarkRelativeAndAdd(index, NB_LEVELS, LEVEL + String.valueOf(level), mipmapLevelAttribute);
            ss.modifyAttribute(ts, value, quark);
        } catch (TimeRangeException e) {
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void closeMipMap() {
        waitForEmptyQueue();
        /*
         * for each attribute that has been received, it updates the minimum,
         * maximum and average at each level of the mipmap tree
         */
        try {
            int nextLevel, currentLevel;
            for (Integer quark : attributes.keySet()) {
                AttributeInfos ai = attributes.get(quark);
                for (int i = 0; i < ai.getCheckPoints().size() - 1; i++) {

                    nextLevel = ss.getQuarkRelative(quark, NB_LEVELS, LEVEL + String.valueOf(i + 1));

                    if (i == 0) {
                        currentLevel = quark;
                    } else {
                        currentLevel = ss.getQuarkRelative(quark, NB_LEVELS, LEVEL + String.valueOf(i));
                    }
                    updateMax(quark, currentLevel, nextLevel, i);
                    updateMin(quark, currentLevel, nextLevel, i);
                    updateAndCloseAvrg(quark, nextLevel, i);
                }
            }
        } catch (AttributeNotFoundException e) {
        }
    }

    private boolean checkLevelExist(int index, int level) {
        if (level > attributes.get(index).getCheckPoints().size() - 2 || level < 0) {
            return false;
        }
        return true;

    }

    private void checkValidAttribute(int index) throws AttributeNotFoundException {
        if (index > ss.getOngoingStateInfoSize() - 1 || index < 0) {
            throw new AttributeNotFoundException();
        }
    }

    private void updateMax(int index, int currentLevel, int nextLevel, int level) {
        try {
            int currentLevelAttribute;
            if (level != 0) {
                currentLevelAttribute = ss.getQuarkRelative(currentLevel, MAX);
            } else {
                currentLevelAttribute = currentLevel;
            }
            int nextLevelAttribute = ss.getQuarkRelativeAndAdd(nextLevel, MAX);

            if (ss.queryOngoingState(nextLevelAttribute).compareTo(ss.queryOngoingState(currentLevelAttribute)) == -1) {
                updateStateInfo(index, ss.queryOngoingState(currentLevelAttribute), level + 1, MAX);
            }
        } catch (AttributeNotFoundException e) {
        }
    }

    private void updateMin(int index, int currentLevel, int nextLevel, int level) {
        try {
            int currentLevelAttribute, nextLevelAttribute;
            if (level != 0) {
                currentLevelAttribute = ss.getQuarkRelativeAndAdd(currentLevel, MIN);
            } else {
                currentLevelAttribute = currentLevel;
            }
            nextLevelAttribute = ss.getQuarkRelativeAndAdd(nextLevel, MIN);

            if (ss.queryOngoingState(nextLevelAttribute).compareTo(ss.queryOngoingState(currentLevelAttribute)) == 1
                    || ss.queryOngoingState(nextLevelAttribute).equals(TmfStateValue.nullValue()) ||
                    ss.queryOngoingState(nextLevelAttribute).compareTo(getNewStateValue(index, String.valueOf(DEFAULT_NEW_ATTRIBUTE_VALUE))) == 0) {
                updateStateInfo(index, ss.queryOngoingState(currentLevelAttribute), level + 1, MIN);
            }
        } catch (AttributeNotFoundException e) {
        }
    }

    private void updateAvrg(int index, int currentLevel, int nextLevel, int level) {
        try {
            int currentLevelAttribute, nextLevelAttribute;
            if (level != 0) {
                BigInteger average;
                currentLevelAttribute = ss.getQuarkRelativeAndAdd(currentLevel, AVRG);
                nextLevelAttribute = ss.getQuarkRelativeAndAdd(nextLevel, AVRG);
                if (ss.queryOngoingState(nextLevelAttribute).equals(TmfStateValue.nullValue())) {
                    average = getBigIntegerValueOf(index, (ss.queryOngoingState(currentLevelAttribute).add(ss.queryOngoingState(nextLevelAttribute))).increment());
                } else {
                    average = getBigIntegerValueOf(index, ss.queryOngoingState(currentLevelAttribute).add(ss.queryOngoingState(nextLevelAttribute)));
                }
                /* update the sum in the upper level */
                updateStateInfo(index, getNewStateValue(index, String.valueOf(average.longValue())), level + 1, AVRG);
                /* update the average of the current level */
                average = getBigIntegerValueOf(index, ss.queryOngoingState(currentLevelAttribute)).divide(BigInteger.valueOf(attributes.get(index).getCheckPoints().get(level)));
                updateStateInfo(index, getNewStateValue(index, String.valueOf(average.longValue())), level, AVRG);
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateAndCloseAvrg(int index, int currentLevel, int level) {
        try {
            BigInteger average;
            int currentLevelAttribute, nextLevelAttribute;
            AttributeInfos ai = attributes.get(index);
            currentLevelAttribute = ss.getQuarkRelativeAndAdd(currentLevel, AVRG);
            /* update the sum in the upper level */
            if (level + 2 <= ai.getCheckPoints().size() - 1) {
                nextLevelAttribute = ss.getQuarkRelativeAndAdd(index, NB_LEVELS, LEVEL + String.valueOf(level + 2), AVRG);
                average = getBigIntegerValueOf(index, ss.queryOngoingState(nextLevelAttribute).add(ss.queryOngoingState(currentLevelAttribute)));
                updateStateInfo(index, getNewStateValue(index, String.valueOf(average.longValue())), level + 2, AVRG);
            }
            /* update the average */
            average = BigInteger.valueOf(((ai.getCounter() + 1) % ai.getCheckPoints().get(level + 1)));
            if (average.equals(BigInteger.valueOf(0L))) {
                average = BigInteger.valueOf(ai.getCheckPoints().get(level + 1));
            }
            average = getBigIntegerValueOf(index, ss.queryOngoingState(currentLevelAttribute)).divide(average);
            updateStateInfo(index, getNewStateValue(index, String.valueOf(average.longValue())), level + 1, AVRG);
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private ITmfStateValue getNewStateValue(int index, String value) {
        Type type = attributes.get(index).getType();
        if (type == Type.INTEGER) {
            return TmfStateValue.newValueInt(Integer.valueOf(value));
        } else if (type == Type.LONG) {
            return TmfStateValue.newValueLong(Long.valueOf(value));
        }
        return TmfStateValue.nullValue();
    }

    private BigInteger getBigIntegerValueOf(int index, ITmfStateValue sv) {
        try {
            Type type = attributes.get(index).getType();
            if (type == Type.INTEGER) {
                return BigInteger.valueOf(sv.unboxInt());
            } else if (type == Type.LONG) {
                return BigInteger.valueOf(sv.unboxLong());
            }
        } catch (StateValueTypeException e) {
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // public methods
    // ------------------------------------------------------------------------
    /**
     * This function is needed to update manually the mipmap and his levels from
     * an external class
     *
     * @param ts
     *            the timestamp of the event
     * @param value
     *            the new value of the corresponding attribute
     * @param path
     *            the path of the attribute we want to mipmap
     */
    public void updateMipMap(long ts, ITmfStateValue value, String... path) {
        if (computeManually) {
            try {
                final Integer currentParentNode = ss.getQuarkAbsoluteAndAdd(MIPMAP);
                int quark = ss.getQuarkRelativeAndAdd(currentParentNode, path);
                ss.updateOngoingState(value, quark);
                updateLevels(quark, value, ts);
            } catch (AttributeNotFoundException e) {
            }
        }
    }

    // ------------------------------------------------------------------------
    // Abstract methods
    // ------------------------------------------------------------------------

    /**
     * @return the range for the mipmap
     */
    protected abstract int getMipMapRange();

    /**
     * @return a map with the attributes to deal with as keys and their values
     */
    protected abstract Map<String[], ITmfStateValue> getEventMap();

    // ------------------------------------------------------------------------
    // Private class
    // ------------------------------------------------------------------------
    private class AttributeInfos {

        private long timestamp;
        private int counter;
        private List<Integer> checkPoints;
        private Type type;

        public AttributeInfos (long ts, Type type) {
            this.timestamp = ts;
            this.type = type;
            this.counter = -1;
            this.checkPoints = new ArrayList<Integer>();
            checkPoints.add(1);
            checkPoints.add(getMipMapRange());
        }
        public int getCounter() {
            return counter;
        }
        public List<Integer> getCheckPoints() {
            return checkPoints;
        }
        public Type getType() {
            return type;
        }
        public long getTimestamp() {
            return timestamp;
        }
        public void setCounter(int counter) {
            this.counter = counter;
        }
        public void setType(Type type) {
            this.type = type;
        }
    }
}
