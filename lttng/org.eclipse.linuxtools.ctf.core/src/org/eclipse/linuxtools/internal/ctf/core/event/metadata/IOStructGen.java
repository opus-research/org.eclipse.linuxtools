/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial Design and Grammar
 *     Francis Giraldeau - Initial API and implementation
 *     Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core.event.metadata;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.ctf.core.event.CTFClock;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.EnumDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.FloatDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IEventHeaderDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.VariantDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.CTFStream;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.parser.CTFParser;
import org.eclipse.linuxtools.internal.ctf.core.Activator;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.metadata.exceptions.ParseException;
import org.eclipse.linuxtools.internal.ctf.core.event.types.ArrayDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.types.SequenceDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.types.StructDeclarationFlattener;
import org.eclipse.linuxtools.internal.ctf.core.event.types.composite.EventHeaderCompactDeclaration;
import org.eclipse.linuxtools.internal.ctf.core.event.types.composite.EventHeaderLargeDeclaration;

/**
 * IOStructGen
 */
public class IOStructGen {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final int DEFAULT_FLOAT_EXPONENT = 8;
    private static final int DEFAULT_FLOAT_MANTISSA = 24;
    private static final int DEFAULT_INT_BASE = 10;
    /**
     * The trace
     */
    private final CTFTrace fTrace;
    private CommonTree fTree;

    /**
     * The current declaration scope.
     */
    private DeclarationScope fScope = null;

    /**
     * Data helpers needed for streaming
     */

    private boolean fHasBeenParsed = false;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tree
     *            the tree (ANTLR generated) with the parsed TSDL data.
     * @param trace
     *            the trace containing the places to put all the read metadata
     */
    public IOStructGen(CommonTree tree, CTFTrace trace) {
        fTrace = trace;
        fTree = tree;

    }

    /**
     * Parse the tree and populate the trace defined in the constructor.
     *
     * @throws ParseException
     *             If there was a problem parsing the metadata
     */
    public void generate() throws ParseException {
        parseRoot(fTree);
    }

    /**
     * Parse a partial tree and populate the trace defined in the constructor.
     * Does not check for a "trace" block as there is only one in the trace and
     * thus
     *
     * @throws ParseException
     *             If there was a problem parsing the metadata
     */
    public void generateFragment() throws ParseException {
        parseIncompleteRoot(fTree);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Sets a new tree to parse
     *
     * @param newTree
     *            the new tree to parse
     */
    public void setTree(CommonTree newTree) {
        fTree = newTree;
    }

    /**
     * Parse the root node.
     *
     * @param root
     *            A ROOT node.
     * @throws ParseException
     */
    private void parseRoot(CommonTree root) throws ParseException {

        List<CommonTree> children = root.getChildren();

        List<CommonTree> traceNode = new ArrayList<>();
        List<CommonTree> streams = new ArrayList<>();
        List<CommonTree> events = new ArrayList<>();
        List<CommonTree> declarations = new ArrayList<>();
        List<CommonTree> environments = new ArrayList<>();
        List<CommonTree> clocks = new ArrayList<>();
        List<CommonTree> callsites = new ArrayList<>();

        /* Create a new declaration scope with no parent. */
        pushScope();

        TSDLASTUtils.dispatchChildrenNodes(children, traceNode, streams, events, declarations, environments, clocks, callsites);
        if (traceNode.isEmpty()) {
            throw new ParseException("Missing trace block"); //$NON-NLS-1$
        }
        if (traceNode.size() != 1) {
            throw new ParseException("Only one trace block is allowed"); //$NON-NLS-1$
        }

        parseDeclarations(declarations);
        parseTrace(traceNode.get(0));
        parseEnvironment(environments);
        parseClocks(clocks);
        parseCallsites(callsites);

        if (!streams.isEmpty()) {
            parseStreams(streams);
        } else {
            /* Add an empty stream that will have a null id */
            fTrace.addStream(new CTFStream(fTrace));
        }

        parseEvents(events);
        popScope();
        fHasBeenParsed = true;
    }

    private void parseEvents(List<CommonTree> events) throws ParseException {
        for (CommonTree event : events) {
            parseEvent(event);
        }
    }

    private void parseStreams(List<CommonTree> streams) throws ParseException {
        for (CommonTree stream : streams) {
            parseStream(stream);
        }
    }

    private void parseCallsites(List<CommonTree> callsites) {
        for (CommonTree callsite : callsites) {
            parseCallsite(callsite);
        }
    }

    private void parseClocks(List<CommonTree> clocks) throws ParseException {
        for (CommonTree clock : clocks) {
            parseClock(clock);
        }
    }

    private void parseEnvironment(List<CommonTree> environments) {
        for (CommonTree environment : environments) {
            parseEnvironment(environment);
        }
    }

    private void parseDeclarations(List<CommonTree> declarations) throws ParseException {
        for (CommonTree decl : declarations) {
            parseRootDeclaration(decl);
        }
    }

    private void parseIncompleteRoot(CommonTree root) throws ParseException {
        List<CommonTree> children = root.getChildren();

        if (!fHasBeenParsed) {
            throw new ParseException("You need to run generate first"); //$NON-NLS-1$
        }
        List<CommonTree> traces = new ArrayList<>();
        List<CommonTree> streams = new ArrayList<>();
        List<CommonTree> events = new ArrayList<>();
        List<CommonTree> declarations = new ArrayList<>();
        List<CommonTree> environments = new ArrayList<>();
        List<CommonTree> clocks = new ArrayList<>();
        List<CommonTree> callsites = new ArrayList<>();
        /* Create a new declaration scope with no parent. */
        pushScope();

        TSDLASTUtils.dispatchChildrenNodes(children, traces, streams, events, declarations, environments, clocks, callsites);
        if (!traces.isEmpty()) {
            throw new ParseException("Trace block defined here, please use generate and not generateFragment to parse this fragment"); //$NON-NLS-1$
        }
        parseDeclarations(declarations);

        parseEnvironment(environments);
        parseClocks(clocks);
        parseCallsites(callsites);

        parseStreams(streams);

        parseEvents(events);
        popScope();
    }

    private void parseCallsite(CommonTree callsite) {

        List<CommonTree> children = callsite.getChildren();
        String name = null;
        String funcName = null;
        long lineNumber = -1;
        long ip = -1;
        String fileName = null;

        for (CommonTree child : children) {
            /* this is a regex to find the leading and trailing quotes */
            final String regex = "^\"|\"$"; //$NON-NLS-1$
            /*
             * this is to replace the previous quotes with nothing...
             * effectively deleting them
             */
            final String nullString = MetadataStrings.EMPTY_STRING;
            final String left = child.getChild(0).getChild(0).getChild(0).getText();
            switch (left) {
            case MetadataStrings.NAME:
                name = child.getChild(1).getChild(0).getChild(0).getText().replaceAll(regex, nullString);
                break;
            case MetadataStrings.FUNC:
                funcName = child.getChild(1).getChild(0).getChild(0).getText().replaceAll(regex, nullString);
                break;
            case MetadataStrings.IP:
                ip = Long.decode(child.getChild(1).getChild(0).getChild(0).getText());
                break;
            case MetadataStrings.FILE:
                fileName = child.getChild(1).getChild(0).getChild(0).getText().replaceAll(regex, nullString);
                break;
            case MetadataStrings.LINE:
                lineNumber = Long.parseLong(child.getChild(1).getChild(0).getChild(0).getText());
                break;
            default:
                Activator.log("Empty callsite"); //$NON-NLS-1$
                break;
            }
        }
        fTrace.addCallsite(name, funcName, ip, fileName, lineNumber);
    }

    private void parseEnvironment(CommonTree environment) {
        List<CommonTree> children = environment.getChildren();
        for (CommonTree child : children) {
            String left;
            String right;
            left = child.getChild(0).getChild(0).getChild(0).getText();
            right = child.getChild(1).getChild(0).getChild(0).getText();
            fTrace.addEnvironmentVar(left, right);
        }
    }

    private void parseClock(CommonTree clock) throws ParseException {
        List<CommonTree> children = clock.getChildren();
        CTFClock ctfClock = new CTFClock();
        for (CommonTree child : children) {
            final String key = child.getChild(0).getChild(0).getChild(0).getText();
            final CommonTree value = (CommonTree) child.getChild(1).getChild(0).getChild(0);
            final int type = value.getType();
            final String text = value.getText();
            switch (type) {
            case CTFParser.INTEGER:
            case CTFParser.DECIMAL_LITERAL:
                /*
                 * Not a pretty hack, this is to make sure that there is no
                 * number overflow due to 63 bit integers. The offset should
                 * only really be an issue in the year 2262. the tracer in C/ASM
                 * can write an offset in an unsigned 64 bit long. In java, the
                 * last bit, being set to 1 will be read as a negative number,
                 * but since it is too big a positive it will throw an
                 * exception. this will happen in 2^63 ns from 1970. Therefore
                 * 293 years from 1970
                 */
                Long numValue;
                try {
                    numValue = Long.parseLong(text);
                } catch (NumberFormatException e) {
                    throw new ParseException("Number conversion issue with " + text, e); //$NON-NLS-1$
                }
                ctfClock.addAttribute(key, numValue);
                break;
            default:
                ctfClock.addAttribute(key, text);
            }

        }
        String nameValue = ctfClock.getName();
        fTrace.addClock(nameValue, ctfClock);
    }

    private void parseTrace(CommonTree traceNode) throws ParseException {

        List<CommonTree> children = traceNode.getChildren();
        if (children == null) {
            throw new ParseException("Trace block is empty"); //$NON-NLS-1$
        }

        pushScope();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS:
                parseTypealias(child);
                break;
            case CTFParser.TYPEDEF:
                parseTypedef(child);
                break;
            case CTFParser.CTF_EXPRESSION_TYPE:
            case CTFParser.CTF_EXPRESSION_VAL:
                parseTraceDeclaration(child);
                break;
            default:
                TSDLASTUtils.childTypeError(child);
                break;
            }
        }

        /*
         * If trace byte order was not specified and not using packet based
         * metadata
         */
        if (fTrace.getByteOrder() == null) {
            throw new ParseException("Trace byte order not set"); //$NON-NLS-1$
        }

        popScope();
    }

    private void parseTraceDeclaration(CommonTree traceDecl)
            throws ParseException {

        /* There should be a left and right */

        CommonTree leftNode = (CommonTree) traceDecl.getChild(0);
        CommonTree rightNode = (CommonTree) traceDecl.getChild(1);

        List<CommonTree> leftStrings = leftNode.getChildren();

        if (!TSDLStringUtils.isAnyUnaryString(leftStrings.get(0))) {
            throw new ParseException("Left side of CTF assignment must be a string"); //$NON-NLS-1$
        }

        String left = TSDLStringUtils.concatenateUnaryStrings(leftStrings);

        switch (left) {
        case MetadataStrings.MAJOR:
            if (fTrace.majorIsSet()) {
                throw new ParseException("major is already set"); //$NON-NLS-1$
            }
            fTrace.setMajor(TSDLASTUtils.getMajorOrMinor(rightNode));
            break;
        case MetadataStrings.MINOR:
            if (fTrace.minorIsSet()) {
                throw new ParseException("minor is already set"); //$NON-NLS-1$
            }
            fTrace.setMinor(TSDLASTUtils.getMajorOrMinor(rightNode));
            break;
        case MetadataStrings.UUID_STRING:
            UUID uuid = TSDLASTUtils.getUUID(rightNode);
            /*
             * If uuid was already set by a metadata packet, compare it to see
             * if it matches
             */
            if (fTrace.uuidIsSet()) {
                if (fTrace.getUUID().compareTo(uuid) != 0) {
                    throw new ParseException("UUID mismatch. Packet says " //$NON-NLS-1$
                            + fTrace.getUUID() + " but metadata says " + uuid); //$NON-NLS-1$
                }
            } else {
                fTrace.setUUID(uuid);
            }
            break;
        case MetadataStrings.BYTE_ORDER:
            ByteOrder byteOrder = TSDLASTUtils.getByteOrder(rightNode, ByteOrder.nativeOrder());
            /*
             * If byte order was already set by a metadata packet, compare it to
             * see if it matches
             */
            if (fTrace.getByteOrder() != null) {
                if (fTrace.getByteOrder() != byteOrder) {
                    throw new ParseException(
                            "Endianness mismatch. Magic number says " //$NON-NLS-1$
                                    + fTrace.getByteOrder()
                                    + " but metadata says " + byteOrder); //$NON-NLS-1$
                }
            } else {
                fTrace.setByteOrder(byteOrder);
                final DeclarationScope parentScope = fScope.getParentScope();

                for (String type : parentScope.getTypeNames()) {
                    IDeclaration d = parentScope.lookupType(type);
                    if (d instanceof IntegerDeclaration) {
                        TSDLIntUtils.setByteOrder(byteOrder, parentScope, type, (IntegerDeclaration) d);
                    } else if (d instanceof StructDeclaration) {
                        setAlign(parentScope, (StructDeclaration) d, byteOrder);
                    }
                }
            }
            break;
        case MetadataStrings.PACKET_HEADER:
            if (fTrace.packetHeaderIsSet()) {
                throw new ParseException("packet.header already defined"); //$NON-NLS-1$
            }
            CommonTree typeSpecifier = (CommonTree) rightNode.getChild(0);
            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("packet.header expects a type specifier"); //$NON-NLS-1$
            }
            IDeclaration packetHeaderDecl = parseTypeSpecifierList(
                    typeSpecifier, null);
            if (!(packetHeaderDecl instanceof StructDeclaration)) {
                throw new ParseException("packet.header expects a struct"); //$NON-NLS-1$
            }
            fTrace.setPacketHeader((StructDeclaration) packetHeaderDecl);
            break;
        default:
            Activator.log(IStatus.WARNING, Messages.IOStructGen_UnknownTraceAttributeWarning + " " + left); //$NON-NLS-1$
            break;
        }
    }

    private void setAlign(DeclarationScope parentScope, StructDeclaration sd,
            ByteOrder byteOrder) throws ParseException {

        for (String s : sd.getFieldsList()) {
            IDeclaration d = sd.getField(s);

            if (d instanceof StructDeclaration) {
                setAlign(parentScope, (StructDeclaration) d, byteOrder);

            } else if (d instanceof VariantDeclaration) {
                setAlign(parentScope, (VariantDeclaration) d, byteOrder);
            } else if (d instanceof IntegerDeclaration) {
                IntegerDeclaration decl = (IntegerDeclaration) d;
                if (decl.getByteOrder() != byteOrder) {
                    IntegerDeclaration newI;
                    newI = IntegerDeclaration.createDeclaration(decl.getLength(),
                            decl.isSigned(), decl.getBase(), byteOrder,
                            decl.getEncoding(), decl.getClock(),
                            decl.getAlignment());
                    sd.getFields().put(s, newI);
                }
            }
        }
    }

    private void setAlign(DeclarationScope parentScope, VariantDeclaration vd,
            ByteOrder byteOrder) throws ParseException {

        for (String s : vd.getFields().keySet()) {
            IDeclaration d = vd.getFields().get(s);

            if (d instanceof StructDeclaration) {
                setAlign(parentScope, (StructDeclaration) d, byteOrder);

            } else if (d instanceof IntegerDeclaration) {
                IntegerDeclaration decl = (IntegerDeclaration) d;
                IntegerDeclaration newI;
                newI = IntegerDeclaration.createDeclaration(decl.getLength(),
                        decl.isSigned(), decl.getBase(), byteOrder,
                        decl.getEncoding(), decl.getClock(),
                        decl.getAlignment());
                vd.getFields().put(s, newI);
            }
        }
    }

    private void parseStream(CommonTree streamNode) throws ParseException {

        CTFStream stream = new CTFStream(fTrace);

        List<CommonTree> children = streamNode.getChildren();
        if (children == null) {
            throw new ParseException("Empty stream block"); //$NON-NLS-1$
        }

        pushScope();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS:
                parseTypealias(child);
                break;
            case CTFParser.TYPEDEF:
                parseTypedef(child);
                break;
            case CTFParser.CTF_EXPRESSION_TYPE:
            case CTFParser.CTF_EXPRESSION_VAL:
                parseStreamDeclaration(child, stream);
                break;
            default:
                TSDLASTUtils.childTypeError(child);
                break;
            }
        }

        if (stream.isIdSet() &&
                (!fTrace.packetHeaderIsSet() || !fTrace.getPacketHeader().hasField(MetadataStrings.STREAM_ID))) {
            throw new ParseException("Stream has an ID, but there is no stream_id field in packet header."); //$NON-NLS-1$
        }

        fTrace.addStream(stream);

        popScope();
    }

    private void parseStreamDeclaration(CommonTree streamDecl, CTFStream stream)
            throws ParseException {

        /* There should be a left and right */

        CommonTree leftNode = (CommonTree) streamDecl.getChild(0);
        CommonTree rightNode = (CommonTree) streamDecl.getChild(1);

        List<CommonTree> leftStrings = leftNode.getChildren();

        if (!TSDLStringUtils.isAnyUnaryString(leftStrings.get(0))) {
            throw new ParseException("Left side of CTF assignment must be a string"); //$NON-NLS-1$
        }

        CommonTree typeSpecifier = null;
        final String left = TSDLStringUtils.concatenateUnaryStrings(leftStrings);
        switch (left) {
        case MetadataStrings.ID:
            if (stream.isIdSet()) {
                throw new ParseException("stream id already defined"); //$NON-NLS-1$
            }
            long streamID = TSDLASTUtils.getStreamID(rightNode);
            stream.setId(streamID);
            break;
        case MetadataStrings.EVENT_HEADER:
            if (stream.isEventHeaderSet()) {
                throw new ParseException("event.header already defined"); //$NON-NLS-1$
            }
            typeSpecifier = (CommonTree) rightNode.getChild(0);
            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("event.header expects a type specifier"); //$NON-NLS-1$
            }
            IDeclaration eventHeaderDecl = parseTypeSpecifierList(
                    typeSpecifier, null);
            if (eventHeaderDecl instanceof StructDeclaration) {
                stream.setEventHeader((StructDeclaration) eventHeaderDecl);
            } else if (eventHeaderDecl instanceof IEventHeaderDeclaration) {
                stream.setEventHeader((IEventHeaderDeclaration) eventHeaderDecl);
            } else {
                throw new ParseException("event.header expects a struct"); //$NON-NLS-1$
            }
            break;
        case MetadataStrings.EVENT_CONTEXT:
            if (stream.isEventContextSet()) {
                throw new ParseException("event.context already defined"); //$NON-NLS-1$
            }
            typeSpecifier = (CommonTree) rightNode.getChild(0);
            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("event.context expects a type specifier"); //$NON-NLS-1$
            }
            IDeclaration eventContextDecl = parseTypeSpecifierList(
                    typeSpecifier, null);
            if (!(eventContextDecl instanceof StructDeclaration)) {
                throw new ParseException("event.context expects a struct"); //$NON-NLS-1$
            }
            stream.setEventContext((StructDeclaration) eventContextDecl);
            break;
        case MetadataStrings.PACKET_CONTEXT:
            if (stream.isPacketContextSet()) {
                throw new ParseException("packet.context already defined"); //$NON-NLS-1$
            }
            typeSpecifier = (CommonTree) rightNode.getChild(0);
            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("packet.context expects a type specifier"); //$NON-NLS-1$
            }
            IDeclaration packetContextDecl = parseTypeSpecifierList(
                    typeSpecifier, null);
            if (!(packetContextDecl instanceof StructDeclaration)) {
                throw new ParseException("packet.context expects a struct"); //$NON-NLS-1$
            }
            stream.setPacketContext((StructDeclaration) packetContextDecl);
            break;
        default:
            Activator.log(IStatus.WARNING, Messages.IOStructGen_UnknownStreamAttributeWarning + " " + left); //$NON-NLS-1$
            break;
        }
    }

    private void parseEvent(CommonTree eventNode) throws ParseException {

        List<CommonTree> children = eventNode.getChildren();
        if (children == null) {
            throw new ParseException("Empty event block"); //$NON-NLS-1$
        }

        EventDeclaration event = new EventDeclaration();

        pushScope();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS:
                parseTypealias(child);
                break;
            case CTFParser.TYPEDEF:
                parseTypedef(child);
                break;
            case CTFParser.CTF_EXPRESSION_TYPE:
            case CTFParser.CTF_EXPRESSION_VAL:
                parseEventDeclaration(child, event);
                break;
            default:
                TSDLASTUtils.childTypeError(child);
                break;
            }
        }

        if (!event.nameIsSet()) {
            throw new ParseException("Event name not set"); //$NON-NLS-1$
        }

        /*
         * If the event did not specify a stream, then the trace must be single
         * stream
         */
        if (!event.streamIsSet()) {
            if (fTrace.nbStreams() > 1) {
                throw new ParseException("Event without stream_id with more than one stream"); //$NON-NLS-1$
            }

            /*
             * If the event did not specify a stream, the only existing stream
             * must not have an id. Note: That behavior could be changed, it
             * could be possible to just get the only existing stream, whatever
             * is its id.
             */
            CTFStream stream = fTrace.getStream(null);

            if (stream != null) {
                event.setStream(stream);
            } else {
                throw new ParseException("Event without stream_id, but there is no stream without id"); //$NON-NLS-1$
            }
        }

        /*
         * Add the event to the stream.
         */
        event.getStream().addEvent(event);

        popScope();
    }

    private void parseEventDeclaration(CommonTree eventDecl,
            EventDeclaration event) throws ParseException {

        /* There should be a left and right */

        CommonTree leftNode = (CommonTree) eventDecl.getChild(0);
        CommonTree rightNode = (CommonTree) eventDecl.getChild(1);

        List<CommonTree> leftStrings = leftNode.getChildren();

        if (!TSDLStringUtils.isAnyUnaryString(leftStrings.get(0))) {
            throw new ParseException("Left side of CTF assignment must be a string"); //$NON-NLS-1$
        }

        CommonTree typeSpecifier = null;
        final String left = TSDLStringUtils.concatenateUnaryStrings(leftStrings);
        switch (left) {
        case MetadataStrings.NAME:
            if (event.nameIsSet()) {
                throw new ParseException("name already defined"); //$NON-NLS-1$
            }
            String name = TSDLASTUtils.getEventName(rightNode);
            event.setName(name);
            break;
        case MetadataStrings.ID:
            if (event.idIsSet()) {
                throw new ParseException("id already defined"); //$NON-NLS-1$
            }
            long id = TSDLASTUtils.getEventID(rightNode);
            if (id > Integer.MAX_VALUE) {
                throw new ParseException("id is greater than int.maxvalue, unsupported. id : " + id); //$NON-NLS-1$
            }
            if (id < 0) {
                throw new ParseException("negative id, unsupported. id : " + id); //$NON-NLS-1$
            }
            event.setId((int) id);
            break;
        case MetadataStrings.STREAM_ID:
            if (event.streamIsSet()) {
                throw new ParseException("stream id already defined"); //$NON-NLS-1$
            }
            long streamId = TSDLASTUtils.getStreamID(rightNode);
            CTFStream stream = fTrace.getStream(streamId);
            if (stream == null) {
                throw new ParseException("Stream " + streamId + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            event.setStream(stream);
            break;
        case MetadataStrings.CONTEXT:
            if (event.contextIsSet()) {
                throw new ParseException("context already defined"); //$NON-NLS-1$
            }
            typeSpecifier = (CommonTree) rightNode.getChild(0);
            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("context expects a type specifier"); //$NON-NLS-1$
            }
            IDeclaration contextDecl = parseTypeSpecifierList(typeSpecifier,
                    null);
            if (!(contextDecl instanceof StructDeclaration)) {
                throw new ParseException("context expects a struct"); //$NON-NLS-1$
            }
            event.setContext((StructDeclaration) contextDecl);
            break;
        case MetadataStrings.FIELDS_STRING:
            if (event.fieldsIsSet()) {
                throw new ParseException("fields already defined"); //$NON-NLS-1$
            }
            typeSpecifier = (CommonTree) rightNode.getChild(0);
            if (typeSpecifier.getType() != CTFParser.TYPE_SPECIFIER_LIST) {
                throw new ParseException("fields expects a type specifier"); //$NON-NLS-1$
            }
            IDeclaration fieldsDecl;
            fieldsDecl = parseTypeSpecifierList(typeSpecifier, null);
            if (!(fieldsDecl instanceof StructDeclaration)) {
                throw new ParseException("fields expects a struct"); //$NON-NLS-1$
            }
            /*
             * The underscores in the event names. These underscores were added
             * by the LTTng tracer.
             */
            final StructDeclaration fields = (StructDeclaration) fieldsDecl;
            event.setFields(fields);
            break;
        case MetadataStrings.LOGLEVEL2:
            long logLevel = TSDLIntUtils.parseUnaryInteger((CommonTree) rightNode.getChild(0));
            event.setLogLevel(logLevel);
            break;
        default:
            /* Custom event attribute, we'll add it to the attributes map */
            String right = TSDLStringUtils.parseUnaryString((CommonTree) rightNode.getChild(0));
            event.setCustomAttribute(left, right);
            break;
        }
    }

    /**
     * Parses a declaration at the root level.
     *
     * @param declaration
     *            The declaration subtree.
     * @throws ParseException
     */
    private void parseRootDeclaration(CommonTree declaration)
            throws ParseException {

        List<CommonTree> children = declaration.getChildren();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEDEF:
                parseTypedef(child);
                break;
            case CTFParser.TYPEALIAS:
                parseTypealias(child);
                break;
            case CTFParser.TYPE_SPECIFIER_LIST:
                parseTypeSpecifierList(child, null);
                break;
            default:
                TSDLASTUtils.childTypeError(child);
            }
        }
    }

    /**
     * Parses a typealias node. It parses the target, the alias, and registers
     * the type in the current scope.
     *
     * @param typealias
     *            A TYPEALIAS node.
     * @throws ParseException
     */
    private void parseTypealias(CommonTree typealias) throws ParseException {

        List<CommonTree> children = typealias.getChildren();

        CommonTree target = null;
        CommonTree alias = null;

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPEALIAS_TARGET:
                target = child;
                break;
            case CTFParser.TYPEALIAS_ALIAS:
                alias = child;
                break;
            default:
                TSDLASTUtils.childTypeError(child);
                break;
            }
        }

        IDeclaration targetDeclaration = parseTypealiasTarget(target);

        if ((targetDeclaration instanceof VariantDeclaration)
                && ((VariantDeclaration) targetDeclaration).isTagged()) {
            throw new ParseException("Typealias of untagged variant is not permitted"); //$NON-NLS-1$
        }

        String aliasString = TSDLASTUtils.parseTypealiasAlias(alias);

        getCurrentScope().registerType(aliasString, targetDeclaration);
    }

    /**
     * Parses the target part of a typealias and gets the corresponding
     * declaration.
     *
     * @param target
     *            A TYPEALIAS_TARGET node.
     * @return The corresponding declaration.
     * @throws ParseException
     */
    private IDeclaration parseTypealiasTarget(CommonTree target)
            throws ParseException {

        List<CommonTree> children = target.getChildren();

        CommonTree typeSpecifierList = null;
        CommonTree typeDeclaratorList = null;
        CommonTree typeDeclarator = null;
        StringBuilder identifierSB = new StringBuilder();

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.TYPE_SPECIFIER_LIST:
                typeSpecifierList = child;
                break;
            case CTFParser.TYPE_DECLARATOR_LIST:
                typeDeclaratorList = child;
                break;
            default:
                TSDLASTUtils.childTypeError(child);
                break;
            }
        }

        if (typeDeclaratorList != null) {
            /*
             * Only allow one declarator
             *
             * eg: "typealias uint8_t *, ** := puint8_t;" is not permitted,
             * otherwise the new type puint8_t would maps to two different
             * types.
             */
            if (typeDeclaratorList.getChildCount() != 1) {
                throw new ParseException("Only one type declarator is allowed in the typealias target"); //$NON-NLS-1$
            }

            typeDeclarator = (CommonTree) typeDeclaratorList.getChild(0);
        }

        /* Parse the target type and get the declaration */
        IDeclaration targetDeclaration = parseTypeDeclarator(typeDeclarator,
                typeSpecifierList, identifierSB);

        /*
         * We don't allow identifier in the target
         *
         * eg: "typealias uint8_t* hello := puint8_t;", the "hello" is not
         * permitted
         */
        if (identifierSB.length() > 0) {
            throw new ParseException("Identifier (" + identifierSB.toString() //$NON-NLS-1$
                    + ") not expected in the typealias target"); //$NON-NLS-1$
        }

        return targetDeclaration;
    }

    /**
     * Parses a typedef node. This creates and registers a new declaration for
     * each declarator found in the typedef.
     *
     * @param typedef
     *            A TYPEDEF node.
     * @throws ParseException
     *             If there is an error creating the declaration.
     */
    private void parseTypedef(CommonTree typedef) throws ParseException {

        CommonTree typeDeclaratorListNode = (CommonTree) typedef.getFirstChildWithType(CTFParser.TYPE_DECLARATOR_LIST);

        CommonTree typeSpecifierListNode = (CommonTree) typedef.getFirstChildWithType(CTFParser.TYPE_SPECIFIER_LIST);

        List<CommonTree> typeDeclaratorList = typeDeclaratorListNode.getChildren();

        for (CommonTree typeDeclaratorNode : typeDeclaratorList) {
            StringBuilder identifierSB = new StringBuilder();

            IDeclaration typeDeclaration = parseTypeDeclarator(
                    typeDeclaratorNode, typeSpecifierListNode, identifierSB);

            if ((typeDeclaration instanceof VariantDeclaration)
                    && ((VariantDeclaration) typeDeclaration).isTagged()) {
                throw new ParseException("Typealias of untagged variant is not permitted"); //$NON-NLS-1$
            }

            getCurrentScope().registerType(identifierSB.toString(),
                    typeDeclaration);
        }
    }

    /**
     * Parses a pair type declarator / type specifier list and returns the
     * corresponding declaration. If it is present, it also writes the
     * identifier of the declarator in the given {@link StringBuilder}.
     *
     * @param typeDeclarator
     *            A TYPE_DECLARATOR node.
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node.
     * @param identifierSB
     *            A StringBuilder that will receive the identifier found in the
     *            declarator.
     * @return The corresponding declaration.
     * @throws ParseException
     *             If there is an error finding or creating the declaration.
     */
    private IDeclaration parseTypeDeclarator(CommonTree typeDeclarator,
            CommonTree typeSpecifierList, StringBuilder identifierSB)
            throws ParseException {

        IDeclaration declaration = null;
        List<CommonTree> children = null;
        List<CommonTree> pointers = new LinkedList<>();
        List<CommonTree> lengths = new LinkedList<>();
        CommonTree identifier = null;

        /* Separate the tokens by type */
        if (typeDeclarator != null) {
            children = typeDeclarator.getChildren();
            for (CommonTree child : children) {

                switch (child.getType()) {
                case CTFParser.POINTER:
                    pointers.add(child);
                    break;
                case CTFParser.IDENTIFIER:
                    identifier = child;
                    break;
                case CTFParser.LENGTH:
                    lengths.add(child);
                    break;
                default:
                    TSDLASTUtils.childTypeError(child);
                    break;
                }
            }

        }

        /*
         * Parse the type specifier list, which is the "base" type. For example,
         * it would be int in int a[3][len].
         */
        declaration = parseTypeSpecifierList(typeSpecifierList, pointers);

        /*
         * Each length subscript means that we must create a nested array or
         * sequence. For example, int a[3][len] means that we have an array of 3
         * (sequences of length 'len' of (int)).
         */
        if (!lengths.isEmpty()) {
            /* We begin at the end */
            Collections.reverse(lengths);

            for (CommonTree length : lengths) {
                /*
                 * By looking at the first expression, we can determine whether
                 * it is an array or a sequence.
                 */
                List<CommonTree> lengthChildren = length.getChildren();

                CommonTree first = lengthChildren.get(0);
                if (TSDLIntUtils.isUnaryInteger(first)) {
                    /* Array */
                    int arrayLength = (int) TSDLIntUtils.parseUnaryInteger(first);

                    if (arrayLength < 1) {
                        throw new ParseException("Array length is negative"); //$NON-NLS-1$
                    }

                    /* Create the array declaration. */
                    declaration = new ArrayDeclaration(arrayLength, declaration);
                } else if (TSDLStringUtils.isAnyUnaryString(first)) {
                    /* Sequence */
                    String lengthName = TSDLStringUtils.concatenateUnaryStrings(lengthChildren);

                    /* check that lengthName was declared */
                    if (isSignedIntegerField(lengthName)) {
                        throw new ParseException("Sequence declared with length that is not an unsigned integer"); //$NON-NLS-1$
                    }
                    /* Create the sequence declaration. */
                    declaration = new SequenceDeclaration(lengthName,
                            declaration);
                } else {
                    TSDLASTUtils.childTypeError(first);
                }
            }
        }

        if (identifier != null) {
            identifierSB.append(identifier.getText());
        }

        return declaration;
    }

    private boolean isSignedIntegerField(String lengthName) throws ParseException {
        IDeclaration decl = getCurrentScope().lookupIdentifierRecursive(lengthName);
        if (decl instanceof IntegerDeclaration) {
            return ((IntegerDeclaration) decl).isSigned();
        }
        throw new ParseException("Is not an integer: " + lengthName); //$NON-NLS-1$

    }

    /**
     * Parses a type specifier list and returns the corresponding declaration.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node.
     * @param pointerList
     *            A list of POINTER nodes that apply to the specified type.
     * @return The corresponding declaration.
     * @throws ParseException
     *             If the type has not been defined or if there is an error
     *             creating the declaration.
     */
    private IDeclaration parseTypeSpecifierList(CommonTree typeSpecifierList,
            List<CommonTree> pointerList) throws ParseException {
        IDeclaration declaration = null;

        /*
         * By looking at the first element of the type specifier list, we can
         * determine which type it belongs to.
         */
        CommonTree firstChild = (CommonTree) typeSpecifierList.getChild(0);

        switch (firstChild.getType()) {
        case CTFParser.FLOATING_POINT:
            declaration = parseFloat(firstChild);
            break;
        case CTFParser.INTEGER:
            declaration = parseInteger(firstChild);
            break;
        case CTFParser.STRING:
            declaration = TSDLStringUtils.parseString(firstChild);
            break;
        case CTFParser.STRUCT:
            declaration = parseStruct(firstChild);
            StructDeclaration structDeclaration = (StructDeclaration) declaration;
            IDeclaration idEnumDecl = structDeclaration.getFields().get("id"); //$NON-NLS-1$
            if (EventHeaderCompactDeclaration.isCompactEventHeader(structDeclaration)) {
                ByteOrder bo = ((EnumDeclaration) idEnumDecl).getContainerType().getByteOrder();
                declaration = new EventHeaderCompactDeclaration(bo);
            } else if (EventHeaderLargeDeclaration.isLargeEventHeader(structDeclaration)) {
                ByteOrder bo = ((EnumDeclaration) idEnumDecl).getContainerType().getByteOrder();
                declaration = new EventHeaderLargeDeclaration(bo);
            }
            break;
        case CTFParser.VARIANT:
            declaration = parseVariant(firstChild);
            break;
        case CTFParser.ENUM:
            declaration = parseEnum(firstChild);
            break;
        case CTFParser.IDENTIFIER:
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
            declaration = parseTypeDeclaration(typeSpecifierList, pointerList);
            break;
        default:
            TSDLASTUtils.childTypeError(firstChild);
        }

        return declaration;
    }

    private IDeclaration parseFloat(CommonTree floatingPoint)
            throws ParseException {

        List<CommonTree> children = floatingPoint.getChildren();

        /*
         * If the integer has no attributes, then it is missing the size
         * attribute which is required
         */
        if (children == null) {
            throw new ParseException("float: missing size attribute"); //$NON-NLS-1$
        }

        /* The return value */
        FloatDeclaration floatDeclaration = null;
        ByteOrder byteOrder = fTrace.getByteOrder();
        long alignment = 0;

        int exponent = DEFAULT_FLOAT_EXPONENT;
        int mantissa = DEFAULT_FLOAT_MANTISSA;

        /* Iterate on all integer children */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.CTF_EXPRESSION_VAL:
                /*
                 * An assignment expression must have 2 children, left and right
                 */

                CommonTree leftNode = (CommonTree) child.getChild(0);
                CommonTree rightNode = (CommonTree) child.getChild(1);

                List<CommonTree> leftStrings = leftNode.getChildren();

                if (!TSDLStringUtils.isAnyUnaryString(leftStrings.get(0))) {
                    throw new ParseException("Left side of ctf expression must be a string"); //$NON-NLS-1$
                }
                String left = TSDLStringUtils.concatenateUnaryStrings(leftStrings);

                switch (left) {
                case MetadataStrings.EXP_DIG:
                    exponent = (int) TSDLIntUtils.parseUnaryInteger((CommonTree) rightNode.getChild(0));
                    break;
                case MetadataStrings.BYTE_ORDER:
                    byteOrder = TSDLASTUtils.getByteOrder(rightNode, fTrace.getByteOrder());
                    break;
                case MetadataStrings.MANT_DIG:
                    mantissa = (int) TSDLIntUtils.parseUnaryInteger((CommonTree) rightNode.getChild(0));
                    break;
                case MetadataStrings.ALIGN:
                    alignment = TSDLIntUtils.getAlignment(rightNode);
                    break;
                default:
                    throw new ParseException("Float: unknown attribute " + left); //$NON-NLS-1$
                }

                break;
            default:
                TSDLASTUtils.childTypeError(child);
                break;
            }
        }
        int size = mantissa + exponent;
        if (size == 0) {
            throw new ParseException("Float missing size attribute"); //$NON-NLS-1$
        }

        alignment = TSDLASTUtils.fixAlignment(alignment, size);

        floatDeclaration = new FloatDeclaration(exponent, mantissa, byteOrder, alignment);

        return floatDeclaration;

    }

    /**
     * Parses a type specifier list as a user-declared type.
     *
     * @param typeSpecifierList
     *            A TYPE_SPECIFIER_LIST node containing a user-declared type.
     * @param pointerList
     *            A list of POINTER nodes that apply to the type specified in
     *            typeSpecifierList.
     * @return The corresponding declaration.
     * @throws ParseException
     *             If the type does not exist (has not been found).
     */
    private IDeclaration parseTypeDeclaration(CommonTree typeSpecifierList,
            List<CommonTree> pointerList) throws ParseException {
        /* Create the string representation of the type declaration */
        String typeStringRepresentation = TSDLASTUtils.createTypeDeclarationString(
                typeSpecifierList, pointerList);

        /* Use the string representation to search the type in the current scope */
        IDeclaration decl = getCurrentScope().lookupTypeRecursive(
                typeStringRepresentation);

        if (decl == null) {
            throw new ParseException("Type " + typeStringRepresentation //$NON-NLS-1$
                    + " has not been defined."); //$NON-NLS-1$
        }

        return decl;
    }

    /**
     * Parses an integer declaration node.
     *
     * @param integer
     *            An INTEGER node.
     * @return The corresponding integer declaration.
     * @throws ParseException
     */
    private IntegerDeclaration parseInteger(CommonTree integer)
            throws ParseException {

        List<CommonTree> children = integer.getChildren();

        /*
         * If the integer has no attributes, then it is missing the size
         * attribute which is required
         */
        if (children == null) {
            throw new ParseException("integer: missing size attribute"); //$NON-NLS-1$
        }

        /* The return value */
        IntegerDeclaration integerDeclaration = null;
        boolean signed = false;
        ByteOrder byteOrder = fTrace.getByteOrder();
        long size = 0;
        long alignment = 0;
        int base = DEFAULT_INT_BASE;
        @NonNull
        String clock = MetadataStrings.EMPTY_STRING;

        Encoding encoding = Encoding.NONE;

        /* Iterate on all integer children */
        for (CommonTree child : children) {
            int type = child.getType();
            if (type == CTFParser.CTF_EXPRESSION_VAL) {
                CommonTree leftNode = (CommonTree) child.getChild(0);
                CommonTree rightNode = (CommonTree) child.getChild(1);
                List<CommonTree> leftStrings = leftNode.getChildren();
                if (!TSDLStringUtils.isAnyUnaryString(leftStrings.get(0))) {
                    throw new ParseException("Left side of ctf expression must be a string"); //$NON-NLS-1$
                }
                String left = TSDLStringUtils.concatenateUnaryStrings(leftStrings);
                switch (left) {
                case MetadataStrings.SIGNED:
                    signed = TSDLIntUtils.getSigned(rightNode);
                    break;
                case MetadataStrings.BYTE_ORDER:
                    byteOrder = TSDLASTUtils.getByteOrder(rightNode, fTrace.getByteOrder());
                    break;
                case MetadataStrings.SIZE:
                    size = TSDLIntUtils.getSize(rightNode);
                    break;
                case MetadataStrings.ALIGN:
                    alignment = TSDLIntUtils.getAlignment(rightNode);
                    break;
                case MetadataStrings.BASE:
                    base = TSDLIntUtils.getBase(rightNode);
                    break;
                case MetadataStrings.ENCODING:
                    encoding = TSDLIntUtils.getEncoding(rightNode);
                    break;
                case MetadataStrings.MAP:
                    clock = TSDLASTUtils.getClock(rightNode);
                    break;
                default:
                    Activator.log(IStatus.WARNING, Messages.IOStructGen_UnknownIntegerAttributeWarning + " " + left); //$NON-NLS-1$
                    break;
                }
            } else {
                TSDLASTUtils.childTypeError(child);
            }
        }

        if (size == 0) {
            throw new ParseException("Integer missing size attribute"); //$NON-NLS-1$
        }

        alignment = TSDLASTUtils.fixAlignment(alignment, size);

        integerDeclaration = IntegerDeclaration.createDeclaration((int) size, signed, base,
                byteOrder, encoding, clock, alignment);

        return integerDeclaration;
    }

    /**
     * Parses a struct declaration and returns the corresponding declaration.
     *
     * @param struct
     *            An STRUCT node.
     * @return The corresponding struct declaration.
     * @throws ParseException
     */
    private StructDeclaration parseStruct(CommonTree struct)
            throws ParseException {

        List<CommonTree> children = struct.getChildren();

        /* The return value */
        StructDeclaration structDeclaration = null;

        /* Name */
        String structName = null;
        boolean hasName = false;

        /* Body */
        CommonTree structBody = null;
        boolean hasBody = false;

        /* Align */
        long structAlign = 0;

        /* Loop on all children and identify what we have to work with. */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.STRUCT_NAME: {
                hasName = true;
                CommonTree structNameIdentifier = (CommonTree) child.getChild(0);
                structName = structNameIdentifier.getText();
                break;
            }
            case CTFParser.STRUCT_BODY: {
                hasBody = true;
                structBody = child;
                break;
            }
            case CTFParser.ALIGN: {
                CommonTree structAlignExpression = (CommonTree) child.getChild(0);
                structAlign = TSDLIntUtils.getAlignment(structAlignExpression);
                break;
            }
            default:
                TSDLASTUtils.childTypeError(child);
                break;
            }
        }

        /*
         * If a struct has just a body and no name (just like the song,
         * "A Struct With No Name" by America (sorry for that...)), it's a
         * definition of a new type, so we create the type declaration and
         * return it. We can't add it to the declaration scope since there is no
         * name, but that's what we want because it won't be possible to use it
         * again to declare another field.
         *
         * If it has just a name, we look it up in the declaration scope and
         * return the associated declaration. If it is not found in the
         * declaration scope, it means that a struct with that name has not been
         * declared, which is an error.
         *
         * If it has both, then we create the type declaration and register it
         * to the current scope.
         *
         * If it has none, then what are we doing here ?
         */
        if (hasBody) {
            /*
             * If struct has a name, check if already defined in the current
             * scope.
             */
            if (hasName && (getCurrentScope().lookupStruct(structName) != null)) {
                throw new ParseException("struct " + structName //$NON-NLS-1$
                        + " already defined."); //$NON-NLS-1$
            }
            /* Create the declaration */
            structDeclaration = new StructDeclaration(structAlign);

            /* Parse the body */
            parseStructBody(structBody, structDeclaration);

            /* If struct has name, add it to the current scope. */
            if (hasName) {
                getCurrentScope().registerStruct(structName, structDeclaration);
            }
        } else /* !hasBody */{
            if (hasName) {
                /* Name and !body */

                /* Lookup the name in the current scope. */
                structDeclaration = getCurrentScope().lookupStructRecursive(structName);

                /*
                 * If not found, it means that a struct with such name has not
                 * been defined
                 */
                if (structDeclaration == null) {
                    throw new ParseException("struct " + structName //$NON-NLS-1$
                            + " is not defined"); //$NON-NLS-1$
                }
            } else {
                /* !Name and !body */

                /* We can't do anything with that. */
                throw new ParseException("struct with no name and no body"); //$NON-NLS-1$
            }
        }
        return StructDeclarationFlattener.tryFlattenStruct(structDeclaration);
    }

    /**
     * Parses a struct body, adding the fields to specified structure
     * declaration.
     *
     * @param structBody
     *            A STRUCT_BODY node.
     * @param structDeclaration
     *            The struct declaration.
     * @throws ParseException
     */
    private void parseStructBody(CommonTree structBody,
            StructDeclaration structDeclaration) throws ParseException {

        List<CommonTree> structDeclarations = structBody.getChildren();

        /*
         * If structDeclaration is null, structBody has no children and the
         * struct body is empty.
         */
        if (structDeclarations != null) {
            pushScope();

            for (CommonTree declarationNode : structDeclarations) {
                parseStructDeclaration(structDeclaration, declarationNode);
            }
            popScope();
        }
    }

    private void parseStructDeclaration(StructDeclaration structDeclaration, CommonTree declarationNode) throws ParseException {
        switch (declarationNode.getType()) {
        case CTFParser.TYPEALIAS:
            parseTypealias(declarationNode);
            break;
        case CTFParser.TYPEDEF:
            parseTypedef(declarationNode);
            break;
        case CTFParser.SV_DECLARATION:
            parseStructDeclaration(declarationNode, structDeclaration);
            break;
        default:
            TSDLASTUtils.childTypeError(declarationNode);
            break;
        }
    }

    /**
     * Parses a declaration found in a struct.
     *
     * @param declaration
     *            A SV_DECLARATION node.
     * @param struct
     *            A struct declaration. (I know, little name clash here...)
     * @throws ParseException
     */
    private void parseStructDeclaration(CommonTree declaration,
            StructDeclaration struct) throws ParseException {

        /* Get the type specifier list node */
        CommonTree typeSpecifierListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_SPECIFIER_LIST);

        /* Get the type declarator list node */
        CommonTree typeDeclaratorListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_DECLARATOR_LIST);

        /* Get the type declarator list */
        List<CommonTree> typeDeclaratorList = typeDeclaratorListNode.getChildren();

        /*
         * For each type declarator, parse the declaration and add a field to
         * the struct
         */
        for (CommonTree typeDeclaratorNode : typeDeclaratorList) {

            StringBuilder identifierSB = new StringBuilder();

            IDeclaration decl = parseTypeDeclarator(typeDeclaratorNode,
                    typeSpecifierListNode, identifierSB);
            String fieldName = identifierSB.toString();
            getCurrentScope().registerIdentifier(fieldName, decl);

            if (struct.hasField(fieldName)) {
                throw new ParseException("struct: duplicate field " //$NON-NLS-1$
                        + fieldName);
            }

            struct.addField(fieldName, decl);

        }
    }

    /**
     * Parses an enum declaration and returns the corresponding declaration.
     *
     * @param theEnum
     *            An ENUM node.
     * @return The corresponding enum declaration.
     * @throws ParseException
     */
    private EnumDeclaration parseEnum(CommonTree theEnum) throws ParseException {

        List<CommonTree> children = theEnum.getChildren();

        /* The return value */
        EnumDeclaration enumDeclaration = null;

        /* Name */
        String enumName = null;

        /* Body */
        CommonTree enumBody = null;

        /* Container type */
        IntegerDeclaration containerTypeDeclaration = null;

        /* Loop on all children and identify what we have to work with. */
        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.ENUM_NAME: {
                CommonTree enumNameIdentifier = (CommonTree) child.getChild(0);
                enumName = enumNameIdentifier.getText();
                break;
            }
            case CTFParser.ENUM_BODY: {
                enumBody = child;
                break;
            }
            case CTFParser.ENUM_CONTAINER_TYPE: {
                containerTypeDeclaration = parseEnumContainerType(child);
                break;
            }
            default:
                TSDLASTUtils.childTypeError(child);
                break;
            }
        }

        /*
         * If the container type has not been defined explicitly, we assume it
         * is "int".
         */
        if (containerTypeDeclaration == null) {
            IDeclaration enumDecl;
            /*
             * it could be because the enum was already declared.
             */
            if (enumName != null) {
                enumDecl = getCurrentScope().lookupEnumRecursive(enumName);
                if (enumDecl != null) {
                    return (EnumDeclaration) enumDecl;
                }
            }

            IDeclaration decl = getCurrentScope().lookupTypeRecursive("int"); //$NON-NLS-1$

            if (decl == null) {
                throw new ParseException("enum container type implicit and type int not defined"); //$NON-NLS-1$
            } else if (!(decl instanceof IntegerDeclaration)) {
                throw new ParseException("enum container type implicit and type int not an integer"); //$NON-NLS-1$
            }

            containerTypeDeclaration = (IntegerDeclaration) decl;
        }

        /*
         * If it has a body, it's a new declaration, otherwise it's a reference
         * to an existing declaration. Same logic as struct.
         */
        if (enumBody != null) {
            /*
             * If enum has a name, check if already defined in the current
             * scope.
             */
            if ((enumName != null)
                    && (getCurrentScope().lookupEnum(enumName) != null)) {
                throw new ParseException("enum " + enumName //$NON-NLS-1$
                        + " already defined"); //$NON-NLS-1$
            }

            /* Create the declaration */
            enumDeclaration = new EnumDeclaration(containerTypeDeclaration);

            /* Parse the body */
            parseEnumBody(enumBody, enumDeclaration);

            /* If the enum has name, add it to the current scope. */
            if (enumName != null) {
                getCurrentScope().registerEnum(enumName, enumDeclaration);
            }
        } else {
            if (enumName != null) {
                /* Name and !body */

                /* Lookup the name in the current scope. */
                enumDeclaration = getCurrentScope().lookupEnumRecursive(enumName);

                /*
                 * If not found, it means that an enum with such name has not
                 * been defined
                 */
                if (enumDeclaration == null) {
                    throw new ParseException("enum " + enumName //$NON-NLS-1$
                            + " is not defined"); //$NON-NLS-1$
                }
            } else {
                /* !Name and !body */
                throw new ParseException("enum with no name and no body"); //$NON-NLS-1$
            }
        }

        return enumDeclaration;

    }

    /**
     * Parses an enum body, adding the enumerators to the specified enum
     * declaration.
     *
     * @param enumBody
     *            An ENUM_BODY node.
     * @param enumDeclaration
     *            The enum declaration.
     * @throws ParseException
     */
    private void parseEnumBody(CommonTree enumBody,
            EnumDeclaration enumDeclaration) throws ParseException {

        List<CommonTree> enumerators = enumBody.getChildren();
        /* enum body can't be empty (unlike struct). */

        pushScope();

        /*
         * Start at -1, so that if the first enumrator has no explicit value, it
         * will choose 0
         */
        long lastHigh = -1;

        for (CommonTree enumerator : enumerators) {
            lastHigh = TSDLASTUtils.parseEnumEnumerator(enumerator, enumDeclaration,
                    lastHigh);
        }

        popScope();

    }

    /**
     * Parses an enum container type node and returns the corresponding integer
     * type.
     *
     * @param enumContainerType
     *            An ENUM_CONTAINER_TYPE node.
     * @return An integer declaration corresponding to the container type.
     * @throws ParseException
     *             If the type does not parse correctly or if it is not an
     *             integer type.
     */
    private IntegerDeclaration parseEnumContainerType(
            CommonTree enumContainerType) throws ParseException {

        /* Get the child, which should be a type specifier list */
        CommonTree typeSpecifierList = (CommonTree) enumContainerType.getChild(0);

        /* Parse it and get the corresponding declaration */
        IDeclaration decl = parseTypeSpecifierList(typeSpecifierList, null);

        /* If is is an integer, return it, else throw an error */
        if (decl instanceof IntegerDeclaration) {
            return (IntegerDeclaration) decl;
        }
        throw new ParseException("enum container type must be an integer"); //$NON-NLS-1$
    }

    private VariantDeclaration parseVariant(CommonTree variant)
            throws ParseException {

        List<CommonTree> children = variant.getChildren();
        VariantDeclaration variantDeclaration = null;

        boolean hasName = false;
        String variantName = null;

        boolean hasBody = false;
        CommonTree variantBody = null;

        boolean hasTag = false;
        String variantTag = null;

        for (CommonTree child : children) {
            switch (child.getType()) {
            case CTFParser.VARIANT_NAME:
                hasName = true;
                CommonTree variantNameIdentifier = (CommonTree) child.getChild(0);
                variantName = variantNameIdentifier.getText();
                break;
            case CTFParser.VARIANT_TAG:
                hasTag = true;
                CommonTree variantTagIdentifier = (CommonTree) child.getChild(0);
                variantTag = variantTagIdentifier.getText();
                break;
            case CTFParser.VARIANT_BODY:
                hasBody = true;
                variantBody = child;
                break;
            default:
                TSDLASTUtils.childTypeError(child);
                break;
            }
        }

        if (hasBody) {
            /*
             * If variant has a name, check if already defined in the current
             * scope.
             */
            if (hasName
                    && (getCurrentScope().lookupVariant(variantName) != null)) {
                throw new ParseException("variant " + variantName //$NON-NLS-1$
                        + " already defined."); //$NON-NLS-1$
            }

            /* Create the declaration */
            variantDeclaration = new VariantDeclaration();

            /* Parse the body */
            parseVariantBody(variantBody, variantDeclaration);

            /* If variant has name, add it to the current scope. */
            if (hasName) {
                getCurrentScope().registerVariant(variantName,
                        variantDeclaration);
            }
        } else /* !hasBody */{
            if (hasName) {
                /* Name and !body */

                /* Lookup the name in the current scope. */
                variantDeclaration = getCurrentScope().lookupVariantRecursive(
                        variantName);

                /*
                 * If not found, it means that a struct with such name has not
                 * been defined
                 */
                if (variantDeclaration == null) {
                    throw new ParseException("variant " + variantName //$NON-NLS-1$
                            + " is not defined"); //$NON-NLS-1$
                }
            } else {
                /* !Name and !body */

                /* We can't do anything with that. */
                throw new ParseException("variant with no name and no body"); //$NON-NLS-1$
            }
        }

        if (hasTag) {
            variantDeclaration.setTag(variantTag);

            IDeclaration decl = getCurrentScope().lookupIdentifierRecursive(variantTag);
            if (decl == null) {
                throw new ParseException("Variant tag not found: " + variantTag); //$NON-NLS-1$
            }
            if (!(decl instanceof EnumDeclaration)) {
                throw new ParseException("Variant tag must be an enum: " + variantTag); //$NON-NLS-1$
            }
            EnumDeclaration tagDecl = (EnumDeclaration) decl;
            Set<String> intersection = new HashSet<>(tagDecl.getLabels());
            intersection.retainAll(variantDeclaration.getFields().keySet());
            if (intersection.isEmpty()) {
                throw new ParseException("Variant contains no values of the tag, impossible to use: " + variantName); //$NON-NLS-1$
            }
        }

        return variantDeclaration;
    }

    private void parseVariantBody(CommonTree variantBody,
            VariantDeclaration variantDeclaration) throws ParseException {

        List<CommonTree> variantDeclarations = variantBody.getChildren();

        pushScope();

        for (CommonTree declarationNode : variantDeclarations) {
            extractVariantDeclarations(variantDeclaration, declarationNode);
        }

        popScope();
    }

    /**
     * Extracts declarations from a variant body and adds it to the variant.
     * @param variantDeclaration the variant to populate
     * @param declarationNode the node to add
     * @throws ParseException an exception because of a malformed TSDL file
     */
    private void extractVariantDeclarations(VariantDeclaration variantDeclaration, CommonTree declarationNode) throws ParseException {
        switch (declarationNode.getType()) {
        case CTFParser.TYPEALIAS:
            parseTypealias(declarationNode);
            break;
        case CTFParser.TYPEDEF:
            parseTypedef(declarationNode);
            break;
        case CTFParser.SV_DECLARATION:
            parseVariantDeclaration(declarationNode, variantDeclaration);
            break;
        default:
            TSDLASTUtils.childTypeError(declarationNode);
            break;
        }
    }

    private void parseVariantDeclaration(CommonTree declaration,
            VariantDeclaration variant) throws ParseException {

        /* Get the type specifier list node */
        CommonTree typeSpecifierListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_SPECIFIER_LIST);

        /* Get the type declarator list node */
        CommonTree typeDeclaratorListNode = (CommonTree) declaration.getFirstChildWithType(CTFParser.TYPE_DECLARATOR_LIST);

        /* Get the type declarator list */
        List<CommonTree> typeDeclaratorList = typeDeclaratorListNode.getChildren();

        /*
         * For each type declarator, parse the declaration and add a field to
         * the variant
         */
        for (CommonTree typeDeclaratorNode : typeDeclaratorList) {

            StringBuilder identifierSB = new StringBuilder();

            IDeclaration decl = parseTypeDeclarator(typeDeclaratorNode,
                    typeSpecifierListNode, identifierSB);

            String name = identifierSB.toString();

            if (variant.hasField(name)) {
                throw new ParseException("variant: duplicate field " //$NON-NLS-1$
                        + name);
            }

            getCurrentScope().registerIdentifier(name, decl);

            variant.addField(name, decl);
        }
    }

    // ------------------------------------------------------------------------
    // Scope management
    // ------------------------------------------------------------------------

    /**
     * Adds a new declaration scope on the top of the scope stack.
     */
    private void pushScope() {
        fScope = new DeclarationScope(fScope);
    }

    /**
     * Removes the top declaration scope from the scope stack.
     */
    private void popScope() {
        fScope = fScope.getParentScope();
    }

    /**
     * Returns the current declaration scope.
     *
     * @return The current declaration scope.
     */
    private DeclarationScope getCurrentScope() {
        return fScope;
    }

}
