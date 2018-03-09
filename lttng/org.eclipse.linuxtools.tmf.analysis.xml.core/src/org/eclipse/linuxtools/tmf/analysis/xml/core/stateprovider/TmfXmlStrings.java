/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider;

/**
 * This file defines all name in the XML Structure for the State Provider
 *
 * @author Florian Wininger
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface TmfXmlStrings {

    /* XML head element */
    static final String HEAD = "head";
    static final String TRACETYPE = "traceType";
    static final String ID = "id";
    static final String LABEL = "label";

    /* XML String */
    static final String NULL = "";
    static final String WILDCARD = "*";
    static final String VARIABLE_PREFIX = "$";

    /* XML Node Name */
    static final String STATE_PROVIDER = "stateProvider";
    static final String VALUE = "value";
    static final String DEFINED_VALUE = "definedValue";
    static final String LOCATION = "location";
    static final String LOCATION_NAME = "locationName";
    static final String EVENT_HANDLER = "eventHandler";
    static final String STATE_ATTRIBUTE = "stateAttribute";
    static final String STATE_VALUE = "stateValue";
    static final String EVENT_FIELD = "eventField";
    static final String STATE_CHANGE = "stateChange";
    static final String IF = "if";
    static final String CONDITION = "condition";
    static final String THEN = "then";
    static final String ELSE = "else";
    static final String FIELD = "field";

    /* XML Node Attribute */
    static final String ANALYSIS_ID = "analysisId";
    static final String NAME = "name";
    static final String VERSION = "version";
    static final String TYPE = "type";

    /* XML Filter */
    static final String FILTER = "filter";
    static final String TRANSITION = "transition";
    static final String DATA = "data";
    static final String START = "start";
    static final String END = "end";

    /* XML constant for Type of Attribute and Value */
    static final String CONSTANT = "constant";
    static final String QUERY = "query";
    static final String STRING = "string";
    static final String INT = "int";
    static final String LONG = "long";
    static final String EVENT_NAME = "eventName";
    static final String DELETE = "delete";
    static final String FORCEDTYPE = "forcedType";
    static final String CPU = "cpu";
    static final String INCREMENT = "increment";
    static final String STACK = "stack";
    static final String POP = "pop";
    static final String PUSH = "push";
    static final String PEEK = "peek";

    /* Value type */
    static final int VALUE_NULL = 0;
    static final int VALUE_TYPE_NULL = 1;
    static final int VALUE_TYPE_TMFSTATE = 2; // null, int, long, string
    static final int VALUE_TYPE_STRING = 3;
    static final int VALUE_TYPE_INT = 4;
    static final int VALUE_TYPE_LONG = 5;
    static final int VALUE_TYPE_EVENTFIELD = 6;
    static final int VALUE_TYPE_QUERY = 7;
    static final int VALUE_TYPE_EVENTNAME = 8;
    static final int VALUE_TYPE_PEEK = 9;
    static final int VALUE_TYPE_POP = 10;
    static final int VALUE_TYPE_PUSH = 11;
    static final int VALUE_TYPE_INCREMENT = 12;
    static final int VALUE_TYPE_INCREMENT_TMFSTATE = 13;
    static final int VALUE_TYPE_INCREMENT_EVENTFIELD = 14;
    static final int VALUE_TYPE_DELETE = -1;

    /* Operator type */
    static final String NOT = "not";
    static final String AND = "and";
    static final String OR = "or";

}