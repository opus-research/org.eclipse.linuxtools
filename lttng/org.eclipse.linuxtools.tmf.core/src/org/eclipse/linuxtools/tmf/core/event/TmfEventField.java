/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation,
 *                        Updated as per TMF Event Model 1.0
 *   Alexandre Montplaisir - Removed Cloneable, made immutable
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A basic implementation of ITmfEventField.
 * <p>
 * Non-value fields are structural (i.e. used to represent the event structure
 * including optional fields) while the valued fields are actual event fields.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfEventType
 */
public class TmfEventField implements ITmfEventField {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final String fName;
    private final Object fValue;
    private final Map<String, ITmfEventField> fFields;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Full constructor
     *
     * @param name
     *            The event field id
     * @param value
     *            The event field value
     * @param fields
     *            The list of subfields
     */
    public TmfEventField(final String name, final Object value, final ITmfEventField[] fields) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        fName = name;
        fValue = value;

        Map<String, ITmfEventField> fieldMap;
        if (fields == null) {
            fieldMap = new LinkedHashMap<String, ITmfEventField>();
        } else {
            fieldMap = new LinkedHashMap<String, ITmfEventField>(fields.length);
            for (ITmfEventField field : fields) {
                fieldMap.put(field.getName(), field);
            }
        }
        fFields = Collections.unmodifiableMap(fieldMap);

    }

    /**
     * Copy constructor
     *
     * @param field
     *            The event field to copy
     */
    public TmfEventField(final TmfEventField field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }
        fName = field.fName;
        fValue = field.fValue;
        fFields = field.fFields; /* Immutable, so safe to shallow-copy */
    }

    // ------------------------------------------------------------------------
    // ITmfEventField
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public Object getValue() {
        return fValue;
    }

    /**
     * @since 2.0
     */
    @Override
    public Map<String, ITmfEventField> getFields() {
        return fFields;
    }

    @Override
    public ITmfEventField getField(final String name) {
        return fFields.get(name);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Create a root field from a list of labels.
     *
     * @param labels the list of labels
     * @return the (flat) root list
     */
    public final static ITmfEventField makeRoot(final String[] labels) {
        final ITmfEventField[] fields = new ITmfEventField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            fields[i] = new TmfEventField(labels[i], null, null);
        }
        // Return a new root field;
        return new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fields);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fName.hashCode();
        result = prime * result + ((fValue == null) ? 0 : fValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEventField)) {
            return false;
        }
        final TmfEventField other = (TmfEventField) obj;
        if (!fName.equals(other.fName)) {
            return false;
        }
        if (fValue == null) {
            if (other.fValue != null) {
                return false;
            }
        } else if (!fValue.equals(other.fValue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (fName.equals(ITmfEventField.ROOT_FIELD_ID)) {
            /*
             * If this field is a top-level "field container", we will print its
             * sub-fields directly.
             */
            appendSubFields(ret);

        } else {
            /* The field has its own values */
            ret.append(fName);
            ret.append('=');
            ret.append(fValue);

            if (fFields.size() > 0) {
                /*
                 * In addition to its own name/value, this field also has
                 * sub-fields.
                 */
                ret.append(" ["); //$NON-NLS-1$
                appendSubFields(ret);
                ret.append(']');
            }
        }
        return ret.toString();
    }

    private void appendSubFields(StringBuilder sb) {
        int i = 0;
        for (ITmfEventField field : getFields().values()) {
            if (i != 0) {
                sb.append(", ");//$NON-NLS-1$
            }
            sb.append(field.toString());
            i++;
        }
    }

    /**
     * @since 2.0
     */
    @Override
    public String getFormattedValue() {
        return getValue().toString();
    }

}
