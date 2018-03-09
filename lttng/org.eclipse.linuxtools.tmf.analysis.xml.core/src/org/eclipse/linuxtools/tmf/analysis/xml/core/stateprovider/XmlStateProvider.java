/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * This is the state change input plug-in for TMF's state system which handles
 * the XML Format
 *
 * @author Florian Wininger
 */
public class XmlStateProvider extends XmlAbstractProvider {

    /**
     * Version number of this state provider. Please bump this if you modify the
     * contents of the generated state history in some way.
     */
    private static final int VERSION = 1;

    private IPath fFile;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Instantiate a new state provider plug-in.
     *
     * FIXME: For now we only support CTF trace types. It may not be too hard to
     * get rid of this requirement, but it hasn't been tested, so we leave it
     *
     * @param trace
     *            The trace
     * @param stateid
     *            The state system id, corresponding to the analysis_id attribute
     *            of the state provider element of the XML file
     * @param file
     *            XML file containing the state provider definition
     */
    public XmlStateProvider(CtfTmfTrace trace, String stateid, IPath file) {
        super(trace, stateid);
        fFile = file;
    }

    /**
     * Function to load the XML file structure
     */
    protected void loadXML() {

        Element doc = (Element) super.loadXMLFile(fFile);
        if (doc == null) {
            return;
        }
    }

    // ------------------------------------------------------------------------
    // IStateChangeInput
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public XmlStateProvider getNewInstance() {
        return new XmlStateProvider((CtfTmfTrace) this.getTrace(), this.getStateId(), fFile);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        // TODO Auto-generated method stub
    }

}