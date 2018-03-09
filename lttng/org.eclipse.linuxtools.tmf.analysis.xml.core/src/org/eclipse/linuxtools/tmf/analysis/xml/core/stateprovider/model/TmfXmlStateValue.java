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

import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class implement a State Value is the XML state provider
 *
 * <pre>
 * Example:
 *   <attribute location="CurrentThread" />
 *   <attribute constant="System_call" />
 *   <value type="null" />
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlStateValue {

    /* path in the State System */
    private ArrayList<TmfXmlAttribute> fPath = new ArrayList<>();

    /* values */
    private ITmfStateValue fValue;
    private String fStringValue = null;
    private ArrayList<TmfXmlAttribute> fQueryValue;

    /* value type */
    private int fType = TmfXmlStrings.VALUE_NULL;

    /* stack action */
    private int fStack = 0;

    /* forced value type */
    private int fForcedType = 0;

    /* eventfield */
    private String fEventfield = null;

    /**
     * List of the state value define
     */
    private HashMap<String, String> fDefinedStates = new HashMap<>();

    /**
     * Constructor
     *
     * @param attribute
     *            node of the attribute
     * @param definedStates
     *            HashMap with all state values
     */
    TmfXmlStateValue(NodeList listofattributes, HashMap<String, String> definedStates) {
        fDefinedStates = definedStates;
        // level attribute path / value
        for (int k = 0; k < listofattributes.getLength(); k++) {
            Node attribute = listofattributes.item(k);
            loadAttributes(attribute);
        }
    }

    private void addAttribute(TmfXmlAttribute iattribute) {
        fPath.add(iattribute);
    }

    /**
     * Load the attribute Node Type <value> <attribute> <eventfield>
     */
    private void loadAttributes(Node attributeNode) {
        if (attributeNode.getNodeType() == Node.ELEMENT_NODE) {
            Element attribute = (Element) attributeNode;

            // we set the value
            if (attribute.getNodeName() == TmfXmlStrings.VALUE) {
                setValue(attributeNode);
            }
            // we set the attribute
            else if (attribute.getNodeName() == TmfXmlStrings.ATTRIBUTE) {
                TmfXmlAttribute Xattribute = new TmfXmlAttribute(attributeNode, fDefinedStates);
                addAttribute(Xattribute);
            }
            // we set the event field
            else if (attribute.getNodeName() == TmfXmlStrings.EVENT_FIELD) {
                fEventfield = new String(attribute.getAttribute(TmfXmlStrings.NAME));
            }
        }
    }

    /**
     * Get the attribute of a element AND convert if necessary with the
     * statesMap
     */
    private String getAttribute(Element attributeElement, String name) {
        String attribute = attributeElement.getAttribute(name);
        if (attribute.startsWith(TmfXmlStrings.VARIABLE_PREFIX)) {
            attribute = fDefinedStates.get(attribute.substring(1));
        }
        return attribute;
    }

    /**
     * Set the value for a <value />
     */
    private void setValue(Node attribute) {

        if (attribute.getNodeType() == Node.ELEMENT_NODE) {
            Element attri = (Element) attribute;

            /* Set the value */
            if (attri.getNodeName() == TmfXmlStrings.VALUE) {
                if (!attri.getAttribute(TmfXmlStrings.STRING).equals(TmfXmlStrings.NULL)) {
                    fValue = TmfStateValue.newValueString(getAttribute(attri, TmfXmlStrings.STRING));
                    fType = TmfXmlStrings.VALUE_TYPE_TMFSTATE;
                }
                else if (!attri.getAttribute(TmfXmlStrings.INT).equals(TmfXmlStrings.NULL)) {
                    fValue = TmfStateValue.newValueInt(Integer.parseInt(getAttribute(attri, TmfXmlStrings.INT)));
                    fType = TmfXmlStrings.VALUE_TYPE_TMFSTATE;
                }
                else if (!attri.getAttribute(TmfXmlStrings.LONG).equals(TmfXmlStrings.NULL)) {
                    fValue = TmfStateValue.newValueLong(Long.parseLong(attri.getAttribute(TmfXmlStrings.LONG)));
                    fType = TmfXmlStrings.VALUE_TYPE_TMFSTATE;
                }
                else if (!attri.getAttribute(TmfXmlStrings.EVENT_FIELD).equals(TmfXmlStrings.NULL)) {
                    fStringValue = getAttribute(attri, TmfXmlStrings.EVENT_FIELD);
                    fType = TmfXmlStrings.VALUE_TYPE_EVENTFIELD;
                }
                else if (attri.getAttribute(TmfXmlStrings.TYPE).equals(TmfXmlStrings.QUERY)) {
                    NodeList childAttributes = attribute.getChildNodes();

                    ArrayList<TmfXmlAttribute> XMLchildAttributes = new ArrayList<>();

                    for (int z = 0; z < childAttributes.getLength(); z++) {
                        Node sousattribute = childAttributes.item(z);
                        TmfXmlAttribute Xsousattribute = new TmfXmlAttribute(sousattribute, fDefinedStates);
                        XMLchildAttributes.add(Xsousattribute);
                    }

                    fQueryValue = XMLchildAttributes;
                    fType = TmfXmlStrings.VALUE_TYPE_QUERY;
                }
                else if (attri.getAttribute(TmfXmlStrings.TYPE).equals(TmfXmlStrings.NULL)) {
                    fValue = TmfStateValue.nullValue();
                    fType = TmfXmlStrings.VALUE_TYPE_TMFSTATE;
                }
                else if (attri.getAttribute(TmfXmlStrings.TYPE).equals(TmfXmlStrings.EVENT_NAME)) {
                    fType = TmfXmlStrings.VALUE_TYPE_EVENTNAME;
                }
                else if (attri.getAttribute(TmfXmlStrings.TYPE).equals(TmfXmlStrings.DELETE)) {
                    fType = TmfXmlStrings.VALUE_TYPE_DELETE;
                }

                if (attri.getAttribute(TmfXmlStrings.TYPE).equals(TmfXmlStrings.INCREMENT)) {
                    if (fType == TmfXmlStrings.VALUE_TYPE_TMFSTATE) {
                        fType = TmfXmlStrings.VALUE_TYPE_INCREMENT_TMFSTATE;
                    }
                    else if (fType == TmfXmlStrings.VALUE_TYPE_EVENTFIELD) {
                        fType = TmfXmlStrings.VALUE_TYPE_INCREMENT_EVENTFIELD;
                    }
                    else {
                        fType = TmfXmlStrings.VALUE_TYPE_INCREMENT;
                    }
                }

                /*
                 * forcedtype allow to convert the type of the eventfield
                 * exemple : the TID of a event arrived with a LONG format but
                 * we want to store the data in an INT
                 */
                if (attri.getAttribute(TmfXmlStrings.FORCEDTYPE).equals(TmfXmlStrings.STRING)) {
                    fForcedType = TmfXmlStrings.VALUE_TYPE_STRING;
                }
                else if (attri.getAttribute(TmfXmlStrings.FORCEDTYPE).equals(TmfXmlStrings.INT)) {
                    fForcedType = TmfXmlStrings.VALUE_TYPE_INT;
                }
                else if (attri.getAttribute(TmfXmlStrings.FORCEDTYPE).equals(TmfXmlStrings.LONG)) {
                    fForcedType = TmfXmlStrings.VALUE_TYPE_LONG;
                }

                /*
                 * Stack Actions : allow to define a stack with PUSH/POP/PEEK methods
                 */
                if (attri.getAttribute(TmfXmlStrings.STACK).equals(TmfXmlStrings.PUSH)) {
                    fStack = TmfXmlStrings.VALUE_TYPE_PUSH;
                }
                else if (attri.getAttribute(TmfXmlStrings.STACK).equals(TmfXmlStrings.POP)) {
                    fStack = TmfXmlStrings.VALUE_TYPE_POP;
                }
                else if (attri.getAttribute(TmfXmlStrings.STACK).equals(TmfXmlStrings.PEEK)) {
                    fStack = TmfXmlStrings.VALUE_TYPE_PEEK;
                }

            }
        }
    }

    /**
     * @return the list of Attribute to have the path in the State System
     */
    public ArrayList<TmfXmlAttribute> getAttributes() {
        return fPath;
    }

    /**
     * @return the type of the value
     */
    public int getTypeValue() {
        return fType;
    }

    /**
     * @return the stack's action
     */
    public int getStackAction() {
        return fStack;
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
    public ArrayList<TmfXmlAttribute> getValueQuery() {
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