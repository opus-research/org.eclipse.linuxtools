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

package org.eclipse.linuxtools.tmf.core.statesystem;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.mipmapFeature.AvgMipmapFeature;
import org.eclipse.linuxtools.tmf.core.mipmapFeature.ITmfMipmapFeature;
import org.eclipse.linuxtools.tmf.core.mipmapFeature.MaxMipmapFeature;
import org.eclipse.linuxtools.tmf.core.mipmapFeature.MinMipmapFeature;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * This is an abstract provider that allows to realize mip mapping in a general
 * way
 *
 * Extend this one for a specific class
 *
 * @since 3.0
 */
public abstract class AbstractTmfMipMapStateProvider extends AbstractTmfStateProvider {

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
    public AbstractTmfMipMapStateProvider(ITmfTrace trace, Class<? extends ITmfEvent> eventType, String id, boolean computeManually) {
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
     *
     * @param mipmapAttribute
     *            The string for the mipmap feature
     */
    public void closeMipMap(String mipmapAttribute) {
        waitForEmptyQueue();
        /*
         * for each attribute that has been received, it updates the minimum,
         * maximum and average at each level of the mipmap tree
         */
        for (Integer quark : attributes.keySet()) {
            AttributeInfos ai = attributes.get(quark);
            if (ai.features.containsKey(mipmapAttribute)) {
                ai.features.get(mipmapAttribute).updateAndCloseFeature();
            }
        }
    }

    // ------------------------------------------------------------------------
    // private methods
    // ------------------------------------------------------------------------

    private void checkAndAddNewEntry(int quark, long ts, Type type, ITmfStateValue value, String mipmapAttribute, int mipmapResolution) {
        if (!attributes.containsKey(quark)) {
            AttributeInfos ai = new AttributeInfos();
            attributes.put(quark, ai);
        }
        addNewMipmapAttribute(quark, value, ts, mipmapAttribute, attributes.get(quark), mipmapResolution, type);
    }

    private void updateCounter(int quark, String mipmapAttribute) {
        /* increment the counter */
        AttributeInfos ai = attributes.get(quark);
        ai.features.get(mipmapAttribute).setCounter(ai.features.get(mipmapAttribute).getCounter() + 1);
    }

    private void updateLevels(int quark, ITmfStateValue value, long ts, String mipmapAttribute) {
        attributes.get(quark).features.get(mipmapAttribute).updateFeatureLevels(quark, value, ts);
    }

    private void addNewMipmapAttribute(int quark, ITmfStateValue value, long ts, String mipmapAttribute, AttributeInfos ai, int mipmapResolution, Type type) {
        /*
         * HACK : Create a new interval with a fake value in order to initialize
         * the first interval at the timestamp where the attribute has been
         * received.
         */
        try {
            if (!ai.features.containsKey(mipmapAttribute)) {
                ITmfMipmapFeature mf = null;
                if (mipmapAttribute.equals(MAX_STRING)) {
                    mf = new MaxMipmapFeature(quark, mipmapResolution, ss, type, ts);
                } else if (mipmapAttribute.equals(MIN_STRING)) {
                    mf = new MinMipmapFeature(quark, mipmapResolution, ss, type, ts);
                } else if (mipmapAttribute.equals(AVG_STRING)) {
                    mf = new AvgMipmapFeature(quark, mipmapResolution, ss, type, ts);
                }
                if (mf != null) {
                    ai.features.put(mipmapAttribute, mf);
                    int nbLevelQuark = ss.getQuarkRelativeAndAdd(quark, mf.getMimapNbLevelString());
                    ss.modifyAttribute(ts, mf.getNewStateValue(1L), nbLevelQuark);
                    int attributeQuark = ss.getQuarkRelativeAndAdd(quark, mf.getMimapNbLevelString(), mipmapAttribute + String.valueOf(1));
                    mf.getFeatureQuarks().add(attributeQuark);
                    ss.modifyAttribute(ts, value, attributeQuark);
                }
            }
        } catch (TimeRangeException e) {
        } catch (AttributeNotFoundException e) {
        } catch (StateValueTypeException e) {
        }
    }

    // ------------------------------------------------------------------------
    // Private class
    // ------------------------------------------------------------------------
    private class AttributeInfos {
        public Map<String, ITmfMipmapFeature> features = new HashMap<String, ITmfMipmapFeature>();

        public AttributeInfos() {
        }
    }
}
