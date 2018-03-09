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

import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class implement a condition tree for a state change
 *
 * <pre>
 * example:
 * <and>
 *   <condition>
 *       <attribute location="CurrentThread" />
 *       <attribute constant="System_call" />
 *       <value type="null" />
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
        OP_EQUALS,
        OP_NOT,
        OP_AND,
        OP_OR,
    }

    /**
     * Constructor
     *
     * @param node
     *            The XMl root of this condition
     * @param provider
     *            The state provider this condition belongs to
     */
    public TmfXmlCondition(Node node, XmlStateProvider provider) {
        fProvider = provider;

        Node rootNode = node;
        /*
         * If the node is an if, take the child as the root condition FIXME:
         * Maybe the caller should do this instead.
         */
        if (node.getNodeName().equals(TmfXmlStrings.IF)) {
            rootNode = node.getChildNodes().item(1);
        }

        NodeList conditionNodes = rootNode.getChildNodes();
        Node conditionNode = conditionNodes.item(1);

        if (node.getNodeName().equals(TmfXmlStrings.NOT)) {
            fOperator = ConditionOperators.OP_NOT;
            fStateValue = null;
            fConditions.add(new TmfXmlCondition(conditionNode, fProvider));
        } else if (node.getNodeName().equals(TmfXmlStrings.AND)) {
            fOperator = ConditionOperators.OP_AND;
            fStateValue = null;
            for (int index = 0; index < conditionNodes.getLength(); index++) {
                fConditions.add(new TmfXmlCondition(conditionNodes.item(index), fProvider));
            }
        } else if (node.getNodeName().equals(TmfXmlStrings.OR)) {
            fOperator = ConditionOperators.OP_OR;
            fStateValue = null;
            for (int index = 0; index < conditionNodes.getLength(); index++) {
                fConditions.add(new TmfXmlCondition(conditionNodes.item(index), fProvider));
            }
        } else if (node.getNodeName().equals(TmfXmlStrings.CONDITION)) {
            fOperator = ConditionOperators.OP_NONE;
            fStateValue = new TmfXmlStateValue(conditionNodes, fProvider);
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
     *             The attribute was not found
     */
    public boolean testForEvent(ITmfEvent event) throws AttributeNotFoundException {
        ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();
        // State Value
        if (fStateValue != null) {
            TmfXmlStateValue filter = fStateValue;
            int quark = -1;
            for (TmfXmlStateAttribute attribute : filter.getAttributes()) {
                quark = attribute.getAttributeQuark(event, quark);
            }

            /* the value to compare to in the XML file */
            ITmfStateValue valueXML;
            valueXML = filter.getValue(event);

            /*
             * The actual value: it can be either queried in the state system of
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

            // Condition Tree
        } else if (!fConditions.isEmpty()) {

            if (fOperator == ConditionOperators.OP_EQUALS) {
                return fConditions.get(0).testForEvent(event);
            } else if (fOperator == ConditionOperators.OP_NOT) {
                return !fConditions.get(0).testForEvent(event);
            } else if (fOperator == ConditionOperators.OP_AND) {
                boolean test = true;
                for (TmfXmlCondition childCondition : fConditions) {
                    test = test && childCondition.testForEvent(event);
                }
                return test;
            } else if (fOperator == ConditionOperators.OP_OR) {
                boolean test = false;
                for (TmfXmlCondition childCondition : fConditions) {
                    test = test || childCondition.testForEvent(event);
                }
                return test;
            }
        }
        return true;
    }

}