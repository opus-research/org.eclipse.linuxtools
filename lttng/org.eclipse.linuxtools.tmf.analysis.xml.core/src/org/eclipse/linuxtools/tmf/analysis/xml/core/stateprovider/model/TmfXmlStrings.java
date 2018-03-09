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

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.model;

/**
 * This file defines all name in the XML Structure for the State Provider
 *
 * @author Florian Wininger
 */
@SuppressWarnings({ "javadoc", "nls" })
public interface TmfXmlStrings {

    /* XML Node Name */
    public static final String STATE_PROVIDER= "stateprovider";
    public static final String VALUE        = "value";

    /* XML Node Attribute */
    public static final String ANALYSIS_ID   = "analysisid";
    public static final String NAME         = "name";

    /* XML head element */
    public static final String HEAD         = "head";
    public static final String TRACETYPE    = "tracetype";
    public static final String ID           = "id";

}