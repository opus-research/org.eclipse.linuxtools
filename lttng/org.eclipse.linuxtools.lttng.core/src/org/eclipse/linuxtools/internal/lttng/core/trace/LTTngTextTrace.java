/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.core.trace;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEventContent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEventField;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEventType;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.jni.JniEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

public class LTTngTextTrace extends TmfTrace implements ITmfEventParser {
    private LttngTimestamp                  eventTimestamp   = null;
    private String                          eventSource      = null;
    private LttngEventType                  eventType        = null;
    private TextLttngEventContent           eventContent     = null;
    private String                          eventReference   = null;
    // The actual event
    private  TextLttngEvent                 currentLttngEvent = null;

    private  HashMap<String, LttngEventType> traceTypes       = null;

    private  String tracepath = ""; //$NON-NLS-1$
    private  FileReader fr = null;
    private  BufferedReader br = null;
    private  Long nbCharRead = 0L;

    private int cpuNumber = -1;

//    private final  boolean showDebug = false;

    public LTTngTextTrace(final IResource resource, final String path) throws Exception {
        this(resource, path, true); // false);
    }

    public LTTngTextTrace(final IResource resource, final String path, final boolean skipIndexing) throws Exception {
        //      super(null, LttngEvent.class, path, 1, !skipIndexing);
        super(null, LttngEvent.class, path, 1000);

        tracepath = path;
        traceTypes      = new HashMap<String, LttngEventType>();

        eventTimestamp        = new LttngTimestamp();
        eventSource           = "Kernel Core"; //$NON-NLS-1$
        eventType             = new LttngEventType();
        eventContent          = new TextLttngEventContent(currentLttngEvent);
        eventReference        = getName();

        currentLttngEvent = new TextLttngEvent(this, eventTimestamp, eventSource, eventType, eventContent, eventReference);
        eventContent.setEvent(currentLttngEvent);

        if ( positionToFirstEvent() == false ) {
            throw new IOException("Fail to position to the beginning of the trace"); //$NON-NLS-1$
        } else {
            setCacheSize(1000);
            ITmfContext context = new TmfContext(new TmfLocation<Long>(0L), 0);
            getIndexer().updateIndex(context, new LttngTimestamp(0L));
            setParser(this);

            final Long endTime = currentLttngEvent.getTimestamp().getValue();
            positionToFirstEvent();

            getNext(new TmfContext(null, 0));
            final Long starTime = currentLttngEvent.getTimestamp().getValue();
            positionToFirstEvent();

            setTimeRange( new TmfTimeRange( new LttngTimestamp(starTime),
                    new LttngTimestamp(endTime)
                    ) );
        }
    }


    public LTTngTextTrace(final LTTngTextTrace oldTrace) throws Exception {
        this(oldTrace.getResource(), oldTrace.getPath(), true);

        // *** VERIFY ***
        // Is this safe?
//        fCheckpoints = oldTrace.fCheckpoints;
    }

    private boolean positionToFirstEvent() {

        boolean isSuccessful = true;

        try {
            if ( br != null ) {
                br.close();
                fr.close();
            }

            fr = new FileReader(tracepath);
            br = new BufferedReader(fr);

            // Skip the 2 lines header
            br.readLine();
            br.readLine();

            // Make sure the event time is consistent
            eventTimestamp.setValue(0L);
        }
        catch (final IOException e) {
            isSuccessful = false;
        }

        return isSuccessful;
    }

    private void skipToPosition(final TmfLocation<Long> skip) {
        try {
            long skipPosition = skip.getLocation();
            if ( skipPosition < 0 ) {
                skipPosition = 0L;
            }

//            if ( showDebug == true ) {
//                System.out.println("skipToPosition(Long skipPosition)"); //$NON-NLS-1$
//                System.out.println("\tSkipping to : " + skipPosition); //$NON-NLS-1$
//                System.out.println();
//            }
            positionToFirstEvent();
            final long nbSkipped = br.skip(skipPosition);
            if ( nbSkipped != skipPosition)
             {
                throw new IOException("Too few characters skipped, positionning failed! (skipToPosition)"); //$NON-NLS-1$
            }

            nbCharRead = skipPosition;
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public TmfContext seekEvent(ITmfLocation<?> location) {
        if (location == null) {
            location = new TmfLocation<Long>(0L);
        }

        if (!((TmfLocation<Long>) location).getLocation().equals(nbCharRead)) {
            skipToPosition((TmfLocation<Long>) location);
        }

        final TmfContext tmpTraceContext =  new TmfContext(location, 0L);

        return tmpTraceContext;
    }

    @Override
    public TmfContext seekEvent(final double ratio) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getLocationRatio(final ITmfLocation<?> location) {
        // TODO Auto-generated method stub
        return 0;
    }

    private LttngEvent parseMyNextEvent(final ITmfContext context) {

        // All parsing variables declared here so to be able to print them into the catch if needed
        String tmpContent = null;
        int tmpCurIndex = 0;
        int tmpPrevIndex = 0;

        String tracefile = ""; //$NON-NLS-1$
        long tmpCpu = 0;
        String marker = ""; //$NON-NLS-1$

        long tmpSecond = 0;
        long tmpNanosecond = 0;

        String parsedPayload = ""; //$NON-NLS-1$
        String markerName = ""; //$NON-NLS-1$
        Object payload = ""; //$NON-NLS-1$

        HashMap<String, LttngEventField> fieldsMap = null;

        LttngEvent returnedEvent = null;

        try {
            tmpContent = br.readLine();

            if (tmpContent != null) {
                // *** NOTE :
                // -1 is the skip the end of line (\n)
                nbCharRead += (tmpContent.length()+1);

                if ( (currentLttngEvent != null) && (currentLttngEvent.getContent().getMapContent() != null) ) {
                    currentLttngEvent.getContent().emptyContent();
                }

                // Tracefile and marker are first in the file
                // Sound like :
                //		kernel.syscall_entry:
                tmpCurIndex = tmpContent.indexOf(".", tmpPrevIndex); //$NON-NLS-1$

                // *** HACK ***
                // Evil exit case here : the two last line of the text dump does not contain "."
                // We should check in a better way (string comparison and such) but it make the whole process to weight a lot more
                // Conclusion : this is ugly but fast.
                if ( tmpCurIndex < 0 ) {
//                    if ( showDebug == true ) {
//                        System.out.println("END OF FILE."); //$NON-NLS-1$
//                        System.out.println();
//                    }
                    return null;
                }

                tracefile = tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim();
                /*System.out.println(tracefile);*/

                tmpPrevIndex = tmpCurIndex;
                tmpCurIndex = tmpContent.indexOf(":", tmpPrevIndex); //$NON-NLS-1$
                marker = tmpContent.substring(tmpPrevIndex+1, tmpCurIndex ).trim();
                /*System.out.println(marker);*/

                // Timestamp is next but is presented in second.milisecond format, we have to split them
                // Sound like :
                //		952.162637168
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf(".", tmpPrevIndex); //$NON-NLS-1$
                tmpSecond = Long.parseLong( tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim() );
                /*System.out.println(tmpSecond);*/

                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf(" ", tmpPrevIndex); //$NON-NLS-1$
                tmpNanosecond = Long.parseLong( tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim() );
                /*System.out.println(tmpNanosecond);*/

                // We have enough information here to set the timestamp
                eventTimestamp.setValue( (tmpSecond * 1000000000) + tmpNanosecond );
                /*System.out.println(eventTimestamp.toString());*/

                // Next field is the reference
                // A long string enclosed by parenthesis and ending with a comma
                // 		(/home/william/workspace/org.eclipse.linuxtools.lttng.tests/traceset/trace-618339events-1293lost-1cpu/kernel_0),
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf("(", tmpPrevIndex); //$NON-NLS-1$
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf("),", tmpPrevIndex); //$NON-NLS-1$
                final String fullTracePath = tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim();
                /*System.out.println(fullTracePath);*/

                final String traceName = fullTracePath.substring(fullTracePath.lastIndexOf("/")+1).trim(); //$NON-NLS-1$
                /*System.out.println(traceName);*/
                eventReference = traceName;
                currentLttngEvent.setReference(traceName);


                // The next few fields are relatives to the state system (pid, ppid, etc...) we need to skip them.
                // They should be like the following :
                //		4175, 4175, hal-acl-tool, , 4168,

                // 1st comma
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex); //$NON-NLS-1$
                // 2nd comma
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex); //$NON-NLS-1$
                // 3rd comma
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex); //$NON-NLS-1$
                // 4th comma
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex); //$NON-NLS-1$
                // 5th comma
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex); //$NON-NLS-1$
                // 6th comma
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex); //$NON-NLS-1$

                // The next field is the CPU, in hexadecimal format
                // Should be like :
                //		0x0,
                tmpPrevIndex = tmpCurIndex+1;
                tmpCurIndex = tmpContent.indexOf("0x", tmpPrevIndex); //$NON-NLS-1$
                tmpPrevIndex = tmpCurIndex+2;

                tmpCurIndex = tmpContent.indexOf(",", tmpPrevIndex); //$NON-NLS-1$
                tmpCpu = Long.parseLong( tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim() );

                // Set the cpu number of trace if we found a "new" cpu
                if ( cpuNumber < (tmpCpu + 1) ) {
                    cpuNumber = (int)(tmpCpu+1);
                }


                // The last field is the parsed content
                // It is enclosed by { }
                // Look like :
                //		SYSCALL { ip = 0xb7f05422, syscall_id = 221 [sys_fcntl64+0x0/0x79] }
                //
                // NOTE : it seems some state system events do not respect this format as they have no payload.
                //		We will create empty payload then.
                final int tmpIndex = tmpContent.indexOf("{", tmpPrevIndex); //$NON-NLS-1$
                if ( tmpIndex != -1 ) {
                    tmpPrevIndex = tmpCurIndex+1;
                    tmpCurIndex = tmpIndex;
                    tmpPrevIndex = tmpCurIndex+1;
                    tmpCurIndex = tmpContent.indexOf("}", tmpPrevIndex); //$NON-NLS-1$
                    parsedPayload = tmpContent.substring(tmpPrevIndex, tmpCurIndex ).trim();

                    // Now add each LttngField
                    boolean isDone = false;
                    int tmpIndexBegin = 0;
                    int tmpIndexEqual = 0;
                    int tmpIndexEnd = 0;

                    fieldsMap = new HashMap<String, LttngEventField>();

                    while ( isDone == false ) {
                        tmpIndexEqual = parsedPayload.indexOf("=", tmpIndexBegin); //$NON-NLS-1$
                        tmpIndexEnd = parsedPayload.indexOf(", ", tmpIndexEqual); //$NON-NLS-1$
                        if ( tmpIndexEnd == -1 ) {
                            tmpIndexEnd = parsedPayload.length();
                            isDone = true;
                        }

                        markerName = parsedPayload.substring(tmpIndexBegin, tmpIndexEqual-1 ).trim();
                        payload = parsedPayload.substring(tmpIndexEqual+1, tmpIndexEnd ).replace("\"", " ").trim(); //$NON-NLS-1$ //$NON-NLS-2$

                        // Try to cast the payload into the correct type
                        try {
                            payload = Long.parseLong((String)payload);
                        }
                        catch (final NumberFormatException e) { }

                        final LttngEventField tmpField = new LttngEventField(markerName, payload);
                        fieldsMap.put(markerName, tmpField);

                        tmpIndexBegin = tmpIndexEnd+1;
                    }
                }
                else {
                    fieldsMap = new HashMap<String, LttngEventField>();

                    markerName = ""; //$NON-NLS-1$
                    payload = ""; //$NON-NLS-1$

                    final LttngEventField tmpField = new LttngEventField(markerName, payload);
                    fieldsMap.put(markerName, tmpField);
                }

                eventContent = new TextLttngEventContent(currentLttngEvent, fieldsMap);

                // We now have what we need for the type
                final String tmpTypeKey = tracefile + "/" + tmpCpu + "/" + marker; //$NON-NLS-1$ //$NON-NLS-2$
                if ( traceTypes.get(tmpTypeKey) == null ) {
                    traceTypes.put(tmpTypeKey, new LttngEventType(tracefile, tmpCpu, marker, 0, fieldsMap.keySet().toArray(new String[fieldsMap.size()] )) );
                }

                currentLttngEvent.setContent(eventContent);
                currentLttngEvent.setType(traceTypes.get(tmpTypeKey));

                returnedEvent = currentLttngEvent;
            }
//            else if ( showDebug == true ) {
//                System.out.println("NULL READING"); //$NON-NLS-1$
//                System.out.println();
//                returnedEvent = null;
//            }
        }
        catch (final Exception e) {
            System.out.println("Pos is :" + nbCharRead); //$NON-NLS-1$
            if ( tmpContent != null )
             {
                System.out.println("Erroneous content is :" + tmpContent); //$NON-NLS-1$
            }

            tmpContent = null;
            e.printStackTrace();
            returnedEvent = null;
        }

        return returnedEvent;
    }

    @Override
    public ITmfLocation<?> getCurrentLocation() {
        return new TmfLocation<Long>(nbCharRead);
    }

    @Override
    public LttngEvent parseEvent(ITmfContext context) {
        context = seekEvent(context.getLocation());
        return parseMyNextEvent(context);

    }

    public int getCpuNumber() {
        return cpuNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#validate(org.eclipse.core.resources.IProject, java.lang.String)
     */
    @Override
    public boolean validate(IProject project, String path) {
        return fileExists(path);
    }

}


// Redefine event to override method we know won't work with a Text tracefile
class TextLttngEvent extends LttngEvent {

    public TextLttngEvent(final TmfTrace parent,
            final LttngTimestamp timestamp,
            final String source,
            final LttngEventType type,
            final LttngEventContent content,
            final String reference)
    {
        super(parent, timestamp, source, type, content, reference, null);
    }

    public TextLttngEvent(final TextLttngEvent oldEvent) {
        this(
                (TmfTrace) oldEvent.getTrace(),
                (LttngTimestamp)oldEvent.getTimestamp(),
                oldEvent.getSource(),
                oldEvent.getType(),
                oldEvent.getContent(),
                oldEvent.getReference()
                );
    }

    @Override
    public JniEvent convertEventTmfToJni() {
        //		System.out.println("WARNING : Cannot use convertEventTmfToJni() on a trace in text format."); //$NON-NLS-1$
        return null;
    }

    @Override
    public void updateJniEventReference(final JniEvent newJniEventReference) {
        //		System.out.println("WARNING : Cannot use updateJniEventReference on a trace in text format. Using null."); //$NON-NLS-1$
    }
}


class TextLttngEventContent extends LttngEventContent {

    public TextLttngEventContent() {
        super();
    }

    public TextLttngEventContent(final TextLttngEvent thisParent) {
        super(thisParent, null);
    }

    public TextLttngEventContent(final TextLttngEvent thisParent, final HashMap<String, LttngEventField> thisContent) {
        super(thisParent, thisContent);
    }

    public TextLttngEventContent(final TextLttngEventContent oldContent) {
        this(((TextLttngEvent) oldContent.getEvent()), oldContent.getMapContent());
    }

    @Override
    public LttngEventField[] getFields() {
        return getMapContent().values().toArray(new LttngEventField[getMapContent().size()]);
    }

    @Override
    public LttngEventField getField(final String name) {
        final LttngEventField returnedField = getMapContent().get(name);

        return returnedField;
    }

    @Override
    public LttngEventField getField(final int position) {
        LttngEventField returnedField = null;
        String label = null;
        //		try {
        label = getEvent().getType().getFieldName(position);
        returnedField = this.getField(label);
        //		}
        //		catch (TmfNoSuchFieldException e) {
        //			System.out.println("Invalid field position requested : " + position + ", ignoring (getField)."); //$NON-NLS-1$ //$NON-NLS-2$
        //		}

        return returnedField;
    }
}
