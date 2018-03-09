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
 * <attribute type="query" />
 *      <attribute constant="CPUs" />
 *      <attribute eventfield="cpu" />
 *      <attribute constant="Current_thread" />
 * </attribute>
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlAttribute {

    /** Type of attribute */
    private int fType = -1;

    /** Attribute's name */
    private String fName = null;

    /** List of attributes for a query */
    private ArrayList<TmfXmlAttribute> fQueryList = null;

    /**
     * List of the defined state values
     */
    private HashMap<String, String> fDefinedStates = new HashMap<>();

    /**
     * Constructor
     *
     * @param attribute
     *            attribute's node
     * @param definedStates
     *            HashMap with all state values
     */
    TmfXmlAttribute(Node attribute, HashMap<String, String> definedStates) {
        fDefinedStates = definedStates;
        setAttribute(attribute);
    }

    /**
     * Constructor
     *
     * @param attribute
     *            Node of the attribute
     */
    TmfXmlAttribute(Node attribute) {
        setAttribute(attribute);
    }

    /**
     * Return the type of the xml attribute. See {@link TmfXmlStrings} for more
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
    public ArrayList<TmfXmlAttribute> getQueryList() {
        return fQueryList;
    }

    /**
     * Get the attribute in a node with the use of the fDefinedStates map to get
     * the real name.
     *
     * @param node
     *            the input node
     * @param name
     *            the attribute's name
     * @return the attribute value
     */
    private String getAttribute(Element node, String name) {
        String attribute = node.getAttribute(name);
        if (attribute.startsWith(TmfXmlStrings.VARIABLE_PREFIX)) {
            // search the attribute in the map without the fist character $
            attribute = fDefinedStates.get(name.substring(1));
        }
        return attribute;
    }

    /**
     * Extract the node's information, set the type and the value
     *
     * @param node
     *            attribute node
     */
    public void setAttribute(Node node) {
        // test for the argument
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element attribute = (Element) node;
            // test for the name of the node
            if (attribute.getNodeName() == TmfXmlStrings.ATTRIBUTE) {
                if (!attribute.getAttribute(TmfXmlStrings.CONSTANT).equals(TmfXmlStrings.NULL)) {
                    fType = TmfXmlStrings.ATTRIBUTE_TYPE_CONSTANT;
                    fName = getAttribute(attribute, TmfXmlStrings.CONSTANT);
                }
                else if (!attribute.getAttribute(TmfXmlStrings.EVENT_FIELD).equals(TmfXmlStrings.NULL)) {
                    fType = TmfXmlStrings.ATTRIBUTE_TYPE_EVENTFIELD;
                    fName = getAttribute(attribute, TmfXmlStrings.EVENT_FIELD);
                }
                else if (attribute.getAttribute(TmfXmlStrings.TYPE).equals(TmfXmlStrings.QUERY)) {
                    NodeList childNodes = node.getChildNodes();
                    ArrayList<TmfXmlAttribute> childAttributes = new ArrayList<>();
                    for (int z = 0; z < childNodes.getLength(); z++) {
                        Node subAttributeNode = childNodes.item(z);
                        TmfXmlAttribute subAttribute = new TmfXmlAttribute(subAttributeNode, fDefinedStates);
                        childAttributes.add(subAttribute);
                    }
                    fType = TmfXmlStrings.ATTRIBUTE_TYPE_QUERY;
                    fQueryList = childAttributes;
                }
                else if (!attribute.getAttribute(TmfXmlStrings.LOCATION).equals(TmfXmlStrings.NULL)) {
                    fType = TmfXmlStrings.ATTRIBUTE_TYPE_LOCATION;
                    fName = getAttribute(attribute, TmfXmlStrings.LOCATION);
                }
            }
        }
    }

}