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

import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
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
public class TmfXmlStateAttribute {

    private enum StateAttributeType {
        ATTRIBUTE_TYPE_NONE,
        ATTRIBUTE_TYPE_CONSTANT,
        ATTRIBUTE_TYPE_EVENTFIELD,
        ATTRIBUTE_TYPE_QUERY,
        ATTRIBUTE_TYPE_LOCATION
    }

    /** Type of attribute */
    private final StateAttributeType fType;

    /** Attribute's name */
    private final String fName;

    /** List of attributes for a query */
    private final List<TmfXmlStateAttribute> fQueryList = new ArrayList<>();

    private final XmlStateProvider fProvider;

    /**
     * Constructor
     *
     * @param attribute
     *            Node of the attribute
     * @param provider
     *            The state provider this state attribute belongs to
     */
    TmfXmlStateAttribute(Element attribute, XmlStateProvider provider) {
        fProvider = provider;

        if (attribute.getNodeName().equals(TmfXmlStrings.CONSTANT)) {
            fType = StateAttributeType.ATTRIBUTE_TYPE_CONSTANT;
            fName = getAttributeString(attribute, TmfXmlStrings.VALUE);
        } else if (attribute.getNodeName().equals(TmfXmlStrings.EVENT_FIELD)) {
            fType = StateAttributeType.ATTRIBUTE_TYPE_EVENTFIELD;
            fName = getAttributeString(attribute, TmfXmlStrings.NAME);
        } else if (attribute.getNodeName().equals(TmfXmlStrings.LOCATION_NAME)) {
            fType = StateAttributeType.ATTRIBUTE_TYPE_LOCATION;
            fName = getAttributeString(attribute, TmfXmlStrings.VALUE);
        } else if (attribute.getNodeName().equals(TmfXmlStrings.QUERY)) {
            NodeList childNodes = attribute.getChildNodes();
            for (int z = 0; z < childNodes.getLength(); z++) {
                Node subAttributeNode = childNodes.item(z);
                if (subAttributeNode.getNodeType() == Node.ELEMENT_NODE) {
                    TmfXmlStateAttribute subAttribute = new TmfXmlStateAttribute((Element) subAttributeNode, fProvider);
                    fQueryList.add(subAttribute);
                }
            }
            fType = StateAttributeType.ATTRIBUTE_TYPE_QUERY;
            fName = null;
        } else {
            throw new IllegalArgumentException("TmfXmlStateAttribute constructor: The XML element is not of the right type"); //$NON-NLS-1$
        }
    }

    /**
     * Get the value of an attribute of the XML element. If the requested value
     * is a pre-defined value in the XML, it returns the real value.
     *
     * @param node
     *            the XML element to get the value from
     * @param name
     *            the attribute's name
     * @return the attribute value
     */
    private String getAttributeString(Element node, String name) {
        String attribute = node.getAttribute(name);
        if (attribute.startsWith(TmfXmlStrings.VARIABLE_PREFIX)) {
            /* search the attribute in the map without the fist character $ */
            attribute = fProvider.getDefinedValue(name.substring(1));
        }
        return attribute;
    }

    /**
     * This method gets the quark for this state attribute in the State System.
     * The method use the ss.getQuarkRelativeAndAdd method in the State System.
     *
     * Unless this attribute is a location, in which case the quark must exist,
     * the quark will be added to the state system.
     *
     * @param event
     *            current event
     * @param i_quark
     *            root quark, use -1 to search the full attribute tree
     * @return the quark describe by attribute.
     */
    public int getAttributeQuark(ITmfEvent event, int i_quark) {
        final ITmfEventField content = event.getContent();
        ITmfStateValue value = TmfStateValue.nullValue();
        int quark = -1;

        ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();

        try {
            switch (fType) {
            case ATTRIBUTE_TYPE_CONSTANT: {
                if (i_quark == -1) {
                    quark = ss.getQuarkAbsoluteAndAdd(fName);
                } else {
                    quark = ss.getQuarkRelativeAndAdd(i_quark, fName);
                }
                break;
            }
            case ATTRIBUTE_TYPE_EVENTFIELD: {
                // exception for CPU which is not in the field
                if (fName.equals(TmfXmlStrings.CPU)) {
                    quark = ss.getQuarkRelativeAndAdd(i_quark, event.getSource());
                }
                else {
                    // stop if the eventfield don't exist
                    if (content.getField(fName) == null) {
                        return quark;
                    }

                    Object field = content.getField(fName).getValue();

                    if (field instanceof String) {
                        String fieldString = (String) field;
                        quark = ss.getQuarkRelativeAndAdd(i_quark, fieldString);
                    } else if (field instanceof Long) {
                        Long fieldLong = (Long) field;
                        quark = ss.getQuarkRelativeAndAdd(i_quark, fieldLong.toString());
                    } else if (field instanceof Integer) {
                        Integer fieldInterger = (Integer) field;
                        quark = ss.getQuarkRelativeAndAdd(i_quark, fieldInterger.toString());
                    }
                }
                break;
            }
            case ATTRIBUTE_TYPE_QUERY: {
                int quarkQuery = -1;

                for (TmfXmlStateAttribute attrib : fQueryList) {
                    quarkQuery = attrib.getAttributeQuark(event, quarkQuery);
                }

                // the query may fail: for example CurrentThread if there
                // has not been a sched_switch event
                if (quarkQuery != -1) {
                    value = ss.queryOngoingState(quarkQuery);
                }

                if (value.getType() == ITmfStateValue.Type.INTEGER) {
                    int result = value.unboxInt();
                    quark = ss.getQuarkRelativeAndAdd(i_quark, String.valueOf(result));
                }
                else if (value.getType() == ITmfStateValue.Type.LONG) {
                    long result = value.unboxLong();
                    quark = ss.getQuarkRelativeAndAdd(i_quark, String.valueOf(result));
                }
                else if (value.getType() == ITmfStateValue.Type.STRING) {
                    String result = value.unboxStr();
                    quark = ss.getQuarkRelativeAndAdd(i_quark, result);
                }
                else { // ITmfStateValue.Type.NULL
                    return -1; // error
                }
                break;
            }
            case ATTRIBUTE_TYPE_LOCATION: {
                quark = i_quark;
                String idLocation = fName;

                for (TmfXmlLocation location : fProvider.getLocations()) {
                    // the good location
                    if (location.getId().equals(idLocation)) {
                        quark = location.getLocationQuark(event, quark);
                    }
                }
                break;
            }
            case ATTRIBUTE_TYPE_NONE:
            default:
                quark = i_quark;
                break;
            }
        } catch (AttributeNotFoundException ae) {
            /*
             * This can be happen before the creation of the node for a query in
             * the state system. Example : current thread before a sched_switch
             */

        } catch (StateValueTypeException e) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            Activator.logError("StateValueTypeException", e); //$NON-NLS-1$
        }

        return quark;
    }

    /**
     * Check whether the XML node is a state attribute node
     *
     * @param node The XML node to check
     * @return whether this XML node is a state attribute
     */
    public static boolean isNodeStateAttribute(Node node) {
        String nodeName = node.getNodeName();
        return nodeName.equals(TmfXmlStrings.CONSTANT) || nodeName.equals(TmfXmlStrings.EVENT_FIELD) ||
                nodeName.equals(TmfXmlStrings.LOCATION_NAME) || nodeName.equals(TmfXmlStrings.QUERY);
    }

}