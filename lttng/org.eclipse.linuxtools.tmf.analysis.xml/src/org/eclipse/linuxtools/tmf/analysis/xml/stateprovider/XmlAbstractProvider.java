/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 * Copyright (c) 2013 Florian Wininger <florian.wininger@polymtl.ca>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.stateprovider;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.tmf.analysis.xml.stateprovider.model.XmlAttribute;
import org.eclipse.linuxtools.tmf.analysis.xml.stateprovider.model.XmlLocation;
import org.eclipse.linuxtools.tmf.analysis.xml.stateprovider.model.XmlStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.stateprovider.model.XmlStrings;
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
 * This is class implement the Provider structure for the state provider and
 * filter
 *
 * @author Florian Wininger
 *
 */
public abstract class XmlAbstractProvider extends AbstractTmfStateProvider {

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

    /**
     * StateID
     */
    private String stateID;

    /**
     * List of all Location
     */
    protected ArrayList<XmlLocation> sslocations = new ArrayList<XmlLocation>();

    /**
     * HashMap for StateValue
     */
    protected HashMap<String, String> stateValues = new HashMap<String, String>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plugin.
     *
     * @param trace
     *            The LTTng 2.0 kernel trace directory
     * @param stateid in the xml file
     */
    public XmlAbstractProvider(CtfTmfTrace trace, String stateid) {
        super(trace, CtfTmfEvent.class, "LTTng Kernel"); //$NON-NLS-1$
        stateID = stateid;
    }

    /**
     * Function to load the XML file structure
     * Load stateValues and locations
     * @param path Path to the xml file
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
            NodeList stateproviderNodes = doc.getElementsByTagName(XmlStrings.STATEPROVIDER);
            Element stateproviderNode = null;

            for (int i = 0; i < stateproviderNodes.getLength(); i++) {
                Element node = (Element) stateproviderNodes.item(i);
                String analysisid = node.getAttribute(XmlStrings.ANALYSISID);
                if (analysisid.equals(stateID)) {
                    stateproviderNode = node;
                }
            }

            if(stateproviderNode == null) {
                return null;
            }

            // parser for stateValues
            NodeList definedStateNodes = stateproviderNode.getElementsByTagName(XmlStrings.STATEVALUE);

            for (int i = 0; i < definedStateNodes.getLength(); i++) {
                Element node = (Element) definedStateNodes.item(i);
                stateValues.put(node.getAttribute(XmlStrings.NAME), node.getAttribute(XmlStrings.VALUE));
            }

            // parser for the location
            NodeList locationNodes = stateproviderNode.getElementsByTagName(XmlStrings.LOCATION);
            sslocations = new ArrayList<XmlLocation>();

            for (int i = 0; i < locationNodes.getLength(); i++) {
                Node node = locationNodes.item(i);
                XmlLocation location = new XmlLocation(node, stateValues);
                sslocations.add(location);
            }

            return stateproviderNode ;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * @return STATEID
     */
    public String getStateID() {
        return stateID;
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        /* We can only set up the locations once the state system is assigned */
        super.assignTargetStateSystem(ssb);
    }

    /**
     * This function get the quark in the State System Use the function
     * ss.getQuarkRelativeAndAdd in the State System
     *
     * To use Absolue path i_quark must be -1
     *
     * @param attribute : access attribute (can be a attribute query)
     * @param event : current event
     * @param i_quark : quark for the root, -1 for all attribute tree
     * @return the new quark
     */
    protected int getQuark(XmlAttribute attribute, CtfTmfEvent event, int i_quark) {
        final ITmfEventField content = event.getContent();
        ITmfStateValue value = TmfStateValue.nullValue();
        int quark = -1;

        try {
            // CONSTANT
            if (attribute.getType() == XmlStrings.ATTRIBUTE_TYPE_CONSTANT) {
                if (i_quark == -1) {
                    quark = ss.getQuarkAbsoluteAndAdd(attribute.getValue());
                } else {
                    quark = ss.getQuarkRelativeAndAdd(i_quark, attribute.getValue());
                }
            }
            // EVENTFIELD
            else if (attribute.getType() == XmlStrings.ATTRIBUTE_TYPE_EVENTFIELD) {
                // exception for CPU which is not in the field
                if (attribute.getValue().equals(XmlStrings.CPU)) {
                    quark = ss.getQuarkRelativeAndAdd(i_quark, String.valueOf(event.getCPU()));
                }
                else {
                    // stop if the eventfield don't exist
                    if(content.getField(attribute.getValue()) == null) {
                        return quark ;
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
            else if (attribute.getType() == XmlStrings.ATTRIBUTE_TYPE_QUERY) {
                int quarkQuery = -1;

                for(XmlAttribute XA : attribute.getQueryList()) {
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
            else if (attribute.getType() == XmlStrings.ATTRIBUTE_TYPE_LOCATION) {
                quark = i_quark;
                String idLocation = attribute.getValue();

                for(XmlLocation location : sslocations) {
                    // the good location
                    if (location.getId().compareTo(idLocation) == 0) {
                        for(XmlAttribute XA : location.getPath()) {
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
            ae.printStackTrace();

        } catch (StateValueTypeException e) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            e.printStackTrace();
        }

        return quark;
    }

    /**
     * Get the value of a State Change
     *
     * @param stateValue StateChange
     * @param event currentevent
     * @return the value
     * @throws AttributeNotFoundException for the query in the state system
     */
    protected ITmfStateValue getValue(XmlStateValue stateValue, CtfTmfEvent event)
            throws AttributeNotFoundException
    {
        ITmfStateValue value = TmfStateValue.nullValue();
        final ITmfEventField content = event.getContent();

        if (stateValue.getTypeValue() == XmlStrings.VALUE_TYPE_TMFSTATE) {
            value = stateValue.getValueTMF();
        }
        else if (stateValue.getTypeValue() == XmlStrings.VALUE_TYPE_EVENTFIELD) {

            if (stateValue.getValueString().equals(XmlStrings.CPU)) {
                value = TmfStateValue.newValueInt(event.getCPU());
            }
            else {
                if (content.getField(stateValue.getValueString()) == null) {
                    return value;
                }

                Object field = content.getField(stateValue.getValueString()).getValue();

                if (field instanceof String) {
                    String fieldString = (String) field;
                    value = TmfStateValue.newValueString(fieldString);

                    if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_INT) {
                        value = TmfStateValue.newValueInt(Integer.parseInt(fieldString));
                    }
                    else if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_LONG) {
                        value = TmfStateValue.newValueLong(Long.parseLong(fieldString));
                    }
                } else if (field instanceof Long) {
                    Long fieldLong = (Long) field;
                    value = TmfStateValue.newValueLong(fieldLong);

                    if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_INT) {
                        value = TmfStateValue.newValueInt(fieldLong.intValue());
                    }
                    else if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_STRING) {
                        value = TmfStateValue.newValueString(fieldLong.toString());
                    }
                } else if (field instanceof Integer) {
                    Integer fieldInterger = (Integer) field;
                    value = TmfStateValue.newValueInt(fieldInterger);

                    if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_LONG) {
                        value = TmfStateValue.newValueLong(fieldInterger.longValue());
                    }
                    else if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_STRING) {
                        value = TmfStateValue.newValueString(fieldInterger.toString());
                    }
                }
            }
        }
        else if (stateValue.getTypeValue() == XmlStrings.VALUE_TYPE_QUERY) {
            int quarkQuery = -1;

            for(XmlAttribute attribute : stateValue.getValueQuery()) {
                if (attribute.getType() != 0) {
                    quarkQuery = getQuark(attribute, event, quarkQuery);
                    if (quarkQuery == -1) {
                        // the query is not valid, we stop the state change
                        break;
                    }
                }
            }
            // the query can fail : exemple CurrentThread don't exist if we have any
            // sched_switch
            if (quarkQuery != -1) {
                value = ss.queryOngoingState(quarkQuery);
            }

        }
        else if (stateValue.getTypeValue() == XmlStrings.VALUE_TYPE_EVENTNAME) {
            value = TmfStateValue.newValueString(event.getEventName());
        }

        return value;
    }

    /**
     * Return the State Value of a StateChange
     *
     * @param stateValue for the current event
     * @param event current event
     * @return the StateValue
     */
    protected static ITmfStateValue getEventField(XmlStateValue stateValue, CtfTmfEvent event) {

        ITmfStateValue value = TmfStateValue.nullValue();
        final ITmfEventField content = event.getContent();

        if (stateValue.getEventFieldName().equals(XmlStrings.CPU)) {
            value = TmfStateValue.newValueInt(event.getCPU());
        }
        else {
            if (content.getField(stateValue.getEventFieldName())==null) {
                return value ;
            }

            Object field = content.getField(stateValue.getEventFieldName()).getValue();

            if (field instanceof String) {
                String fieldString = (String) field;
                value = TmfStateValue.newValueString(fieldString);

                if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_INT) {
                    value = TmfStateValue.newValueInt(Integer.parseInt(fieldString));
                }
                else if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_LONG) {
                    value = TmfStateValue.newValueLong(Long.parseLong(fieldString));
                }
            } else if (field instanceof Long) {
                Long fieldLong = (Long) field;
                value = TmfStateValue.newValueLong(fieldLong);

                if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_INT) {
                    value = TmfStateValue.newValueInt(fieldLong.intValue());
                }
                else if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_STRING) {
                    value = TmfStateValue.newValueString(fieldLong.toString());
                }
            } else if (field instanceof Integer) {
                Integer fieldInterger = (Integer) field;
                value = TmfStateValue.newValueInt(fieldInterger);

                if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_LONG) {
                    value = TmfStateValue.newValueLong(fieldInterger.longValue());
                }
                else if (stateValue.getForcedType() == XmlStrings.VALUE_TYPE_STRING) {
                    value = TmfStateValue.newValueString(fieldInterger.toString());
                }
            }
        }
        return value;
    }

}