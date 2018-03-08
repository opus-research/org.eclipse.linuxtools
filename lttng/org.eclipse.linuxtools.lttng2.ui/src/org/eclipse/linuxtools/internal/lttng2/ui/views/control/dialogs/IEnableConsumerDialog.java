/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.ui.views.control.dialogs;

/**
 * Taken from the lttng-tools(3) man page on enable-consumer with permission:
 *
 * Enable a consumer for the tracing session and domain.
 *
 * By default, every tracing session has a consumer attached to it using the
 * local filesystem as output. The trace is written in $HOME/lttng-traces. This
 * command allows the user to specify a specific URL after the session was
 * created for a specific domain. If no domain is specified, the consumer is
 * applied on all domains.
 *
 * Without options, the behavior is to enable a consumer to the current URL. The
 * default URL is the local filesystem at the path of the session mentioned
 * above.
 *
 * The enable-consumer feature supports both local and network transport. You
 * must have a running lttng-relayd for network transmission or any other daemon
 * that can understand the streaming protocol of LTTng.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public interface IEnableConsumerDialog {
    /**
     * Apply on session name
     *
     * @param Name
     *            the name of the session
     */
    public void setSessionName(String Name);

    /**
     * Apply for the kernel Tracer
     *
     * @return if the kernel tracer should update its consumer
     */
    public boolean isKernelConsumer();

    /**
     * Apply for the userspace Tracer
     *
     * @return if the userspace tracer should update its consumer
     */
    public boolean isUserspaceConsumer();

    /**
     * @param URL
     *            The URL of the address of the target. An url can be
     *            file/net/TCP/IPv6.
     */
    public void setUrl(String URL);

    /**
     * @param URL
     *            The URL of the data address. An url can be file/net/TCP/IPv6.
     *            Can be encoded as
     *            proto://[HOST|IP][:PORT1[:PORT2]][/TRACE_PATH]
     *            proto can be
     *            file://
     *            net://
     *            net6:// (not supported yet by the tracer)
     *            tcp://
     *            tcp6:// (not supported yet by the tracer)
     */
    public void setDataUrl(String URL);

    /**
     * @param URL
     *            The url of the control address. An url can be
     *            file/net/TCP/IPv6.
     */
    public void setControlUrl(String URL);

    /**
     * needs to be called with setControlUrl or setDataUrl to run those
     */
    public void enable();

}
