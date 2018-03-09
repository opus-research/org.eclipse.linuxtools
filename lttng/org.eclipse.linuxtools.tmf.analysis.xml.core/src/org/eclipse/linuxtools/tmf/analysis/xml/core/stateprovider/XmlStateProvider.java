/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.Messages;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlAttribute;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlCondition;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlEventHandler;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlLocation;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlStateChange;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.osgi.util.NLS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This is the state change input plug-in for TMF's state system which handles
 * the XML Format
 *
 * @author Florian Wininger
 */
public class XmlStateProvider extends AbstractTmfStateProvider {

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

    private IPath fFilePath;

    /** StateID */
    private String fStateId;

    /** List of all EventHandler */
    private ArrayList<TmfXmlEventHandler> ssprovider = new ArrayList<>();

    /** List of all Location. */
    protected ArrayList<TmfXmlLocation> fLocations = new ArrayList<>();

    /** HashMap for StateValue. */
    protected HashMap<String, String> fStateValues = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plug-in.
     *
     * FIXME: For now we only support CTF trace types. It may not be too hard to
     * get rid of this requirement, but it hasn't been tested, so we leave it
     *
     * @param trace
     *            The CTF trace
     * @param stateid
     *            The state system id, corresponding to the analysis_id
     *            attribute of the state provider element of the XML file
     * @param file
     *            Path to the XML file containing the state provider definition
     */
    public XmlStateProvider(CtfTmfTrace trace, String stateid, IPath file) {
        super(trace, CtfTmfEvent.class, stateid);
        fStateId = stateid;
        fFilePath = file;
        loadXML();
    }

    /**
     * Get the state id of the state provider
     *
     * @return The state id of the state provider
     */
    public String getStateId() {
        return fStateId;
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public XmlStateProvider getNewInstance() {
        return new XmlStateProvider((CtfTmfTrace) this.getTrace(), getStateId(), fFilePath);
    }

    @Override
    protected void eventHandle(ITmfEvent ev) {
        CtfTmfEvent event = (CtfTmfEvent) ev;
        makeChange(event);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Loads the XML file and returns the element at the root of the current
     * state provider.
     *
     * @return The XML node at the root of the state provider
     */
    protected Node loadXMLNode() {

        try {
            File XMLFile = fFilePath.toFile();
            if (XMLFile == null || !XMLFile.exists() || !XMLFile.isFile()) {
                return null;
            }

            /* Load the XML File */
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XMLFile);
            doc.getDocumentElement().normalize();

            /* get the state providers and find the corresponding one */
            NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);
            Element stateproviderNode = null;

            for (int i = 0; i < stateproviderNodes.getLength(); i++) {
                Element node = (Element) stateproviderNodes.item(i);
                String analysisid = node.getAttribute(TmfXmlStrings.ANALYSIS_ID);
                if (analysisid.equals(fStateId)) {
                    stateproviderNode = node;
                }
            }

            if (stateproviderNode == null) {
                return null;
            }

            // parser for stateValues
            NodeList definedStateNodes = stateproviderNode.getElementsByTagName(TmfXmlStrings.STATE_VALUE);

            for (int i = 0; i < definedStateNodes.getLength(); i++) {
                Element node = (Element) definedStateNodes.item(i);
                fStateValues.put(node.getAttribute(TmfXmlStrings.NAME), node.getAttribute(TmfXmlStrings.VALUE));
            }

            // parser for the location
            NodeList locationNodes = stateproviderNode.getElementsByTagName(TmfXmlStrings.LOCATION);
            fLocations = new ArrayList<>();

            for (int i = 0; i < locationNodes.getLength(); i++) {
                Node node = locationNodes.item(i);
                TmfXmlLocation location = new TmfXmlLocation(node, fStateValues);
                fLocations.add(location);
            }

            return stateproviderNode;
        } catch (ParserConfigurationException e) {
            Activator.logError("Error loading XML file", e); //$NON-NLS-1$
        } catch (SAXException e) {
            Activator.logError(NLS.bind(Messages.XmlUtils_XmlValidationError, e.getLocalizedMessage()), e);
        } catch (IOException e) {
            Activator.logError("Error loading XML file", e); //$NON-NLS-1$
        }

        return null;
    }

    /**
     * Function to load the XML file structure
     */
    protected void loadXML() {

        Element doc = (Element) loadXMLNode();
        if (doc == null) {
            return;
        }

        /* parser for the eventhandler */
        NodeList nodes = doc.getElementsByTagName(TmfXmlStrings.EVENT_HANDLER);
        ssprovider = new ArrayList<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            TmfXmlEventHandler handler = new TmfXmlEventHandler(node, fStateValues);
            ssprovider.add(handler);
        }
    }

    /**
     * Test conditions
     *
     * @return 1 if condition is true, otherwise 0. If error -1.
     * @throws AttributeNotFoundException
     */
    private boolean testCondition(CtfTmfEvent event, TmfXmlCondition condition) throws AttributeNotFoundException {

        // State Value
        if (condition.getStateValue() != null) {
            TmfXmlStateValue filter = condition.getStateValue();
            int quark = -1;
            for (TmfXmlAttribute attribute : filter.getAttributes()) {
                quark = getQuark(attribute, event, quark);
                // the query is not valid, we stop the state change
                if (quark == -1) {
                    throw new AttributeNotFoundException();
                }
            }

            // the value in the XML file
            ITmfStateValue valueXML;
            valueXML = getValue(filter, event);

            ITmfStateValue valueState;
            // the value in the state
            if (quark != -1) {
                valueState = ss.queryOngoingState(quark);
            }
            // the value in the event field
            else {
                valueState = getEventField(filter, event);
            }
            return valueXML.equals(valueState);

            // Condition Tree
        } else if (!condition.getConditions().isEmpty()) {

            if (condition.getOperator() == TmfXmlStrings.OP_EQUALS) {
                return testCondition(event, condition.getConditions().get(0));
            } else if (condition.getOperator() == TmfXmlStrings.OP_NOT) {
                return !testCondition(event, condition.getConditions().get(0));
            } else if (condition.getOperator() == TmfXmlStrings.OP_AND) {
                boolean test = true;
                for (TmfXmlCondition childCondition : condition.getConditions()) {
                    test = test && testCondition(event, childCondition);
                }
                return test;
            } else if (condition.getOperator() == TmfXmlStrings.OP_OR) {
                boolean test = false;
                for (TmfXmlCondition childCondition : condition.getConditions()) {
                    test = test || testCondition(event, childCondition);
                }
                return test;
            }
        }
        return true;
    }

    /**
     * Make all change for a event
     */
    private void makeChange(CtfTmfEvent event) {
        final String eventName = event.getEventName();
        final long ts = event.getTimestamp().getValue();

        try {
            for (TmfXmlEventHandler eventHandler : ssprovider) {
                String eventHandlerName = eventHandler.getName();

                // test for correct name
                boolean goodName = eventHandlerName.equals(eventName);

                // test for the wildcart at the end
                goodName = goodName || (eventHandlerName.endsWith(TmfXmlStrings.WILDCART)
                        && eventName.startsWith(eventHandlerName.replace(TmfXmlStrings.WILDCART, TmfXmlStrings.NULL)));

                if (!goodName) {
                    continue;
                }

                // States Changes
                for (TmfXmlStateChange stateChange : eventHandler.getStateChanges()) {
                    // Conditions
                    int quark = -1;
                    boolean condition = true;
                    boolean error = false; // error level

                    // if we have at least one condition
                    if (stateChange.getConditions() != null) {
                        try {
                            condition = testCondition(event, stateChange.getConditions());
                        } catch (AttributeNotFoundException ae) {
                            error = true;
                        }
                    }

                    TmfXmlStateValue action = null;
                    // if all condition are good
                    if (condition) {
                        action = stateChange.getThenValue();
                    }
                    else {
                        action = stateChange.getElseValue();
                    }

                    if (action == null) {
                        return;
                    }

                    quark = -1;
                    for (TmfXmlAttribute attribute : action.getAttributes()) {
                        quark = getQuark(attribute, event, quark);
                        // the query is not valid, we stop the state
                        // change
                        if (quark == -1) {
                            error = true;
                            break;
                        }
                    }

                    if (error) {
                        return;
                    }

                    // auto increment the node
                    if (action.getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT) {
                        ss.incrementAttribute(ts, quark);
                    }
                    else if (action.getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT_TMFSTATE) {
                        if (action.getValueTMF().getType() == ITmfStateValue.Type.INTEGER) {
                            int increment = action.getValueTMF().unboxInt();
                            int currentValue = ss.queryOngoingState(quark).unboxInt();
                            ITmfStateValue value = TmfStateValue.newValueInt(increment + currentValue);
                            ss.modifyAttribute(ts, value, quark);
                        }
                    }
                    else if (action.getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT_EVENTFIELD) {
                        int increment = 0;
                        ITmfStateValue incrementValue = getValue(action, event);
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
                    else if (action.getTypeValue() != TmfXmlStrings.VALUE_TYPE_DELETE) {
                        ITmfStateValue value = getValue(action, event);
                        if (action.getStackAction() == TmfXmlStrings.VALUE_TYPE_PUSH) {
                            ss.pushAttribute(ts, value, quark);
                        } else if (action.getStackAction() == TmfXmlStrings.VALUE_TYPE_POP) {
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

        } catch (AttributeNotFoundException ae) {
            /*
             * This would indicate a problem with the logic of the manager here,
             * so it shouldn't happen.
             */
            Activator.logError("Attribute not found", ae); //$NON-NLS-1$
        } catch (TimeRangeException tre) {
            /*
             * This would happen if the events in the trace aren't ordered
             * chronologically, which should never be the case ...
             */
            Activator.logError("TimeRangeException caught in the state system's event manager.  Are the events in the trace correctly ordered?", tre); //$NON-NLS-1$
        } catch (StateValueTypeException sve) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            Activator.logError("State value type error", sve); //$NON-NLS-1$
        }

    }

    /**
     * This method get the quark describe by "attribute" in the State System.
     * The method use the ss.getQuarkRelativeAndAdd method in the State System.
     *
     * To use absolute path i_quark must be -1.
     *
     * @param attribute
     *            access attribute (can be a attribute query)
     * @param event
     *            current event
     * @param i_quark
     *            root quark, -1 for all attribute tree
     * @return the quark describe by attribute.
     */
    protected int getQuark(TmfXmlAttribute attribute, CtfTmfEvent event, int i_quark) {
        final ITmfEventField content = event.getContent();
        ITmfStateValue value = TmfStateValue.nullValue();
        int quark = -1;

        try {
            // CONSTANT
            if (attribute.getType() == TmfXmlStrings.ATTRIBUTE_TYPE_CONSTANT) {
                if (i_quark == -1) {
                    quark = ss.getQuarkAbsoluteAndAdd(attribute.getValue());
                } else {
                    quark = ss.getQuarkRelativeAndAdd(i_quark, attribute.getValue());
                }
            }
            // EVENTFIELD
            else if (attribute.getType() == TmfXmlStrings.ATTRIBUTE_TYPE_EVENTFIELD) {
                // exception for CPU which is not in the field
                if (attribute.getValue().equals(TmfXmlStrings.CPU)) {
                    quark = ss.getQuarkRelativeAndAdd(i_quark, String.valueOf(event.getCPU()));
                }
                else {
                    // stop if the eventfield don't exist
                    if (content.getField(attribute.getValue()) == null) {
                        return quark;
                    }

                    Object field = content.getField(attribute.getValue()).getValue();

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
            }
            // query
            else if (attribute.getType() == TmfXmlStrings.ATTRIBUTE_TYPE_QUERY) {
                int quarkQuery = -1;

                for (TmfXmlAttribute XA : attribute.getQueryList()) {
                    quarkQuery = getQuark(XA, event, quarkQuery);
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

            }
            // LOCATION
            else if (attribute.getType() == TmfXmlStrings.ATTRIBUTE_TYPE_LOCATION) {
                quark = i_quark;
                String idLocation = attribute.getValue();

                for (TmfXmlLocation location : fLocations) {
                    // the good location
                    if (location.getId().compareTo(idLocation) == 0) {
                        for (TmfXmlAttribute XA : location.getPath()) {
                            quark = getQuark(XA, event, quark);
                        }
                    }
                }
            }
            else {
                quark = i_quark;
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
     * Get the value of a Xml stateValue.
     *
     * @param stateValue
     *            the Xml stateValue.
     * @param event
     *            currentevent.
     * @return the value
     * @throws AttributeNotFoundException
     *             for the query in the state system
     */
    protected ITmfStateValue getValue(TmfXmlStateValue stateValue, CtfTmfEvent event)
            throws AttributeNotFoundException {
        ITmfStateValue value = TmfStateValue.nullValue();
        final ITmfEventField content = event.getContent();

        // If the value is a ITmfStateValue
        if (stateValue.getTypeValue() == TmfXmlStrings.VALUE_TYPE_TMFSTATE
                || stateValue.getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT_TMFSTATE) {
            value = stateValue.getValueTMF();
        }
        // If the value want to access to a event field.
        else if (stateValue.getTypeValue() == TmfXmlStrings.VALUE_TYPE_EVENTFIELD
                || stateValue.getTypeValue() == TmfXmlStrings.VALUE_TYPE_INCREMENT_EVENTFIELD) {

            // Exception for "CPU"
            // FIXME : Nameclash if a eventfield have "cpu" for name.
            if (stateValue.getValueString().equals(TmfXmlStrings.CPU)) {
                value = TmfStateValue.newValueInt(event.getCPU());
            }
            else {
                // If the field doesn't exist, return a nullValue.
                if (content.getField(stateValue.getValueString()) == null) {
                    return value;
                }

                Object field = content.getField(stateValue.getValueString()).getValue();

                // Try to find the good type. The type can be forced by
                // "forcedType" argument. In the case of a thread id, the event
                // field is a long, but it's stored with a int.
                if (field instanceof String) {
                    String fieldString = (String) field;
                    value = TmfStateValue.newValueString(fieldString);

                    if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_INT) {
                        value = TmfStateValue.newValueInt(Integer.parseInt(fieldString));
                    }
                    else if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_LONG) {
                        value = TmfStateValue.newValueLong(Long.parseLong(fieldString));
                    }
                } else if (field instanceof Long) {
                    Long fieldLong = (Long) field;
                    value = TmfStateValue.newValueLong(fieldLong);

                    if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_INT) {
                        value = TmfStateValue.newValueInt(fieldLong.intValue());
                    }
                    else if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_STRING) {
                        value = TmfStateValue.newValueString(fieldLong.toString());
                    }
                } else if (field instanceof Integer) {
                    Integer fieldInterger = (Integer) field;
                    value = TmfStateValue.newValueInt(fieldInterger);

                    if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_LONG) {
                        value = TmfStateValue.newValueLong(fieldInterger.longValue());
                    }
                    else if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_STRING) {
                        value = TmfStateValue.newValueString(fieldInterger.toString());
                    }
                }
            }
        }
        // If the value make a new query in the state system.
        else if (stateValue.getTypeValue() == TmfXmlStrings.VALUE_TYPE_QUERY) {
            int quarkQuery = -1;

            for (TmfXmlAttribute attribute : stateValue.getValueQuery()) {
                if (attribute.getType() != 0) {
                    quarkQuery = getQuark(attribute, event, quarkQuery);
                    if (quarkQuery == -1) {
                        // the query is not valid, we stop the state change
                        break;
                    }
                }
            }
            // the query can fail : example CurrentThread don't exist if we have
            // any sched_switch
            if (quarkQuery != -1) {
                value = ss.queryOngoingState(quarkQuery);
            }

        }
        // If the value is the eventname.
        else if (stateValue.getTypeValue() == TmfXmlStrings.VALUE_TYPE_EVENTNAME) {
            value = TmfStateValue.newValueString(event.getEventName());
        }

        return value;
    }

    /**
     * Return the State Value of a StateChange
     *
     * @param stateValue
     *            for the current event
     * @param event
     *            current event
     * @return the StateValue
     */
    protected static ITmfStateValue getEventField(TmfXmlStateValue stateValue, CtfTmfEvent event) {

        ITmfStateValue value = TmfStateValue.nullValue();
        final ITmfEventField content = event.getContent();

        if (stateValue.getEventFieldName().equals(TmfXmlStrings.CPU)) {
            value = TmfStateValue.newValueInt(event.getCPU());
        }
        else {
            if (content.getField(stateValue.getEventFieldName()) == null) {
                return value;
            }

            Object field = content.getField(stateValue.getEventFieldName()).getValue();

            if (field instanceof String) {
                String fieldString = (String) field;
                value = TmfStateValue.newValueString(fieldString);

                if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_INT) {
                    value = TmfStateValue.newValueInt(Integer.parseInt(fieldString));
                }
                else if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_LONG) {
                    value = TmfStateValue.newValueLong(Long.parseLong(fieldString));
                }
            } else if (field instanceof Long) {
                Long fieldLong = (Long) field;
                value = TmfStateValue.newValueLong(fieldLong);

                if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_INT) {
                    value = TmfStateValue.newValueInt(fieldLong.intValue());
                }
                else if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_STRING) {
                    value = TmfStateValue.newValueString(fieldLong.toString());
                }
            } else if (field instanceof Integer) {
                Integer fieldInterger = (Integer) field;
                value = TmfStateValue.newValueInt(fieldInterger);

                if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_LONG) {
                    value = TmfStateValue.newValueLong(fieldInterger.longValue());
                }
                else if (stateValue.getForcedType() == TmfXmlStrings.VALUE_TYPE_STRING) {
                    value = TmfStateValue.newValueString(fieldInterger.toString());
                }
            }
        }
        return value;
    }

}