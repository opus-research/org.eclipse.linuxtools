package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.event.IEventDeclaration;
import org.eclipse.linuxtools.ctf.core.event.io.BitBuffer;
import org.eclipse.linuxtools.ctf.core.event.types.Encoding;
import org.eclipse.linuxtools.ctf.core.event.types.IntegerDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.event.types.StructDefinition;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.internal.ctf.core.event.EventDeclaration;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEventFactory;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;

/**
 * The class <code>CtfTmfEventFactoryForTest</code> is an adaptation of
 * <code>{@link CtfTmfEventFactory}</code> to create events in the tests
 *
 * @author ekadkou
 *
 */
public class CtfTmfEventFactoryForTest {

    private static CtfTmfTrace fTrace;
    private static StructDeclaration fields = new StructDeclaration(32);
    private static IntegerDeclaration id = new IntegerDeclaration(64, true, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, null, 32);
    private static EventDeclaration edec = new EventDeclaration();

    /**
     * the CtfTmfTestTrace class is used to generate ctfTmfTrace for tests
     *
     * @author ekadkou
     *
     */
    public static class CtfTmfTestTrace extends CtfTmfTrace {
        @Override
        public long getOffset() {
            return 0;
        }

        @Override
        public CTFTrace getCTFTrace() {
            return fSquashTrace;
        }

        CTFTrace fSquashTrace = new CTFTrace();

    }

    static class EventDefinitionForTest extends EventDefinition {

        public EventDefinitionForTest(IEventDeclaration declaration) {
            super(declaration, null);
        }

        @Override
        public int getCPU() {
            return 0;
        }

        @Override
        public StructDefinition getContext() {
            return null;
        }

    }

    /**
     * @param ts
     *            The timestamp of the event
     * @param value
     *            the value of the event
     * @return a new Ctf Event
     */
    public static CtfTmfEvent createEvent(long ts, long value) {

        if (fTrace == null) {
            fTrace = new CtfTmfTestTrace();
        }
        if (fields.getFieldsList().isEmpty()) {
            fields.addField("value", id);
        }
        if (edec.getFields() == null) {
            edec.setContext(null);
            edec.setId(0);
            edec.setName("event");
            edec.setFields(fields);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(128);
        bb.putLong(value);
        bb.flip();
        BitBuffer input = new BitBuffer(bb);

        StructDefinition sdef = fields.createDefinition(null, "fields");

        sdef.read(input);

        EventDefinition ed = new EventDefinitionForTest(edec);
        ed.setTimestamp(ts);
        ed.setFields(sdef);

        return CtfTmfEventFactory.createEvent(ed, "no", fTrace);
    }

}
