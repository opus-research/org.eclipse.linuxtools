/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.analysis.xml.core.stateprovider.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.w3c.dom.Element;

/**
 * This Class implement a condition tree for a state change
 *
 * <pre>
 * example:
 * <and>
 *   <condition>
 *       <stateAttribute type="location" value="CurrentThread" />
 *       <stateAttribute type="constant" value="System_call" />
 *       <stateValue type="null" />
 *   </condition>
 *   <condition>
 *   </condition>
 * </and>
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlCondition {

    private final List<TmfXmlCondition> fConditions = new ArrayList<>();
    private final TmfXmlStateValue fStateValue;
    private final ConditionOperators fOperator;
    private final XmlStateProvider fProvider;

    private enum ConditionOperators {
        OP_NONE,
        OP_NOT,
        OP_AND,
        OP_OR,
    }

    /**
     * Constructor
     *
     * @param node
     *            The XML root of this condition
     * @param provider
     *            The state provider this condition belongs to
     */
    public TmfXmlCondition(Element node, XmlStateProvider provider) {
        fProvider = provider;

        Element rootNode = node;
        /* Process the conditions: in each case, only process Element nodes */
        List<Element> childElements = XmlUtils.getChildElements(rootNode);

        /*
         * If the node is an if, take the child as the root condition
         *
         * FIXME: Maybe the caller should do this instead.
         */
        if (node.getNodeName().equals(TmfXmlStrings.IF)) {
            if (childElements.isEmpty()) {
                throw new IllegalArgumentException("TmfXmlCondition constructor: IF node has no child element"); //$NON-NLS-1$
            }
            rootNode = childElements.get(0);
            childElements = XmlUtils.getChildElements(rootNode);
        }

        if (rootNode.getNodeName().equals(TmfXmlStrings.CONDITION)) {
            fOperator = ConditionOperators.OP_NONE;
            /* The last element is a state value node */
            Element stateValueElement = childElements.remove(childElements.size() - 1);

            /*
             * A state value is either preceded by an eventField or a number of
             * state attributes
             */
            if (childElements.size() == 1 && childElements.get(0).getNodeName().equals(TmfXmlStrings.ELEMENT_FIELD)) {
                fStateValue = new TmfXmlStateValue(stateValueElement, fProvider, childElements.get(0).getAttribute(TmfXmlStrings.NAME));
            } else {
                List<TmfXmlStateAttribute> attributes = new ArrayList<>();
                for (Element element : childElements) {
                    if (!element.getNodeName().equals(TmfXmlStrings.STATE_ATTRIBUTE)) {
                        throw new IllegalArgumentException("TmfXmlCondition: a condition either has a eventField element or a number of TmfXmlStateAttribute elements before the state value"); //$NON-NLS-1$
                    }
                    TmfXmlStateAttribute attribute = new TmfXmlStateAttribute(element, fProvider);
                    attributes.add(attribute);
                }
                fStateValue = new TmfXmlStateValue(stateValueElement, fProvider, attributes);
            }
        } else if (rootNode.getNodeName().equals(TmfXmlStrings.NOT)) {
            fOperator = ConditionOperators.OP_NOT;
            fStateValue = null;
            fConditions.add(new TmfXmlCondition(childElements.get(0), fProvider));
        } else if (rootNode.getNodeName().equals(TmfXmlStrings.AND)) {
            fOperator = ConditionOperators.OP_AND;
            fStateValue = null;
            for (Element condition : childElements) {
                fConditions.add(new TmfXmlCondition(condition, fProvider));
            }
        } else if (rootNode.getNodeName().equals(TmfXmlStrings.OR)) {
            fOperator = ConditionOperators.OP_OR;
            fStateValue = null;
            for (Element condition : childElements) {
                fConditions.add(new TmfXmlCondition(condition, fProvider));
            }
        } else {
            throw new IllegalArgumentException("TmfXmlCondition constructor: XML node is of the wrong type"); //$NON-NLS-1$
        }
    }

    /**
     * Test the result of the condition for an event
     *
     * @param event
     *            The event on which to test the condition
     * @return Whether the condition is true or not
     * @throws AttributeNotFoundException
     *             The state attribute was not found
     */
    public boolean testForEvent(ITmfEvent event) throws AttributeNotFoundException {
        ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();
        /*
         * The condition is either the equality check of a state value or a
         * boolean operation on other conditions
         */
        if (fStateValue != null) {
            TmfXmlStateValue filter = fStateValue;
            int quark = -1;
            for (TmfXmlStateAttribute attribute : filter.getAttributes()) {
                quark = attribute.getAttributeQuark(event, quark);
                /*
                 * When verifying a condition, the state attribute must exist,
                 * if it does not, the query is not valid, we stop the condition
                 * check
                 */
                if (quark == -1) {
                    throw new AttributeNotFoundException();
                }
            }

            /* Get the value to compare to from the XML file */
            ITmfStateValue valueXML;
            valueXML = filter.getValue(event);

            /*
             * The actual value: it can be either queried in the state system or
             * found in the event
             */
            ITmfStateValue valueState;
            if (quark != -1) {
                valueState = ss.queryOngoingState(quark);
            }
            else {
                valueState = filter.getEventField(event);
            }
            return valueXML.equals(valueState);

        } else if (!fConditions.isEmpty()) {
            /* Verify a condition tree */

            if (fOperator == ConditionOperators.OP_NOT) {
                return !fConditions.get(0).testForEvent(event);
            } else if (fOperator == ConditionOperators.OP_AND) {
                boolean test = true;
                for (TmfXmlCondition childCondition : fConditions) {
                    test = test && childCondition.testForEvent(event);
                    if (!test) {
                        break;
                    }
                }
                return test;
            } else if (fOperator == ConditionOperators.OP_OR) {
                boolean test = false;
                for (TmfXmlCondition childCondition : fConditions) {
                    test = test || childCondition.testForEvent(event);
                    if (test) {
                        break;
                    }
                }
                return test;
            }
        } else {
            throw new IllegalStateException("TmfXmlCondition: the condition should be either a state value or be the result of a condition tree"); //$NON-NLS-1$
        }
        return true;
    }

}