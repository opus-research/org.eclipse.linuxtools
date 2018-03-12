package org.eclipse.linuxtools.tmf.pcap.core.event;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;

/**
 * Class that represents the type of a PcapEvent.
 *
 * @author Vincent Perot
 */
public class PcapEventType extends TmfEventType {

    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * The default Context ID for a PcapEvent
     */
    @SuppressWarnings("null")
    public static final @NonNull String DEFAULT_PCAP_CONTEXT_ID = Messages.PcapEventType_DefaultContext == null ? EMPTY_STRING : Messages.PcapEventType_DefaultContext;

    /**
     * The default Pcap Type ID for a PcapEvent
     */
    @SuppressWarnings("null")
    public static final @NonNull String DEFAULT_PCAP_TYPE_ID = Messages.PcapEventType_DefaultTypeID == null ? EMPTY_STRING : Messages.PcapEventType_DefaultTypeID;

    /**
     * Default constructor
     */
    public PcapEventType() {
        this(DEFAULT_PCAP_TYPE_ID, null);
    }

    /**
     * Full constructor
     *
     * @param typeId
     *            the type name
     * @param root
     *            the root field
     */
    public PcapEventType(final String typeId, final @Nullable ITmfEventField root) {
        super(DEFAULT_PCAP_CONTEXT_ID, typeId, root);
    }

    /**
     * Copy constructor
     *
     * @param type
     *            the other type
     */
    public PcapEventType(final PcapEventType type) {
        super(type);
    }

    @Override
    public @Nullable String toString() {
        return getName();
    }

}
