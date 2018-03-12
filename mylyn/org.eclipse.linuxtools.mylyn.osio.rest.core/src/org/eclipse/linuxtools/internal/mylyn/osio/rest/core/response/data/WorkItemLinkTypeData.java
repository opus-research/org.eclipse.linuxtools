package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class WorkItemLinkTypeData implements IdNamed {
	
	private String type;
	
	private String id;
	
	private WorkItemLinkTypeAttributes attributes;
	
	private WorkItemLinkTypeRelationships relationships;
	
	private GenericLinks links;
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		if (attributes.getTopology().equals("tree")) { //$NON-NLS-1$
			return attributes.getName() + "for Tree"; //$NON-NLS-1$
		}
		return attributes.getName();
	}
	
	public String getId() {
		return id;
	}
	
	public WorkItemLinkTypeAttributes getAttributes() {
		return attributes;
	}
	
	public WorkItemLinkTypeRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
