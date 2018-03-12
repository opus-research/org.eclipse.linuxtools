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

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.StringDeclaration;
import org.eclipse.linuxtools.ctf.parser.CTFParser;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;

/**
 * String operations within a TSDL abstract syntax tree
 *
 * @author Matthew Khouzam
 */
public final class TSDLStringUtils {

    private TSDLStringUtils() {
    }

    /**
     * Parses a unary string node and return the string value.
     *
     * @param unaryString
     *            The unary string node to parse (type UNARY_EXPRESSION_STRING
     *            or UNARY_EXPRESSION_STRING_QUOTES).
     * @return The string value.
     */
    /*
     * It would be really nice to remove the quotes earlier, such as in the
     * parser.
     */
    public static String parseUnaryString(CommonTree unaryString) {

        CommonTree value = (CommonTree) unaryString.getChild(0);
        String strval = value.getText();

        /* Remove quotes */
        if (unaryString.getType() == CTFParser.UNARY_EXPRESSION_STRING_QUOTES) {
            strval = strval.substring(1, strval.length() - 1);
        }

        return strval;
    }

    /**
     * Is the string unary (literal)?
     *
     * @param node
     *            The node to check.
     * @return True if the given node is an unary string.
     */
    public static boolean isUnaryString(CommonTree node) {
        return ((node.getType() == CTFParser.UNARY_EXPRESSION_STRING));
    }

    /**
     * Is the string unary or a composed string (literal)
     *
     * @param node
     *            The node to check.
     * @return True if the given node is any type of unary string (no quotes,
     *         quotes, etc).
     */
    public static boolean isAnyUnaryString(CommonTree node) {
        return ((node.getType() == CTFParser.UNARY_EXPRESSION_STRING) || (node.getType() == CTFParser.UNARY_EXPRESSION_STRING_QUOTES));
    }

    /**
     * Concatenates a list of unary strings separated by arrows (->) or dots.
     *
     * @param strings
     *            A list, first element being an unary string, subsequent
     *            elements being ARROW or DOT nodes with unary strings as child.
     * @return The string representation of the unary string chain.
     */
    public static String concatenateUnaryStrings(List<CommonTree> strings) {

        StringBuilder sb = new StringBuilder();

        CommonTree first = strings.get(0);
        sb.append(parseUnaryString(first));

        boolean isFirst = true;

        for (CommonTree ref : strings) {
            if (isFirst) {
                isFirst = false;
                continue;
            }

            CommonTree id = (CommonTree) ref.getChild(0);

            if (ref.getType() == CTFParser.ARROW) {
                sb.append("->"); //$NON-NLS-1$
            } else { /* DOT */
                sb.append('.');
            }

            sb.append(parseUnaryString(id));
        }

        return sb.toString();
    }

    /**
     * Parse a string node from a TSDL file
     * @param stringNode the node in the AST
     * @return a string declaration
     * @throws ParseException a malformed TSDL expression would cause an exception
     */
    public static StringDeclaration parseString(CommonTree stringNode)
            throws ParseException {

        List<CommonTree> children = stringNode.getChildren();
        StringDeclaration stringDeclaration = null;

        if (children == null) {
            stringDeclaration = new StringDeclaration();
        } else {
            Encoding encoding = Encoding.UTF8;
            for (CommonTree child : children) {
                switch (child.getType()) {
                case CTFParser.CTF_EXPRESSION_VAL:
                    /*
                     * An assignment expression must have 2 children, left and
                     * right
                     */

                    CommonTree leftNode = (CommonTree) child.getChild(0);
                    CommonTree rightNode = (CommonTree) child.getChild(1);

                    List<CommonTree> leftStrings = leftNode.getChildren();

                    if (!isAnyUnaryString(leftStrings.get(0))) {
                        throw new ParseException("Left side of ctf expression must be a string"); //$NON-NLS-1$
                    }
                    String left = concatenateUnaryStrings(leftStrings);

                    if (left.equals(MetadataStrings.ENCODING)) {
                        encoding = TSDLIntUtils.getEncoding(rightNode);
                    } else {
                        throw new ParseException("String: unknown attribute " //$NON-NLS-1$
                                + left);
                    }

                    break;
                default:
                    TSDLASTUtils.childTypeError(child);
                    break;
                }
            }

            stringDeclaration = new StringDeclaration(encoding);
        }

        return stringDeclaration;
    }

}
