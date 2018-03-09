/*******************************************************************************
 * Copyright (c) 2013 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model;

import java.util.ArrayList;
import java.util.HashMap;

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
public class XmlCondition {

    private ArrayList<XmlCondition> fConditions = new ArrayList<XmlCondition>();
    private XmlStateValue fStateValue = null;
    private int fOperator = 0;

    /**
     * Constructor
     *
     * @param node
     *            conditions
     * @param definedStates
     *            HashMap with all state values
     */
    public XmlCondition(Node node, HashMap<String, String> definedStates) {
        loadCondition(node, definedStates);
    }

    private void loadCondition(Node node, HashMap<String, String> definedStates) {
        NodeList conditionNodes = node.getChildNodes();
        Node conditionNode = conditionNodes.item(1);

        if (node.getNodeName().equals(XmlStrings.IF)) {
            loadCondition(conditionNode, definedStates);
        } else if (node.getNodeName().equals(XmlStrings.NOT)) {
            fOperator = XmlStrings.OP_NOT;
            fConditions.add(new XmlCondition(conditionNode, definedStates));
        } else if (node.getNodeName().equals(XmlStrings.AND)) {
            fOperator = XmlStrings.OP_AND;
            for (int index = 0; index < conditionNodes.getLength(); index++) {
                fConditions.add(new XmlCondition(conditionNodes.item(index), definedStates));
            }
        } else if (node.getNodeName().equals(XmlStrings.OR)) {
            fOperator = XmlStrings.OP_OR;
            for (int index = 0; index < conditionNodes.getLength(); index++) {
                fConditions.add(new XmlCondition(conditionNodes.item(index), definedStates));
            }
        } else if (node.getNodeName().equals(XmlStrings.CONDITION)) {
            fStateValue = new XmlStateValue(conditionNodes, definedStates);
        }
    }

    /**
     * @return conditions
     */
    public ArrayList<XmlCondition> getConditions() {
        return fConditions;
    }

    /**
     * @return state value
     */
    public XmlStateValue getStateValue() {
        return fStateValue;
    }

    /**
     * @return operator
     */
    public int getOperator() {
        return fOperator;
    }

}