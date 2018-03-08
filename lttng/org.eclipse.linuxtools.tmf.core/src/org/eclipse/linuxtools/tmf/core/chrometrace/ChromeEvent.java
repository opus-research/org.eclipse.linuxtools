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
public class ChromeEvent extends TmfEvent implements Comparable<ChromeEvent> {

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
    public ChromeEvent() {
        fTime = null;
        fType = null;
        fField = null;
        fRank = -1;
        fTrace = null;
        fRef = null;
        fSource = null;
    }

    /**
     * @param rank
     *            don't use, let us use it
     * @param ts
     *            don't use, let us use it
     * @param type
     *            don't use, let us use it
     * @param pid
     *            don't use, let us use it
     * @param tid
     *            don't use, let us use it
     * @param fields
     *            don't use, let us use it
     * @param trace
     *            don't use, let us use it
     */
    @SuppressWarnings("nls")
    public ChromeEvent(int rank, long ts, String type, String pid, String tid, List<String> fields, ITmfTrace trace) {
        fRank = rank;
        fTrace = trace;
        fTime = new TmfTimestamp(ts, -6);
        fType = new TmfEventType("ctx", type, null);
        List<TmfEventField> ef = new ArrayList<TmfEventField>();
        fRef = fields.get(0);
        fSource = fields.get(2);
        ef.add(new TmfEventField("pid", fields.get(0), null));
        ef.add(new TmfEventField("tid", fields.get(1), null));
        ef.add(new TmfEventField("ph", fields.get(2), null));
        ef.add(new TmfEventField("name", fields.get(3), null));

        List<TmfEventField> args = new ArrayList<TmfEventField>();
        for (int i = 4; i < fields.size(); i+=2) {
            args.add(new TmfEventField(fields.get(i), fields.get(i+1), null));
        }
        final TmfEventField[] array = new TmfEventField[0];
        ef.add(new TmfEventField("args", args.toArray(array), args.toArray(array)) );
        fField = new TmfEventField(ITmfEventField.ROOT_FIELD_ID, ef.toArray(array));
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

    @Override
    public int compareTo(ChromeEvent o) {
        return fTime.compareTo(o.getTimestamp());
    }

}
