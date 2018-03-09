/*******************************************************************************
 * Copyright (c) 2014 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.analysis.xml.core.stateprovider.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.XmlStateProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This Class implements a Location in the XML state provider
 *
 * <pre>
 * example:
 *  <location id="CurrentCPU">
 *    <constant value="CPUs" />
 *    <eventField name="cpu" />
 *    ...
 *  </location>
 * </pre>
 *
 * @author Florian Wininger
 *
 */
public class TmfXmlLocation {

    /** Path in the State System */
    private final List<TmfXmlStateAttribute> fPath = new ArrayList<>();

    /** ID : name of the location */
    private final String fId;

    private final XmlStateProvider fProvider;

    /**
     * Constructor
     *
     * @param location
     *            XML node element
     * @param provider
     *            The state provider this location belongs to
     */
    public TmfXmlLocation(Element location, XmlStateProvider provider) {
        fId = location.getAttribute(TmfXmlStrings.ID);
        fProvider = provider;

        NodeList attributesList = location.getChildNodes();
        /* Load the state attributes */
        for (int i = 0; i < attributesList.getLength(); i++) {
            Node attribute = attributesList.item(i);
            if (attribute.getNodeType() == Node.ELEMENT_NODE && TmfXmlStateAttribute.isNodeStateAttribute(attribute)) {
                TmfXmlStateAttribute xAttribute = new TmfXmlStateAttribute((Element) attribute, fProvider);
                fPath.add(xAttribute);
            }
        }
    }

    /**
     * Get the id of the location
     *
     * @return get the id of a location
     */
    public String getId() {
        return fId;
    }

    /**
     * Get the quark for the path represented by this location
     *
     * @param event
     *            The event being handled
     * @param startQuark
     *            The starting quark for relative search, use -1 for the root of
     *            the attribute tree
     * @return The quark at the leaf of the path
     */
    public int getLocationQuark(ITmfEvent event, int startQuark) {
        int quark = startQuark;
        for (TmfXmlStateAttribute attrib : fPath) {
            quark = attrib.getAttributeQuark(event, quark);
        }
        return quark;
    }

}