/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils;
import org.eclipse.linuxtools.internal.docker.ui.utils.IRunnableWithResult;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel.ExposedPortModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RunImageMainTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "RunMainTab.name"; //$NON-NLS-1$
	// private static final String ERROR_PULLING_IMAGE = "ImagePullError.msg";
	// //$NON-NLS-1$

	private static final int COLUMNS = 3;

	private final DataBindingContext dbc = new DataBindingContext();
	private ImageRunSelectionModel model;
	private boolean initialized = false;
	private Map<String, ImageRunSelectionModel> modelMap = new HashMap<>();

	private Combo connectionSelectionCombo;
	private Combo imageSelectionCombo;
	private Text containerNameText;
	private Text entrypointText;
	private Text commandText;
	private Button allocatePseudoTTY;
	private Button interactiveButton;
	private Button removeWhenExitsButton;

	public RunImageMainTab() {
		IDockerConnection[] connections = DockerConnectionManager.getInstance()
				.getConnections();
		if (connections == null) {
			return;
		}
		this.model = new ImageRunSelectionModel(connections[0]);
	}

	public ImageRunSelectionModel getModel() {
		return model;
	}

	private class ImageSelectionValidator extends MultiValidator {

		private final IObservableValue imageSelectionObservable;

		ImageSelectionValidator(
				final IObservableValue imageSelectionObservable) {
			this.imageSelectionObservable = imageSelectionObservable;
		}

		@Override
		protected IStatus validate() {
			final String selectedImageName = (String) imageSelectionObservable
					.getValue();
			if (selectedImageName.isEmpty()) {
				model.setSelectedImageNeedsPulling(false);
				return ValidationStatus.error(WizardMessages
						.getString("ImageRunSelectionPage.specifyImageMsg")); //$NON-NLS-1$
			}
			if (model.getSelectedImage() != null) {
				model.setSelectedImageNeedsPulling(false);
				return ValidationStatus.ok();
			}
			model.setSelectedImageNeedsPulling(true);
			return ValidationStatus.warning(WizardMessages.getFormattedString(
					"ImageRunSelectionPage.imageNotFoundMessage", //$NON-NLS-1$
					selectedImageName));
		}

		@Override
		public IObservableList getTargets() {
			WritableList targets = new WritableList();
			targets.add(imageSelectionObservable);
			return targets;
		}
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.applyTo(container);
		setDefaultValues();
		createImageSettingsSection(container);
		createSectionSeparator(container, true);
		createRunOptionsSection(container);
		final IObservableValue imageSelectionObservable = BeanProperties
				.value(ImageRunSelectionModel.class,
						ImageRunSelectionModel.SELECTED_IMAGE_NAME)
				.observe(model);
		imageSelectionObservable
				.addValueChangeListener(onImageSelectionChange());
		final ImageSelectionValidator imageSelectionValidator = new ImageSelectionValidator(
				imageSelectionObservable);
		dbc.addValidationStatusProvider(imageSelectionValidator);
		//
		setControl(container);
	}

	private void setDefaultValues() {
		final IDockerImage selectedImage = model.getSelectedImage();
		if (selectedImage == null) {
			return;
		}
		findImageInfo(selectedImage);
	}

	private void createSectionSeparator(final Composite container,
			final boolean separator) {
		final int SECTION_INDENT = 10;
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false)
				.indent(SWT.DEFAULT, SECTION_INDENT)
				.applyTo(new Label(container, separator
						? (SWT.SEPARATOR | SWT.HORIZONTAL) : SWT.NONE));
	}

	/**
	 * Creates the {@link Composite} container that will display widgets to
	 * select an {@link IDockerImage}, name it and specify the command to run.
	 * 
	 * @param container
	 *            the parent {@link Composite}
	 */
	private void createImageSettingsSection(final Composite container) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1)
				.applyTo(new Label(container, SWT.NONE));
		final Label connectionSelectionLabel = new Label(container, SWT.NONE);
		connectionSelectionLabel
				.setText(WizardMessages.getString("Connection.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(connectionSelectionLabel);
		connectionSelectionCombo = new Combo(container, SWT.BORDER);
		connectionSelectionCombo
				.addSelectionListener(new LaunchConfigurationChangeListener());
		connectionSelectionCombo.setToolTipText(
				LaunchMessages.getString("RunMainTabSelectConnection.tooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(connectionSelectionCombo);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.applyTo(new Label(container, SWT.NONE));
		new ControlDecoration(connectionSelectionCombo, SWT.TOP | SWT.LEFT);
		final ComboViewer connectionSelectionComboViewer = new ComboViewer(
				connectionSelectionCombo);
		connectionSelectionComboViewer
				.setContentProvider(new ArrayContentProvider());
		connectionSelectionComboViewer.setInput(getConnectionNames());
		dbc.bindValue(
				WidgetProperties.selection().observe(connectionSelectionCombo),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.SELECTED_CONNECTION_NAME)
						.observe(model));
		// Image selection name
		final Label imageSelectionLabel = new Label(container, SWT.NONE);
		imageSelectionLabel.setText(WizardMessages.getString("Image.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageSelectionLabel);
		imageSelectionCombo = new Combo(container, SWT.BORDER);
		imageSelectionCombo
				.addSelectionListener(new LaunchConfigurationChangeListener());
		final ComboViewer imageSelectionComboViewer = new ComboViewer(
				imageSelectionCombo);
		imageSelectionCombo.setToolTipText(WizardMessages
				.getString("ImageRunSelectionPage.selectTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(imageSelectionCombo);
		new ControlDecoration(imageSelectionCombo, SWT.TOP | SWT.LEFT);
		new ContentProposalAdapter(imageSelectionCombo,
				new ComboContentAdapter() {
					@Override
					public void insertControlContents(Control control,
							String text, int cursorPosition) {
						final Combo combo = (Combo) control;
						final Point selection = combo.getSelection();
						combo.setText(text);
						selection.x = text.length();
						selection.y = selection.x;
						combo.setSelection(selection);
					}
				}, getImageNameContentProposalProvider(imageSelectionCombo),
				null, null);
		// image search
		final Button searchImageButton = new Button(container, SWT.NONE);
		searchImageButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.search")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.hint(LaunchConfigurationUtils
						.getButtonWidthHint(searchImageButton), SWT.DEFAULT)
				.applyTo(searchImageButton);
		searchImageButton.addSelectionListener(onSearchImage());
		// // link to pull image
		// final Label fillerLabel = new Label(container, SWT.NONE);
		// GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
		// .grab(false, false).span(1, 1).applyTo(fillerLabel);
		// final Link pullImageLink = new Link(container, SWT.NONE);
		// pullImageLink.setText(
		// WizardMessages.getString("ImageRunSelectionPage.pullImage"));
		// //$NON-NLS-1$
		// GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
		// .grab(true, false).span(COLUMNS - 1, 1).applyTo(pullImageLink);
		// pullImageLink.addSelectionListener(onPullImage());
		// dbc.bindValue(WidgetProperties.enabled().observe(pullImageLink),
		// BeanProperties
		// .value(ImageRunSelectionModel.class,
		// ImageRunSelectionModel.SELECTED_IMAGE_NEEDS_PULLING)
		// .observe(model));
		// bind combo with model (for values and selection)
		imageSelectionComboViewer
				.setContentProvider(new ObservableListContentProvider());
		dbc.bindList(WidgetProperties.items().observe(imageSelectionCombo),
				BeanProperties
						.list(ImageRunSelectionModel.class,
								ImageRunSelectionModel.IMAGE_NAMES)
						.observe(model));
		dbc.bindValue(WidgetProperties.selection().observe(imageSelectionCombo),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.SELECTED_IMAGE_NAME)
						.observe(model));
		// Container name (optional)
		final Label containerNameLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageSelectionLabel);
		containerNameLabel.setText("Name:"); //$NON-NLS-1$
		containerNameText = new Text(container, SWT.BORDER);
		containerNameText
				.addModifyListener(new LaunchConfigurationChangeListener());
		containerNameText.setToolTipText(WizardMessages
				.getString("ImageRunSelectionPage.containerTooltip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(containerNameText);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.applyTo(new Label(container, SWT.NONE));
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(containerNameText),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.CONTAINER_NAME)
						.observe(model));

		// EntryPoint (optional)
		final Label entrypointLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageSelectionLabel);
		entrypointLabel.setText(
				WizardMessages.getString("ImageRunSelectionPage.entrypoint")); //$NON-NLS-1$
		// TODO: include SWT.SEARCH | SWT.ICON_SEARCH to support value reset
		entrypointText = new Text(container, SWT.BORDER);
		entrypointText
				.addModifyListener(new LaunchConfigurationChangeListener());

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(entrypointText);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.applyTo(new Label(container, SWT.NONE));
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(entrypointText),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.ENTRYPOINT)
						.observe(model));

		// Command (optional)
		final Label commandLabel = new Label(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageSelectionLabel);
		commandLabel.setText(
				WizardMessages.getString("ImageRunSelectionPage.command")); //$NON-NLS-1$
		commandText = new Text(container, SWT.BORDER);
		commandText.addModifyListener(new LaunchConfigurationChangeListener());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(1, 1).applyTo(commandText);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(1, 1)
				.applyTo(new Label(container, SWT.NONE));
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(commandText),
				BeanProperties.value(ImageRunSelectionModel.class,
						ImageRunSelectionModel.COMMAND).observe(model));
	}

	private String[] getConnectionNames() {
		IDockerConnection[] connections = DockerConnectionManager.getInstance().getConnections();
		ArrayList<String> connectionNames = new ArrayList<>();
		for (IDockerConnection connection :  connections) {
			connectionNames.add(connection.getName());
		}
		return connectionNames.toArray(new String[] {});
	}

	/**
	 * Creates an {@link IContentProposalProvider} to propose
	 * {@link IDockerImage} names based on the current text.
	 * 
	 * @param items
	 * @return
	 */
	private IContentProposalProvider getImageNameContentProposalProvider(
			final Combo imageSelectionCombo) {
		return new IContentProposalProvider() {

			@Override
			public IContentProposal[] getProposals(final String contents,
					final int position) {
				final List<IContentProposal> proposals = new ArrayList<>();
				for (String imageName : imageSelectionCombo.getItems()) {
					if (imageName.contains(contents)) {
						proposals.add(new ContentProposal(imageName, imageName,
								imageName, position));
					}
				}
				return proposals.toArray(new IContentProposal[0]);
			}
		};
	}

	private IValueChangeListener onImageSelectionChange() {
		return new IValueChangeListener() {

			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				final IDockerImage selectedImage = model.getSelectedImage();
				// skip if the selected image does not exist in the local Docker
				// host
				if (selectedImage == null) {
					model.setExposedPorts(new WritableList());
					return;
				}
				findImageInfo(selectedImage);
			}
		};
	}

	// private SelectionListener onPullImage() {
	// return new SelectionAdapter() {
	//
	// @Override
	// public void widgetSelected(final SelectionEvent e) {
	// pullSelectedImage();
	// }
	// };
	// }
	//
	// private void pullSelectedImage() {
	// try {
	// ProgressMonitorDialog dialog = new ProgressMonitorDialog(
	// this.getShell());
	// dialog.run(true, true, new IRunnableWithProgress() {
	//
	// @Override
	// public void run(final IProgressMonitor monitor)
	// throws InterruptedException {
	// final IDockerConnection connection = model
	// .getSelectedConnection();
	// final String imageName = model.getSelectedImageName();
	// monitor.beginTask(WizardMessages.getFormattedString(
	// "ImageRunSelectionPage.pullingTask", imageName), 1); //$NON-NLS-1$
	// try {
	// connection.pullImage(imageName,
	// new ImagePullProgressHandler(connection,
	// imageName));
	// } catch (final DockerException e) {
	// Display.getDefault().syncExec(new Runnable() {
	// @Override
	// public void run() {
	// MessageDialog.openError(
	// Display.getCurrent().getActiveShell(),
	// DVMessages.getFormattedString(
	// ERROR_PULLING_IMAGE, imageName),
	// e.getMessage());
	// }
	// });
	// } finally {
	// monitor.done();
	// // refresh the widgets
	// model.refreshImageNames();
	// if (model.getImageNames().contains(imageName)) {
	// model.setSelectedImageName(imageName);
	// }
	// }
	// }
	// });
	// } catch (InvocationTargetException | InterruptedException e) {
	// Activator.log(e);
	// }
	// }

	private static final class FindImageInfoRunnable
			implements IRunnableWithResult<IDockerImageInfo> {
		private final IDockerImage selectedImage;
		private IDockerImageInfo selectedImageInfo;

		private FindImageInfoRunnable(IDockerImage selectedImage) {
			this.selectedImage = selectedImage;
		}

		@Override
		public void run(final IProgressMonitor monitor) {
			selectedImageInfo = selectedImage.getConnection()
					.getImageInfo(selectedImage.id());
		}

		@Override
		public IDockerImageInfo getResult() {
			return selectedImageInfo;
		}
	}

	private void findImageInfo(final IDockerImage selectedImage) {
		try {
			final FindImageInfoRunnable findImageInfoRunnable = new FindImageInfoRunnable(
					selectedImage);
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					this.getShell());
			dialog.run(true, true, findImageInfoRunnable);
			final IDockerImageInfo selectedImageInfo = findImageInfoRunnable
					.getResult();
			final Set<String> exposedPortInfos = selectedImageInfo.config()
					.exposedPorts();
			final WritableList availablePorts = new WritableList();
			if (exposedPortInfos != null) {
				for (String exposedPortInfo : exposedPortInfos) {
					final String privatePort = exposedPortInfo.substring(0,
							exposedPortInfo.indexOf('/'));
					final String type = exposedPortInfo
							.substring(exposedPortInfo.indexOf('/')); // $NON-NLS-1$
					final ExposedPortModel exposedPort = new ExposedPortModel(
							privatePort, type, "", privatePort);
					availablePorts.add(exposedPort); // $NON-NLS-1$
				}
			}
			model.setExposedPorts(availablePorts);
			model.setCommand(selectedImageInfo.config().cmd());
			model.setEntrypoint(selectedImageInfo.config().entrypoint());

		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}
	}

	private void createRunOptionsSection(final Composite container) {
		// interactive/show in console mode
		interactiveButton = new Button(container, SWT.CHECK);
		interactiveButton
				.addSelectionListener(new LaunchConfigurationChangeListener());
		interactiveButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.openStdin")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false).applyTo(interactiveButton);
		dbc.bindValue(WidgetProperties.selection().observe(interactiveButton),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.INTERACTIVE_MODE)
						.observe(model));
		// allocate pseudo-TTY
		allocatePseudoTTY = new Button(container, SWT.CHECK);
		allocatePseudoTTY
				.addSelectionListener(new LaunchConfigurationChangeListener());
		allocatePseudoTTY
				.setText(WizardMessages.getString("ImageRunSelectionPage.tty")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false).applyTo(allocatePseudoTTY);
		dbc.bindValue(WidgetProperties.selection().observe(allocatePseudoTTY),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.ALLOCATE_PSEUDO_TTY)
						.observe(model));

		// remove when exits
		removeWhenExitsButton = new Button(container, SWT.CHECK);
		removeWhenExitsButton
				.addSelectionListener(new LaunchConfigurationChangeListener());
		removeWhenExitsButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.autoRemove")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.span(COLUMNS, 1).grab(true, false)
				.applyTo(removeWhenExitsButton);
		dbc.bindValue(
				WidgetProperties.selection().observe(removeWhenExitsButton),
				BeanProperties
						.value(ImageRunSelectionModel.class,
								ImageRunSelectionModel.REMOVE_WHEN_EXITS)
						.observe(model));
	}

	private SelectionListener onSearchImage() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final ImageSearch imageSearchWizard = new ImageSearch(
						RunImageMainTab.this.model
								.getSelectedConnection(),
						RunImageMainTab.this.model
								.getSelectedImageName());
				final boolean completed = CommandUtils
						.openWizard(imageSearchWizard, getShell());
				if (completed) {
					model.setSelectedImageName(
							imageSearchWizard.getSelectedImage());
				}
			}
		};
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_MAIN_TAB);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

	}

	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		super.activated(workingCopy);
	}

	@Override
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		super.deactivated(workingCopy);
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		if (!initialized) {
			initialized = true;
			modelMap.put(configuration.getName(), model);
		}
		model = modelMap.get(configuration.getName());
		IDockerConnection[] connections = DockerConnectionManager.getInstance()
				.getConnections();
		if (model == null) {
			model = new ImageRunSelectionModel(connections[0]);
			modelMap.put(configuration.getName(), model);
		}
		try {
			String connectionName = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
					connections[0].getName());
			connectionSelectionCombo.setText(connectionName);
			model.setSelectedConnectionName(connectionName);
			String imageName = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.IMAGE_NAME, ""); //$NON-NLS-1$
			imageSelectionCombo.setText(imageName);
			model.setSelectedImageName(imageName);
			String command = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.COMMAND, ""); //$NON-NLS-1$
			commandText.setText(command);
			model.setCommand(command);
			String entryPoint = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.ENTRYPOINT, ""); //$NON-NLS-1$
			entrypointText.setText(entryPoint);
			model.setEntrypoint(entryPoint);
			String containerName = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME,
					""); //$NON-NLS-1$
			containerNameText.setText(containerName);
			model.setContainerName(containerName);
			boolean removeContainer = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.AUTO_REMOVE,
					false);
			removeWhenExitsButton.setSelection(removeContainer);
			model.setRemoveWhenExits(removeContainer);
			boolean interactive = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.INTERACTIVE,
					false);
			interactiveButton.setSelection(interactive);
			model.setInteractiveMode(interactive);
			boolean useTTY = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.ALLOCATE_PSEUDO_CONSOLE,
					false);
			allocatePseudoTTY.setSelection(useTTY);
			model.setAllocatePseudoTTY(useTTY);
			// model.setAllocatePseudoTTY(useTTY);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
				connectionSelectionCombo.getText());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.IMAGE_NAME,
				imageSelectionCombo.getText());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.COMMAND,
				commandText.getText());
		if (!entrypointText.getText().isEmpty())
			configuration.setAttribute(
					IRunDockerImageLaunchConfigurationConstants.ENTRYPOINT,
					entrypointText.getText());
		if (!containerNameText.getText().isEmpty())
			configuration.setAttribute(
					IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME,
					containerNameText.getText());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.ALLOCATE_PSEUDO_CONSOLE,
				allocatePseudoTTY.getSelection());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.AUTO_REMOVE,
				removeWhenExitsButton.getSelection());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.INTERACTIVE,
				interactiveButton.getSelection());
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {
			if (launchConfig.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.CONNECTION_NAME,
					"").isEmpty()) {
				return false;
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
		return super.isValid(launchConfig);
	}

	protected class LaunchConfigurationChangeListener extends SelectionAdapter
			implements ModifyListener {

		@Override
		public void modifyText(final ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}

		@Override
		public void widgetSelected(final SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}
	}
	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

}
