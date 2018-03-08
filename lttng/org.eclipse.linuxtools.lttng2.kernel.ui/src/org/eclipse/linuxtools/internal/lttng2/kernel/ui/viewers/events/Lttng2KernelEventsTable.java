package org.eclipse.linuxtools.internal.lttng2.kernel.ui.viewers.events;

import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * Add events table for kernel traces. LTTng2EventsTable should be for userspace traces
 *
 * @author Matthew Khouzam
 *
 */
public class Lttng2KernelEventsTable extends TmfEventsTable {

    // Table column names
    static private final String TIMESTAMP_COLUMN = Messages.EventsTable_timestampColumn;
    static private final String CHANNEL_COLUMN = Messages.EventsTable_channelColumn;
    static private final String TYPE_COLUMN = Messages.EventsTable_typeColumn;
    static private final String CONTENT_COLUMN = Messages.EventsTable_contentColumn;
    static private final String PID_COLUMN = Messages.EventsTable_pidColumn;
    static private final String[] COLUMN_NAMES = new String[] {
            TIMESTAMP_COLUMN,
            CHANNEL_COLUMN,
            PID_COLUMN,
            TYPE_COLUMN,
            CONTENT_COLUMN
    };
    static private final ColumnData[] COLUMN_DATA = new ColumnData[] {
            new ColumnData(COLUMN_NAMES[0], 150, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[1], 120, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[2], 120, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[3], 200, SWT.LEFT),
            new ColumnData(COLUMN_NAMES[4], 100, SWT.LEFT)
    };

    /**
     * Constructor
     * @param parent parent control
     * @param cacheSize the amount of events to buffer
     */
    public Lttng2KernelEventsTable(Composite parent, int cacheSize) {
        super(parent, cacheSize, COLUMN_DATA);
        fTable.getColumns()[0].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_TIMESTAMP);
        fTable.getColumns()[1].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_REFERENCE);
        fTable.getColumns()[2].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_SOURCE);
        fTable.getColumns()[3].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_TYPE);
        fTable.getColumns()[4].setData(Key.FIELD_ID, ITmfEvent.EVENT_FIELD_CONTENT);
    }

    @Override
    protected ITmfEventField[] extractItemFields(ITmfEvent input) {
        if (!(input instanceof CtfKernelEvent)) {
            return new TmfEventField[0];
        }
        CtfKernelEvent event = (CtfKernelEvent) input;
        ITmfEventField[] fields = new TmfEventField[0];
        fields = new TmfEventField[] {
                new TmfEventField(ITmfEvent.EVENT_FIELD_TIMESTAMP, event.getTimestamp().toString()),
                new TmfEventField(ITmfEvent.EVENT_FIELD_REFERENCE, event.getReference()),
                new TmfEventField(ITmfEvent.EVENT_FIELD_SOURCE, event.getSource()),
                new TmfEventField(ITmfEvent.EVENT_FIELD_TYPE, event.getType().getName()),
                new TmfEventField(ITmfEvent.EVENT_FIELD_CONTENT, event.getContent().toString())
        };
        return fields;
    }

}
