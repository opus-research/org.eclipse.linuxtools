package org.eclipse.linuxtools.tmf.core.chrometrace;

import org.eclipse.linuxtools.tmf.core.chrometrace.ChromeTrace.ChromeLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

/**
 * @since 2.0
 */
public class ChromeContext extends TmfContext {
    private ChromeLocation fLoc;
    private ChromeTrace parent;

    public ChromeContext(ChromeLocation loc, ChromeTrace trace){
        parent = trace;
        fLoc = loc;
    }

}
