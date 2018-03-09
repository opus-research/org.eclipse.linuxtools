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

import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This Class implement a State Change is the XML state provider
 *
 * <pre>
 *  example 1: Simple state change
 *  <stateChange>
 *      <location value="CurrentThread" />
 *      <constant value="System_call" />
 *      <stateValue type="null" />
 *  </stateChange>
 *
 *  example 2: Conditional state change
 *  <stateChange>
 *     <if>
 *      <condition>
 *        <location value="CurrentThread" />
 *        <constant value="System_call" />
 *        <stateValue type="null" />
 *      </condition>
 *     </if>
 *    <then>
 *      <location value="CurrentThread" />
 *      <constant value="Status" />
 *      <stateValue int="$PROCESS_STATUS_RUN_USERMODE"/>
 *    </then>
 *    <else>
 *      <location value="CurrentThread" />
 *      <constant value="Status" />
 *      <stateValue int="$PROCESS_STATUS_RUN_SYSCALL"/>
 *    </else>
 *  </stateChange>
 * </pre>
 *
 * @author Florian Wininger
 *
 */
public class TmfXmlStateChange {

    private final IXmlStateChange fChange;
    private final XmlStateProvider fProvider;

    /**
     * Constructor
     *
     * @param statechange
     *            XML node root of this state change
     * @param provider
     *            The state provider this state change belongs to
     */
    TmfXmlStateChange(Element statechange, XmlStateProvider provider) {
        fProvider = provider;

        /*
         * child nodes is either a list of TmfXmlStateAttributes and
         * TmfXmlStateValues, or an if-then-else series of nodes.
         */
        Node ifNode = statechange.getElementsByTagName(TmfXmlStrings.IF).item(0);
        /* the state change has a condition */
        if (ifNode != null) {
            fChange = new XmlConditionalChange(statechange);
        } else {
            /* the state change does not have a condition */
            fChange = new XmlStateValueChange(statechange);
        }
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
        fChange.handleEvent(event);
    }

    /* Interface for both private classes to handle the event */
    private interface IXmlStateChange {
        void handleEvent(ITmfEvent event) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException;
    }

    /**
     * Conditional state change with a condition to verify
     */
    private class XmlConditionalChange implements IXmlStateChange {
        private final TmfXmlCondition fCondition;
        private final TmfXmlStateChange fThenChange;
        private final TmfXmlStateChange fElseChange;

        public XmlConditionalChange(Element statechange) {
            /*
             * The if node exist, it has been verified before calling this
             */
            Node ifNode = statechange.getElementsByTagName(TmfXmlStrings.IF).item(0);
            fCondition = new TmfXmlCondition((Element) ifNode, fProvider);

            Node thenNode = statechange.getElementsByTagName(TmfXmlStrings.THEN).item(0);
            if (thenNode != null) {
                fThenChange = new TmfXmlStateChange((Element) thenNode, fProvider);
            } else {
                fThenChange = null;
            }
            Node elseNode = statechange.getElementsByTagName(TmfXmlStrings.ELSE).item(0);
            if (elseNode != null) {
                fElseChange = new TmfXmlStateChange((Element) elseNode, fProvider);
            } else {
                fElseChange = null;
            }

        }

        @Override
        public void handleEvent(ITmfEvent event) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException {
            TmfXmlStateChange toExecute = fThenChange;
            if (!fCondition.testForEvent(event)) {
                toExecute = fElseChange;
            }

            if (toExecute == null) {
                return;
            }
            toExecute.handleEvent(event);

        }
    }

    /**
     * State change with no condition
     */
    private class XmlStateValueChange implements IXmlStateChange {
        private final TmfXmlStateValue fValue;

        public XmlStateValueChange(Element statechange) {
            List<Element> childElements = XmlUtils.getChildElements(statechange);

            /*
             * Last child element is the state value, the others are attributes
             * to reach to value to set
             */
            Element stateValueElement = childElements.remove(childElements.size() - 1);
            List<TmfXmlStateAttribute> attributes = new ArrayList<>();
            for (Element element : childElements) {
                if (!element.getNodeName().equals(TmfXmlStrings.STATE_ATTRIBUTE)) {
                    throw new IllegalArgumentException("TmfXmlStateChange: a state change must have only TmfXmlStateAttribute elements before the state value"); //$NON-NLS-1$
                }
                TmfXmlStateAttribute attribute = new TmfXmlStateAttribute(element, fProvider);
                attributes.add(attribute);
            }
            if (attributes.isEmpty()) {
                throw new IllegalArgumentException("TmfXmlStateChange: a state change must have at least one TmfXmlStateAttribute element before the state value"); //$NON-NLS-1$
            }
            fValue = new TmfXmlStateValue(stateValueElement, fProvider, attributes);

        }

        @Override
        public void handleEvent(ITmfEvent event) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException {
            fValue.handleEvent(event);
        }

    }

}