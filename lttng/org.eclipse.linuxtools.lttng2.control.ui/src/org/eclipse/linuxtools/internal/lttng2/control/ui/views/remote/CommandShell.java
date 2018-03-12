/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated using Executor Framework
 *   Markus Schorn - Bug 448058: Use org.eclipse.remote in favor of RSE
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.remote;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.messages.Messages;
import org.eclipse.linuxtools.internal.lttng2.control.ui.views.preferences.ControlPreferences;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteProcess;
import org.eclipse.remote.core.IRemoteProcessBuilder;

/**
 * <p>
 * Implementation of remote command execution using IRemoteConnection.
 * </p>
 *
 * @author Patrick Tasse
 * @author Bernd Hufmann
 */
public class CommandShell implements ICommandShell {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String BEGIN_TAG = "org.eclipse.linuxtools-BEGIN-TAG:"; //$NON-NLS-1$
    private static final String END_TAG = "org.eclipse.linuxtools-END-TAG:"; //$NON-NLS-1$
    private static final String RSE_ADAPTER_ID = "org.eclipse.ptp.remote.RSERemoteServices"; //$NON-NLS-1$


    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private IRemoteConnection fConnection = null;
    private final ExecutorService fExecutor = Executors.newFixedThreadPool(1);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Create a new command shell
     *
     * @param connection the remote connection for this shell
     */
    public CommandShell(IRemoteConnection connection) {
        fConnection = connection;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void connect() throws ExecutionException {
    }

    @Override
    public void disconnect() {
    }

    @Override
    public ICommandResult executeCommand(final String command, final IProgressMonitor monitor) throws ExecutionException {
        if (fConnection.isOpen()) {
            FutureTask<CommandResult> future = new FutureTask<>(new Callable<CommandResult>() {
                @Override
                public CommandResult call() throws IOException, CancellationException {
                    if (monitor == null || !monitor.isCanceled()) {
                        final boolean isRSE = RSE_ADAPTER_ID.equals(fConnection.getRemoteServices().getId());
                        IRemoteProcess process = startRemoteProcess(isRSE, command);
                        InputReader stdout = new InputReader(process.getInputStream());
                        InputReader stderr = new InputReader(process.getErrorStream());

                        try {
                            stdout.waitFor(monitor);
                            stderr.waitFor(monitor);
                            if (monitor == null || !monitor.isCanceled()) {
                                return createResult(isRSE, process.waitFor(), stdout.toString(), stderr.toString());
                            }
                        } catch (OperationCanceledException e) {
                        } catch (InterruptedException e) {
                            return new CommandResult(1, new String[0], new String[] {e.getMessage()});
                        } finally {
                            stdout.stop();
                            stderr.stop();
                            process.destroy();
                        }
                    }
                    return new CommandResult(1, new String[0], new String[] {"cancelled"}); //$NON-NLS-1$
                }
            });

            fExecutor.execute(future);

            try {
                return future.get(ControlPreferences.getInstance().getCommandTimeout(), TimeUnit.SECONDS);
            } catch (java.util.concurrent.ExecutionException ex) {
                throw new ExecutionException(Messages.TraceControl_ExecutionFailure, ex);
            } catch (InterruptedException ex) {
                throw new ExecutionException(Messages.TraceControl_ExecutionCancelled, ex);
            } catch (TimeoutException ex) {
                throw new ExecutionException(Messages.TraceControl_ExecutionTimeout, ex);
            } finally {
                future.cancel(true);
            }
        }
        throw new ExecutionException(Messages.TraceControl_ShellNotConnected, null);
    }

    private IRemoteProcess startRemoteProcess(boolean isRSE, String command) throws IOException {
        if (!isRSE) {
            String[] args = command.trim().split("\\s+"); //$NON-NLS-1$
            IRemoteProcessBuilder processBuilder = fConnection.getProcessBuilder(args);
            return processBuilder.start();
        }

        IRemoteProcess process = fConnection.getProcessBuilder("sh").start(); //$NON-NLS-1$
        try (Writer writer = new OutputStreamWriter(process.getOutputStream())) {
            writer.append("echo ").append(BEGIN_TAG).append(';'); //$NON-NLS-1$
            writer.append(command).append(';');
            writer.append("echo ").append(END_TAG).append(" $?;"); //$NON-NLS-1$ //$NON-NLS-2$
            writer.write("exit\n"); //$NON-NLS-1$
            writer.flush();
        }
        return process;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private static CommandResult createResult(boolean isRSE, int origResult, String origStdout, String origStderr) {
        final int result;
        final String stdout, stderr;
        if (isRSE) {
            String[] holder = {origStdout};
            result = unwrapOutput(holder);
            stdout = holder[0];
            // Workaround if error stream is not available and stderr output is written
            // in standard output above. This is true for the SshTerminalShell implementation.
            stderr = origStderr.isEmpty() ? stdout : origStderr;
        } else {
            result = origResult;
            stdout = origStdout;
            stderr = origStderr;
        }

        String[] output = splitLines(stdout);
        String[] error = result == 0 ? null : splitLines(stderr);
        return new CommandResult(result, output, error);
    }

    private static String[] splitLines(String output) {
        if (output == null) {
            return null;
        }
        return output.split("\\r?\\n"); //$NON-NLS-1$
    }

    private static int unwrapOutput(String[] outputHolder) {
        String output = outputHolder[0];
        int begin = output.indexOf("echo " + BEGIN_TAG); //$NON-NLS-1$
        if (begin > 0) {
            begin = output.indexOf(BEGIN_TAG, begin + 5 + BEGIN_TAG.length());
        } else {
            begin = output.indexOf(BEGIN_TAG);
        }
        if (begin < 0) {
            outputHolder[0] = ""; //$NON-NLS-1$
            return 1;
        }

        begin += BEGIN_TAG.length();
        int end = output.indexOf(END_TAG, begin);
        if (end < 0) {
            outputHolder[0] = output.substring(begin).trim();
            return 1;
        }

        outputHolder[0] = output.substring(begin, end).trim();
        String tail = output.substring(end + END_TAG.length()).trim();
        int numEnd;
        for (numEnd = 0; numEnd < tail.length(); numEnd++) {
            if (!Character.isDigit(tail.charAt(numEnd))) {
                break;
            }
        }
        try {
            return Integer.parseInt(tail.substring(0, numEnd));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
