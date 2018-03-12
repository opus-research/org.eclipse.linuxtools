/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

import java.util.Map;
import java.util.stream.Collectors;

public class Space implements IdNamed {

	private String id;
	
	private String type;
	
	private SpaceAttributes attributes;
	
	private SpaceRelationships relationships;
	
	private GenericLinksForSpace links;
	
	private Map<String, WorkItemTypeData> workItemTypes;
	
	private Map<String, IdNamed> workItemTypesIdNamed;
	
	private Map<String, WorkItemLinkTypeData> workItemLinkTypes;
	
	private Map<String, Area> areas;
	
	private Map<String, IdNamed> areasIdNamed;
	
	private Map<String, Iteration> iterations;
	
	private Map<String, IdNamed> iterationsIdNamed;
	
	private Map<String, User> users;
	
	private Map<String, IdNamed> usersIdNamed;
		
	public String getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}
	
	public SpaceAttributes getAttributes() {
		return attributes;
	}
	
	public String getName() {
		return attributes.getName();
	}
	
	public SpaceRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinksForSpace getLinks() {
		return links;
	}

	public void setWorkItemTypes(Map<String, WorkItemTypeData> workItemTypes) {
		this.workItemTypes = workItemTypes;
		this.workItemTypesIdNamed = workItemTypes.entrySet().stream()
	     .collect(Collectors.toMap(Map.Entry::getKey, e -> (IdNamed)e.getValue()));
	}
	
	public Map<String, WorkItemTypeData> getWorkItemTypes() {
		return workItemTypes;
	}
	
	public void setWorkItemLinkTypes(Map<String, WorkItemLinkTypeData> workItemLinkTypes) {
		this.workItemLinkTypes = workItemLinkTypes;
	}
	
	public Map<String, WorkItemLinkTypeData> getWorkItemLinkTypes() {
		return workItemLinkTypes;
	}
	
 	public void setUsers(Map<String, User> users) {
		this.users = users;
		this.usersIdNamed = users.entrySet().stream()
			     .collect(Collectors.toMap(Map.Entry::getKey, e -> (IdNamed)e.getValue()));
	}
 	
	public Map<String, User> getUsers() {
		return users;
	}
	
 	public void setAreas(Map<String, Area> areas) {
		this.areas = areas;
		this.areasIdNamed = areas.entrySet().stream()
			     .collect(Collectors.toMap(Map.Entry::getKey, e -> (IdNamed)e.getValue()));
	}
	
	public Map<String, Area> getAreas() {
		return areas;
	}
	
	public void setIterations(Map<String, Iteration> iterations) {
		this.iterations = iterations;
		this.iterationsIdNamed = iterations.entrySet().stream()
			     .collect(Collectors.toMap(Map.Entry::getKey, e -> (IdNamed)e.getValue()));
	}
	
	public Map<String, Iteration> getIterations() {
		return iterations;
	}
	
	public Map<String, IdNamed> getMapFor(String member) {
		if ("area".equals(member)) {
			return areasIdNamed;
		} else if ("baseType".equals(member)) {
			return workItemTypesIdNamed;
		} else if ("iteration".equals(member)) {
			return iterationsIdNamed;
		} else if ("assignees".equals(member)) {
			return usersIdNamed;
		}
		return null;
	}
	
}
