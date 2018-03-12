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
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
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
                    synchronized (fConnection) {
                        IRemoteProcessBuilder processBuilder = fConnection.getProcessBuilder(splitCommand(command));

//                        processBuilder.environment();
                        IRemoteProcess process= processBuilder.start();
                        InputReader stdout= new InputReader(process.getInputStream());
                        InputReader stderr= new InputReader(process.getErrorStream());

                        try {
                            stdout.waitFor();
                            stderr.waitFor();
                            int result = process.waitFor();

                            String output= stdout.getOutput();
                            String error= null;
                            if (result != 0) {
                                error= stderr.getOutput();
                                // Workaround if error stream is not available and stderr output is written
                                // in standard output above. This is true for the SshTerminalShell implementation.
                                if (error.length() == 0) {
                                    error= output;
                                }
                            }
                            return new CommandResult(result, splitLines(output), splitLines(error));
                        } catch (InterruptedException e) {
                            return new CommandResult(1, new String[0], new String[] {e.getMessage()});
                        } finally {
                            stdout.stop();
                            stderr.stop();
                        }
                    }
                }

                private String[] splitLines(String output) {
                    if (output == null) {
                        return null;
                    }
                    return output.split("\\r?\\n"); //$NON-NLS-1$
                }

                private String[] splitCommand(String shellCommand) {
                    return shellCommand.trim().split("\\s+"); //$NON-NLS-1$
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
            }
        }
        throw new ExecutionException(Messages.TraceControl_ShellNotConnected, null);
    }
}
