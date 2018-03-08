/**********************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceEventComponent;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the trace event component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceEventPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The trace event 'name' property ID.
     */
    public static final String TRACE_EVENT_NAME_PROPERTY_ID = "trace.event.name"; //$NON-NLS-1$
    /**
     * The trace event 'type' property ID.
     */
    public static final String TRACE_EVENT_TYPE_PROPERTY_ID = "trace.event.type"; //$NON-NLS-1$
    /**
     * The trace event 'log level' property ID.
     */
    public static final String TRACE_EVENT_LOGLEVEL_PROPERTY_ID = "trace.event.loglevel"; //$NON-NLS-1$
    /**
     * The trace event 'state' property ID.
     */
    public static final String TRACE_EVENT_STATE_PROPERTY_ID = "trace.event.state"; //$NON-NLS-1$
    /**
     *  The trace event 'name' property name.
     */
    public static final String TRACE_EVENT_NAME_PROPERTY_NAME = Messages.TraceControl_EventNamePropertyName;
    /**
     * The trace event 'type' property name.
     */
    public static final String TRACE_EVENT_TYPE_PROPERTY_NAME = Messages.TraceControl_EventTypePropertyName;
    /**
     * The trace event 'log level' property name.
     */
    public static final String TRACE_EVENT_LOGLEVEL_PROPERTY_NAME = Messages.TraceControl_LogLevelPropertyName;
    /**
     * The trace event 'state' property name.
     */
    public static final String TRACE_EVENT_STATE_PROPERTY_NAME = Messages.TraceControl_StatePropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The event component which this property source is for.
     */
    protected final TraceEventComponent fEvent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param component - the base event component
     */
    public TraceEventPropertySource(TraceEventComponent component) {
        fEvent = component;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.BasePropertySource#getPropertyDescriptors()
     */
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        List<IPropertyDescriptor> list = new ArrayList<IPropertyDescriptor> ();
        list.add(new TextPropertyDescriptor(TRACE_EVENT_NAME_PROPERTY_ID, TRACE_EVENT_NAME_PROPERTY_NAME));
        list.add(new TextPropertyDescriptor(TRACE_EVENT_TYPE_PROPERTY_ID, TRACE_EVENT_TYPE_PROPERTY_NAME));
        list.add( new TextPropertyDescriptor(TRACE_EVENT_STATE_PROPERTY_ID, TRACE_EVENT_STATE_PROPERTY_NAME));
        if (fEvent.getLogLevel() != TraceLogLevel.LEVEL_UNKNOWN) {
            list.add(new TextPropertyDescriptor(TRACE_EVENT_LOGLEVEL_PROPERTY_ID, TRACE_EVENT_LOGLEVEL_PROPERTY_NAME));
        }
        return list.toArray(new IPropertyDescriptor[list.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.BasePropertySource#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(Object id) {
        if(TRACE_EVENT_NAME_PROPERTY_ID.equals(id)) {
            return fEvent.getName();
        }
        if (TRACE_EVENT_TYPE_PROPERTY_ID.equals(id)) {
            return fEvent.getEventType().name();
        }
        if (TRACE_EVENT_LOGLEVEL_PROPERTY_ID.equals(id)) {
            return fEvent.getLogLevel().name();
        }
        if (TRACE_EVENT_STATE_PROPERTY_ID.equals(id)) {
            return fEvent.getState().name();
        }
        return null;
    }

}
