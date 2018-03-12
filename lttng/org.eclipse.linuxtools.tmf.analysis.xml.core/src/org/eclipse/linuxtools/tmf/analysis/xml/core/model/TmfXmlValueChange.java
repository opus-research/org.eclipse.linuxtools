/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Naser Ezzati - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.model;

import java.util.List;

import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This Class implement a condition For putting labels on entries in the XML-defined state system
 *
 * <pre>
 *  example 1: Simple Condition
 *  <entryLabel>
 *      <stateAttribute type="constant" value="System_call" />
 *  </entryLabel>
 *
 *  example 2: Conditional state change
 * <entryLabel>
 *     <if>
 *      <condition>
 *        <stateAttribute type="location" value="CurrentThread" />
 *        <stateAttribute type="constant" value="Status" />
 *        <stateValue type="int" value="3" />
 *      </condition>
 *     </if>
 *    <then>
 *      <stateAttribute type="constant" value="System_call" />
 *    </then>
 *    <else>
 *       <if>
 *          <condition>
 *            <stateAttribute type="location" value="CurrentThread" />
 *            <stateAttribute type="constant" value="Status" />
 *            <stateValue type="int" value="2" />
 *          </condition>
 *       </if>
 *          <then>
 *            <stateAttribute type="constant" value="Status" />
 *          </then>
 *    </else>
 * </entryLabel>
 * </pre>
 *
 * @author Naser Ezzati
 * @since 2.0
 */
public class TmfXmlValueChange {

    private final IXmlStateChange fChange;
    private final IXmlStateSystemContainer fContainer;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param statechange
     *            XML node root of this state change
     * @param container
     *            The state system container this state change belongs to
     */
    public TmfXmlValueChange(ITmfXmlModelFactory modelFactory, Element statechange, IXmlStateSystemContainer container) {
        fContainer = container;

        /*
         * child nodes is either a list of TmfXmlStateAttributes and
         * TmfXmlStateValues, or an if-then-else series of nodes.
         */

        Node ifNode = statechange.getElementsByTagName(TmfXmlStrings.IF).item(0);
        if (ifNode != null) {
            /* the state change has a condition */
            fChange = new XmlConditionalChange(modelFactory, statechange);
        } else {
            /* the state change does not have a condition */
            fChange = new XmlStateValueChange(modelFactory, statechange);
        }
    }

    /**
     * Execute the state change for an entry. If necessary, it validates the
     * condition and executes the required change.
     *
     * @param quark
     *            The quark to process
     * @param time
     *            The time to check the value of quark
     * @throws AttributeNotFoundException
     *             Pass through the exception it received
     * @throws TimeRangeException
     *             Pass through the exception it received
     * @throws StateValueTypeException
     *             Pass through the exception it received
     * @return   the quark for the label or -1 if there is no value or the condition is not true
     */
    public int handleEntry(int quark, long time) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException {
        return fChange.handleEntry(quark, time);
    }

    /* Interface for both private classes to handle the event */
    private interface IXmlStateChange {

        int handleEntry(int quark,long time) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException;
    }

    /**
     * Conditional state change with a condition to verify
     */
    private class XmlConditionalChange implements IXmlStateChange {
        private final TmfXmlCondition fCondition;
        private final TmfXmlValueChange fThenChange;
        private final TmfXmlValueChange fElseChange;

        public XmlConditionalChange(ITmfXmlModelFactory modelFactory, Element statechange) {
            /*
             * The if node exists, it has been verified before calling this
             */
            Node ifNode = statechange.getElementsByTagName(TmfXmlStrings.IF).item(0);
            fCondition = modelFactory.createCondition((Element) ifNode, fContainer);

            Node thenNode = statechange.getElementsByTagName(TmfXmlStrings.THEN).item(0);
            if (thenNode == null) {
                throw new IllegalArgumentException("Conditional state change: there should be a then clause."); //$NON-NLS-1$
            }
            fThenChange = modelFactory.createValueChange((Element) thenNode, fContainer);

            Node elseNode = statechange.getElementsByTagName(TmfXmlStrings.ELSE).item(0);
            if (elseNode != null) {
                fElseChange = modelFactory.createValueChange((Element) elseNode, fContainer);
            } else {
                fElseChange = null;
            }
        }

        @Override
        public int handleEntry(int quark, long time) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException {
            TmfXmlValueChange toExecute = fThenChange;
            try {
                if (!fCondition.testForEntry(quark, time)) {
                    toExecute = fElseChange;
                }
            } catch (AttributeNotFoundException e) {
                /*
                 * An attribute in the condition did not exist (yet), return
                 * from the state change
                 */
                return -1;
            } catch (StateSystemDisposedException e) {

                return -1;
            }

            if (toExecute == null) {
                return -1;
            }
            return toExecute.handleEntry(quark, time);
        }
    }

    /**
     * State change with no condition
     */
    private class XmlStateValueChange implements IXmlStateChange {

        private final ITmfXmlStateAttribute fAttribute;

        public XmlStateValueChange(ITmfXmlModelFactory modelFactory, Element statechange) {
            List<Element> childElements = XmlUtils.getChildElements(statechange);

            /*
             * Last child element is the state value, the others are attributes
             * to reach to value to set
             */
            Element stateValueElement = childElements.remove(childElements.size() - 1);
            if (!stateValueElement.getNodeName().equals(TmfXmlStrings.STATE_ATTRIBUTE)) {
                throw new IllegalArgumentException("TmfXmlStateChange: a state change must have only TmfXmlStateAttribute elements before the state value"); //$NON-NLS-1$
            }

            fAttribute = modelFactory.createStateAttribute(stateValueElement, fContainer);

        }

        @Override
        public int handleEntry(int quark, long time) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException {
            return fAttribute.getAttributeQuark(quark);
        }

    }

}