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

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.parser.CTFParser;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * AST Utilities to parse the TSDL {@link CommonTree}
 *
 * @author Matthew Khouzam
 *
 */
public final class TSDLASTUtils {
    private static final long DEFAULT_ALIGNMENT = 8;

    private TSDLASTUtils() {
        // don't do this
    }

    /**
     * Dispatch nodes to the correct list (is the node a trace, stream, event,
     * etc..)
     *
     * @param children
     *            the nodes to dispatch
     * @param traces
     *            the trace block, should only have one node in it at the end,
     *            please check (modified)
     * @param streams
     *            the streams list (modified)
     * @param events
     *            the events list (modified)
     * @param declarations
     *            the declarations list (modified)
     * @param environments
     *            the environments list (modified)
     * @param clocks
     *            the clocks list (modified)
     * @param callsites
     *            the call sites list (modified)
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static void dispatchChildrenNodes(List<CommonTree> children, List<CommonTree> traces, List<CommonTree> streams, List<CommonTree> events, List<CommonTree> declarations, List<CommonTree> environments, List<CommonTree> clocks,
            List<CommonTree> callsites) throws ParseException {
        for (CommonTree child : children) {
            final int type = child.getType();
            switch (type) {
            case CTFParser.DECLARATION:
                declarations.add(child);
                break;
            case CTFParser.TRACE:
                traces.add(child);
                break;
            case CTFParser.STREAM:
                streams.add(child);
                break;
            case CTFParser.EVENT:
                events.add(child);
                break;
            case CTFParser.CLOCK:
                clocks.add(child);
                break;
            case CTFParser.ENV:
                environments.add(child);
                break;
            case CTFParser.CALLSITE:
                callsites.add(child);
                break;
            default:
                TSDLASTUtils.childTypeError(child);
            }
        }
    }

    /**
     * Throws a ParseException stating that the parent-child relation between
     * the given node and its parent is not valid. It means that the shape of
     * the AST is unexpected.
     *
     * @param child
     *            The invalid child node.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static void childTypeError(CommonTree child) throws ParseException {
        CommonTree parent = (CommonTree) child.getParent();
        String error = "Parent " + CTFParser.tokenNames[parent.getType()] //$NON-NLS-1$
                + " can't have a child of type " //$NON-NLS-1$
                + CTFParser.tokenNames[child.getType()] + "."; //$NON-NLS-1$

        throw new ParseException(error);
    }

    /**
     * Get a major or minor version of the trace
     *
     * @param versionNode
     *            the node with the major or minor version number
     * @return the major or minor version of the trace
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static long getMajorOrMinor(CommonTree versionNode)
            throws ParseException {

        CommonTree firstChild = (CommonTree) versionNode.getChild(0);

        if (TSDLIntUtils.isUnaryInteger(firstChild)) {
            if (versionNode.getChildCount() > 1) {
                throw new ParseException("Invalid value for major/minor"); //$NON-NLS-1$
            }

            long m = TSDLIntUtils.parseUnaryInteger(firstChild);

            if (m < 0) {
                throw new ParseException("Invalid value for major/minor"); //$NON-NLS-1$
            }

            return m;
        }
        throw new ParseException("Invalid value for major/minor"); //$NON-NLS-1$
    }

    /**
     * Get the UUID from a node
     *
     * @param uuidNode
     *            the node with the uuid
     * @return the UUID
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static UUID getUUID(CommonTree uuidNode) throws ParseException {

        CommonTree firstChild = (CommonTree) uuidNode.getChild(0);

        if (TSDLStringUtils.isAnyUnaryString(firstChild)) {
            if (uuidNode.getChildCount() > 1) {
                throw new ParseException("Invalid value for UUID"); //$NON-NLS-1$
            }

            String uuidstr = TSDLStringUtils.parseUnaryString(firstChild);

            try {
                return UUID.fromString(uuidstr);
            } catch (IllegalArgumentException e) {
                throw new ParseException("Invalid format for UUID", e); //$NON-NLS-1$
            }
        }
        throw new ParseException("Invalid value for UUID"); //$NON-NLS-1$
    }

    /**
     * Creates the string representation of a list of pointers.
     *
     * @param pointerList
     *            A list of pointer nodes. If pointerList is null, this function
     *            does nothing.
     * @param sb
     *            A stringbuilder to which will be appended the string.
     */
    public static void createPointerListString(List<CommonTree> pointerList,
            StringBuilder sb) {
        if (pointerList == null) {
            return;
        }

        for (CommonTree pointer : pointerList) {

            sb.append(" *"); //$NON-NLS-1$
            if (pointer.getChildCount() > 0) {

                sb.append(" const"); //$NON-NLS-1$
            }
        }
    }

    /**
     * Get the Stream Id
     *
     * @param streamIdNode
     *            the node with the stream id
     * @return the stream id
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static long getStreamID(CommonTree streamIdNode) throws ParseException {

        CommonTree firstChild = (CommonTree) streamIdNode.getChild(0);

        if (TSDLIntUtils.isUnaryInteger(firstChild)) {
            if (streamIdNode.getChildCount() > 1) {
                throw new ParseException("invalid value for stream id"); //$NON-NLS-1$
            }

            long intval = TSDLIntUtils.parseUnaryInteger(firstChild);

            return intval;
        }
        throw new ParseException("invalid value for stream id"); //$NON-NLS-1$
    }

    /**
     * Get the event name from a node
     *
     * @param eventNameNode
     *            the event name node
     * @return the event name in string should not be null
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static String getEventName(CommonTree eventNameNode)
            throws ParseException {

        CommonTree firstChild = (CommonTree) eventNameNode.getChild(0);

        if (TSDLStringUtils.isAnyUnaryString(firstChild)) {
            String str = TSDLStringUtils.concatenateUnaryStrings(eventNameNode.getChildren());

            return str;
        }
        throw new ParseException("invalid value for event name"); //$NON-NLS-1$
    }

    /**
     * Parse the event id node
     *
     * @param eventIdNode
     *            the event id node
     * @return the event id
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static long getEventID(CommonTree eventIdNode) throws ParseException {

        CommonTree firstChild = (CommonTree) eventIdNode.getChild(0);

        if (TSDLIntUtils.isUnaryInteger(firstChild)) {
            if (eventIdNode.getChildCount() > 1) {
                throw new ParseException("invalid value for event id"); //$NON-NLS-1$
            }

            long intval = TSDLIntUtils.parseUnaryInteger(firstChild);
            if (intval > Integer.MAX_VALUE) {
                throw new ParseException("Event id larger than int.maxvalue, something is amiss"); //$NON-NLS-1$
            }
            return intval;
        }
        throw new ParseException("invalid value for event id"); //$NON-NLS-1$
    }

    /**
     * Parses the alias part of a typealias. It parses the underlying specifier
     * list and declarator and creates the string representation that will be
     * used to register the type.
     *
     * @param alias
     *            A TYPEALIAS_ALIAS node.
     * @return The string representation of the alias.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    static String parseTypealiasAlias(CommonTree alias)
            throws ParseException {

        List<CommonTree> children = alias.getChildren();

        CommonTree typeSpecifierList = null;
        CommonTree typeDeclaratorList = null;
        CommonTree typeDeclarator = null;
        List<CommonTree> pointers = new LinkedList<>();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPE_SPECIFIER_LIST:
                typeSpecifierList = child;
                break;
            case CTFParser.TYPE_DECLARATOR_LIST:
                typeDeclaratorList = child;
                break;
            default:
                childTypeError(child);
                break;
            }
        }

        /* If there is a type declarator list, extract the pointers */
        if (typeDeclaratorList != null) {
            /*
             * Only allow one declarator
             *
             * eg: "typealias uint8_t := puint8_t *, **;" is not permitted.
             */
            if (typeDeclaratorList.getChildCount() != 1) {
                throw new ParseException("Only one type declarator is allowed in the typealias alias"); //$NON-NLS-1$
            }

            typeDeclarator = (CommonTree) typeDeclaratorList.getChild(0);

            List<CommonTree> typeDeclaratorChildren = typeDeclarator.getChildren();

            for (CommonTree child : typeDeclaratorChildren) {
                switch (child.getType()) {
                case CTFParser.POINTER:
                    pointers.add(child);
                    break;
                case CTFParser.IDENTIFIER:
                    throw new ParseException("Identifier (" + child.getText() //$NON-NLS-1$
                            + ") not expected in the typealias target"); //$NON-NLS-1$
                default:
                    childTypeError(child);
                    break;
                }
            }
        }

        return TSDLASTUtils.createTypeDeclarationString(typeSpecifierList, pointers);
    }

    /*
     * Alignment
     */

    static long fixAlignment(long alignment, long size) {
        long retVal = alignment;
        if (alignment == 0) {
            retVal = ((size % TSDLASTUtils.DEFAULT_ALIGNMENT) == 0) ? 1 : TSDLASTUtils.DEFAULT_ALIGNMENT;
        }
        return retVal;
    }

    /**
     * Determines if the given value is a valid alignment value.
     *
     * @param alignment
     *            The value to check.
     * @return True if it is valid.
     */
    static boolean isValidAlignment(long alignment) {
        return !((alignment <= 0) || ((alignment & (alignment - 1)) != 0));
    }

    static @NonNull String getClock(CommonTree rightNode) {
        String clock = rightNode.getChild(1).getChild(0).getChild(0).getText();
        return clock == null ? MetadataStrings.EMPTY_STRING : clock;
    }

    /**
     * Parses an enumerator node and adds an enumerator declaration to an
     * enumeration declaration.
     *
     * The high value of the range of the last enumerator is needed in case the
     * current enumerator does not specify its value.
     *
     * @param enumerator
     *            An ENUM_ENUMERATOR node.
     * @param enumDeclaration
     *            en enumeration declaration to which will be added the
     *            enumerator.
     * @param lastHigh
     *            The high value of the range of the last enumerator
     * @return The high value of the value range of the current enumerator.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    static long parseEnumEnumerator(CommonTree enumerator,
            EnumDeclaration enumDeclaration, long lastHigh)
            throws ParseException {

        List<CommonTree> children = enumerator.getChildren();

        long low = 0, high = 0;
        boolean valueSpecified = false;
        String label = null;

        for (CommonTree child : children) {
            if (TSDLStringUtils.isAnyUnaryString(child)) {
                label = TSDLStringUtils.parseUnaryString(child);
            } else if (child.getType() == CTFParser.ENUM_VALUE) {

                valueSpecified = true;

                low = TSDLIntUtils.parseUnaryInteger((CommonTree) child.getChild(0));
                high = low;
            } else if (child.getType() == CTFParser.ENUM_VALUE_RANGE) {

                valueSpecified = true;

                low = TSDLIntUtils.parseUnaryInteger((CommonTree) child.getChild(0));
                high = TSDLIntUtils.parseUnaryInteger((CommonTree) child.getChild(1));
            } else {
                childTypeError(child);
            }
        }

        if (!valueSpecified) {
            low = lastHigh + 1;
            high = low;
        }

        if (low > high) {
            throw new ParseException("enum low value greater than high value"); //$NON-NLS-1$
        }

        if (!enumDeclaration.add(low, high, label)) {
            throw new ParseException("enum declarator values overlap."); //$NON-NLS-1$
        }

        if (valueSpecified && (BigInteger.valueOf(low).compareTo(enumDeclaration.getContainerType().getMinValue()) == -1 ||
                BigInteger.valueOf(high).compareTo(enumDeclaration.getContainerType().getMaxValue()) == 1)) {
            throw new ParseException("enum value is not in range"); //$NON-NLS-1$
        }

        return high;
    }

    /**
     * Creates the string representation of a type declaration (type specifier
     * list + pointers).
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node.
     * @param pointers
     *            A list of POINTER nodes.
     * @return The string representation.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    static String createTypeDeclarationString(
            CommonTree typeSpecifierList, List<CommonTree> pointers)
            throws ParseException {
        StringBuilder sb = new StringBuilder();

        TSDLASTUtils.createTypeSpecifierListString(typeSpecifierList, sb);
        createPointerListString(pointers, sb);

        return sb.toString();
    }

    /**
     * Creates the string representation of a list of type specifiers.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node.
     * @param sb
     *            A StringBuilder to which will be appended the string.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    static void createTypeSpecifierListString(
            CommonTree typeSpecifierList, StringBuilder sb)
            throws ParseException {

        List<CommonTree> children = typeSpecifierList.getChildren();

        boolean firstItem = true;

        for (CommonTree child : children) {
            if (!firstItem) {
                sb.append(' ');

            }

            firstItem = false;

            /* Append the string that represents this type specifier. */
            TSDLASTUtils.createTypeSpecifierString(child, sb);
        }
    }

    /**
     * Creates the string representation of a type specifier.
     *
     * @param typeSpecifier
     *            A TYPE_SPECIFIER node.
     * @param sb
     *            A StringBuilder to which will be appended the string.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    static void createTypeSpecifierString(CommonTree typeSpecifier,
            StringBuilder sb) throws ParseException {
        switch (typeSpecifier.getType()) {
        case CTFParser.FLOATTOK:
        case CTFParser.INTTOK:
        case CTFParser.LONGTOK:
        case CTFParser.SHORTTOK:
        case CTFParser.SIGNEDTOK:
        case CTFParser.UNSIGNEDTOK:
        case CTFParser.CHARTOK:
        case CTFParser.DOUBLETOK:
        case CTFParser.VOIDTOK:
        case CTFParser.BOOLTOK:
        case CTFParser.COMPLEXTOK:
        case CTFParser.IMAGINARYTOK:
        case CTFParser.CONSTTOK:
        case CTFParser.IDENTIFIER:
            sb.append(typeSpecifier.getText());
            break;
        case CTFParser.STRUCT: {
            CommonTree structName = (CommonTree) typeSpecifier.getFirstChildWithType(CTFParser.STRUCT_NAME);
            if (structName == null) {
                throw new ParseException("nameless struct found in createTypeSpecifierString"); //$NON-NLS-1$
            }

            CommonTree structNameIdentifier = (CommonTree) structName.getChild(0);

            sb.append(structNameIdentifier.getText());
            break;
        }
        case CTFParser.VARIANT: {
            CommonTree variantName = (CommonTree) typeSpecifier.getFirstChildWithType(CTFParser.VARIANT_NAME);
            if (variantName == null) {
                throw new ParseException("nameless variant found in createTypeSpecifierString"); //$NON-NLS-1$
            }

            CommonTree variantNameIdentifier = (CommonTree) variantName.getChild(0);

            sb.append(variantNameIdentifier.getText());
            break;
        }
        case CTFParser.ENUM: {
            CommonTree enumName = (CommonTree) typeSpecifier.getFirstChildWithType(CTFParser.ENUM_NAME);
            if (enumName == null) {
                throw new ParseException("nameless enum found in createTypeSpecifierString"); //$NON-NLS-1$
            }

            CommonTree enumNameIdentifier = (CommonTree) enumName.getChild(0);

            sb.append(enumNameIdentifier.getText());
            break;
        }
        case CTFParser.FLOATING_POINT:
        case CTFParser.INTEGER:
        case CTFParser.STRING:
            throw new ParseException("CTF type found in createTypeSpecifierString"); //$NON-NLS-1$
        default:
            childTypeError(typeSpecifier);
            break;
        }
    }

    /**
     * Gets the value of a "byte_order" attribute.
     *
     * @param rightNode
     *            A CTF_RIGHT node.
     * @param defaultByteOrder
     *            The default byte order in case none is set (Should be native
     *            of the trace, not the host)
     * @return The "byte_order" value.
     * @throws ParseException
     *             a malformed TSDL expression would cause an exception
     */
    public static ByteOrder getByteOrder(CommonTree rightNode, ByteOrder defaultByteOrder) throws ParseException {

        CommonTree firstChild = (CommonTree) rightNode.getChild(0);

        if (TSDLStringUtils.isUnaryString(firstChild)) {
            String strval = TSDLStringUtils.concatenateUnaryStrings(rightNode.getChildren());

            switch (strval) {
            case MetadataStrings.LE:
                return ByteOrder.LITTLE_ENDIAN;
            case MetadataStrings.BE:
            case MetadataStrings.NETWORK:
                return ByteOrder.BIG_ENDIAN;
            case MetadataStrings.NATIVE:
                return defaultByteOrder;
            default:
                throw new ParseException("Invalid value for byte order"); //$NON-NLS-1$
            }
        }
        throw new ParseException("Invalid value for byte order"); //$NON-NLS-1$
    }

}
