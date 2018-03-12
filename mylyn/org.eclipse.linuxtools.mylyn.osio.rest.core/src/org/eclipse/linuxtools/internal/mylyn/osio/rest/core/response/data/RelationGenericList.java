package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class RelationGenericList {
	
	private GenericData[] data;
	
	private GenericLinks links;
	
	private Object meta;
	
	public GenericData[] getData() {
		return data;
	}
	
	public GenericLinks getLinks() {
		return links;
	}
	
	public Object getMeta() {
		return meta;
	}

}
