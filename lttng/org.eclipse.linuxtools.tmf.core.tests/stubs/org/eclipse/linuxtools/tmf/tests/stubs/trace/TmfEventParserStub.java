/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.event.TmfEventStub;

/**
 * <b><u>TmfEventParserStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfEventParserStub implements ITmfEventParser {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private static final int NB_TYPES = 10;
    private final TmfEventType[] fTypes;
    private final ITmfTrace fEventStream;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfEventParserStub(final ITmfTrace eventStream) {
        fEventStream = eventStream;
        fTypes = new TmfEventType[NB_TYPES];
        for (int i = 0; i < NB_TYPES; i++) {
            final Vector<String> fields = new Vector<String>();
            for (int j = 1; j <= i; j++) {
                final String field = "Fmt-" + i + "-Fld-" + j;
                fields.add(field);
            }
            final String[] fieldArray = new String[i];
            final ITmfEventField rootField = TmfEventField.makeRoot(fields.toArray(fieldArray));
            fTypes[i] = new TmfEventType("UnitTest", "Type-" + i, rootField);
        }
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    static final String typePrefix = "Type-";
    @Override
    public ITmfEvent parseEvent(final ITmfContext context) {

        if (! (fEventStream instanceof TmfTraceStub)) {
            return null;
        }

        // Highly inefficient...
        final RandomAccessFile stream = ((TmfTraceStub) fEventStream).getStream();
        //       	String name = eventStream.getName();
        //       	name = name.substring(name.lastIndexOf('/') + 1);

        // no need to use synchronized since it's already cover by the calling method

        long location = 0;
        if (context != null && context.getLocation() != null) {
            location = (Long) context.getLocation().getLocationInfo();
            try {
                stream.seek(location);

                final long ts        = stream.readLong();
                final String source  = stream.readUTF();
                final String type    = stream.readUTF();
                final Integer reference  = stream.readInt();
                final int typeIndex  = Integer.parseInt(type.substring(typePrefix.length()));
                final String[] fields = new String[typeIndex];
                for (int i = 0; i < typeIndex; i++) {
                    fields[i] = stream.readUTF();
                }

                final StringBuffer content = new StringBuffer("[");
                if (typeIndex > 0) {
                    content.append(fields[0]);
                }
                for (int i = 1; i < typeIndex; i++) {
                    content.append(", ").append(fields[i]);
                }
                content.append("]");

                final TmfEventField root = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, content.toString());
                final ITmfEvent event = new TmfEventStub(fEventStream,
                        new TmfTimestamp(ts, -3, 0),     // millisecs
                        source, fTypes[typeIndex], root, reference.toString());
                return event;
            } catch (final EOFException e) {
            } catch (final IOException e) {
            }
        }
        return null;
    }

}
