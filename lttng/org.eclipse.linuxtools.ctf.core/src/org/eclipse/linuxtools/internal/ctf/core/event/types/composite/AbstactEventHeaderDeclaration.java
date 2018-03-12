/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.types.composite;

import org.eclipse.linuxtools.ctf.core.event.types.Declaration;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;

abstract class AbstactEventHeaderDeclaration extends Declaration {

    /**
     * is an event header declaration
     *
     * @param declaration
     *            the declaration
     * @param idName
     *            the id,
     * @param idSize
     *            the id size
     * @param variantName
     *            the variant name
     * @param variantCount
     *            the size of the variant
     * @param headerMaxSize
     *            maximum size of the variant
     * @param timestampName
     *            the timestamp name
     * @param compactName
     *            the compact id
     * @param compactCount
     *            number of elements in the compact field
     * @param compactTimestampSize
     *            the compact timestamp size
     * @param extendedName
     *            the extended id
     * @param extendedFieldCount
     *            number of elements in the extended field
     * @param extendedIdSize
     *            size in bits of the id in an extended field
     * @param extendedTimestampSize
     *            the full timestamp size in bits
     * @return true if the event matches
     */
    protected static boolean isEventHeaderDeclaration(
            StructDeclaration declaration,
            String idName, int idSize,
            String variantName, int variantCount, int headerMaxSize,
            String timestampName,
            String compactName, int compactCount, int compactTimestampSize,
            String extendedName, int extendedFieldCount, int extendedIdSize, int extendedTimestampSize) {
        IDeclaration iDeclaration = declaration.getFields().get(idName);
        if (!(iDeclaration instanceof EnumDeclaration)) {
            return false;
        }
        EnumDeclaration eId = (EnumDeclaration) iDeclaration;
        if (eId.getContainerType().getLength() != idSize) {
            return false;
        }
        iDeclaration = declaration.getFields().get(variantName);

        if (!(iDeclaration instanceof VariantDeclaration)) {
            return false;
        }
        VariantDeclaration vDec = (VariantDeclaration) iDeclaration;
        if (!vDec.hasField(compactName) || !vDec.hasField(extendedName)) {
            return false;
        }
        if (vDec.getFields().size() != variantCount) {
            return false;
        }
        final int maximumSize = vDec.getMaximumSize();
        final int enumMaxSize = eId.getMaximumSize();
        final int mask = (int) (declaration.getAlignment() - 1);
        if ((maximumSize + (enumMaxSize + mask) & ~mask) != headerMaxSize) {
            return false;
        }
        iDeclaration = vDec.getFields().get(compactName);
        if (!(iDeclaration instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration compactDec = (StructDeclaration) iDeclaration;
        if (compactDec.getFields().size() != compactCount) {
            return false;
        }
        if (!compactDec.hasField(timestampName)) {
            return false;
        }
        iDeclaration = compactDec.getFields().get(timestampName);
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        IntegerDeclaration tsDec = (IntegerDeclaration) iDeclaration;
        if (tsDec.getLength() != compactTimestampSize || tsDec.isSigned()) {
            return false;
        }
        iDeclaration = vDec.getFields().get(extendedName);
        if (!(iDeclaration instanceof StructDeclaration)) {
            return false;
        }
        StructDeclaration extendedDec = (StructDeclaration) iDeclaration;
        if (!extendedDec.hasField(timestampName)) {
            return false;
        }
        if (extendedDec.getFields().size() != extendedFieldCount) {
            return false;
        }
        iDeclaration = extendedDec.getFields().get(timestampName);
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        tsDec = (IntegerDeclaration) iDeclaration;
        if (tsDec.getLength() != extendedTimestampSize || tsDec.isSigned()) {
            return false;
        }
        iDeclaration = extendedDec.getFields().get(idName);
        if (!(iDeclaration instanceof IntegerDeclaration)) {
            return false;
        }
        IntegerDeclaration iId = (IntegerDeclaration) iDeclaration;
        if (iId.getLength() != extendedIdSize || iId.isSigned()) {
            return false;
        }
        return true;
    }

}