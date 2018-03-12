/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.model.readonly;

import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.linuxtools.tmf.analysis.xml.core.model.TmfXmlStateValue;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * Implements a state value is a read only mode. See {@link TmfXmlStateValue}
 * for the syntax of the state value.
 *
 * In read mode, a state value will typically be used to find a path to a value,
 * so the value is known and there is a path of attributes that should lead to
 * it.
 *
 * @author Geneviève Bastien
 */
public class TmfXmlReadOnlyStateValue extends TmfXmlStateValue {

    /**
     * Constructor where the path to the value is a list of state attributes
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param attributes
     *            The attributes representing the path to this value
     */
    public TmfXmlReadOnlyStateValue(TmfXmlReadOnlyModelFactory modelFactory, Element node,
            IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes) {
        super(modelFactory, node, container, attributes, null);
    }

    /**
     * Constructor where the path to the value is an event field
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param eventField
     *            The event field where to get the value
     */
    public TmfXmlReadOnlyStateValue(TmfXmlReadOnlyModelFactory modelFactory, Element node,
            IXmlStateSystemContainer container, String eventField) {
        super(modelFactory, node, container, Collections.EMPTY_LIST, eventField);
    }

    @Override
    protected TmfXmlStateValueBase initializeStateValue(ITmfXmlModelFactory modelFactory, Element node) {
        TmfXmlStateValueBase stateValueType = null;
        /* Process the XML Element state value */
        String type = node.getAttribute(TmfXmlStrings.TYPE);
        String value = getSsContainer().getAttributeValue(node.getAttribute(TmfXmlStrings.VALUE));

        switch (type) {
        case TmfXmlStrings.TYPE_INT: {
            /* Integer value */
            ITmfStateValue stateValue = TmfStateValue.newValueInt(Integer.parseInt(value));
            stateValueType = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.TYPE_LONG: {
            /* Long value */
            ITmfStateValue stateValue = TmfStateValue.newValueLong(Long.parseLong(value));
            stateValueType = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.TYPE_STRING: {
            /* String value */
            ITmfStateValue stateValue = TmfStateValue.newValueString(value);
            stateValueType = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.TYPE_NULL: {
            /* Null value */
            ITmfStateValue stateValue = TmfStateValue.nullValue();
            stateValueType = new TmfXmlStateValueTmf(stateValue);
            break;
        }
        case TmfXmlStrings.EVENT_FIELD:

            break;
        case TmfXmlStrings.TYPE_EVENT_NAME:

            break;
        case TmfXmlStrings.TYPE_DELETE:

            break;
        case TmfXmlStrings.TYPE_QUERY:

            break;
        default:
            throw new IllegalArgumentException(String.format("TmfXmlStateValue constructor: unexpected element %s for stateValue type", type)); //$NON-NLS-1$
        }
        return stateValueType;
    }

    // ----------------------------------------------------------
    // Internal state value classes for the different types
    // ----------------------------------------------------------

    /**
     * Base class for all state value. Contain default methods to handle event,
     * process or increment the value
     */
    abstract class TmfXmlStateValueTypeReadOnly extends TmfXmlStateValueBase {

        @Override
        public final void handleEvent(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            if (isIncrement()) {
                incrementValue(event, quark, timestamp);
            } else {
                ITmfStateValue value = getValue(event);
                processValue(quark, timestamp, value);
            }
        }

        @Override
        protected void processValue(int quark, long timestamp, ITmfStateValue value) throws AttributeNotFoundException, TimeRangeException, StateValueTypeException {
            switch (getStackType()) {
            case POP:

                break;
            case PUSH:

                break;
            case NULL:
            case PEEK:
            default:

                break;
            }
        }

        @Override
        protected void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {

        }
    }

    /* This state value uses a constant value, defined in the XML */
    private class TmfXmlStateValueTmf extends TmfXmlStateValueTypeReadOnly {

        private final ITmfStateValue fValue;

        public TmfXmlStateValueTmf(ITmfStateValue value) {
            fValue = value;
        }

        @Override
        public ITmfStateValue getValue(ITmfEvent event) {
            return fValue;
        }

        @Override
        public void incrementValue(ITmfEvent event, int quark, long timestamp) throws StateValueTypeException, TimeRangeException, AttributeNotFoundException {
            ITmfStateSystem ss = getStateSystem();
            switch (fValue.getType()) {
            case LONG: {
                long incrementLong = fValue.unboxLong();
                long currentValue = ss.queryOngoingState(quark).unboxLong();
                ITmfStateValue value = TmfStateValue.newValueLong(incrementLong + currentValue);
                processValue(quark, timestamp, value);
                return;
            }
            case INTEGER: {
                int increment = fValue.unboxInt();
                int currentValue = ss.queryOngoingState(quark).unboxInt();
                ITmfStateValue value = TmfStateValue.newValueInt(increment + currentValue);
                processValue(quark, timestamp, value);
                break;
            }
            case DOUBLE:
            case NULL:
            case STRING:
            default:
                Activator.logWarning("TmfXmlStateValue: The increment value is not a number type"); //$NON-NLS-1$
                break;
            }
        }
    }
}
