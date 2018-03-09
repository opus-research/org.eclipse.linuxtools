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

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class implement a State Change is the XML state provider
 *
 * <pre>
 *  example 1: Simple state change
 *  <stateChange>
 *      <attribute location="CurrentThread" />
 *      <attribute constant="System_call" />
 *      <value type="null" />
 *  </stateChange>
 *
 *  example 2: Conditional state change
 *  <stateChange>
 *     <if>
 *      <condition>
 *        <attribute location="CurrentThread" />
 *        <attribute constant="System_call" />
 *        <value type="null" />
 *      </condition>
 *     </if>
 *    <then>
 *      <attribute location="CurrentThread" />
 *      <attribute constant="Status" />
 *      <value int="$PROCESS_STATUS_RUN_USERMODE"/>
 *    </then>
 *    <else>
 *      <attribute location="CurrentThread" />
 *      <attribute constant="Status" />
 *      <value int="$PROCESS_STATUS_RUN_SYSCALL"/>
 *    </else>
 *  </stateChange>
 * </pre>
 *
 * @author Florian Wininger
 *
 */
public class TmfXmlStateChange {

    private TmfXmlCondition fCondition = null;

    private TmfXmlStateValue fThenValue = null;

    private TmfXmlStateValue fElseValue = null;

    /**
     * Constructor
     *
     * @param statechange
     *            Xml node
     * @param definedStates
     *            HashMap with all state values
     */
    TmfXmlStateChange(Node statechange, HashMap<String, String> definedStates) {
        if (statechange.getNodeName().equals(TmfXmlStrings.STATE_CHANGE)) {
            NodeList childNodes = statechange.getChildNodes();
            Node node = childNodes.item(1);

            Node ifNode = ((Element) statechange).getElementsByTagName(TmfXmlStrings.IF).item(0);
            /* the state change has a condition */
            if (ifNode != null) {
                fCondition = new TmfXmlCondition(node, definedStates);

                Node thenNode = ((Element) statechange).getElementsByTagName(TmfXmlStrings.THEN).item(0);
                if (thenNode != null) {
                    fThenValue = new TmfXmlStateValue(thenNode.getChildNodes(), definedStates);
                }
                Node elseNode = ((Element) statechange).getElementsByTagName(TmfXmlStrings.ELSE).item(0);
                if (elseNode != null) {
                    fElseValue = new TmfXmlStateValue(elseNode.getChildNodes(), definedStates);
                }
            }
            /* the state change does not have a condition */
            else {
                fThenValue = new TmfXmlStateValue(childNodes, definedStates);
            }
        }
    }

    /**
     * @return Get the condition
     */
    public TmfXmlCondition getConditions() {
        return fCondition;
    }

    /**
     * @return Get the default action
     */
    public TmfXmlStateValue getThenValue() {
        return fThenValue;
    }

    /**
     * @return Get the other action
     */
    public TmfXmlStateValue getElseValue() {
        return fElseValue;
    }

}