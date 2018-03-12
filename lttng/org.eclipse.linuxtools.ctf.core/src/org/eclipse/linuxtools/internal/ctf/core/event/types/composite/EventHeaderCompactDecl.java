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

import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.linuxtools.ctf.core.event.types.Declaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IEventHeaderDecl;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;

/**
 * An event header declaration is a declaration of a structure defined in the
 * CTF spec examples section 6.1.1 . It is used in LTTng traces. This will
 * accelerate reading of the trace.
 *
 * Reminder
 *
 * <pre>
 * struct event_header_compact {
 *     enum : uint5_t { compact = 0 ... 30, extended = 31 } id;
 *     variant <id> {
 *         struct {
 *             uint27_clock_monotonic_t timestamp;
 *         } compact;
 *         struct {
 *             uint32_t id;
 *             uint64_clock_monotonic_t timestamp;
 *         } extended;
 *     } v;
 * } align(8);
 * </pre>
 *
 * @author Matthew Khouzam
 */
public class EventHeaderCompactDecl extends Declaration implements IEventHeaderDecl {

    /**
     * The id is 5 bits
     */
    private static final int COMPACT_ID = 5;
    private static final int EXTENDED_VALUE = 31;
    /**
     * Full sized id is 32 bits
     */
    private static final int ID_SIZE = 32;
    /**
     * Full sized timestamp is 64 bits
     */
    private static final int FULL_TS = 64;
    /**
     * Compact timestamp is 27 bits,
     */
    private static final int COMPACT_TS = 27;
    /**
     * Maximum size = largest this header can be
     */
    private static final int MAX_SIZE = 104;
    /**
     * Byte aligned
     */
    private static final int ALIGN = 8;
    /**
     * Name of the variant according to the spec
     */
    private static final String V = "v"; //$NON-NLS-1$

    private final ByteOrder fByteOrder;

    /**
     * Event Header Declaration
     *
     * @param byteOrder
     *            the byteorder
     */
    public EventHeaderCompactDecl(ByteOrder byteOrder) {
        fByteOrder = byteOrder;
    }

    @Override
    public EventHeaderDef createDefinition(IDefinitionScope definitionScope, String fieldName, BitBuffer input) throws CTFReaderException {
        alignRead(input);
        ByteOrder bo = input.getByteOrder();
        input.setByteOrder(fByteOrder);
        int enumId = (int) input.get(COMPACT_ID, false);
        if (enumId != EXTENDED_VALUE) {
            long timestamp2 = input.get(COMPACT_TS, false);
            input.setByteOrder(bo);
            return new EventHeaderDef(this, enumId, timestamp2, COMPACT_TS);
        }
        // needed since we read 5 bits
        input.position(input.position() + 3);
        int id = (int) input.get(ID_SIZE, false);
        long timestampLong = input.get(FULL_TS, false);
        input.setByteOrder(bo);
        return new EventHeaderDef(this, id, timestampLong, FULL_TS);

    }

    @Override
    public long getAlignment() {
        return ALIGN;
    }

    @Override
    public int getMaximumSize() {
        return MAX_SIZE;
    }

    /**
     * Check if a given struct declaration is an event header
     *
     * @param declaration
     *            the declaration
     * @return true if the struct is a compact event header
     */
    public static boolean isCompactEventHeader(StructDeclaration declaration) {

        IDeclaration iDeclaration = declaration.getFields().get(ID);
        if (!(iDeclaration instanceof EnumDeclaration)) {
            return false;
        }
        EnumDeclaration eId = (EnumDeclaration) iDeclaration;
        if (eId.getContainerType().getLength() != COMPACT_ID) {
            return false;
        }
        iDeclaration = declaration.getFields().get(V);

        if (!(iDeclaration instanceof VariantDeclaration)) {
            return false;
        }
        VariantDeclaration vDec = (VariantDeclaration) iDeclaration;
        if (!vDec.hasField(COMPACT) || !vDec.hasField(EXTENDED)) {
            return false;
        }
        iDeclaration = vDec.getFields().get(COMPACT);
        StructDeclaration compactDec = (StructDeclaration) iDeclaration;
        if (!compactDec.hasField(TIMESTAMP)) {
            return false;
        }
        iDeclaration = compactDec.getFields().get(TIMESTAMP);
        IntegerDeclaration tsDec = (IntegerDeclaration) iDeclaration;
        if (tsDec.getLength() != COMPACT_TS || tsDec.isSigned()) {
            return false;
        }
        iDeclaration = vDec.getFields().get(EXTENDED);
        StructDeclaration extendedDec = (StructDeclaration) iDeclaration;
        if (!extendedDec.hasField(TIMESTAMP)) {
            return false;
        }
        iDeclaration = extendedDec.getFields().get(TIMESTAMP);
        tsDec = (IntegerDeclaration) iDeclaration;
        if (tsDec.getLength() != FULL_TS || tsDec.isSigned()) {
            return false;
        }
        iDeclaration = extendedDec.getFields().get(ID);
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        IntegerDeclaration iId = (IntegerDeclaration) iDeclaration;
        if (iId.getLength() != ID_SIZE || iId.isSigned()) {
            return false;
        }
        return true;
    }
}
