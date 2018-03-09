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
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
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

/**
 * This Class implements a single attribute value
 *
 * <pre>
 * Examples:
 * <stateAttribute type="constant" value="Threads" />
 * <stateAttribute type="query" />
 *      <stateAttribute type="constant" value="CPUs" />
 *      <stateAttribute type="eventField" value="cpu" />
 *      <stateAttribute type="constant" value="Current_thread" />
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
     *            XML element of the attribute
     * @param provider
     *            The state provider this state attribute belongs to
     */
    TmfXmlStateAttribute(Element attribute, XmlStateProvider provider) {
        fProvider = provider;

        String type = attribute.getAttribute(TmfXmlStrings.TYPE);
        if (type.equals(TmfXmlStrings.TYPE_CONSTANT)) {
            fType = StateAttributeType.ATTRIBUTE_TYPE_CONSTANT;
            fName = fProvider.getAttributeValue(attribute.getAttribute(TmfXmlStrings.VALUE));
        } else if (type.equals(TmfXmlStrings.EVENT_FIELD)) {
            fType = StateAttributeType.ATTRIBUTE_TYPE_EVENTFIELD;
            fName = fProvider.getAttributeValue(attribute.getAttribute(TmfXmlStrings.VALUE));
        } else if (type.equals(TmfXmlStrings.TYPE_LOCATION)) {
            fType = StateAttributeType.ATTRIBUTE_TYPE_LOCATION;
            fName = fProvider.getAttributeValue(attribute.getAttribute(TmfXmlStrings.VALUE));
        } else if (type.equals(TmfXmlStrings.TYPE_QUERY)) {
            List<Element> childElements = XmlUtils.getChildElements(attribute);
            for (Element subAttributeNode : childElements) {
                TmfXmlStateAttribute subAttribute = new TmfXmlStateAttribute(subAttributeNode, fProvider);
                fQueryList.add(subAttribute);
            }
            fType = StateAttributeType.ATTRIBUTE_TYPE_QUERY;
            fName = null;
        } else if (type.equals(TmfXmlStrings.NULL)) {
            fType = StateAttributeType.ATTRIBUTE_TYPE_NONE;
            fName = null;
        } else {
            throw new IllegalArgumentException("TmfXmlStateAttribute constructor: The XML element is not of the right type"); //$NON-NLS-1$
        }
    }

    /**
     * This method gets the quark for this state attribute in the State System.
     * The method use the ss.getQuarkRelativeAndAdd method in the State System.
     *
     * Unless this attribute is a location, in which case the quark must exist,
     * the quark will be added to the state system.
     *
     * @param event
     *            The current event being handled
     * @param i_quark
     *            root quark, use -1 to search the full attribute tree
     * @return the quark described by attribute or -1 if quark cannot be found
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
                /* special case if field is CPU which is not in the field */
                if (fName.equals(TmfXmlStrings.CPU)) {
                    quark = ss.getQuarkRelativeAndAdd(i_quark, event.getSource());
                }
                else {
                    /* stop if the event field doesn't exist */
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
                    if (quarkQuery == -1) {
                        break;
                    }
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
                    quark = -1; // error
                }
                break;
            }
            case ATTRIBUTE_TYPE_LOCATION: {
                quark = i_quark;
                String idLocation = fName;

                /* TODO: Add a fProvider.getLocation(id) method */
                for (TmfXmlLocation location : fProvider.getLocations()) {
                    if (location.getId().equals(idLocation)) {
                        quark = location.getLocationQuark(event, quark);
                        if (quark == -1) {
                            break;
                        }
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
            quark = -1;
        } catch (StateValueTypeException e) {
            /*
             * This would happen if we were trying to push/pop attributes not of
             * type integer. Which, once again, should never happen.
             */
            Activator.logError("StateValueTypeException", e); //$NON-NLS-1$
            quark = -1;
        }

        return quark;
    }

}