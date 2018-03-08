package org.eclipse.linuxtools.tmf.core.chrometrace;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * @author ematkho
 * @since 2.0
 */
public class ChromeEvent extends TmfEvent {

    final ITmfTimestamp fTime;
    final ITmfEventType fType;
    final ITmfEventField fField;
    final int fRank;
    final String fRef;
    final String fSource;
    final ITmfTrace fTrace;

    /**
     * don't use
     */
    public ChromeEvent(){
        fTime = null;
        fType = null;
        fField = null;
        fRank = -1;
        fTrace = null;
        fRef = null;
        fSource = null;
    }

    /**
     * @param rank don't use, let us use it
     * @param ts
     * @param type
     * @param pid
     * @param tid
     * @param fields
     * @param trace
     */
    public ChromeEvent(int rank, long ts, String type, String pid, String tid, List<String> fields, ITmfTrace trace) {
        fRank = rank;
        fTrace = trace;
        fTime = new TmfTimestamp(ts);
        fType = new TmfEventType("ctx", type, null);
        List<TmfEventField> ef = new ArrayList<TmfEventField>();
        fRef = fields.get(0);
        fSource = fields.get(2);
        ef.add(new TmfEventField("pid", fields.get(0), null));
        ef.add(new TmfEventField("tid", fields.get(1), null));
        ef.add(new TmfEventField("ph", fields.get(2), null));
        ef.add(new TmfEventField("name", fields.get(3), null));
        for (int i = 4; i < fields.size(); i++) {
            ef.add(new TmfEventField("arg" + (i - 4), fields.get(i), null));
        }
        fField = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, ef.toArray(new TmfEventField[0]));
    }

    @Override
    public ITmfEventField getContent() {
        return fField;
    }

    @Override
    public String getSource() {
        return fSource;
    }
    @Override
    public ITmfEventType getType() {
        return fType;
    }

    @Override
    public ITmfTimestamp getTimestamp() {
        return fTime;
    }

    @Override
    public long getRank() {
        return fRank;
    }

    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public String getReference() {
        return fRef;
    }


}
