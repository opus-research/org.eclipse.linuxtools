/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.filter.model;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;


/**
 * Filter node for the comparison operation
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfFilterCompareNode extends TmfFilterTreeNode {

	public static final String NODE_NAME = "COMPARE"; //$NON-NLS-1$
	public static final String NOT_ATTR = "not"; //$NON-NLS-1$
	public static final String FIELD_ATTR = "field"; //$NON-NLS-1$
	public static final String RESULT_ATTR = "result"; //$NON-NLS-1$
	public static final String TYPE_ATTR = "type"; //$NON-NLS-1$
	public static final String VALUE_ATTR = "value"; //$NON-NLS-1$

	/**
	 * Supported comparison types
	 */
	public static enum Type {
		NUM,
		ALPHA,
		TIMESTAMP
	}

	private boolean fNot = false;
	private String fField;
	private int fResult;
	private Type fType = Type.NUM;
	private String fValue;
	private Number fValueNumber;
	private TmfTimestamp fValueTimestamp;

	public TmfFilterCompareNode(ITmfFilterTreeNode parent) {
		super(parent);
	}

	public boolean isNot() {
		return fNot;
	}

	public void setNot(boolean not) {
		this.fNot = not;
	}

	public String getField() {
		return fField;
	}

	public void setField(String field) {
		this.fField = field;
	}

	public int getResult() {
		return fResult;
	}

	public void setResult(int result) {
		this.fResult = result;
	}

	public Type getType() {
		return fType;
	}

	public void setType(Type type) {
		this.fType = type;
		setValue(fValue);
	}

	public String getValue() {
		return fValue;
	}

	public void setValue(String value) {
		this.fValue = value;
		fValueNumber = null;
		fValueTimestamp = null;
		if (value == null) {
			return;
		}
		if (fType == Type.NUM) {
			try {
				fValueNumber = NumberFormat.getInstance().parse(value).doubleValue();
			} catch (ParseException e) {
			}
		} else if (fType == Type.TIMESTAMP) {
			try {
				fValueTimestamp = new TmfTimestamp((long) (1E9 * NumberFormat.getInstance().parse(value.toString()).doubleValue()));
			} catch (ParseException e) {
			}
		}
	}

	@Override
	public String getNodeName() {
		return NODE_NAME;
	}

	@Override
	public boolean matches(ITmfEvent event) {
        Object value = getFieldValue(event, fField);
        if (value == null) {
            return false ^ fNot;
        }
        if (fType == Type.NUM) {
            if (fValueNumber != null) {
                if (value instanceof Number) {
                    Double valueDouble = ((Number) value).doubleValue();
                    return (valueDouble.compareTo(fValueNumber.doubleValue()) == fResult) ^ fNot;
                }
                try {
                    Double valueDouble = NumberFormat.getInstance().parse(value.toString())
                                    .doubleValue();
                    return (valueDouble.compareTo(fValueNumber.doubleValue()) == fResult)
                                    ^ fNot;
                } catch (ParseException e) {
                }
            }
        } else if (fType == Type.ALPHA) {
            String valueString = value.toString();
            int comp = valueString.compareTo(fValue.toString());
            if (comp < -1) {
                comp = -1;
            } else if (comp > 1) {
                comp = 1;
            }
            return (comp == fResult) ^ fNot;
        } else if (fType == Type.TIMESTAMP) {
            if (fValueTimestamp != null) {
                if (value instanceof TmfTimestamp) {
                    TmfTimestamp valueTimestamp = (TmfTimestamp) value;
                    return (valueTimestamp.compareTo(fValueTimestamp, false) == fResult) ^ fNot;
                }
                try {
                    TmfTimestamp valueTimestamp = new TmfTimestamp((long) (1E9 * NumberFormat
                                    .getInstance().parse(value.toString()).doubleValue()));
                    return (valueTimestamp.compareTo(fValueTimestamp, false) == fResult) ^ fNot;
                } catch (ParseException e) {
                }
            }
        }
        return false ^ fNot;
	}

	@Override
	public List<String> getValidChildren() {
		return new ArrayList<String>(0);
	}

	@Override
	public String toString() {
		String result = (fResult == 0 ? "= " : fResult < 0 ? "< " : "> "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String open = (fType == Type.NUM ? "" : fType == Type.ALPHA ? "\"" : "["); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String close = (fType == Type.NUM ? "" : fType == Type.ALPHA ? "\"" : "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return fField + (fNot ? " not " : " ") + result + open + fValue + close; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public ITmfFilterTreeNode clone() {
		TmfFilterCompareNode clone = (TmfFilterCompareNode) super.clone();
		clone.fField = fField;
		clone.setValue(fValue);
		return clone;
	}
}
