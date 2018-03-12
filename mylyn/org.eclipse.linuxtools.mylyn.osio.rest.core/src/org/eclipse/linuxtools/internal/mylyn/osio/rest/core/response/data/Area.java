package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class Area implements IdNamed {
	
	private String type;
	
	private String id;
	
	private AreaAttributes attributes;
	
	private AreaRelationships relationships;
	
	private GenericLinks links;
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return attributes.getName();
	}
	
	public String getId() {
		return id;
	}
	
	public AreaAttributes getAttributes() {
		return attributes;
	}
	
	public AreaRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
