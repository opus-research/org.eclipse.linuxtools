package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class AreaRelationships {
	
	private RelationGeneric space;
	
	private RelationGeneric parent;
	
	private RelationGeneric children;
	
	private RelationGeneric workitems;
	
	public RelationGeneric getSpace() {
		return space;
	}
	
	public RelationGeneric getParent() {
		return parent;
	}
	
	public RelationGeneric getChildren() {
		return children;
	}
	
	public RelationGeneric getWorkItems() {
		return workitems;
	}

}
