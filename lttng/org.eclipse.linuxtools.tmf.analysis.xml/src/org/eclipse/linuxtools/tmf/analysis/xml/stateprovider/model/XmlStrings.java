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

package org.eclipse.linuxtools.tmf.analysis.xml.stateprovider.model;

/**
 * This file defines all name in the XML Structure for the State Provider
 *
 * @author Florian Wininger
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface XmlStrings {

    /* XML String */
    static final String NULL = "";
    static final String WILDCART = "*";
    static final String VARIABLE_PREFIX = "$";

    /* XML Node Name */
    static final String STATEPROVIDER= "stateprovider";
    static final String STATEVALUE   = "stateValue";
    static final String LOCATION     = "location";
    static final String EVENTHANDLER = "eventHandler";
    static final String ATTRIBUTE    = "attribute";
    static final String VALUE        = "value";
    static final String EVENTFIELD   = "eventfield";
    static final String STATECHANGE  = "stateChange";
    static final String IF           = "if";
    static final String CONDITION    = "condition";
    static final String THEN         = "then";
    static final String ELSE         = "else";

    /* XML Node Attribute */
    static final String ANALYSISID   = "analysisid";
    static final String ID           = "id";
    static final String NAME         = "name";
    static final String TYPE         = "type";


    /* XML Filter */
    static final String FILTER       = "filter";
    static final String TRANSITION   = "transition";
    static final String DATA         = "data";
    static final String START        = "start";
    static final String END          = "end";

    /* XML constant for Type of Attribute and Value */
    static final String CONSTANT     = "constant";
    static final String QUERY        = "query";
    static final String STRING       = "string";
    static final String INT          = "int";
    static final String LONG         = "long";
    static final String EVENTNAME    = "eventname";
    static final String DELETE       = "delete";
    static final String FORCEDTYPE   = "forcedtype";
    static final String CPU          = "cpu";

    /* Attribute type */
    static final int ATTRIBUTE_TYPE_CONSTANT   = 1;
    static final int ATTRIBUTE_TYPE_EVENTFIELD = 2;
    static final int ATTRIBUTE_TYPE_QUERY      = 3;
    static final int ATTRIBUTE_TYPE_LOCATION   = 4;

    /* Value type */
    static final int VALUE_NULL            = 0;
    static final int VALUE_TYPE_NULL       = 1;
    static final int VALUE_TYPE_TMFSTATE   = 2; // null, int, long, string
    static final int VALUE_TYPE_STRING     = 3;
    static final int VALUE_TYPE_INT        = 4;
    static final int VALUE_TYPE_LONG       = 5;
    static final int VALUE_TYPE_EVENTFIELD = 6;
    static final int VALUE_TYPE_QUERY      = 7;
    static final int VALUE_TYPE_EVENTNAME  = 8;
    static final int VALUE_TYPE_DELETE     = -1;

    /* Operator type */
    static final int OP_EQUALS   = 0;
    static final String NOT      = "not";
    static final int OP_NOT      = 1;
    static final String AND      = "and";
    static final int OP_AND      = 2;
    static final String OR       = "or";
    static final int OP_OR       = 3;

}