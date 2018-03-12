/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial Design and Implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.ctf.core.event.metadata;

import java.nio.ByteOrder;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.parser.CTFParser;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * Integer operations within a TSDL abstract syntax tree
 *
 * @author Matthew Khouzam
 */

public final class TSDLIntUtils {

    private static final int INTEGER_BASE_2 = 2;
    private static final int INTEGER_BASE_8 = 8;
    private static final int INTEGER_BASE_10 = 10;
    private static final int INTEGER_BASE_16 = 16;

    private TSDLIntUtils() {
    }

    /**
     * Parses an unary integer (dec, hex or oct).
     *
     * @param unaryInteger
     *            An unary integer node.
     * @return The integer value.
     * @throws ParseException
     *             on an invalid integer format ("bob" for example)
     */
    public static long parseUnaryInteger(CommonTree unaryInteger) throws ParseException {

        List<CommonTree> children = unaryInteger.getChildren();
        CommonTree value = children.get(0);
        String strval = value.getText();

        long intval;
        try {
            intval = Long.decode(strval);
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid integer format: " + strval, e); //$NON-NLS-1$
        }

        /* The rest of children are sign */
        if ((children.size() % 2) == 0) {
            return -intval;
        }
        return intval;
    }

    /**
     * Is this a unary integer (a literal)
     *
     * @param node
     *            The node to check.
     * @return True if the given node is an unary integer.
     */
    public static boolean isUnaryInteger(CommonTree node) {
        return ((node.getType() == CTFParser.UNARY_EXPRESSION_DEC) ||
                (node.getType() == CTFParser.UNARY_EXPRESSION_HEX) || (node.getType() == CTFParser.UNARY_EXPRESSION_OCT));
    }

    /**
     * Gets the value of a "signed" integer attribute.
     *
     * @param rightNode
     *            A CTF_RIGHT node.
     * @return The "signed" value as a boolean.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static boolean getSigned(CommonTree rightNode)
            throws ParseException {

        boolean ret = false;
        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (TSDLStringUtils.isUnaryString(firstChild)) {
            String strval = TSDLStringUtils.concatenateUnaryStrings(rightNode.getChildren());

            switch (strval) {
            case MetadataStrings.TRUE:
            case MetadataStrings.TRUE2:
                ret = true;
                break;
            case MetadataStrings.FALSE:
            case MetadataStrings.FALSE2:
                ret = false;
                break;
            default:
                throw new ParseException("Invalid boolean value " //$NON-NLS-1$
                        + firstChild.getChild(0).getText());
            }
        } else if (isUnaryInteger(firstChild)) {
            /* Happens if the value is something like "1234.hello" */
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("Invalid boolean value"); //$NON-NLS-1$
            }

            long intval = parseUnaryInteger(firstChild);

            if (intval == 1) {
                ret = true;
            } else if (intval == 0) {
                ret = false;
            } else {
                throw new ParseException("Invalid boolean value " //$NON-NLS-1$
                        + firstChild.getChild(0).getText());
            }
        } else {
            throw new ParseException();
        }

        return ret;
    }

    /**
     * Gets the value of an "encoding" integer attribute.
     *
     * @param rightNode
     *            A CTF_RIGHT node.
     * @return The "encoding" value.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static @NonNull Encoding getEncoding(CommonTree rightNode)
            throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (TSDLStringUtils.isUnaryString(firstChild)) {
            String strval = TSDLStringUtils.concatenateUnaryStrings(rightNode.getChildren());

            switch (strval) {
            case MetadataStrings.UTF8:
                return Encoding.UTF8;
            case MetadataStrings.ASCII:
                return Encoding.ASCII;
            case MetadataStrings.NONE:
                return Encoding.NONE;
            default:
                throw new ParseException("Invalid value for encoding"); //$NON-NLS-1$
            }
        }
        throw new ParseException("Invalid value for encoding"); //$NON-NLS-1$
    }

    /**
     * Gets the value of a "base" integer attribute.
     *
     * @param rightNode
     *            An CTF_RIGHT node.
     * @return The "base" value.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static int getBase(CommonTree rightNode) throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryInteger(firstChild)) {
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("invalid base value"); //$NON-NLS-1$
            }

            long intval = parseUnaryInteger(firstChild);
            if ((intval == TSDLIntUtils.INTEGER_BASE_2) || (intval == TSDLIntUtils.INTEGER_BASE_8) || (intval == TSDLIntUtils.INTEGER_BASE_10)
                    || (intval == TSDLIntUtils.INTEGER_BASE_16)) {
                return (int) intval;
            }
            throw new ParseException("Invalid value for base"); //$NON-NLS-1$
        } else if (TSDLStringUtils.isUnaryString(firstChild)) {
            String strval = TSDLStringUtils.concatenateUnaryStrings(rightNode.getChildren());
            switch (strval) {
            case MetadataStrings.DECIMAL:
            case MetadataStrings.DEC:
            case MetadataStrings.DEC_CTE:
            case MetadataStrings.INT_MOD:
            case MetadataStrings.UNSIGNED_CTE:
                return TSDLIntUtils.INTEGER_BASE_10;
            case MetadataStrings.HEXADECIMAL:
            case MetadataStrings.HEX:
            case MetadataStrings.X:
            case MetadataStrings.X2:
            case MetadataStrings.POINTER:
                return TSDLIntUtils.INTEGER_BASE_16;
            case MetadataStrings.OCTAL:
            case MetadataStrings.OCT:
            case MetadataStrings.OCTAL_CTE:
                return TSDLIntUtils.INTEGER_BASE_8;
            case MetadataStrings.BINARY:
            case MetadataStrings.BIN:
                return TSDLIntUtils.INTEGER_BASE_2;
            default:
                throw new ParseException("Invalid value for base"); //$NON-NLS-1$
            }
        } else {
            throw new ParseException("invalid value for base"); //$NON-NLS-1$
        }
    }

    /**
     * Gets the value of a "size" integer attribute.
     *
     * @param rightNode
     *            A CTF_RIGHT node.
     * @return The "size" value.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static long getSize(CommonTree rightNode) throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (isUnaryInteger(firstChild)) {
            if (rightNode.getChildCount() > 1) {
                throw new ParseException("Invalid value for size"); //$NON-NLS-1$
            }

            long size = parseUnaryInteger(firstChild);

            if (size < 1) {
                throw new ParseException("Invalid value for size"); //$NON-NLS-1$
            }

            return size;
        }
        throw new ParseException("Invalid value for size"); //$NON-NLS-1$
    }

    /**
     * Gets the value of a "align" integer or struct attribute.
     *
     * @param node
     *            A CTF_RIGHT node or directly an unary integer.
     * @return The align value.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static long getAlignment(CommonTree node) throws ParseException {
        /*
         * If a CTF_RIGHT node was passed, call getAlignment with the first
         * child
         */
        if (node.getType() == CTFParser.CTF_RIGHT) {
            if (node.getChildCount() > 1) {
                throw new ParseException("Invalid alignment value"); //$NON-NLS-1$
            }

            return getAlignment((CommonTree) node.getChild(0));
        } else if (isUnaryInteger(node)) {
            long alignment = parseUnaryInteger(node);

            if (!TSDLASTUtils.isValidAlignment(alignment)) {
                throw new ParseException("Invalid value for alignment : " //$NON-NLS-1$
                        + alignment);
            }

            return alignment;
        }
        throw new ParseException("Invalid value for alignment"); //$NON-NLS-1$
    }

    /**
     * Set the byte order of an integer declaration
     *
     * @param byteOrder
     *            the desired byte order
     * @param parentScope
     *            the parentScope
     * @param name
     *            the name of the integer declaration
     * @param integerDeclaration
     *            the integer declaration
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static void setByteOrder(ByteOrder byteOrder,
            final DeclarationScope parentScope, String name,
            IntegerDeclaration integerDeclaration) throws ParseException {

        if (integerDeclaration.getByteOrder() != byteOrder) {
            IntegerDeclaration newI;
            newI = IntegerDeclaration.createDeclaration(integerDeclaration.getLength(), integerDeclaration.isSigned(),
                    integerDeclaration.getBase(), byteOrder, integerDeclaration.getEncoding(),
                    integerDeclaration.getClock(), integerDeclaration.getAlignment());
            parentScope.replaceType(name, newI);
        }
    }

}
