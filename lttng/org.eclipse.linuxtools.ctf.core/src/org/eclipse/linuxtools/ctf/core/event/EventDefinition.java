/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFStreamInputReader;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Representation of a particular instance of an event.
 */
public final class EventDefinition implements IDefinitionScope {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * A null event, can be used for testing or poison pilling
     *
     * @since 3.0
     */
    @NonNull
    public static final EventDefinition NULL_EVENT = new EventDefinition(new EventDeclaration(), null, -1L, null, null, null, null);

    /**
     * The corresponding event declaration.
     */
    private final IEventDeclaration fDeclaration;

    /**
     * The timestamp of the current event.
     */
    private final long fTimestamp;

    /**
     * The event context structure definition.
     */
    private final ICompositeDefinition fEventContext;

    private final ICompositeDefinition fStreamContext;

    private final ICompositeDefinition fPacketContext;

    /**
     * The event fields structure definition.
     */
    private final StructDefinition fFields;

    /**
     * The StreamInputReader that reads this event definition.
     */
    private final CTFStreamInputReader fStreamInputReader;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs an event definition.
     *
     * @param declaration
     *            The corresponding event declaration
     * @param streamInputReader
     *            The SIR from where this EventDef was read
     * @param timestamp
     *            event timestamp
     * @param eventContext
     *            The event context
     * @param packetContext
     *            the packet context
     * @param streamEventContext
     *            the stream context
     * @param fields
     *            The event fields
     * @since 3.0
     */
    public EventDefinition(IEventDeclaration declaration,
            CTFStreamInputReader streamInputReader,
            long timestamp,
            StructDefinition streamEventContext,
            StructDefinition eventContext,
            StructDefinition packetContext,
            StructDefinition fields) {
        this(declaration, streamInputReader, timestamp, (ICompositeDefinition) streamEventContext, eventContext, packetContext, fields);
    }

    /**
     * Constructs an event definition.
     *
     * @param declaration
     *            The corresponding event declaration
     * @param streamInputReader
     *            The SIR from where this EventDef was read
     * @param timestamp
     *            event timestamp
     * @param eventContext
     *            The event context
     * @param packetContext
     *            the packet context
     * @param streamEventContext
     *            the stream context
     * @param fields
     *            The event fields
     * @since 3.1
     */
    public EventDefinition(IEventDeclaration declaration,
            CTFStreamInputReader streamInputReader,
            long timestamp,
            ICompositeDefinition streamEventContext,
            StructDefinition eventContext,
            StructDefinition packetContext,
            StructDefinition fields) {
        fDeclaration = declaration;
        fStreamInputReader = streamInputReader;
        fTimestamp = timestamp;
        fFields = fields;
        fEventContext = eventContext;
        fPacketContext = packetContext;
        fStreamContext = streamEventContext;
    }

    // ------------------------------------------------------------------------
    // Getters/Setters/Predicates
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public LexicalScope getScopePath() {
        String eventName = fDeclaration.getName();
        if (eventName == null) {
            return null;
        }
        LexicalScope myScope = LexicalScope.EVENT.getChild(eventName);
        if (myScope == null) {
            myScope = new LexicalScope(LexicalScope.EVENT, eventName);
        }
        return myScope;
    }

    /**
     * Gets the declaration (the form) of the data
     *
     * @return the event declaration
     * @since 2.0
     */
    public IEventDeclaration getDeclaration() {
        return fDeclaration;
    }

    /**
     * Gets the fields of a definition
     *
     * @return the fields of a definition in struct form. Can be null.
     */
    public StructDefinition getFields() {
        return fFields;
    }

    /**
     * Gets the context of this event without the context of the stream
     *
     * @return the context in struct form
     * @since 1.2
     */
    public StructDefinition getEventContext() {
        if( fEventContext instanceof StructDefinition) {
            return (StructDefinition) fEventContext;
        }
        return null;
    }

    /**
     * Gets the context of this event within a stream
     *
     * @return the context
     * @since 3.1
     */
    public ICompositeDefinition getMergedContext() {

        /* Most common case so far */
        if (fStreamContext == null) {
            return fEventContext;
        }

        /* streamContext is not null, but the context of the event is null */
        if (fEventContext == null) {
            return fStreamContext;
        }

        // TODO: cache if this is a performance issue

        /* The stream context and event context are assigned. */
        StructDeclaration mergedDeclaration = new StructDeclaration(1);

        Builder<String> builder = ImmutableList.<String> builder();
        List<Definition> fieldValues = new ArrayList<>();

        /* Add fields from the stream */
        for (String fieldName : fStreamContext.getFieldNames()) {
            Definition definition = fStreamContext.getDefinition(fieldName);
            mergedDeclaration.addField(fieldName, definition.getDeclaration());
            builder.add(fieldName);
            fieldValues.add(definition);
        }

        ImmutableList<String> fieldNames = builder.build();
        /*
         * Add fields from the event context, overwrite the stream ones if
         * needed.
         */
        for (String fieldName : fEventContext.getFieldNames()) {
            Definition definition = fEventContext.getDefinition(fieldName);
            mergedDeclaration.addField(fieldName, definition.getDeclaration());
            if (fieldNames.contains(fieldName)) {
                fieldValues.set((fieldNames.indexOf(fieldName)), definition);
            } else {
                builder.add(fieldName);
                fieldValues.add(definition);
            }
        }
        fieldNames = builder.build();
        StructDefinition mergedContext = new StructDefinition(mergedDeclaration, this, "context", //$NON-NLS-1$
                fieldNames,
                fieldValues.toArray(new Definition[fieldValues.size()]));
        return mergedContext;

    }

    /**
     * Gets the context of this event within a stream
     *
     * @return the context in struct form
     * @deprecated use @link {@link EventDefinition#getMergedContext()}
     */
    @Deprecated
    public StructDefinition getContext() {
        ICompositeDefinition mergedContext = getMergedContext();
        return (StructDefinition) (mergedContext instanceof StructDefinition ? mergedContext : null);
    }

    /**
     * Gets the stream input reader that this event was made by
     *
     * @return the parent
     * @since 3.0
     */
    public CTFStreamInputReader getStreamInputReader() {
        return fStreamInputReader;
    }

    /**
     * Gets the context of packet the event is in.
     *
     * @return the packet context
     */
    public StructDefinition getPacketContext() {
        if (fPacketContext instanceof StructDefinition) {
            return (StructDefinition) fPacketContext;
        }
        throw new UnsupportedOperationException("Context is not a structDefinition"); //$NON-NLS-1$
    }

    /**
     * gets the CPU the event was generated by. Slightly LTTng specific
     *
     * @return The CPU the event was generated by
     */
    public int getCPU() {
        return fStreamInputReader.getCPU();
    }

    /**
     * Get the time stamp
     *
     * @return the time stamp
     */
    public long getTimestamp() {
        return fTimestamp;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public Definition lookupDefinition(String lookupPath) {
        if (lookupPath.equals("context")) { //$NON-NLS-1$
            if( fEventContext instanceof Definition) {
                return (Definition) fEventContext;
            }
        } else if (lookupPath.equals("fields")) { //$NON-NLS-1$
            return fFields;
        }
        return null;
    }

    @Override
    public String toString() {
        Iterable<String> list;
        StringBuilder retString = new StringBuilder();
        final String cr = System.getProperty("line.separator");//$NON-NLS-1$

        retString.append("Event type: " + fDeclaration.getName() + cr); //$NON-NLS-1$
        retString.append("Timestamp: " + Long.toString(fTimestamp) + cr); //$NON-NLS-1$

        if (fEventContext != null) {
            list = fEventContext.getDeclaration().getFieldsList();

            for (String field : list) {
                retString.append(field
                        + " : " + fEventContext.getDefinition(field).toString() + cr); //$NON-NLS-1$
            }
        }

        if (fFields != null) {
            list = fFields.getDeclaration().getFieldsList();

            for (String field : list) {
                retString.append(field
                        + " : " + fFields.getDefinition(field).toString() + cr); //$NON-NLS-1$
            }
        }

        return retString.toString();
    }

}
