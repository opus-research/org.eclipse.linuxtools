/*******************************************************************************
 * Copyright (c) 2013 Ecole Polytechnique
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model;

/**
 * This file defines all name in the XML Structure for the State Provider
 *
 * @author Florian Wininger
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface XmlStrings {

    /* XML String */
    public static final String NULL = "";
    public static final String WILDCART = "*";
    public static final String VARIABLE_PREFIX = "$";

    /* XML Node Name */
    public static final String STATEPROVIDER= "stateprovider";
    public static final String STATEVALUE   = "stateValue";
    public static final String LOCATION     = "location";
    public static final String EVENTHANDLER = "eventHandler";
    public static final String ATTRIBUTE    = "attribute";
    public static final String VALUE        = "value";
    public static final String EVENTFIELD   = "eventfield";
    public static final String STATECHANGE  = "stateChange";
    public static final String IF           = "if";
    public static final String CONDITION    = "condition";
    public static final String THEN         = "then";
    public static final String ELSE         = "else";

    /* XML Node Attribute */
    public static final String ANALYSISID   = "analysisid";
    public static final String ID           = "id";
    public static final String NAME         = "name";
    public static final String TYPE         = "type";


    /* XML Filter */
    public static final String FILTER       = "filter";
    public static final String TRANSITION   = "transition";
    public static final String DATA         = "data";
    public static final String START        = "start";
    public static final String END          = "end";

    /* XML constant for Type of Attribute and Value */
    public static final String CONSTANT     = "constant";
    public static final String QUERY        = "query";
    public static final String STRING       = "string";
    public static final String INT          = "int";
    public static final String LONG         = "long";
    public static final String EVENTNAME    = "eventname";
    public static final String DELETE       = "delete";
    public static final String FORCEDTYPE   = "forcedtype";
    public static final String CPU          = "cpu";
    public static final String INCREMENT    = "increment";
    public static final String STACK        = "stack";
    public static final String POP          = "pop";
    public static final String PUSH         = "push";
    public static final String PEEK         = "peek";

    /* Attribute type */
    public static final int ATTRIBUTE_TYPE_CONSTANT   = 1;
    public static final int ATTRIBUTE_TYPE_EVENTFIELD = 2;
    public static final int ATTRIBUTE_TYPE_QUERY      = 3;
    public static final int ATTRIBUTE_TYPE_LOCATION   = 4;

    /* Value type */
    public static final int VALUE_NULL            = 0;
    public static final int VALUE_TYPE_NULL       = 1;
    public static final int VALUE_TYPE_TMFSTATE   = 2; // null, int, long, string
    public static final int VALUE_TYPE_STRING     = 3;
    public static final int VALUE_TYPE_INT        = 4;
    public static final int VALUE_TYPE_LONG       = 5;
    public static final int VALUE_TYPE_EVENTFIELD = 6;
    public static final int VALUE_TYPE_QUERY      = 7;
    public static final int VALUE_TYPE_EVENTNAME  = 8;
    public static final int VALUE_TYPE_PEEK       = 9;
    public static final int VALUE_TYPE_POP        = 10;
    public static final int VALUE_TYPE_PUSH       = 11;
    public static final int VALUE_TYPE_INCREMENT  = 12;
    public static final int VALUE_TYPE_INCREMENT_TMFSTATE   = 13;
    public static final int VALUE_TYPE_INCREMENT_EVENTFIELD = 14;
    public static final int VALUE_TYPE_DELETE     = -1;

    /* Operator type */
    public static final int OP_EQUALS   = 0;
    public static final String NOT      = "not";
    public static final int OP_NOT      = 1;
    public static final String AND      = "and";
    public static final int OP_AND      = 2;
    public static final String OR       = "or";
    public static final int OP_OR       = 3;

    /* Xml Header strings */
    public static final String HEAD        = "head";
    public static final String TRACETYPE   = "tracetype";

}