package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class WorkItemLinkRelationships {
	
	public RelationWorkItemLinkType link_type;
	
	public RelationWorkItem source;
	
	public RelationWorkItem target;
	
	public RelationWorkItemLinkType getLink_type() {
		return link_type;
	}
	
	public RelationWorkItem getSource() {
		return source;
	}
	
	public RelationWorkItem getTarget() {
		return target;
	}

}
