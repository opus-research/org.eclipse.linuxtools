/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Marc-Andre Laperle - Add persistent index support
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.text;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.parsers.custom.AbstractCustomTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;

/**
 * Extension of TmfTrace for handling of line-based text traces parsed using
 * regular expressions. Each line that matches the first line pattern indicates
 * the start of a new event. The subsequent lines can contain additional
 * information that is added to the current event.
 *
 * @param <T>
 *            TmfEvent class returned by this trace
 *
 * @since 3.0
 */
public abstract class TextTrace<T extends TextTraceEvent> extends AbstractCustomTrace {

    private static final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);
    private static final int MAX_LINES = 100;
    private static final int MAX_CONFIDENCE = 100;

    /** The default separator used for multi-line fields */
    protected static final String SEPARATOR = " | "; //$NON-NLS-1$

    /** The text file */
    protected BufferedRandomAccessFile fFile;

    /**
     * Constructor
     */
    public TextTrace() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * The default implementation computes the confidence as the percentage of
     * lines in the first 100 lines of the file which match the first line
     * pattern.
     */
    @Override
    public IStatus validate(IProject project, String path) {
        File file = new File(path);
        if (!file.exists()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "File not found: " + path); //$NON-NLS-1$
        }
        if (!file.isFile()) {
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Not a file. It's a directory: " + path); //$NON-NLS-1$
        }
        int confidence = 0;
        try (BufferedRandomAccessFile rafile = new BufferedRandomAccessFile(path, "r")) { //$NON-NLS-1$
            int lineCount = 0;
            double matches = 0.0;
            String line = rafile.getNextLine();
            while ((line != null) && (lineCount++ < MAX_LINES)) {
                Matcher matcher = getFirstLinePattern().matcher(line);
                if (matcher.matches()) {
                    int groupCount = matcher.groupCount();
                    matches += (1.0 + groupCount / ((double) groupCount + 1));
                }
                confidence = (int) (MAX_CONFIDENCE * matches / lineCount);
                line = rafile.getNextLine();
            }
        } catch (IOException e) {
            Activator.logError("Error validating file: " + path, e); //$NON-NLS-1$
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "IOException validating file: " + path, e); //$NON-NLS-1$
        }

        return new TraceValidationStatus(confidence, Activator.PLUGIN_ID);

    }

    private void setupContext(TextTraceContext context, long rawPos, String line, Matcher matcher) throws IOException {
        context.setLocation(new TmfLongLocation(rawPos));
        context.firstLineMatcher = matcher;
        context.firstLine = line;
        context.nextLineLocation = fFile.getFilePointer();
    }

    @Override
    public TextTraceEvent parseEvent(ITmfContext tmfContext) {
        TextTraceContext context = seekEvent(tmfContext.getLocation());
        return parse(context);
    }

    @Override
    public synchronized T getNext(ITmfContext context) {
        if (!(context instanceof TextTraceContext)) {
            throw new IllegalArgumentException();
        }
        TextTraceContext savedContext = new TextTraceContext(context.getLocation(), context.getRank());
        T event = parse((TextTraceContext) context);
        if (event != null) {
            updateAttributes(savedContext, event.getTimestamp());
            context.increaseRank();
        }
        return event;
    }

    /**
     * Parse the next event. The context is advanced.
     *
     * @param tmfContext
     *            the context
     * @return the next event or null
     */
    protected synchronized T parse(TextTraceContext tmfContext) {
        if (fFile == null) {
            return null;
        }
        TextTraceContext context = tmfContext;
        if (context.getLocation() == null || !(context.getLocation().getLocationInfo() instanceof Long) || NULL_LOCATION.equals(context.getLocation())) {
            return null;
        }

        T event = parseFirstLine(context.firstLineMatcher, context.firstLine);

        try {
            if (fFile.getFilePointer() != context.nextLineLocation) {
                fFile.seek(context.nextLineLocation);
            }
            long rawPos = fFile.getFilePointer();
            String line = fFile.getNextLine();
            while (line != null) {
                Matcher matcher = getFirstLinePattern().matcher(line);
                if (matcher.matches()) {
                    setupContext(context, rawPos, line, matcher);
                    return event;
                }
                parseNextLine(event, line);
                rawPos = fFile.getFilePointer();
                line = fFile.getNextLine();
            }
        } catch (IOException e) {
            Activator.logError("Error reading file: " + getPath(), e); //$NON-NLS-1$
        }

        context.setLocation(NULL_LOCATION);
        return event;
    }

    /**
     * Gets the first line pattern.
     *
     * @return The first line pattern
     */
    protected abstract Pattern getFirstLinePattern();

    /**
     * Parses the first line data and returns a new event.
     *
     * @param matcher
     *            The matcher
     * @param line
     *            The line to parse
     * @return The parsed event
     */
    protected abstract T parseFirstLine(Matcher matcher, String line);

    /**
     * Parses the next line data for the current event.
     *
     * @param event
     *            The current event being parsed
     * @param line
     *            The line to parse
     */
    protected abstract void parseNextLine(T event, String line);


    @Override
    public synchronized TextTraceContext seekEvent(double ratio) {
        return (TextTraceContext)super.seekEvent(ratio);
    }

    @Override
    public synchronized TextTraceContext seekEvent(ITmfLocation location) {
        return (TextTraceContext)super.seekEvent(location);
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    @Override
    protected TmfContext match(TmfContext context, long rawPos, String line) throws IOException {
        Matcher matcher = getFirstLinePattern().matcher(line);
        if (matcher.matches()) {
            if (context instanceof TextTraceContext) {
                TextTraceContext textTraceContext = (TextTraceContext) context;
                setupContext(textTraceContext, rawPos, line, matcher);
                return context;
            }
        }
        return null;
    }

    @Override
    protected TmfContext getNullContext() {
        return new TextTraceContext(NULL_LOCATION, ITmfContext.UNKNOWN_RANK);
    }

    /**
     * Strip quotes surrounding a string
     *
     * @param input
     *            The input string
     * @return The string without quotes
     */
    protected static String replaceQuotes(String input) {
        String out = input.replaceAll("^\"|(\"\\s*)$", ""); //$NON-NLS-1$//$NON-NLS-2$
        return out;
    }

    /**
     * Strip brackets surrounding a string
     *
     * @param input
     *            The input string
     * @return The string without brackets
     */
    protected static String replaceBrackets(String input) {
        String out = input.replaceAll("^\\{|(\\}\\s*)$", ""); //$NON-NLS-1$//$NON-NLS-2$
        return out;
    }

    @Override
    protected BufferedRandomAccessFile getFile() {
        return fFile;
    }

    @Override
    protected void setFile(BufferedRandomAccessFile file) {
        fFile = file;
    }
}
