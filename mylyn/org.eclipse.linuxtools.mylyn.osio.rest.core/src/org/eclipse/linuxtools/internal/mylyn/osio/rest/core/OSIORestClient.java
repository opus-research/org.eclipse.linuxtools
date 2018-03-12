/*******************************************************************************
 * Copyright (c) 2013, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Area;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.AreaListResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.IdNamed;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Iteration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.IterationListResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Named;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.RestResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SpaceResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.User;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.UsersResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemLinkTypeData;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemLinkTypeResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeData;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeResponse;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.osgi.util.NLS;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.reflect.TypeToken;

public class OSIORestClient {

	private final CommonHttpClient client;
	
	private final RepositoryLocation location;
	
	private String userName;
	
	private Map<String, Space> cachedSpaces;
	
	private Map<String, User> cachedUsers;

	private final OSIORestConnector connector;

	public static final int MAX_RETRIEVED_PER_QUERY = 50;

	public OSIORestClient(RepositoryLocation location, OSIORestConnector connector) {
		client = new CommonHttpClient(location);
		this.location = location;
		userName = location.getProperty(IOSIORestConstants.REPOSITORY_AUTH_ID);
		this.connector = connector;
	}

	public CommonHttpClient getClient() {
		return client;
	}

	public boolean validate(IOperationMonitor monitor) throws OSIORestException {
		@SuppressWarnings("restriction")
		RepositoryLocation location = getClient().getLocation();
		if (location.getProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN) != null) {
			UserCredentials credentials = location.getCredentials(AuthenticationType.REPOSITORY);
//			Preconditions.checkState(credentials != null, "Authentication requested without valid credentials");
			String userName = location.getProperty(IOSIORestConstants.REPOSITORY_AUTH_ID);
			OSIORestUser response = new OSIORestGetAuthUser(client).run(monitor);
			if (response.getUsername().equals(userName)) {
				return true;
			}
		}
		return false;
	}

	public OSIORestConfiguration getConfiguration(TaskRepository repository, IOperationMonitor monitor) {
		try {
			OSIORestConfiguration config = new OSIORestConfiguration(repository.getUrl());
			Map<String, Space> spaces = getSpaces(monitor);
			for (Space space : spaces.values()) {
				Map<String, WorkItemTypeData> workItemTypes = getSpaceWorkItemTypes(new NullOperationMonitor(), space);
				space.setWorkItemTypes(workItemTypes);
				Map<String, WorkItemLinkTypeData> workItemLinkTypes = getSpaceWorkItemLinkTypes(new NullOperationMonitor(), space);
				space.setWorkItemLinkTypes(workItemLinkTypes);
				Map<String, Area> areas = getSpaceAreas(new NullOperationMonitor(), space);
				space.setAreas(areas);
				Map<String, Iteration> iterations = getSpaceIterations(new NullOperationMonitor(), space);
				space.setIterations(iterations);
				Map<String, User> users = getUsers(new NullOperationMonitor(), space);
				space.setUsers(users);
			}
			config.setSpaces(spaces);
			return config;
		} catch (Exception e) {
			StatusHandler
					.log(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, "Could not get the Configuration", e)); //$NON-NLS-1$
			return null;
		}
	}

	public <R extends RestResponse<E>, E extends Named> Map<String, E> retrieveItems(IOperationMonitor monitor,
			String path, TypeToken<?> typeToken) throws OSIORestException {
		R response = new OSIORestGetRequest<R>(client, path, typeToken).run(monitor);
		E[] members = response.getArray();
		Map<String, E> map = new TreeMap<>();
		for (E member : members) {
			map.put(member.getName(), member);
		}
		return map;
	}
	
	public <R extends RestResponse<E>, E extends Named> Map<String, E> retrieveItemsAuth(IOperationMonitor monitor,
			String path, TypeToken<?> typeToken) throws OSIORestException {
		R response = new OSIORestGetRequest<R>(client, path, typeToken, true).run(monitor);
		E[] members = response.getArray();
		Map<String, E> map = new TreeMap<>();
		for (E member : members) {
			map.put(member.getName(), member);
		}
		return map;
	}

	public <R extends RestResponse<E>, E extends IdNamed> Map<String, E> retrieveItemsById(IOperationMonitor monitor,
			String path, TypeToken<?> typeToken) throws OSIORestException {
		R response = new OSIORestGetRequest<R>(client, path, typeToken).run(monitor);
		E[] members = response.getArray();
		return Maps.uniqueIndex(Lists.newArrayList(members), new Function<E, String>() {
			public String apply(E input) {
				return input.getId();
			};
		});
	}

	private Map<String, Space> getCachedSpaces(IOperationMonitor monitor) throws OSIORestException {
		if (cachedSpaces == null) {
			cachedSpaces = getSpaces(monitor);
		}
		return cachedSpaces;
	}

	private Map<String, Space> getSpaces(IOperationMonitor monitor) throws OSIORestException {
		cachedSpaces = retrieveItems(monitor, "/namedspaces/" + userName, new TypeToken<SpaceResponse>() { //$NON-NLS-1$
		});
		return cachedSpaces;
	}
	
	private Map<String, User> getUsers(IOperationMonitor monitor, Space space) throws OSIORestException {
		Map<String, User> users = retrieveItemsAuth(monitor, "/spaces/" + space.getId() + "/collaborators", new TypeToken<UsersResponse>() { //$NON-NLS-1$
		});
		return users;
	}
	
	private Map<String, WorkItemTypeData> getSpaceWorkItemTypes(IOperationMonitor monitor, Space space) throws OSIORestException {
		String linkSuffix = connector.getURLSuffix(space.getLinks().getWorkItemTypes());
		return retrieveItems(monitor, linkSuffix, new TypeToken<WorkItemTypeResponse>() {
		});
	}

	private Map<String, WorkItemLinkTypeData> getSpaceWorkItemLinkTypes(IOperationMonitor monitor, Space space) throws OSIORestException {
		String linkSuffix = connector.getURLSuffix(space.getLinks().getWorkItemLinkTypes());
		return retrieveItemsById(monitor, linkSuffix, new TypeToken<WorkItemLinkTypeResponse>() {
		});
	}

	private Map<String, Area> getSpaceAreas(IOperationMonitor monitor, Space space) throws OSIORestException {
		return retrieveItems(monitor, "/spaces/" + space.getId() + "/areas", new TypeToken<AreaListResponse>() { //$NON-NLS-1$ //$NON-NLS-2$
		});
	}

	private Map<String, Iteration> getSpaceIterations(IOperationMonitor monitor, Space space) throws OSIORestException {
		return retrieveItems(monitor, "/spaces/" + space.getId() + "/iterations", new TypeToken<IterationListResponse>() { //$NON-NLS-1$ //$NON-NLS-2$
		});
	}

	public RepositoryResponse postTaskData(TaskData taskData, Set<TaskAttribute> oldAttributes,
			TaskRepository repository, IOperationMonitor monitor) throws OSIORestException {
		if (taskData.isNew()) {
			TaskAttribute spaceAttribute = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().SPACE.getKey());
			String spaceName = spaceAttribute.getValue();
			Map<String, Space> spaces = getCachedSpaces(new NullOperationMonitor());
			Space space = spaces.get(spaceName);
			String id = null;
			try {
				id = new OSIORestPostNewTask(client, taskData, space, connector, repository).run(monitor);
			} catch (CoreException e1) {
				throw new OSIORestException(e1);
			}
			return new RepositoryResponse(ResponseKind.TASK_CREATED, id);
		} else {
			OSIORestConfiguration config;
			try {
				config = connector.getRepositoryConfiguration(repository);
			} catch (CoreException e1) {
				throw new OSIORestException(e1);
			}

			TaskAttribute newComment = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().NEW_COMMENT.getKey());
			if (newComment != null) {
				String value = newComment.getValue();
				if (value != null && !value.isEmpty()) {
					new OSIORestPostNewCommentTask(client, taskData, oldAttributes).run(monitor);
					newComment.setValue("");
				}
			}
			new OSIORestPatchUpdateTask(client, taskData, oldAttributes, config.getSpaces()).run(monitor);
			return new RepositoryResponse(ResponseKind.TASK_UPDATED, taskData.getTaskId());
		}
	}

	private final Function<String, String> removeLeadingZero = new Function<String, String>() {

		@Override
		public String apply(String input) {
			while (input.startsWith("0")) { //$NON-NLS-1$
				input = input.substring(1);
			}
			return input;
		}
	};

	public void getTaskData(Set<String> taskIds, TaskRepository taskRepository, TaskDataCollector collector,
			IOperationMonitor monitor) throws OSIORestException {
		OSIORestConfiguration config;
		try {
			config = connector.getRepositoryConfiguration(taskRepository);
		} catch (CoreException e1) {
			throw new OSIORestException(e1);
		}

		for (String taskId : taskIds) {
			if (taskId.isEmpty()) {
				continue;
			}
			String[] tokens = taskId.split("#"); //$NON-NLS-1$
			String spaceName = tokens[0];
			String wiNumber = tokens[1];
			try {
				// We need to translate from the space's workitem number to the real id
				// The easiest way is to use a namedspaces request that we know will give
				// us a "ResourceMovedPermanently" error which will contain the URL of the
				// real location of the workitem which contains the workitem uuid.
				String query = "/namedspaces/" + userName + "/" + spaceName + "/workitems/" + wiNumber; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String wid = ""; //$NON-NLS-1$
				try {
				    wid = new OSIORestGetWID(client, query, taskRepository).run(monitor);
				} catch (OSIORestResourceMovedPermanentlyException e) {
					Header h = e.getHeader();
					HeaderElement[] elements = h.getElements();
					for (HeaderElement element : elements) {
						if ("Location".equals(element.getName())) { //$NON-NLS-1$
							int index = element.getValue().indexOf("workitem/"); //$NON-NLS-1$
							wid = element.getValue().substring(index + 9);
						}
					}
				}
				String workitemquery = "/workitems/" + wid; //$NON-NLS-1$
				TaskData taskData = new OSIORestGetSingleTaskData(client, connector, workitemquery, taskRepository)
						.run(monitor);
				Map<String, Space> spaces = getCachedSpaces(monitor);
				new OSIORestGetTaskComments(getClient(), spaces.get(spaceName),taskData).run(monitor);
				new OSIORestGetTaskCreator(getClient(), taskData).run(monitor);
				new OSIORestGetTaskLinks(getClient(), spaces.get(spaceName), taskData).run(monitor);
				setTaskAssignees(taskData);
				config.updateSpaceOptions(taskData);
				config.addValidOperations(taskData);
				collector.accept(taskData);
			} catch (RuntimeException | CoreException e) {
				// if the Throwable was warped in a RuntimeException in
				// OSIORestGetTaskData.JSonTaskDataDeserializer.deserialize()
				// we now remove the warper and throw a  OSIORestException
				Throwable cause = e.getCause();
				if (cause instanceof CoreException) {
					throw new OSIORestException(cause);
				}
			}
		}

	}
	
	private void setTaskAssignees(TaskData taskData) throws OSIORestException {
		TaskAttribute assigneeIDs = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().ASSIGNEE_IDS.getKey());
		List<String> ids = assigneeIDs.getValues();
		for (String id : ids) {
			new OSIORestGetTaskAssignee(getClient(), id, taskData).run(new NullOperationMonitor());
		}
	}

	private String formSearchUrl(String url) throws OSIORestException {
		String searchFilter = ""; //$NON-NLS-1$
		int index = url.indexOf("?");
		if (index < 0) {
			return searchFilter;
		}
		String query = url.substring(index + 1);
		String[] settings = query.split("&"); //$NON-NLS-1$
		
		Map<String, Set<String>> fieldMap = new HashMap<>();
		
		for (String setting : settings) {
			String[] tokens = setting.split("="); //$NON-NLS-1$
			String name = tokens[0];
			String value = tokens[1];
			
			Set<String> field = fieldMap.get(name);
			if (field == null) {
				field = new TreeSet<>();
				fieldMap.put(name, field);
			}
			
			field.add(value);
		}
		
		if (!fieldMap.isEmpty()) {
		searchFilter += "filter[expression]={\"$AND\":["; //$NON-NLS-1$
		String lastKey = null;
		String keySeparator = ""; //$NON-NLS-1$
		String itemSeparator = ""; //$NON-NLS-1$
		Map<String, Space> spaces = getCachedSpaces(new NullOperationMonitor());
		Set<String> allSpaceNames = new TreeSet<>(spaces.keySet());
		Space firstSpace = spaces.values().iterator().next();
		Map<String, WorkItemTypeData> workitemTypes = firstSpace.getWorkItemTypes();
		for (String key : fieldMap.keySet()) {
			if (!key.equals(lastKey)) {
				searchFilter += keySeparator + "{\"$OR\":["; //$NON-NLS-1$
				keySeparator = "]},"; //$NON-NLS-1$
				itemSeparator = ""; //$NON-NLS-1$
			}
			if ("space".equals(key)) { //$NON-NLS-1$
				Set<String> spaceSet = fieldMap.get(key);
				for (String name : spaceSet) {
					Space space = null;
					if (spaces != null) {
						space = spaces.get(name);
					}
					if (space != null) {
						searchFilter += itemSeparator + "{\"space\":\"" + space.getId() + "\"}"; //$NON-NLS-1$ //$NON-NLS-2$
						itemSeparator = ","; //$NON-NLS-1$
					}
				}
			} else if ("assignees".equals(key)) { //$NON-NLS-1$
				Set<String> userSet = fieldMap.get(key);
				Set<String> spaceNames = fieldMap.get("space") != null ? fieldMap.get("space") : allSpaceNames;
				for (String spaceName : spaceNames) { //$NON-NLS-1$
					Space space = cachedSpaces.get(spaceName);
					if (space != null) {
						Map<String, User> users = space.getUsers();
						for (String name : userSet) {
							User user = users.get(name);
							if (user != null) {
								searchFilter += itemSeparator + "{\"assignee\":\"" + user.getAttributes().getIdentityID() + "\"}"; //$NON-NLS-1$ //$NON-NLS-2$
								itemSeparator = ","; //$NON-NLS-1$
							}
						}
					}
				}
			} else if ("baseType".equals(key)) { //$NON-NLS-1$
				Set<String> workitemSet = fieldMap.get(key);
				for (String name : workitemSet) {
					WorkItemTypeData workitemType = null;
					if (workitemTypes != null) {
						workitemType = workitemTypes.get(name);
					}
					if (workitemType != null) {
						searchFilter += itemSeparator + "{\"workitemtype\":\"" + workitemType.getId() + "\"}"; //$NON-NLS-1$ //$NON-NLS-2$
						itemSeparator = ","; //$NON-NLS-1$
					}
				}
			} else if ("area".equals(key)) { //$NON-NLS-1$ 
				Set<String> areaSet = fieldMap.get(key);
				for (String spaceName : fieldMap.get("space")) { //$NON-NLS-1$
					Space space = cachedSpaces.get(spaceName);
					if (space != null) {
						Map<String, Area> areas = space.getAreas();
						for (String name : areaSet) {
							Area area = areas.get(name);
							if (area != null) {
								searchFilter += itemSeparator + "{\"area\":\"" + area.getId() + "\"}"; //$NON-NLS-1$ //$NON-NLS-2$
								itemSeparator = ","; //$NON-NLS-1$
							}
						}
					}
				}
			} else if ("iteration".equals(key)) { //$NON-NLS-1$ 
				Set<String> iterationSet = fieldMap.get(key);
				for (String spaceName : fieldMap.get("space")) { //$NON-NLS-1$
					Space space = cachedSpaces.get(spaceName);
					if (space != null) {
						Map<String, Iteration> iterations = space.getIterations();
						for (String name : iterationSet) {
							Iteration iteration = iterations.get(name);
							if (iteration != null) {
								searchFilter += itemSeparator + "{\"iteration\":\"" + iteration.getId() + "\"}"; //$NON-NLS-1$ //$NON-NLS-2$
								itemSeparator = ","; //$NON-NLS-1$
							}
						}
					}
				}
			} else if ("system.state".equals(key)) { //$NON-NLS-1$
				Set<String> stateSet = fieldMap.get(key);
				for (String name : stateSet) {
					searchFilter += itemSeparator + "{\"state\":\"" + name + "\"}"; //$NON-NLS-1$ //$NON-NLS-2$
					itemSeparator = ","; //$NON-NLS-1$
				}
			}
		}
		searchFilter += "]}]}"; //$NON-NLS-1$
		}
		
		searchFilter = URLQueryEncoder.transform(searchFilter);
		
		return location.getUrl() + "/search?" + searchFilter;
	}
	
	public IStatus performQuery(TaskRepository taskRepository, final IRepositoryQuery query,
			final TaskDataCollector resultCollector, IOperationMonitor monitor) throws OSIORestException {
		String queryUrl = query.getUrl();
		int index = queryUrl.indexOf("?"); //$NON-NLS-1$
		if (index > 0) {
			String queryParams = queryUrl.substring(index + 1);
			if (!queryParams.startsWith("filter")) { //$NON-NLS-1$
				queryUrl = formSearchUrl(queryUrl);
			}
		}
		String queryUrlSuffix = connector.getURLSuffix(queryUrl);
		try {
			List<TaskData> taskDataArray = new OSIORestGetTaskData(client, connector, queryUrlSuffix, taskRepository)
					.run(monitor);
			for (final TaskData taskData : taskDataArray) {
				taskData.setPartial(true);
				SafeRunner.run(new ISafeRunnable() {

					@Override
					public void run() throws Exception {
						resultCollector.accept(taskData);
					}

					@Override
					public void handleException(Throwable exception) {
						StatusHandler.log(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
								NLS.bind("Unexpected error during result collection. TaskID {0} in repository {1}", //$NON-NLS-1$
										taskData.getTaskId(), taskData.getRepositoryUrl()),
								exception));
					}
				});
			}
		} catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
					NLS.bind("Unexpected error during result collection in repository {0}", //$NON-NLS-1$
							taskRepository.getRepositoryUrl()),
					e));
		}

		return Status.OK_STATUS;
	}

}
