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

import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class implement a State Value is the XML state provider
 *
 *   <attribute location="CurrentThread" />
 *   <attribute constant="System_call" />
 *   <value type="null" />
 *
 * @author Florian Wininger
 *
 */

public class XmlStateValue {

    // path in the State System
    private ArrayList<XmlAttribute> fPath = new ArrayList<XmlAttribute>();

    // value
    private ITmfStateValue fValue;
    private String fStringValue;
    private ArrayList<XmlAttribute> fQueryValue;

    // value type
    private int fType = 0;
    // forced value type
    private int fForcedType = 0;

    // eventfield
    private String fEventfield = null;

    /**
     * List of the state value define
     */
    private HashMap<String, String> fDefinedStates = new HashMap<String, String>();

    /**
     * Constructor
     *
     * @param attribute : node of the attribute
     * @param definedStates : HashMap with all state values
     */
    XmlStateValue(NodeList listofattributes, HashMap<String, String> definedStates) {
        fDefinedStates = definedStates;
        // level attribute path / value
        for (int k = 0; k < listofattributes.getLength(); k++) {
            Node attribute = listofattributes.item(k);
            loadAttributes(attribute);
        }
    }

    /**
     * Add the attribute in the attribute list
     *
     * @param i_attribute
     */
    private void addAttribute(XmlAttribute i_attribute) {
        fPath.add(i_attribute);
    }

    /**
     * Load the attribute Node Type
     * <value>
     * <attribute>
     * <eventfield>
     *
     * @param attributeNode
     */
    private void loadAttributes(Node attributeNode) {
        if (attributeNode.getNodeType() == Node.ELEMENT_NODE) {
            Element attribute = (Element) attributeNode;

            // we set the value
            if (attribute.getNodeName() == XmlStrings.VALUE) {
                setValue(attributeNode);
            }
            // we set the attribute
            else if (attribute.getNodeName() == XmlStrings.ATTRIBUTE) {
                XmlAttribute Xattribute = new XmlAttribute(attributeNode, fDefinedStates);
                addAttribute(Xattribute);
            }
            // we set the event field
            else if (attribute.getNodeName() == XmlStrings.EVENTFIELD) {
                fEventfield = new String(attribute.getAttribute(XmlStrings.NAME));
            }
        }
    }

    /**
     * Get the attribute of a element AND convert if necessary with the
     * statesMap
     *
     * @param attributeElement
     * @param name
     * @return the value of the attribute
     */
    private String getAttribute(Element attributeElement, String name) {
        String attribute = attributeElement.getAttribute(name);
        if (attribute.startsWith(XmlStrings.VARIABLE_PREFIX)) {
            attribute = fDefinedStates.get(attribute.substring(1));
        }
        return attribute;
    }

    /**
     * Set the value for a <value />
     *
     * @param attribute
     */
    private void setValue(Node attribute) {

        if (attribute.getNodeType() == Node.ELEMENT_NODE) {
            Element attri = (Element) attribute;

            // we set the value
            if (attri.getNodeName() == XmlStrings.VALUE) {
                if (!attri.getAttribute(XmlStrings.STRING).equals(XmlStrings.NULL)) {
                    fValue = TmfStateValue.newValueString(getAttribute(attri, XmlStrings.STRING));
                    fType = XmlStrings.VALUE_TYPE_TMFSTATE;
                }
                else if (!attri.getAttribute(XmlStrings.INT).equals(XmlStrings.NULL)) {
                    fValue = TmfStateValue.newValueInt(Integer.parseInt(getAttribute(attri, XmlStrings.INT)));
                    fType = XmlStrings.VALUE_TYPE_TMFSTATE;
                }
                else if (!attri.getAttribute(XmlStrings.LONG).equals(XmlStrings.NULL)) {
                    fValue = TmfStateValue.newValueLong(Long.parseLong(attri.getAttribute(XmlStrings.LONG)));
                    fType = XmlStrings.VALUE_TYPE_TMFSTATE;
                }
                else if (!attri.getAttribute(XmlStrings.EVENTFIELD).equals(XmlStrings.NULL)) {
                    fStringValue = getAttribute(attri, XmlStrings.EVENTFIELD);
                    fType = XmlStrings.VALUE_TYPE_EVENTFIELD;
                }
                else if (attri.getAttribute(XmlStrings.TYPE).equals(XmlStrings.QUERY)) {
                    NodeList childAttributes = attribute.getChildNodes();

                    ArrayList<XmlAttribute> XMLchildAttributes = new ArrayList<XmlAttribute>();

                    for (int z = 0; z < childAttributes.getLength(); z++) {
                        Node sousattribute = childAttributes.item(z);
                        XmlAttribute Xsousattribute = new XmlAttribute(sousattribute, fDefinedStates);
                        XMLchildAttributes.add(Xsousattribute);
                    }

                    fQueryValue = XMLchildAttributes;
                    fType = XmlStrings.VALUE_TYPE_QUERY;
                }
                else if (attri.getAttribute(XmlStrings.TYPE).equals(XmlStrings.NULL)) {
                    fValue = TmfStateValue.nullValue();
                    fType = XmlStrings.VALUE_TYPE_TMFSTATE;
                }
                else if (attri.getAttribute(XmlStrings.TYPE).equals(XmlStrings.EVENTNAME)) {
                    fType = XmlStrings.VALUE_TYPE_EVENTNAME;
                }
                else if (attri.getAttribute(XmlStrings.TYPE).equals(XmlStrings.DELETE)) {
                    fType = XmlStrings.VALUE_TYPE_DELETE;
                }

                /*
                 * forcedtype allow to convert the type of the eventfield
                 * exemple : the TID of a event arrived with a LONG format but
                 * we want to store the data in an INT
                 */
                if (attri.getAttribute(XmlStrings.FORCEDTYPE).equals(XmlStrings.STRING)) {
                    fForcedType = XmlStrings.VALUE_TYPE_STRING;
                }
                else if (attri.getAttribute(XmlStrings.FORCEDTYPE).equals(XmlStrings.INT)) {
                    fForcedType = XmlStrings.VALUE_TYPE_INT;
                }
                else if (attri.getAttribute(XmlStrings.FORCEDTYPE).equals(XmlStrings.LONG)) {
                    fForcedType = XmlStrings.VALUE_TYPE_LONG;
                }
            }
        }
    }

    /**
     * @return the list of Attribute to have the path in the State System
     */
    public ArrayList<XmlAttribute> getAttributes() {
        return fPath;
    }

    /**
     * @return the type of the value
     */
    public int getTypeValue() {
        return fType;
    }

    /**
     * @return a TMFStateValue for null, int, long, string value
     */
    public ITmfStateValue getValueTMF() {
        return fValue;
    }

    /**
     * @return the String of the value
     */
    public String getValueString() {
        return fStringValue;
    }

    /**
     * @return the path of the query of the value
     */
    public ArrayList<XmlAttribute> getValueQuery() {
        return fQueryValue;
    }

    /**
     * @return Get the EventFieldName
     */
    public String getEventFieldName() {
        return fEventfield;
    }

    /**
     * @return the real type of the eventfield
     */
    public int getForcedType() {
        return fForcedType;
    }
}