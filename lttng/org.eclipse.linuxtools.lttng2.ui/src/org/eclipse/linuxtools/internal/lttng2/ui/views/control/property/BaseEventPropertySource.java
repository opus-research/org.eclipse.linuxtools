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
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventComponent;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * <p>
 * Property source implementation for the base event component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class BaseEventPropertySource extends BasePropertySource {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The base event 'name' property ID.
     */
    public static final String BASE_EVENT_NAME_PROPERTY_ID = "base.event.name"; //$NON-NLS-1$
    /**
     * The base event 'type' property ID.
     */
    public static final String BASE_EVENT_TYPE_PROPERTY_ID = "base.event.type"; //$NON-NLS-1$
    /**
     * The base event 'log level' property ID.
     */
    public static final String BASE_EVENT_LOGLEVEL_PROPERTY_ID = "base.event.loglevel"; //$NON-NLS-1$
    /**
     *  The base event 'name' property name.
     */
    public static final String BASE_EVENT_NAME_PROPERTY_NAME = Messages.TraceControl_EventNamePropertyName;
    /**
     * The base event 'type' property name.
     */
    public static final String BASE_EVENT_TYPE_PROPERTY_NAME = Messages.TraceControl_EventTypePropertyName;
    /**
     * The base event 'log level' property name.
     */
    public static final String BASE_EVENT_LOGLEVEL_PROPERTY_NAME = Messages.TraceControl_LogLevelPropertyName;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The base event component which this property source is for.
     */
    private final BaseEventComponent fBaseEvent;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param component - the base event component
     */
    public BaseEventPropertySource(BaseEventComponent component) {
        fBaseEvent = component;
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
        list.add(new TextPropertyDescriptor(BASE_EVENT_NAME_PROPERTY_ID, BASE_EVENT_NAME_PROPERTY_NAME));
        list.add(new TextPropertyDescriptor(BASE_EVENT_TYPE_PROPERTY_ID, BASE_EVENT_TYPE_PROPERTY_NAME));
        if (fBaseEvent.getLogLevel() != TraceLogLevel.LEVEL_UNKNOWN) {
            list.add(new TextPropertyDescriptor(BASE_EVENT_LOGLEVEL_PROPERTY_ID, BASE_EVENT_LOGLEVEL_PROPERTY_NAME));
        }
        return list.toArray(new IPropertyDescriptor[list.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.BasePropertySource#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(Object id) {
        if(BASE_EVENT_NAME_PROPERTY_ID.equals(id)) {
            return fBaseEvent.getName();
        }
        if (BASE_EVENT_TYPE_PROPERTY_ID.equals(id)) {
            return fBaseEvent.getEventType().name();
        }
        if (BASE_EVENT_LOGLEVEL_PROPERTY_ID.equals(id)) {
            return fBaseEvent.getLogLevel().name();
        }
        return null;
    }

}
