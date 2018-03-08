package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class CtfIteratorManager{
    static private HashMap<CtfTmfTrace, CtfIteratorViceManager> map = null;

    synchronized static public void init(){
        if( map == null ) {
            map = new HashMap<CtfTmfTrace, CtfIteratorViceManager>();
        }
    }

    synchronized public static void AddTrace(CtfTmfTrace trace){
        map.put(trace, new CtfIteratorViceManager(trace));
    }
    synchronized public static CtfIterator getIterator(CtfTmfTrace trace, CtfTmfLightweightContext ctx){
        return map.get(trace).getIterator(ctx);
    }
}

class CtfIteratorViceManager {
    private final int MAX_SIZE = 100;
    private HashMap<CtfTmfLightweightContext, CtfIterator> map;
    private ArrayList<CtfTmfLightweightContext> randomAccess;
    private CtfTmfTrace fTrace;
    private Random rnd;

    public CtfIteratorViceManager(CtfTmfTrace trace) {
        init(trace);
    }

    void init(CtfTmfTrace trace){
        map = new HashMap<CtfTmfLightweightContext, CtfIterator>();
        randomAccess = new ArrayList<CtfTmfLightweightContext>();
        rnd = new Random(System.nanoTime());
        fTrace = trace;
    }

    synchronized public CtfIterator getIterator( CtfTmfLightweightContext context ){
        CtfIterator retVal = map.get(context);
        if(retVal == null){
            final int size = randomAccess.size();
            if( size < MAX_SIZE ){
                final CtfIterator elem = new CtfIterator(fTrace);
                addElement(context, elem);
                retVal = elem;
            } else{
                CtfIterator elem = removeRandomElement(size);
                addElement(context, elem);
                retVal = elem;
            }
            retVal.seek((Long)context.getLocation().getLocation());
        }
        return retVal;
    }

    /**
     * @param context
     * @param elem
     */
    private void addElement(CtfTmfLightweightContext context, CtfIterator elem) {
        map.put(context, elem);
        randomAccess.add(context);
    }

    /**
     * @param size
     * @return
     */
    private CtfIterator removeRandomElement(final int size) {
        final int pos = rnd.nextInt(size);
        CtfTmfLightweightContext victim = randomAccess.get(pos);

        final int index = size-1;
        if( index != pos ){
            randomAccess.set(pos, randomAccess.get(index));
        }
        randomAccess.remove(index);
        CtfIterator elem = map.get(victim);
        map.remove(victim);
        return elem;
    }


}
