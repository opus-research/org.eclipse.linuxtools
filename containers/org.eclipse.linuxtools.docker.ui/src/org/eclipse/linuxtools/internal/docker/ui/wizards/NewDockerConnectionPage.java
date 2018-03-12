/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import static org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings.UNIX_SOCKET;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionSettings;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection.Builder;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author xcoulon
 *
 */
public class NewDockerConnectionPage extends WizardPage {

	private final DataBindingContext dbc = new DataBindingContext();

	private final NewDockerConnectionPageModel model;

	public NewDockerConnectionPage() {
		super("NewDockerConnectionPage", "Connect to a Docker daemon",
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage("Select the binding mode to connect to the Docker daemon");
		this.model = new NewDockerConnectionPageModel();
	}

	@Override
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.applyTo(container);
		createConnectionSettingsContainer(container);
		// setup validation support
		WizardPageSupport.create(this, dbc);
		setPageComplete(false);
		setControl(container);
	}

	/**
	 * Creates the connection settings container, where the user can choose how
	 * to connect to the docker daemon (using sockets or TCP with SSL - or not)
	 * 
	 * @param parent
	 *            the parent container (ie, the main container in the preference
	 *            page)
	 */
	private void createConnectionSettingsContainer(final Composite parent) {
		final int COLUMNS = 3;
		final int INDENT = 20;
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 2).applyTo(container);

		// Connection name
		final Label connectionNameLabel = new Label(container, SWT.NONE);
		connectionNameLabel.setText("Connection name:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(connectionNameLabel);
		final Text connectionNameText = new Text(container, SWT.BORDER);
		connectionNameText.setToolTipText("Name of the connection");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(connectionNameText);
		final IObservableValue connectionNameObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.CONNECTION_NAME)
				.observe(model);
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(connectionNameText),
				connectionNameObservable);
		// use custom settings
		final Button customConnectionSettingsButton = new Button(container,
				SWT.CHECK);
		customConnectionSettingsButton
				.setText("Use custom connection settings:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).indent(0, 10).span(COLUMNS, 1)
				.applyTo(customConnectionSettingsButton);
		final IObservableValue customConnectionSettingsObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.CUSTOM_SETTINGS)
				.observe(model);
		dbc.bindValue(
				WidgetProperties.selection()
						.observe(customConnectionSettingsButton),
				customConnectionSettingsObservable);

		final Group customSettingsGroup = new Group(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, false)
				.applyTo(customSettingsGroup);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 2).applyTo(customSettingsGroup);

		// Unix socket binding mode
		final Button unixSocketSelectionButton = new Button(customSettingsGroup,
				SWT.RADIO);
		unixSocketSelectionButton.setText("Unix socket");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).applyTo(unixSocketSelectionButton);
		final IObservableValue bindingModeObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.BINDING_MODE)
				.observe(model);
		dbc.bindValue(
				WidgetProperties.selection().observe(unixSocketSelectionButton),
				bindingModeObservable,
				targetToModelBindingModeConverter(
						EnumDockerConnectionSettings.UNIX_SOCKET),
				modelToTargetBidingModeConverter(
						EnumDockerConnectionSettings.UNIX_SOCKET));
		// Unix socket path
		final Label unixSocketPathLabel = new Label(customSettingsGroup,
				SWT.NONE);
		unixSocketPathLabel.setText("Location:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).applyTo(unixSocketPathLabel);
		final Text unixSocketPathText = new Text(customSettingsGroup,
				SWT.BORDER);
		unixSocketPathText.setToolTipText("Path to the socket file");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(unixSocketPathText);
		final Button unixSocketPathBrowseButton = new Button(
				customSettingsGroup, SWT.BUTTON1);
		unixSocketPathBrowseButton.setText("Browse...");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(unixSocketPathBrowseButton);
		final IObservableValue unixSocketPathObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.UNIX_SOCKET_PATH)
				.observe(model);
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(unixSocketPathText),
				unixSocketPathObservable);
		// TCP Host binding mode
		final Button tcpConnectionSelectionButton = new Button(
				customSettingsGroup, SWT.RADIO);
		tcpConnectionSelectionButton.setText("TCP Connection");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).applyTo(tcpConnectionSelectionButton);
		dbc.bindValue(
				WidgetProperties.selection()
						.observe(tcpConnectionSelectionButton),
				bindingModeObservable,
				targetToModelBindingModeConverter(
						EnumDockerConnectionSettings.TCP_CONNECTION),
				modelToTargetBidingModeConverter(
						EnumDockerConnectionSettings.TCP_CONNECTION));
		// TCP Host
		final Label tcpHostLabel = new Label(customSettingsGroup, SWT.NONE);
		tcpHostLabel.setText("Host:");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).applyTo(tcpHostLabel);
		final Text tcpHostText = new Text(customSettingsGroup, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(true, false).applyTo(tcpHostText);
		final IObservableValue tcpHostObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.TCP_HOST)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(tcpHostText),
				tcpHostObservable);
		// TCP Host authentication
		final Button tcpAuthButton = new Button(customSettingsGroup, SWT.CHECK);
		tcpAuthButton.setText("Enable authentication");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT, 0).span(3, 1).applyTo(tcpAuthButton);
		final IObservableValue enableTcpAuthObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.ENABLE_TCP_AUTH)
				.observe(model);
		dbc.bindValue(WidgetProperties.selection().observe(tcpAuthButton),
				enableTcpAuthObservable);

		// TCP Cert path
		final Label tcpCertPathLabel = new Label(customSettingsGroup, SWT.NONE);
		tcpCertPathLabel.setText("Path:");
		tcpCertPathLabel.setToolTipText("Path to the certificates folder");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.indent(INDENT * 2, 0).applyTo(tcpCertPathLabel);
		final Text tcpCertPathText = new Text(customSettingsGroup, SWT.BORDER);
		tcpCertPathText.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(tcpCertPathText);
		final Button tcpCertPathBrowseButton = new Button(customSettingsGroup,
				SWT.BUTTON1);
		tcpCertPathBrowseButton.setText("Browse...");
		tcpCertPathText.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(tcpCertPathBrowseButton);
		final IObservableValue tcpCertPathObservable = BeanProperties
				.value(NewDockerConnectionPageModel.class,
						NewDockerConnectionPageModel.TCP_CERT_PATH)
				.observe(model);
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(tcpCertPathText),
				tcpCertPathObservable);
		// Test connection
		final Button testConnectionButton = new Button(container, SWT.NONE);
		testConnectionButton.setText("Test Connection");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).align(SWT.END, SWT.CENTER)
				.applyTo(testConnectionButton);
		testConnectionButton
				.addSelectionListener(onTestConnectionButtonSelection());

		// group controls to easily enable/disable them
		final Control[] bindingModeSelectionControls = new Control[] {
				unixSocketSelectionButton, tcpConnectionSelectionButton };
		final Control[] unixSocketControls = new Control[] { unixSocketPathText,
				unixSocketPathBrowseButton };
		final Control[] tcpConnectionControls = new Control[] { tcpHostText,
				tcpAuthButton };
		final Control[] tcpAuthControls = new Control[] { tcpCertPathText,
				tcpCertPathBrowseButton };
		bindingModeObservable.addValueChangeListener(onBindingModeChange(
				unixSocketControls, tcpConnectionControls, tcpAuthControls));
		enableTcpAuthObservable
				.addValueChangeListener(onTcpAuthSelection(tcpAuthControls));
		customConnectionSettingsObservable
				.addValueChangeListener(onCustomConnectionSettingsSelection(
						bindingModeSelectionControls, unixSocketControls,
						tcpAuthControls, tcpConnectionControls));

		retrieveDefaultConnectionSettings(connectionNameText,
				customConnectionSettingsButton);
		if (!model.isCustomSettings()) {
			setWidgetsEnabled(false, bindingModeSelectionControls,
					unixSocketControls, tcpConnectionControls, tcpAuthControls);
		}

		// set validation
		final ConnectionSettingsValidator validator = new ConnectionSettingsValidator(
				connectionNameObservable, unixSocketPathObservable,
				tcpHostObservable, tcpCertPathObservable);
		dbc.addValidationStatusProvider(validator);
	}

	private UpdateValueStrategy modelToTargetBidingModeConverter(
			final EnumDockerConnectionSettings bindingValue) {
		return new UpdateValueStrategy() {
			@Override
			public Object convert(final Object value) {
				if (value != null && value.equals(bindingValue)) {
					return true;
				}
				return false;
			}
		};
	}

	private UpdateValueStrategy targetToModelBindingModeConverter(
			final EnumDockerConnectionSettings bindingValue) {
		return new UpdateValueStrategy() {
			@Override
			public Object convert(Object value) {
				if (value != null && value.equals(Boolean.TRUE)) {
					return bindingValue;
				}
				return null;
			}
		};
	}

	/**
	 * Sets the default settings by looking for the:
	 * <ul>
	 * <li>a Unix socket at /var/run/docker.sock</li>
	 * <li>the following environment variables:
	 * <ul>
	 * <li>DOCKER_HOST</li>
	 * <li>DOCKER_CERT_PATH</li>
	 * <li>DOCKER_TLS_VERIFY</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * and sets the default connection settings accordingly.
	 */
	private void retrieveDefaultConnectionSettings(final Control controlToFocus,
			final Button customConnectionSettingsButton) {
		// let's run this in a job and show the progress in the wizard
		// progressbar
		try {
			getWizard().getContainer().run(true, true,
					new IRunnableWithProgress() {
						@Override
						public void run(final IProgressMonitor monitor) {
							monitor.beginTask(
									"Retrieving Docker connection settings...",
									1);
							try {
								final DockerConnection.Defaults defaults = new DockerConnection.Defaults();
								model.setBindingMode(defaults.getBindingMode());
								model.setConnectionName(defaults.getName());
								model.setUnixSocketPath(
										defaults.getUnixSocketPath());
								model.setTcpHost(defaults.getTcpHost());
								model.setEnableTcpAuth(
										defaults.getTcpTlsVerify());
								model.setTcpCertPath(defaults.getTcpCertPath());
								model.setCustomSettings(false);
							} catch (DockerException e) {
								model.setCustomSettings(true);
								// force user to input custom settings
								Display.getDefault().syncExec(new Runnable() {
									@Override
									public void run() {
										customConnectionSettingsButton
										.setEnabled(false);
									}
								});
								Activator.log(e);
							}
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									controlToFocus.setFocus();
								}
							});
							monitor.done();
						}
					});
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}

	}

	private IValueChangeListener onCustomConnectionSettingsSelection(
			final Control[] bindingModeSelectionControls,
			final Control[] unixSocketControls, final Control[] tcpAuthControls,
			final Control[] tcpConnectionControls) {
		return new IValueChangeListener() {

			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				if (model.isCustomSettings()) {
					setWidgetsEnabled(true, bindingModeSelectionControls);
					if (model.getBindingMode() == UNIX_SOCKET) {
						setWidgetsEnabled(true, unixSocketControls);
					} else {
						if (model.isEnableTcpAuth()) {
							setWidgetsEnabled(true, tcpAuthControls);
						}
						setWidgetsEnabled(true, tcpConnectionControls);
					}
				} else {
					setWidgetsEnabled(false, unixSocketControls,
							tcpConnectionControls, tcpAuthControls);
				}
			}
		};
	}

	private IValueChangeListener onBindingModeChange(
			final Control[] unixSocketControls,
			final Control[] tcpConnectionControls,
			final Control[] tcpAuthControls) {
		return new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				setWidgetsEnabled(
						model.getBindingMode() == EnumDockerConnectionSettings.UNIX_SOCKET,
						unixSocketControls);
				setWidgetsEnabled(
						model.getBindingMode() == EnumDockerConnectionSettings.TCP_CONNECTION,
						tcpConnectionControls);
				setWidgetsEnabled(model.isEnableTcpAuth(), tcpAuthControls);
			}
		};
	}

	private IValueChangeListener onTcpAuthSelection(
			final Control[] tcpAuthControls) {
		return new IValueChangeListener() {
			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				setWidgetsEnabled(model.isEnableTcpAuth(), tcpAuthControls);
			}
		};

	}

	private void setWidgetsEnabled(final boolean enabled,
			final Control... controls) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
		// set the focus on the fist element of the group.
		if (controls.length > 0 && enabled) {
			controls[0].setFocus();
		}
	}

	private void setWidgetsEnabled(final boolean enabled,
			final Control[]... controlGroups) {
		for (Control[] controlGroup : controlGroups) {
			for (Control control : controlGroup) {
				control.setEnabled(enabled);
			}
		}
	}

	/**
	 * Verifies that the given connection settings work by trying to connect to
	 * the target Docker daemon
	 * 
	 * @return
	 */
	private SelectionListener onTestConnectionButtonSelection() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final ArrayBlockingQueue<Boolean> resultQueue = new ArrayBlockingQueue<>(
						1);
				try {
					getWizard().getContainer().run(true, false,
							new IRunnableWithProgress() {
						@Override
						public void run(final IProgressMonitor monitor) {
							monitor.beginTask("Pinging Docker daemon...",
									IProgressMonitor.UNKNOWN);
							try {
								final DockerConnection dockerConnection = getDockerConnection();
								dockerConnection.open(false);
								dockerConnection.ping();
								dockerConnection.close();
								resultQueue.add(true);
							} catch (DockerException e) {
								Activator.log(e);
								resultQueue.add(false);
							}
						}
					});
				} catch (InvocationTargetException | InterruptedException o_O) {
					Activator.log(o_O);
				}
				try {
					final Boolean result = resultQueue.poll(5000,
							TimeUnit.MILLISECONDS);
					if (result != null && result.equals(Boolean.TRUE)) {
						new MessageDialog(Display.getDefault().getActiveShell(),
								"Success", null, "Ping succeeded !",
								SWT.ICON_INFORMATION, new String[] { "OK" }, 0)
										.open();
					} else {
						new MessageDialog(Display.getDefault().getActiveShell(),
								"Failure", null, "Ping failed !",
								SWT.ICON_ERROR, new String[] { "OK" }, 0)
										.open();
					}
				} catch (InterruptedException o_O) {
					new MessageDialog(Display.getDefault().getActiveShell(),
							"Failure", null, "Ping failed !", SWT.ICON_ERROR,
							new String[] { "OK" }, 0).open();
				}
			}

		};
	}

	/**
	 * Opens a new {@link DockerConnection} using the settings of this
	 * {@link NewDockerConnectionPage}.
	 * 
	 * @return
	 * @throws DockerCertificateException
	 */
	protected DockerConnection getDockerConnection() {
		if (model.getBindingMode() == UNIX_SOCKET) {
			return new DockerConnection.Builder()
					.name(model.getConnectionName())
					.unixSocket(model.getUnixSocketPath()).build();
		} else {
			final Builder builder = new DockerConnection.Builder()
					.name(model.getConnectionName())
					.tcpHost(model.getTcpHost());
			if (model.isEnableTcpAuth()) {
				builder.tcpCertPath(model.getTcpCertPath());
			}
			return builder.build();
		}
	}

	class ConnectionSettingsValidator extends MultiValidator {

		private final IObservableValue connectionNameObservable;
		private final IObservableValue unixSocketPathObservable;
		private final IObservableValue tcpHostObservable;
		private final IObservableValue tcpCertPathObservable;

		public ConnectionSettingsValidator(
				final IObservableValue connectionNameObservable,
				final IObservableValue unixSocketPathObservable,
				final IObservableValue tcpHostObservable,
				final IObservableValue tcpCertPathObservable) {
			this.connectionNameObservable = connectionNameObservable;
			this.unixSocketPathObservable = unixSocketPathObservable;
			this.tcpHostObservable = tcpHostObservable;
			this.tcpCertPathObservable = tcpCertPathObservable;
		}

		@Override
		protected IStatus validate() {
			final String connectionName = (String) connectionNameObservable
					.getValue();
			final String unixSocketPath = (String) unixSocketPathObservable
					.getValue();
			final String tcpHost = (String) tcpHostObservable.getValue();
			final String tcpCertPath = (String) tcpCertPathObservable
					.getValue();
			if (connectionName == null || connectionName.trim().isEmpty()) {
				return ValidationStatus
						.error("Connection name cannot be empty");
			} else if (model
					.getBindingMode() == EnumDockerConnectionSettings.UNIX_SOCKET
					&& (unixSocketPath == null
							|| unixSocketPath.trim().isEmpty())) {
				return ValidationStatus
						.error("Unix socket location cannot be empty");
			} else if (model
					.getBindingMode() == EnumDockerConnectionSettings.TCP_CONNECTION
					&& (tcpHost == null || tcpHost.trim().isEmpty())) {
				return ValidationStatus.error("TCP host cannot be empty");
			} else if (model.isEnableTcpAuth()
					&& (tcpCertPath == null || tcpCertPath.trim().isEmpty())) {
				return ValidationStatus.error(
						"Path to TCP Connection certificates cannot be empty");
			}
			return ValidationStatus.ok();
		}

	}

	class NewDockerConnectionPageModel extends BaseDatabindingModel {

		static final String CONNECTION_NAME = "connectionName";
		static final String CUSTOM_SETTINGS = "customSettings";
		static final String BINDING_MODE = "bindingMode";
		static final String UNIX_SOCKET_PATH = "unixSocketPath";
		static final String TCP_HOST = "tcpHost";
		static final String ENABLE_TCP_AUTH = "enableTcpAuth";
		static final String TCP_CERT_PATH = "tcpCertPath";

		private String connectionName;
		private boolean customSettings;
		private EnumDockerConnectionSettings bindingMode = UNIX_SOCKET;
		private String unixSocketPath;
		private String tcpHost;
		private boolean enableTcpAuth;
		private String tcpCertPath;

		public String getConnectionName() {
			return connectionName;
		}

		public void setConnectionName(final String connectionName) {
			firePropertyChange(CONNECTION_NAME, this.connectionName,
					this.connectionName = connectionName);
		}

		public boolean isCustomSettings() {
			return customSettings;
		}

		public void setCustomSettings(final boolean customSettings) {
			firePropertyChange(CUSTOM_SETTINGS, this.customSettings,
					this.customSettings = customSettings);
		}

		public EnumDockerConnectionSettings getBindingMode() {
			return bindingMode;
		}

		public void setBindingMode(
				final EnumDockerConnectionSettings bindingMode) {
			if (bindingMode != null) {
				firePropertyChange(BINDING_MODE, this.bindingMode,
						this.bindingMode = bindingMode);
			}
		}

		public String getUnixSocketPath() {
			return unixSocketPath;
		}

		public void setUnixSocketPath(final String unixSocketPath) {
			firePropertyChange(UNIX_SOCKET_PATH, this.unixSocketPath,
					this.unixSocketPath = unixSocketPath);
		}

		public String getTcpHost() {
			return tcpHost;
		}

		public void setTcpHost(final String tcpHost) {
			firePropertyChange(TCP_HOST, this.tcpHost, this.tcpHost = tcpHost);
		}

		public boolean isEnableTcpAuth() {
			return enableTcpAuth;
		}

		public void setEnableTcpAuth(final boolean enableTcpAuth) {
			firePropertyChange(ENABLE_TCP_AUTH, this.enableTcpAuth,
					this.enableTcpAuth = enableTcpAuth);
		}

		public String getTcpCertPath() {
			return tcpCertPath;
		}

		public void setTcpCertPath(final String tcpCertPath) {
			firePropertyChange(TCP_CERT_PATH, this.tcpCertPath,
					this.tcpCertPath = tcpCertPath);
		}

	}

}
