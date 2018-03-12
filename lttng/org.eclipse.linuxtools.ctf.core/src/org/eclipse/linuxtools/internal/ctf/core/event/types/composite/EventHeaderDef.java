/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.types.composite;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.scope.LexicalScope;
import org.eclipse.linuxtools.ctf.core.event.types.Declaration;
import org.eclipse.linuxtools.ctf.core.event.types.Definition;
import org.eclipse.linuxtools.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.linuxtools.ctf.core.event.types.IEventHeaderDecl;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDefinition;

import com.google.common.collect.ImmutableList;

/**
 * An event header definition, as shown in the example of the CTF spec
 *
 * @author Matthew Khouzam
 */
public final class EventHeaderDef extends Definition implements ICompositeDefinition {

    private static final List<String> FIELD_NAMES = ImmutableList.of(IEventHeaderDecl.ID, IEventHeaderDecl.TIMESTAMP);

    private final int fId;
    private final long fTimestamp;
    private final int fTimestampLength;

    /**
     * Event header defintion
     *
     * @param id
     *            the event id
     * @param timestamp
     *            the timestamp
     * @param eventHeaderDecl
     *            The declaration of this defintion
     * @param timestampLength
     *            the number of bits valid in the timestamp
     */
    public EventHeaderDef(@NonNull Declaration eventHeaderDecl, int id, long timestamp, int timestampLength) {
        super(eventHeaderDecl, null, LexicalScope.EVENT_HEADER.toString(), LexicalScope.EVENT_HEADER);
        fId = id;
        fTimestamp = timestamp;
        fTimestampLength = timestampLength;
    }

    /**
     * Gets the timestamp declaration
     *
     * @return the timestamp declaration
     */
    public int getTimestampLength() {
        return fTimestampLength;
    }

    /**
     * Get the event id
     *
     * @return the event id
     */
    public int getId() {
        return fId;
    }

    /**
     * Get the timestamp
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return fTimestamp;
    }

    @Override
    public Definition getDefinition(String fieldName) {
        if (fieldName.equals(IEventHeaderDecl.ID)) {
            return new IntegerDefinition(IntegerDeclaration.INT_32B_DECL, null, IEventHeaderDecl.ID, getId());
        } else if (fieldName.equals(IEventHeaderDecl.TIMESTAMP)) {
            return new IntegerDefinition(IntegerDeclaration.INT_64B_DECL, null, IEventHeaderDecl.TIMESTAMP, getTimestamp());
        }
        return null;
    }

    @Override
    public List<String> getFieldNames() {
        return FIELD_NAMES;
    }
}