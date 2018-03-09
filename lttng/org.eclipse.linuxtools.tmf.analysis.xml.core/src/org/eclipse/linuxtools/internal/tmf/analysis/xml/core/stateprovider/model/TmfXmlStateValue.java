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

import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
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
    private final List<TmfXmlStateAttribute> fPath = new ArrayList<>();

    /* values */
    private ITmfStateValue fValue;
    private String fStringValue = null;
    private List<TmfXmlStateAttribute> fQueryValue;

    /* value type */
    private int fType = TmfXmlStrings.VALUE_NULL;

    /* stack action */
    private int fStack = 0;

    /* forced value type */
    private int fForcedType = 0;

    /* eventfield */
    private String fEventfield = null;

    private final XmlStateProvider fProvider;

    /**
     * Constructor. It receives a list of XML nodes. The last one is a state
     * value and the other ones are state attributes.
     *
     * @param attribute
     *            nodes of the state attributes and state value
     * @param provider
     *            The state provider this state value belongs to
     */
    TmfXmlStateValue(NodeList attributeList, XmlStateProvider provider) {
        fProvider = provider;
        // level attribute path / value
        for (int i = 0; i < attributeList.getLength(); i++) {
            Node attribute = attributeList.item(i);
            loadAttributes(attribute);
        }

    }

    private void addAttribute(TmfXmlStateAttribute iattribute) {
        fPath.add(iattribute);
    }

    /**
     * Load the attribute Node Type <value> <attribute> <eventfield>
     */
    private void loadAttributes(Node attributeNode) {
        if (attributeNode.getNodeType() == Node.ELEMENT_NODE) {
            Element attribute = (Element) attributeNode;

            // we set the value
            if (attribute.getNodeName().equals(TmfXmlStrings.STATE_VALUE)) {
                setValue(attributeNode);
            }
            // we set the attribute
            else if (TmfXmlStateAttribute.isNodeStateAttribute(attribute)) {
                TmfXmlStateAttribute Xattribute = new TmfXmlStateAttribute((Element) attributeNode, fProvider);
                addAttribute(Xattribute);
            }
            // we set the event field
            else if (attribute.getNodeName() == TmfXmlStrings.FIELD) {
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
            attribute = fProvider.getDefinedValue(attribute.substring(1));
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
            if (attri.getNodeName() == TmfXmlStrings.STATE_VALUE) {
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

                    ArrayList<TmfXmlStateAttribute> XMLchildAttributes = new ArrayList<>();

                    for (int z = 0; z < childAttributes.getLength(); z++) {
                        Node sousattribute = childAttributes.item(z);
                        if (sousattribute.getNodeType() == Node.ELEMENT_NODE) {
                            TmfXmlStateAttribute Xsousattribute = new TmfXmlStateAttribute((Element) sousattribute, fProvider);
                            XMLchildAttributes.add(Xsousattribute);
                        }
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
                 * Stack Actions : allow to define a stack with PUSH/POP/PEEK
                 * methods
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
     * Get the value of a Xml stateValue.
     *
     * @param event
     *            currentevent.
     * @return the value
     * @throws AttributeNotFoundException
     *             for the query in the state system
     */
    public ITmfStateValue getValue(ITmfEvent event)
            throws AttributeNotFoundException {
        ITmfStateValue value = TmfStateValue.nullValue();
        final ITmfEventField content = event.getContent();
        ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();

        // If the value is a ITmfStateValue
        if (getTypeValue() == TmfXmlStrings.VALUE_TYPE_TMFSTATE
                || getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT_TMFSTATE) {
            value = getValueTMF();
        }
        // If the value want to access to a event field.
        else if (getTypeValue() == TmfXmlStrings.VALUE_TYPE_EVENTFIELD
                || getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT_EVENTFIELD) {

            // Exception for "CPU"
            // FIXME : Nameclash if a eventfield have "cpu" for name.
            if (getValueString().equals(TmfXmlStrings.CPU)) {
                value = TmfStateValue.newValueInt(Integer.valueOf(event.getSource()));
            }
            else {
                // If the field doesn't exist, return a nullValue.
                if (content.getField(getValueString()) == null) {
                    return value;
                }

                Object field = content.getField(getValueString()).getValue();

                // Try to find the good type. The type can be forced by
                // "forcedType" argument. In the case of a thread id, the event
                // field is a long, but it's stored with a int.
                if (field instanceof String) {
                    String fieldString = (String) field;
                    value = TmfStateValue.newValueString(fieldString);

                    if (getForcedType() == TmfXmlStrings.VALUE_TYPE_INT) {
                        value = TmfStateValue.newValueInt(Integer.parseInt(fieldString));
                    }
                    else if (getForcedType() == TmfXmlStrings.VALUE_TYPE_LONG) {
                        value = TmfStateValue.newValueLong(Long.parseLong(fieldString));
                    }
                } else if (field instanceof Long) {
                    Long fieldLong = (Long) field;
                    value = TmfStateValue.newValueLong(fieldLong);

                    if (getForcedType() == TmfXmlStrings.VALUE_TYPE_INT) {
                        value = TmfStateValue.newValueInt(fieldLong.intValue());
                    }
                    else if (getForcedType() == TmfXmlStrings.VALUE_TYPE_STRING) {
                        value = TmfStateValue.newValueString(fieldLong.toString());
                    }
                } else if (field instanceof Integer) {
                    Integer fieldInterger = (Integer) field;
                    value = TmfStateValue.newValueInt(fieldInterger);

                    if (getForcedType() == TmfXmlStrings.VALUE_TYPE_LONG) {
                        value = TmfStateValue.newValueLong(fieldInterger.longValue());
                    }
                    else if (getForcedType() == TmfXmlStrings.VALUE_TYPE_STRING) {
                        value = TmfStateValue.newValueString(fieldInterger.toString());
                    }
                }
            }
        }
        // If the value make a new query in the state system.
        else if (getTypeValue() == TmfXmlStrings.VALUE_TYPE_QUERY) {
            int quarkQuery = -1;

            for (TmfXmlStateAttribute attribute : getValueQuery()) {
                quarkQuery = attribute.getAttributeQuark(event, quarkQuery);
                if (quarkQuery == -1) {
                    // the query is not valid, we stop the state change
                    break;
                }
            }
            // the query can fail : example CurrentThread don't exist if we have
            // any sched_switch
            if (quarkQuery != -1) {
                value = ss.queryOngoingState(quarkQuery);
            }

        }
        // If the value is the eventname.
        else if (getTypeValue() == TmfXmlStrings.VALUE_TYPE_EVENTNAME) {
            value = TmfStateValue.newValueString(event.getType().getName());
        }

        return value;
    }

    /**
     * Return the State Value of a StateChange
     *
     * @param event
     *            current event
     * @return the StateValue
     */
    public ITmfStateValue getEventField(ITmfEvent event) {

        ITmfStateValue value = TmfStateValue.nullValue();
        final ITmfEventField content = event.getContent();

        if (getEventFieldName().equals(TmfXmlStrings.CPU)) {
            value = TmfStateValue.newValueInt(Integer.valueOf(event.getSource()));
        }
        else {
            if (content.getField(getEventFieldName()) == null) {
                return value;
            }

            Object field = content.getField(getEventFieldName()).getValue();

            if (field instanceof String) {
                String fieldString = (String) field;
                value = TmfStateValue.newValueString(fieldString);

                if (getForcedType() == TmfXmlStrings.VALUE_TYPE_INT) {
                    value = TmfStateValue.newValueInt(Integer.parseInt(fieldString));
                }
                else if (getForcedType() == TmfXmlStrings.VALUE_TYPE_LONG) {
                    value = TmfStateValue.newValueLong(Long.parseLong(fieldString));
                }
            } else if (field instanceof Long) {
                Long fieldLong = (Long) field;
                value = TmfStateValue.newValueLong(fieldLong);

                if (getForcedType() == TmfXmlStrings.VALUE_TYPE_INT) {
                    value = TmfStateValue.newValueInt(fieldLong.intValue());
                }
                else if (getForcedType() == TmfXmlStrings.VALUE_TYPE_STRING) {
                    value = TmfStateValue.newValueString(fieldLong.toString());
                }
            } else if (field instanceof Integer) {
                Integer fieldInterger = (Integer) field;
                value = TmfStateValue.newValueInt(fieldInterger);

                if (getForcedType() == TmfXmlStrings.VALUE_TYPE_LONG) {
                    value = TmfStateValue.newValueLong(fieldInterger.longValue());
                }
                else if (getForcedType() == TmfXmlStrings.VALUE_TYPE_STRING) {
                    value = TmfStateValue.newValueString(fieldInterger.toString());
                }
            }
        }
        return value;
    }

    /**
     * @return the list of Attribute to have the path in the State System
     */
    public List<TmfXmlStateAttribute> getAttributes() {
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
    public List<TmfXmlStateAttribute> getValueQuery() {
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

    /**
     * Execute the state change for an event. It validates the condition and
     * executes the required change.
     *
     * @param event
     *            The event to process
     * @throws AttributeNotFoundException
     *             Pass through the exception it received
     * @throws TimeRangeException
     *             Pass through the exception it received
     * @throws StateValueTypeException
     *             Pass through the exception it received
     */
    public void handleEvent(ITmfEvent event) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException {
        int quark = -1;
        for (TmfXmlStateAttribute attribute : getAttributes()) {
            quark = attribute.getAttributeQuark(event, quark);
            // the query is not valid, we stop the state
            // change
            if (quark == -1) {
                throw new AttributeNotFoundException();
            }
        }
        if (quark == -1) {
            throw new AttributeNotFoundException();
        }

        ITmfStateSystemBuilder ss = fProvider.getAssignedStateSystem();
        long ts = event.getTimestamp().getValue();

        // auto increment the node
        if (getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT) {
            ss.incrementAttribute(ts, quark);
        }
        else if (getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT_TMFSTATE) {
            if (getValueTMF().getType() == ITmfStateValue.Type.INTEGER) {
                int increment = getValueTMF().unboxInt();
                int currentValue = ss.queryOngoingState(quark).unboxInt();
                ITmfStateValue value = TmfStateValue.newValueInt(increment + currentValue);
                ss.modifyAttribute(ts, value, quark);
            }
        }
        else if (getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT_EVENTFIELD) {
            int increment = 0;
            ITmfStateValue incrementValue = getValue(event);
            if (incrementValue.getType() == ITmfStateValue.Type.INTEGER) {
                increment = incrementValue.unboxInt();
            }
            else if (incrementValue.getType() == ITmfStateValue.Type.LONG) {
                increment = (int) incrementValue.unboxLong();
            }
            int currentValue = ss.queryOngoingState(quark).unboxInt();
            ITmfStateValue value = TmfStateValue.newValueInt(increment + currentValue);
            ss.modifyAttribute(ts, value, quark);
        }
        // update the node
        else if (getTypeValue() != TmfXmlStrings.VALUE_TYPE_DELETE) {
            ITmfStateValue value = getValue(event);
            if (getStackAction() == TmfXmlStrings.VALUE_TYPE_PUSH) {
                ss.pushAttribute(ts, value, quark);
            } else if (getStackAction() == TmfXmlStrings.VALUE_TYPE_POP) {
                ss.popAttribute(ts, quark);
            } else {
                ss.modifyAttribute(ts, value, quark);
            }
        }
        // delete the node
        else {
            ss.removeAttribute(ts, quark);
        }

    }
}