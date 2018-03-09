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

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.XmlAttribute;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.XmlCondition;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.XmlEventHandler;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.XmlStateChange;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.XmlStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model.XmlStrings;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
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
 * This is the state change input plug-in for TMF's state system which handles
 * the XML Format
 *
 * @author Florian Wininger
 */
public class XmlStateProvider extends XmlAbstractProvider {

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

    private IPath fFile;

    /** List of all EventHandler */
    private ArrayList<XmlEventHandler> ssprovider = new ArrayList<XmlEventHandler>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plug-in.
     *
     * FIXME: Does the trace really need to be a Ctf trace? The analysis module
     * should make sure the trace is of the right type
     *
     * @param trace
     *            The trace
     * @param stateid
     *            The state system id, corresponding to the analysisid attribute
     *            of the stateprovider element of the Xml file
     * @param file
     *            Xml file containing the state provider definition
     */
    public XmlStateProvider(CtfTmfTrace trace, String stateid, IPath file) {
        super(trace, stateid);
        fFile = file;
        loadXML();
    }

    /**
     * Function to load the XML file structure
     */
    protected void loadXML() {

        Element doc = (Element) super.loadXMLFile(fFile);
        if (doc == null) {
            return;
        }

        /* parser for the eventhandler */
        NodeList nodes = doc.getElementsByTagName(XmlStrings.EVENTHANDLER);
        ssprovider = new ArrayList<XmlEventHandler>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            XmlEventHandler handler = new XmlEventHandler(node, fStateValues);
            ssprovider.add(handler);
        }
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

    @Override
    public XmlStateProvider getNewInstance() {
        return new XmlStateProvider((CtfTmfTrace) this.getTrace(), this.getStateID(), fFile);
    }

    /**
     * Test conditions
     *
     * @return 1 if condition is true, otherwise 0. If error -1.
     * @throws AttributeNotFoundException
     */
    private boolean testCondition(CtfTmfEvent event, XmlCondition condition) throws AttributeNotFoundException {

        // State Value
        if (condition.getStateValue() != null) {
            XmlStateValue filter = condition.getStateValue();
            int quark = -1;
            for (XmlAttribute attribute : filter.getAttributes()) {
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

            if (condition.getOperator() == XmlStrings.OP_EQUALS) {
                return testCondition(event, condition.getConditions().get(0));
            } else if (condition.getOperator() == XmlStrings.OP_NOT) {
                return !testCondition(event, condition.getConditions().get(0));
            } else if (condition.getOperator() == XmlStrings.OP_AND) {
                boolean test = true;
                for (XmlCondition childCondition : condition.getConditions()) {
                    test = test && testCondition(event, childCondition);
                }
                return test;
            } else if (condition.getOperator() == XmlStrings.OP_OR) {
                boolean test = false;
                for (XmlCondition childCondition : condition.getConditions()) {
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
            for (XmlEventHandler eventHandler : ssprovider) {
                String eventHandlerName = eventHandler.getName();

                // test for correct name
                boolean goodName = eventHandlerName.equals(eventName);

                // test for the wildcart at the end
                goodName = goodName || (eventHandlerName.endsWith(XmlStrings.WILDCART)
                        && eventName.startsWith(eventHandlerName.replace(XmlStrings.WILDCART, XmlStrings.NULL)));

                if (goodName) {
                    // States Changes
                    for (XmlStateChange stateChange : eventHandler.getStateChanges()) {
                        // Conditions
                        int quark = -1;
                        boolean condition = true;
                        boolean error = false; // error level

                        // if we have at less one condition
                        if (stateChange.getConditions() != null) {
                            try {
                                condition = testCondition(event, stateChange.getConditions());
                            } catch (AttributeNotFoundException ae) {
                                error = true;
                            }
                        }

                        XmlStateValue action = null;
                        // if all condition are good
                        if (condition) {
                            action = stateChange.getThenValue();
                        }
                        else {
                            action = stateChange.getElseValue();
                        }

                        if (action != null) {
                            quark = -1;
                            for (XmlAttribute attribute : action.getAttributes()) {
                                quark = getQuark(attribute, event, quark);
                                // the query is not valid, we stop the state change
                                if (quark == -1) {
                                    error = true;
                                    break;
                                }
                            }

                            if (!error) {
                                // auto increment the node
                                if (action.getTypeValue() == XmlStrings.VALUE_TYPE_INCREMENT) {
                                    ss.incrementAttribute(ts, quark);
                                }
                                else if (action.getTypeValue() == XmlStrings.VALUE_TYPE_INCREMENT_TMFSTATE) {
                                    if (action.getValueTMF().getType() == ITmfStateValue.Type.INTEGER) {
                                        int increment = action.getValueTMF().unboxInt();
                                        int currentValue = ss.queryOngoingState(quark).unboxInt();
                                        ITmfStateValue value = TmfStateValue.newValueInt(increment + currentValue);
                                        ss.modifyAttribute(ts, value, quark);
                                    }
                                }
                                else if (action.getTypeValue() == XmlStrings.VALUE_TYPE_INCREMENT_EVENTFIELD) {
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
                                else if (action.getTypeValue() != XmlStrings.VALUE_TYPE_DELETE) {
                                    ITmfStateValue value = getValue(action, event);
                                    if (action.getStackAction() == XmlStrings.VALUE_TYPE_PUSH) {
                                        ss.pushAttribute(ts, value, quark);
                                    } else if (action.getStackAction() == XmlStrings.VALUE_TYPE_POP) {
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

    @Override
    protected void eventHandle(ITmfEvent ev) {
        CtfTmfEvent event = (CtfTmfEvent) ev;
        makeChange(event);
    }

}