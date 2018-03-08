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

package org.eclipse.linuxtools.tmf.analysis.xml.stateprovider.model;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class implement a State Change is the XML state provider
 *
 * <stateChange>
 *     <attribute location="CurrentThread" />
 *     <attribute constant="System_call" />
 *     <value type="null" />
 * </stateChange>
 *OR
 * <stateChange>
 *    <if>
 *     <condition>
 *       <attribute location="CurrentThread" />
 *       <attribute constant="System_call" />
 *       <value type="null" />
 *     </condition>
 *    </if>
 *   <then>
 *     <attribute location="CurrentThread" />
 *     <attribute constant="Status" />
 *     <value int="$PROCESS_STATUS_RUN_USERMODE"/>
 *   </then>
 *   <else>
 *     <attribute location="CurrentThread" />
 *     <attribute constant="Status" />
 *     <value int="$PROCESS_STATUS_RUN_SYSCALL"/>
 *   </else>
 * </stateChange>
 *
 * @author Florian Wininger
 *
 */

public class XmlStateChange {

    private XmlCondition fCondition = null;

    private XmlStateValue fThenValue = null;

    private XmlStateValue fElseValue = null;

    /**
     * Constructor
     *
     * @param statechange node
     * @param definedStates HashMap with all state values
     */
    XmlStateChange(Node statechange, HashMap<String, String> definedStates) {
        if (statechange.getNodeName().equals(XmlStrings.STATECHANGE)) {
            NodeList childNodes = statechange.getChildNodes();
            Node node = childNodes.item(1);

            Node ifNode = ((Element) statechange).getElementsByTagName(XmlStrings.IF).item(0);
            // the state change have a condition
            if (ifNode != null) {
                fCondition = new XmlCondition(node, definedStates);

                Node thenNode = ((Element) statechange).getElementsByTagName(XmlStrings.THEN).item(0);
                if (thenNode != null) {
                    fThenValue = new XmlStateValue(thenNode.getChildNodes(), definedStates);
                }
                Node elseNode = ((Element) statechange).getElementsByTagName(XmlStrings.ELSE).item(0);
                if (elseNode != null) {
                    fElseValue = new XmlStateValue(elseNode.getChildNodes(), definedStates);
                }
            }
            // the state change have any condition
            else {
                fThenValue = new XmlStateValue(childNodes, definedStates);
            }
        }
    }

    /**
     * @return Get the condition
     */
    public XmlCondition getConditions() {
        return fCondition;
    }

    /**
     * @return Get the default action
     */
    public XmlStateValue getThenValue() {
        return fThenValue;
    }

    /**
     * @return Get the other action
     */
    public XmlStateValue getElseValue() {
        return fElseValue;
    }

}