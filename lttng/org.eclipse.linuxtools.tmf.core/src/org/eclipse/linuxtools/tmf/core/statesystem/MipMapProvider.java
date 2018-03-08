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
import org.eclipse.linuxtools.tmf.core.util.Pair;

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

    private Map<Integer, Pair<Long, Integer>> stateChangeCounter = new HashMap<Integer, Pair<Long, Integer>>();
    private Map<Integer, List<Integer>> checkPoints = new HashMap<Integer, List<Integer>>();
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
     *            true when we want it willbe done manually
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
                if (entry.getValue().getType() == Type.LONG) {
                    int fieldQuark = ss.getQuarkRelativeAndAdd(currentParentNode, entry.getKey());
                    checkAndAddNewEntry(fieldQuark, ts);
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

    private void checkAndAddNewEntry(int index, long ts) {
        try {
            checkValidAttribute(index);
            /* if this is a new index, add a new entry into the structures */
            if (!stateChangeCounter.containsKey(index)) {
                stateChangeCounter.put(index, new Pair<Long, Integer>(ts, -1));
                modifyMipMapAttribute(index, TmfStateValue.newValueLong(DEFAULT_NEW_ATTRIBUTE_VALUE), 1, MAX, ts);
                modifyMipMapAttribute(index, TmfStateValue.newValueLong(DEFAULT_NEW_ATTRIBUTE_VALUE), 1, MIN, ts);
                modifyMipMapAttribute(index, TmfStateValue.newValueLong(DEFAULT_NEW_ATTRIBUTE_VALUE), 1, AVRG, ts);
            }

            if (!checkPoints.containsKey(index)) {
                List<Integer> checkpointsList = new ArrayList<Integer>();
                checkPoints.put(index, checkpointsList);
                checkPoints.get(index).add(1);
                checkPoints.get(index).add(getMipMapRange());
                int quark = ss.getQuarkRelativeAndAdd(index, NB_LEVELS);
                ss.modifyAttribute(ts, TmfStateValue.newValueLong(1L), quark);
            }
        } catch (AttributeNotFoundException e) {
        } catch (TimeRangeException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateCounter(long ts, ITmfStateValue value, int index) {
        /* increment the counter */
        Pair<Long, Integer> data = stateChangeCounter.get(index);
        data.setSecond(data.getSecond() + 1);
        if (!computeManually) {
            updateLevels(index, value, ts);
        }
    }

    private void updateLevels(int index, ITmfStateValue value, long ts) {
        try {
            ITmfStateValue toCompare = value;
            int numberOfLevel = checkPoints.get(index).size() - 1;
            for (int i = 0; i < numberOfLevel; i++) {
                /* update the upper level */
                if (stateChangeCounter.get(index).getSecond() % checkPoints.get(index).get(i) == 0) {
                    if (stateChangeCounter.get(index).getSecond() % checkPoints.get(index).get(i + 1) != 0) {
                        if (i == 0) {
                            /* update values at the first level */
                            int quark = ss.getQuarkRelative(index, NB_LEVELS, LEVEL + String.valueOf(i + 1), MAX);
                            if (ss.queryOngoingState(quark).unboxLong() < toCompare.unboxLong()) {
                                updateStateInfo(index, toCompare, i + 1, MAX);
                            }
                            quark = ss.getQuarkRelative(index, NB_LEVELS, LEVEL + String.valueOf(i + 1), MIN);
                            if (ss.queryOngoingState(quark).unboxLong() > toCompare.unboxLong() || ss.queryOngoingState(quark).unboxLong() == DEFAULT_NEW_ATTRIBUTE_VALUE) {
                                updateStateInfo(index, toCompare, i + 1, MIN);
                            }
                            quark = ss.getQuarkRelative(index, NB_LEVELS, LEVEL + String.valueOf(i + 1), AVRG);
                            ITmfStateValue sv = TmfStateValue.newValueLong(ss.queryOngoingState(quark).unboxLong() + toCompare.unboxLong());
                            updateStateInfo(index, sv, i + 1, AVRG);
                        }
                    } else {
                        updateLevel(index, i + 1);
                        modifyMipMapAttribute(index, toCompare, i + 1, MAX, ts);
                        modifyMipMapAttribute(index, toCompare, i + 1, MIN, ts);
                        if (i == 0) {
                            modifyMipMapAttribute(index, toCompare, i + 1, AVRG, ts);
                        } else {
                            modifyMipMapAttribute(index, TmfStateValue.newValueLong(0L), i + 1, AVRG, ts);
                        }
                    }
                }
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateLevel(int index, int level) {
        /*
         * we don't want to update upper levels when we have only received the
         * first event for this index
         */
        if (stateChangeCounter.get(index).getSecond() == 0) {
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
                checkPoints.get(index).add((int) Math.pow(getMipMapRange(), level + 1));
                int quark = ss.getQuarkRelative(index, NB_LEVELS);
                ss.updateOngoingState(TmfStateValue.newValueLong(ss.queryOngoingState(quark).unboxLong() + 1), quark);

                /*
                 * initialize the fist interval for the new level in order to
                 * have the correct timestamp when the first value of the level
                 * will be inserted
                 */
                modifyMipMapAttribute(index, TmfStateValue.newValueLong(DEFAULT_NEW_ATTRIBUTE_VALUE), level + 1, MAX, stateChangeCounter.get(index).getFirst());
                modifyMipMapAttribute(index, TmfStateValue.newValueLong(DEFAULT_NEW_ATTRIBUTE_VALUE), level + 1, MIN, stateChangeCounter.get(index).getFirst());
                modifyMipMapAttribute(index, TmfStateValue.newValueLong(DEFAULT_NEW_ATTRIBUTE_VALUE), level + 1, AVRG, stateChangeCounter.get(index).getFirst());
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
            for (Integer quark : checkPoints.keySet()) {
                for (int i = 0; i < checkPoints.get(quark).size() - 1; i++) {

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
        if (level > checkPoints.get(index).size() - 2 || level < 0) {
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

            if (ss.queryOngoingState(nextLevelAttribute).unboxLong() < ss.queryOngoingState(currentLevelAttribute).unboxLong()) {
                updateStateInfo(index, ss.queryOngoingState(currentLevelAttribute), level + 1, MAX);
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
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

            if (ss.queryOngoingState(nextLevelAttribute).unboxLong() > ss.queryOngoingState(currentLevelAttribute).unboxLong()
                    || ss.queryOngoingState(nextLevelAttribute).equals(TmfStateValue.nullValue()) || ss.queryOngoingState(nextLevelAttribute).unboxLong() == DEFAULT_NEW_ATTRIBUTE_VALUE) {
                updateStateInfo(index, ss.queryOngoingState(currentLevelAttribute), level + 1, MIN);
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
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
                    average = BigInteger.valueOf(ss.queryOngoingState(currentLevelAttribute).unboxLong() + ss.queryOngoingState(nextLevelAttribute).unboxLong() + 1L);
                } else {

                    average = BigInteger.valueOf(ss.queryOngoingState(currentLevelAttribute).unboxLong() + ss.queryOngoingState(nextLevelAttribute).unboxLong());
                }
                /* update the sum in the upper level */
                updateStateInfo(index, TmfStateValue.newValueLong(average.longValue()), level + 1, AVRG);
                /* update the average of the current level */
                average = BigInteger.valueOf(ss.queryOngoingState(currentLevelAttribute).unboxLong()).divide(BigInteger.valueOf(checkPoints.get(index).get(level)));
                updateStateInfo(index, TmfStateValue.newValueLong(average.longValue()), level, AVRG);
            }
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    private void updateAndCloseAvrg(int index, int currentLevel, int level) {
        try {
            BigInteger average;
            int currentLevelAttribute, nextLevelAttribute;
            currentLevelAttribute = ss.getQuarkRelativeAndAdd(currentLevel, AVRG);
            /* update the sum in the upper level */
            if (level + 2 <= checkPoints.get(index).size() - 1) {
                nextLevelAttribute = ss.getQuarkRelativeAndAdd(index, NB_LEVELS, LEVEL + String.valueOf(level + 2), AVRG);
                average = BigInteger.valueOf(ss.queryOngoingState(nextLevelAttribute).unboxLong() + ss.queryOngoingState(currentLevelAttribute).unboxLong());
                updateStateInfo(index, TmfStateValue.newValueLong(average.longValue()), level + 2, AVRG);
            }
            /* update the average */
            average = BigInteger.valueOf(((stateChangeCounter.get(index).getSecond() + 1) % checkPoints.get(index).get(level + 1)));
            if (average.equals(BigInteger.valueOf(0L))) {
                average = BigInteger.valueOf(checkPoints.get(index).get(level + 1));
            }
            average = BigInteger.valueOf(ss.queryOngoingState(currentLevelAttribute).unboxLong()).divide(average);
            updateStateInfo(index, TmfStateValue.newValueLong(average.longValue()), level + 1, AVRG);
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
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
     * @return the range, the way to consider the state changes
     */
    protected abstract int getMipMapRange();

    /**
     * @return a map with the attributes to deal with as keys and their values
     */
    protected abstract Map<String[], ITmfStateValue> getEventMap();
}
