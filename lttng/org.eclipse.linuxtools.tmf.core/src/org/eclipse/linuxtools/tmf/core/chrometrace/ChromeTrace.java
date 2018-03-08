package org.eclipse.linuxtools.tmf.core.chrometrace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * @author ematkho
 * @since 2.0
 */
public class ChromeTrace extends TmfTrace implements ITmfEventParser {

    private final static String compareTo = "{\"traceEvents\""; //$NON-NLS-1$
    private ITmfEvent events[];

    private ChromeLocation fLoc;

    @Override
    public boolean validate(IProject project, String path) {
        File chromeTrace = new File(path);
        if (chromeTrace.isFile()) {
            FileReader fr = null;
            try {
                fr = new FileReader(chromeTrace);
                char cbuf[] = new char[compareTo.length()];
                fr.read(cbuf);
                String start = new String(cbuf);
                fr.close();
                return compareTo.equals(start);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void initTrace(IResource resource, String path, Class<? extends ITmfEvent> type) throws TmfTraceException {
        super.initTrace(resource, path, type);
        parse(new File(path));
    }

    @Override
    protected void processEvent(ITmfEvent event) {
    }

    @SuppressWarnings("nls")
    private void parse(File file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(file, JsonNode.class);
            final JsonNode jsonNode = rootNode.get("traceEvents");
            List<ChromeEvent> eventList = new ArrayList<ChromeEvent>();
            for (int i = 0; jsonNode.has(i); i++) {
                JsonNode event = jsonNode.get(i);
                String type = event.get("cat").getValueAsText();
                long ts = event.get("ts").getLongValue();
                String pid = Integer.toString(event.get("pid").getIntValue());
                String tid = Integer.toString(event.get("tid").getIntValue());
                String name = event.get("name").getTextValue();
                JsonNode argsNode = event.get("args");
                List<String> fields = new ArrayList<String>();
                String ph = event.get("ph").getTextValue();
                fields.add(pid);
                fields.add(tid);
                fields.add(ph);
                fields.add(name);
                Iterator<String> argIter = argsNode.getFieldNames();
                while (argIter.hasNext()) {
                    final String argName = argIter.next();
                    fields.add(argName);
                    fields.add(argsNode.get(argName).toString());
                }
                ChromeEvent c = new ChromeEvent(i, ts, type, pid, tid, fields, this);
                if (!type.equals("__metadata")) {
                    eventList.add(c);
                }
            }
            Collections.sort(eventList);
            events = eventList.toArray(new ChromeEvent[0]);
        } catch (FileNotFoundException e) {
        } catch (JsonParseException e) {
        } catch (JsonMappingException e) {
        } catch (IOException e) {
        }
    }

    @Override
    public synchronized long getNbEvents() {
        return events.length;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        return fLoc;
    }

    @Override
    public synchronized ITmfEvent getNext(ITmfContext context) {
        ChromeContext c = (ChromeContext) context;
        if (c.getLocation() == null) {
            c.setLocation(new ChromeLocation(0));
        }
        Integer pos = (Integer) c.getLocation().getLocationInfo();


        if (pos >= events.length) {
            return null;
        }
        final ITmfEvent event = events[pos];
        updateAttributes(context, event.getTimestamp());
        context.setLocation(new ChromeLocation(pos + 1));
        context.setRank(pos + 1);
        return event;
    }

    @Override
    public double getLocationRatio(ITmfLocation location) {
        if (events.length > 0) {
            return ((Integer) fLoc.getLocationInfo()) / events.length;
        }
        return 0;
    }

    @Override
    public ITmfContext seekEvent(ITmfLocation location) {
        ITmfLocation loc = location;
        if (loc == null) {
            loc = new ChromeLocation(0);
        }
        Integer index = (Integer) loc.getLocationInfo();
        index = Math.min(events.length - 1, index);
        index = Math.max(0, index);
        fLoc = new ChromeLocation(index);
        final ChromeContext chromeContext = new ChromeContext(fLoc, this);
        chromeContext.setRank(index);
        return chromeContext;
    }

    @Override
    public ITmfContext seekEvent(double ratio) {
        Integer index = (int) (ratio * events.length);
        return seekEvent(index);
    }

    class ChromeLocation extends TmfLocation {
        public ChromeLocation(Integer location) {
            super(location);
        }
    }

    @Override
    public ITmfEvent parseEvent(ITmfContext context) {
        Integer locationInfo = (Integer) context.getLocation().getLocationInfo();
        return events[locationInfo];
    }

    @Override
    public ITmfTimestamp getInitialRangeOffset() {
        return new TmfTimestamp(100000000, -9);
    }
}
