package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class RelationWorkItemLinkCategoryData {
	
	private String type;
	
	private String id;
	
	private WorkItemLinkCategoryAttributes attributes;
	
	private GenericLinks links;
	
	public String getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	
	public WorkItemLinkCategoryAttributes getAttributes() {
		return attributes;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
