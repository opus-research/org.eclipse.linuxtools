/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Updated to new Event Table API
 *******************************************************************************/

package org.eclipse.linuxtools.btf.ui;

import java.util.Collection;

import org.eclipse.linuxtools.btf.core.trace.BtfColumnNames;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventTableColumn;
import org.eclipse.linuxtools.tmf.ui.viewers.events.TmfEventsTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.ImmutableList;

/**
 * BTF event viewer
 *
 * @author Matthew Khouzam
 */
public class BtfEventViewer extends TmfEventsTable {

    // ------------------------------------------------------------------------
    // Column definition
    // ------------------------------------------------------------------------

    private static final Collection<TmfEventTableColumn> BTF_COLUMNS = ImmutableList.of(
            new BtfSourceColumn(),
            new BtfSourceInstanceColumn(),
            new BtfTargetColumn(),
            new BtfTargetInstanceColumn(),
            new BtfEventColumn(),
            new BtfNotesColumn()
            );

    private static class BtfSourceColumn extends TmfEventTableColumn {
        public BtfSourceColumn() {
            super(BtfColumnNames.SOURCE.toString(), 120, SWT.LEFT);
        }

        @Override
        public String getItemString(ITmfEvent event) {
            String ret = event.getSource();
            return (ret == null ? EMPTY_STRING : ret);
        }
    }

    private static class BtfSourceInstanceColumn extends TmfEventTableColumn {
        public BtfSourceInstanceColumn() {
            super(BtfColumnNames.SOURCE_INSTANCE.toString(), 100, SWT.LEFT);
        }

        @Override
        public String getItemString(ITmfEvent event) {
            ITmfEventField field = event.getContent().getField(BtfColumnNames.SOURCE_INSTANCE.toString());
            if (field == null) {
                return EMPTY_STRING;
            }
            String ret = field.toString();
            return (ret == null ? EMPTY_STRING : ret);
        }
    }

    private static class BtfTargetColumn extends TmfEventTableColumn {
        public BtfTargetColumn() {
            super(BtfColumnNames.TARGET.toString(), 90, SWT.LEFT);
        }

        @Override
        public String getItemString(ITmfEvent event) {
            String ret = event.getReference();
            return (ret == null ? EMPTY_STRING : ret);
        }
    }

    private static class BtfTargetInstanceColumn extends TmfEventTableColumn {
        public BtfTargetInstanceColumn() {
            super(BtfColumnNames.TARGET_INSTANCE.toString(), 100, SWT.LEFT);
        }

        @Override
        public String getItemString(ITmfEvent event) {
            ITmfEventField field = event.getContent().getField(BtfColumnNames.TARGET_INSTANCE.toString());
            if (field == null) {
                return EMPTY_STRING;
            }
            String ret = field.toString();
            return (ret == null ? EMPTY_STRING : ret);
        }
    }

    private static class BtfEventColumn extends TmfEventTableColumn {
        public BtfEventColumn() {
            super(BtfColumnNames.EVENT.toString(), 110, SWT.LEFT);
        }

        @Override
        public String getItemString(ITmfEvent event) {
            ITmfEventField field = event.getContent().getField(BtfColumnNames.EVENT.toString());
            if (field == null) {
                return EMPTY_STRING;
            }
            String ret = field.toString();
            return (ret == null ? EMPTY_STRING : ret);
        }
    }

    private static class BtfNotesColumn extends TmfEventTableColumn {

        private static final TmfEventField NULL_NOTE_FIELD =
                new TmfEventField(BtfColumnNames.NOTES.toString(), null, null);

        public BtfNotesColumn() {
            super(BtfColumnNames.NOTES.toString(), 100, SWT.LEFT);
        }

        @Override
        public String getItemString(ITmfEvent event) {
            ITmfEventField notesField = event.getContent().getField(BtfColumnNames.NOTES.toString());
            if (notesField == null) {
                notesField = NULL_NOTE_FIELD;
            }
            String ret = notesField.toString();
            return (ret == null ? EMPTY_STRING : ret);
        }
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Basic constructor, will use default column data.
     *
     * @param parent
     *            The parent composite UI object
     * @param cacheSize
     *            The size of the event table cache
     */
    public BtfEventViewer(Composite parent, int cacheSize) {
        super(parent, cacheSize, BTF_COLUMNS);
    }
}