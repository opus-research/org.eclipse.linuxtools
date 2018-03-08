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

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class implement a Location is the XML state provider
 *
 *  <location id="CurrentCPU">
 *    <attribute constant="CPUs" />
 *    <attribute eventfield="cpu" />
 *    ...
 *  </location>
 *
 * @author Florian Wininger
 *
 */

public class XmlLocation {

    /**
     * Path in the State System
     */
    private ArrayList<XmlAttribute> fPath = new ArrayList<XmlAttribute>();

    /**
     * ID : name of the location
     */
    private String fId;

    /**
     * List of the defined state values
     */
    private HashMap<String, String> fDefinedStates;

    /**
     * Implement a location
     * @param location node
     * @param definedStates HashMap of state variables
     */
    public XmlLocation(Node location, HashMap<String, String> definedStates) {
        fDefinedStates = definedStates;
        fId = ((Element) location).getAttribute(XmlStrings.ID);

        NodeList listofattributes = location.getChildNodes();
        // attribute level
        for (int k = 0; k < listofattributes.getLength(); k++) {
            loadAttributes(listofattributes.item(k));
        }
    }

    private void loadAttributes(Node attribute) {
        if (attribute.getNodeType() == Node.ELEMENT_NODE) {
            if (attribute.getNodeName() == XmlStrings.ATTRIBUTE)  {
                XmlAttribute XAttribute = new XmlAttribute(attribute, fDefinedStates);
                fPath.add(XAttribute);
            }
        }
    }

    /**
     * @return get the id="" of a location
     */
    public String getId() {
        return fId;
    }

    /**
     * @return get the List of Attribute to have the path in the State System
     */
    public ArrayList<XmlAttribute> getPath() {
        return fPath;
    }

}