package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class Iteration implements IdNamed {
	
	private String type;
	
	private String id;
	
	private IterationAttributes attributes;
	
	private IterationRelationships relationships;
	
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
	
	public IterationAttributes getAttributes() {
		return attributes;
	}
	
	public IterationRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
