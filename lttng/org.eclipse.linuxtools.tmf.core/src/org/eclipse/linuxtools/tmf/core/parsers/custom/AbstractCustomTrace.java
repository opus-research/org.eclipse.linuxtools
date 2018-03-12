package org.eclipse.linuxtools.tmf.core.parsers.custom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.TmfBTreeTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;

/**
 * @author Matthew Khouzam
 * @since 3.1
 */
public abstract class AbstractCustomTrace extends TmfTrace implements ITmfEventParser, ITmfPersistentlyIndexable {

    static final TmfLongLocation NULL_LOCATION = new TmfLongLocation(-1L);

    /**
     * Number of events per checkpoint
     */
    protected int fCheckpointSize;

    /**
     * @return the fFile
     */
    protected abstract BufferedRandomAccessFile getFile();

    /**
     * @param file
     */
    protected abstract void setFile(BufferedRandomAccessFile file);

    public AbstractCustomTrace() {
        super();
        fCheckpointSize = -1;
    }

    public AbstractCustomTrace(IResource resource, Class<? extends ITmfEvent> type, String path, int cacheSize, long interval, int checkpointSize, ITmfEventParser parser) throws TmfTraceException {
        super(resource, type, path, cacheSize, interval, parser);
        fCheckpointSize = checkpointSize;
    }

    public AbstractCustomTrace(TmfTrace trace, int checkpointSize) throws TmfTraceException {
        super(trace);
        fCheckpointSize = checkpointSize;
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> eventType) throws TmfTraceException {
        super.initTrace(resource, path, eventType);
        try {
            setFile(new BufferedRandomAccessFile(getPath(), "r")); //$NON-NLS-1$
        } catch (IOException e) {
            throw new TmfTraceException(e.getMessage(), e);
        }
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        if (getFile() != null) {
            try {
                getFile().close();
            } catch (IOException e) {
            } finally {
                setFile(null);
            }
        }
    }

    @Override
    public ITmfTraceIndexer getIndexer() {
        return super.getIndexer();
    }

    @Override
    public synchronized TmfContext seekEvent(final ITmfLocation location) {
        TmfContext context = getNullContext();
        if (NULL_LOCATION.equals(location) || getFile() == null) {
            return context;
        }
        try {
            if (location == null) {
                getFile().seek(0);
            } else if (location.getLocationInfo() instanceof Long) {
                getFile().seek((Long) location.getLocationInfo());
            }
            long rawPos = getFile().getFilePointer();
            String line = getFile().getNextLine();
            while (line != null) {
                context = match(context, rawPos, line);
                if (context != null) {
                    return context;
                }
                rawPos = getFile().getFilePointer();
                line = getFile().getNextLine();
            }
            return context;
        } catch (final FileNotFoundException e) {
            Activator.logError("Error seeking event. File not found: " + getPath(), e); //$NON-NLS-1$
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return context;
        }

    }

    protected abstract TmfContext getNullContext();

    protected abstract TmfContext match(final TmfContext context, long rawPos, String line) throws IOException;

    @Override
    public synchronized TmfContext seekEvent(final double ratio) {
        if (getFile() == null) {
            return getNullContext();
        }
        try {
            long pos = Math.round(ratio * getFile().length());
            while (pos > 0) {
                getFile().seek(pos - 1);
                if (getFile().read() == '\n') {
                    break;
                }
                pos--;
            }
            final ITmfLocation location = new TmfLongLocation(pos);
            final TmfContext context = seekEvent(location);
            context.setRank(ITmfContext.UNKNOWN_RANK);
            return context;
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
            return getNullContext();
        }
    }

    @Override
    public synchronized double getLocationRatio(final ITmfLocation location) {
        if (getFile() == null) {
            return 0;
        }
        try {
            if (location.getLocationInfo() instanceof Long) {
                return ((Long) location.getLocationInfo()).doubleValue() / getFile().length();
            }
        } catch (final IOException e) {
            Activator.logError("Error seeking event. File: " + getPath(), e); //$NON-NLS-1$
        }
        return 0;
    }

    @Override
    public synchronized int getCheckpointSize() {
        if (fCheckpointSize == -1) {
            TmfCheckpoint c = new TmfCheckpoint(TmfTimestamp.ZERO, new TmfLongLocation(0L), 0);
            ByteBuffer b = ByteBuffer.allocate(ITmfCheckpoint.MAX_SERIALIZE_SIZE);
            b.clear();
            c.serialize(b);
            fCheckpointSize = b.position();
        }

        return fCheckpointSize;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        // appears to be unused
        return null;
    }

    @Override
    public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
        return new TmfLongLocation(bufferIn);
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfBTreeTraceIndexer(this, interval);
    }

}