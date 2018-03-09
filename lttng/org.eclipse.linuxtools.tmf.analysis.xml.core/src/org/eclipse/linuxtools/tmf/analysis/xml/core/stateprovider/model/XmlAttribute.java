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
 * This Class stored a attribute node of the XML
 *
 * <pre>
 * Examples:
 * <attribute constant="Threads" />
 * <attribute type="query">
 * <attribute constant="CPUs" />
 * <attribute eventfield="cpu" />
 * <attribute constant="Current_thread" />
 * </attribute>
 * </pre>
 *
 * @author Florian Wininger
 */
public class XmlAttribute {

    /** Type of attribute */
    private int fType = -1;

    /** Attribute's name */
    private String fName = null;

    /** List of attributes for a query */
    private ArrayList<XmlAttribute> fQueryList = null;

    /**
     * List of the defined state values
     */
    private HashMap<String, String> fDefinedStates = new HashMap<String, String>();

    /**
     * Constructor
     *
     * @param attribute
     *            : attribute's node
     * @param definedStates
     *            : HashMap with all state values
     */
    XmlAttribute(Node attribute, HashMap<String, String> definedStates) {
        fDefinedStates = definedStates;
        setAttribute(attribute);
    }

    /**
     * Constructor
     *
     * @param attribute
     *            Node of the attribute
     */
    XmlAttribute(Node attribute) {
        setAttribute(attribute);
    }

    /**
     * Return the type of the xml attribute. See {@link XmlStrings} for more
     * informations
     *
     * @return the type of the XMLAttributes
     */
    public int getType() {
        return fType;
    }

    /**
     * @return the relative name of the Attribute in the State System
     */
    public String getValue() {
        return fName;
    }

    /**
     * @return the list of the XMLAttribute to a path in the State System
     */
    public ArrayList<XmlAttribute> getQueryList() {
        return fQueryList;
    }

    /**
     * Get the attribute in a node with the use of the fDefinedStates map to get
     * the real name
     *
     * @param node
     * @param name
     * @return the attribute value
     */
    private String getAttribute(Element node, String name) {
        String attribute = node.getAttribute(name);
        if (attribute.startsWith(XmlStrings.VARIABLE_PREFIX)) {
            // search the attribute in the map without the fist character $
            attribute = fDefinedStates.get(name.substring(1));
        }
        return attribute;
    }

    /**
     * Extract the node's information, set the type and the value
     *
     * @param node
     *            of the attribute
     */
    public void setAttribute(Node node) {
        // test for the argument
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element attribute = (Element) node;
            // test for the name of the node
            if (attribute.getNodeName() == XmlStrings.ATTRIBUTE) {
                if (!attribute.getAttribute(XmlStrings.CONSTANT).equals(XmlStrings.NULL)) {
                    fType = XmlStrings.ATTRIBUTE_TYPE_CONSTANT;
                    fName = getAttribute(attribute, XmlStrings.CONSTANT);
                }
                else if (!attribute.getAttribute(XmlStrings.EVENTFIELD).equals(XmlStrings.NULL)) {
                    fType = XmlStrings.ATTRIBUTE_TYPE_EVENTFIELD;
                    fName = getAttribute(attribute, XmlStrings.EVENTFIELD);
                }
                else if (attribute.getAttribute(XmlStrings.TYPE).equals(XmlStrings.QUERY)) {
                    NodeList childNodes = node.getChildNodes();
                    ArrayList<XmlAttribute> childAttributes = new ArrayList<XmlAttribute>();
                    for (int z = 0; z < childNodes.getLength(); z++) {
                        Node subAttributeNode = childNodes.item(z);
                        XmlAttribute subAttribute = new XmlAttribute(subAttributeNode, fDefinedStates);
                        childAttributes.add(subAttribute);
                    }
                    fType = XmlStrings.ATTRIBUTE_TYPE_QUERY;
                    fQueryList = childAttributes;
                }
                else if (!attribute.getAttribute(XmlStrings.LOCATION).equals(XmlStrings.NULL)) {
                    fType = XmlStrings.ATTRIBUTE_TYPE_LOCATION;
                    fName = getAttribute(attribute, XmlStrings.LOCATION);
                }
            }
        }
    }

}