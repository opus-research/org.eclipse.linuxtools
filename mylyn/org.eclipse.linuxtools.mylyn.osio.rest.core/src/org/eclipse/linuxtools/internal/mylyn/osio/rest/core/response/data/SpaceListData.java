package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class SpaceListData implements IdNamed {
	
	private SpaceAttributes attributes;
	
	private String id;
	
	private GenericLinksForSpace spaceLinks;
	
	private SpaceRelationships relationships;
	
	private PagingLinks pagingLinks;
	
	private SpaceListMeta spaceListMeta;
	
	public String getId() {
		return id; 
	}
	
	public String getName() {
		return attributes.getName();
	}
	
	public GenericLinksForSpace getSpaceLinks() {
		return spaceLinks;
	}
	
	public SpaceRelationships getRelationships() {
		return relationships;
	}
	
	public PagingLinks getPagingLinks() {
		return pagingLinks;
	}
	
	public SpaceListMeta getSpaceListMeta() {
		return spaceListMeta;
	}

}
