package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class WorkItemLinkData implements Named {
	
	private String type;
	
	private WorkItemLinkAttributes attributes;
	
	private WorkItemLinkRelationships relationships;
	
	private GenericLinks links;
	
	private String id;
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return type;
	}
	
	public WorkItemLinkAttributes getAttributes() {
		return attributes;
	}
	
	public WorkItemLinkRelationships getRelationships() {
		return relationships;
	}
	
	public String getId() {
		return id;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
