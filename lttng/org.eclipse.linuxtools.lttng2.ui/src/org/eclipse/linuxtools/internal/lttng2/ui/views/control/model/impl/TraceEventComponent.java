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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.IEventInfo;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEnablement;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceEventType;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.TraceLogLevel;
import org.eclipse.linuxtools.internal.lttng2.core.control.model.impl.EventInfo;
import org.eclipse.linuxtools.internal.lttng2.ui.Activator;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceControlComponent;
import org.eclipse.linuxtools.internal.lttng2.ui.views.control.property.TraceEventPropertySource;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertySource;


/**
 * <p>
 * Implementation of the trace channel component.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceEventComponent extends TraceControlComponent {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * Path to icon file for this component (enabled state).
     */
    public static final String TRACE_EVENT_ICON_FILE_ENABLED = "icons/obj16/event_enabled.gif"; //$NON-NLS-1$
    /**
     * Path to icon file for this component (disabled state).
     */
    public static final String TRACE_EVENT_ICON_FILE_DISABLED = "icons/obj16/event_disabled.gif"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The event information.
     */
    protected IEventInfo fEventInfo = null;
    /**
     * The image to be displayed when in disabled state.
     */
    private Image fDisabledImage = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - the name of the component.
     * @param parent - the parent of this component.
     */
    public TraceEventComponent(String name, ITraceControlComponent parent) {
        super(name, parent);
        setImage(TRACE_EVENT_ICON_FILE_ENABLED);
        setToolTip(Messages.TraceControl_EventDisplayName);
        fEventInfo = new EventInfo(name);
        fDisabledImage = Activator.getDefault().loadIcon(TRACE_EVENT_ICON_FILE_DISABLED);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceControlComponent#getImage()
     */
    @Override
    public Image getImage() {
        if (fEventInfo.getState() == TraceEnablement.DISABLED) {
            return fDisabledImage;
        }
        return super.getImage();
    }

    /**
     * Sets the event information.
     * @param eventInfo - the event information to set.
     */
    public void setEventInfo(IEventInfo eventInfo) {
        fEventInfo = eventInfo;
    }

    /**
     * @return the trace event type
     */
    public TraceEventType getEventType() {
        return fEventInfo.getEventType();
    }

    /**
     * Sets the trace event type to the given type
     * @param type - type to set
     */
    public void setEventType(TraceEventType type) {
        fEventInfo.setEventType(type);
    }

    /**
     * Sets the trace event type to the type specified by the given name.
     * @param typeName - event type name
     */
    public void setEventType(String typeName) {
        fEventInfo.setEventType(typeName);
    }

    /**
     * @return the event state (enabled or disabled).
     */
    public TraceEnablement getState() {
        return fEventInfo.getState();
    }

    /**
     * Sets the event state (enablement) to the given value.
     * @param state - state to set.
     */
    public void setState(TraceEnablement state) {
        fEventInfo.setState(state);
    }

    /**
     * Sets the event state (enablement) to the value specified by the given name.
     * @param stateName - state to set.
     */
    public void setState(String stateName) {
        fEventInfo.setState(stateName);
    }

    /**
     * @return the trace event log level
     */
    public TraceLogLevel getLogLevel() {
        return fEventInfo.getLogLevel();
    }

    /**
     * Sets the trace event log level to the given level
     * @param level - event log level to set
     */
    public void setLogLevel(TraceLogLevel level) {
        fEventInfo.setLogLevel(level);
    }

    /**
     * Sets the trace event log level to the level specified by the given name.
     * @param levelName - event log level name
     */
    public void setLogLevel(String levelName) {
        fEventInfo.setLogLevel(levelName);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.TraceControlComponent#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySource.class) {
            return new TraceEventPropertySource(this);
        }
        return null;
    }

    /**
     * @return session name from parent
     */
    public String getSessionName() {
       return ((TraceChannelComponent)getParent()).getSessionName();
    }

    /**
     * @return session from parent
     */
    public TraceSessionComponent getSession() {
       return ((TraceChannelComponent)getParent()).getSession();
    }

    /**
     * @return channel name from parent
     */
    public String getChannelName() {
        return getParent().getName();
    }

    /**
     * @return if domain is kernel or UST
     */
    public boolean isKernel() {
        return ((TraceChannelComponent)getParent()).isKernel();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Add contexts to given channels and or events
     *
     * @param contexts
     *            - a list of contexts to add
     * @throws ExecutionException
     *             If the command fails
     */
    public void addContexts(List<String> contexts) throws ExecutionException {
        addContexts(contexts, new NullProgressMonitor());
    }

    /**
     * Add contexts to given channels and or events
     *
     * @param contexts
     *            - a list of contexts to add
     * @param monitor
     *            - a progress monitor
     * @throws ExecutionException
     *             If the command fails
     */
    public void addContexts(List<String> contexts, IProgressMonitor monitor)
            throws ExecutionException {
        getControlService().addContexts(getSessionName(), getChannelName(),
                getName(), isKernel(), contexts, monitor);
    }
}
