/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
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
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlAttribute;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlLocation;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class implement the XmlProvider data structure.
 *
 * @author Florian Wininger
 */
public abstract class XmlAbstractProvider extends AbstractTmfStateProvider {

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

    /** StateID. */
    private String fStateID;

    /** List of all Location. */
    protected ArrayList<TmfXmlLocation> fLocations = new ArrayList<>();

    /** HashMap for StateValue. */
    protected HashMap<String, String> fStateValues = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param stateid
     *            in the XML file
     */
    public XmlAbstractProvider(CtfTmfTrace trace, String stateid) {
        super(trace, CtfTmfEvent.class, "LTTng Kernel"); //$NON-NLS-1$
        fStateID = stateid;
    }

    /**
     * Function to load the XML file structure Load stateValues and locations
     *
     * @param path
     *            Path to the XML file
     * @return stateprovider node
     */
    protected Node loadXMLFile(IPath path) {
        try {
            File XMLFile = path.toFile();
            if (XMLFile == null || !XMLFile.exists() || !XMLFile.isFile()) {
                return null;
            }

            // Load the XML File
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XMLFile);
            doc.getDocumentElement().normalize();

            // get State Providers
            NodeList stateproviderNodes = doc.getElementsByTagName(TmfXmlStrings.STATE_PROVIDER);
            Element stateproviderNode = null;

            for (int i = 0; i < stateproviderNodes.getLength(); i++) {
                Element node = (Element) stateproviderNodes.item(i);
                String analysisid = node.getAttribute(TmfXmlStrings.ANALYSISID);
                if (analysisid.equals(fStateID)) {
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

        } catch (Exception ex) {
            Activator.logError("Error in loadXMLFile method", ex); //$NON-NLS-1$
        }
        return null;
    }

    /**
     * Get the stateid of the state provider.
     *
     * @return the STATEID of this state provider.
     */
    public String getStateID() {
        return fStateID;
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
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