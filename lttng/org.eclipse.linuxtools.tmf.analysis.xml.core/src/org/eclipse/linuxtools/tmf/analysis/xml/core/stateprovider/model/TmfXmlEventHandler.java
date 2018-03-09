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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class implements an EventHandler in the XML state provider
 *
 * <pre>
 * example:
 * <eventHandler eventname="eventName">
 *  <stateChange>
 *      ...
 *  </stateChange>
 *  <stateChange>
 *      ...
 *  </stateChange>
 * </eventHandler>
 * </pre>
 *
 * @author Florian Wininger
 *
 */
public class TmfXmlEventHandler {

    /* list of states changes */
    private ArrayList<TmfXmlStateChange> fStateChangeList;

    /* name */
    private String fName;

    /**
     * Constructor
     *
     * @param node
     *            XML Evenhandler node
     * @param definedStates
     *            HashMap of state variables
     */
    public TmfXmlEventHandler(Node node, HashMap<String, String> definedStates) {
        fStateChangeList = new ArrayList<>();
        load(node, definedStates);
    }

    private void load(Node node, HashMap<String, String> definedStates) {
        fName = ((Element) node).getAttribute(TmfXmlStrings.EVENT_NAME);

        NodeList nodesChange = node.getChildNodes();
        // level statechange
        for (int j = 0; j < nodesChange.getLength(); j++) {
            Node nodechange = nodesChange.item(j);
            if (nodechange.getNodeType() == Node.ELEMENT_NODE) {
                TmfXmlStateChange stateChange = new TmfXmlStateChange(nodechange, definedStates);
                fStateChangeList.add(stateChange);
            }
        }
    }

    /**
     * @return list of state changes
     */
    public ArrayList<TmfXmlStateChange> getStateChanges() {
        return fStateChangeList;
    }

    /**
     * @return name of the eventHandler
     */
    public String getName() {
        return fName;
    }

}