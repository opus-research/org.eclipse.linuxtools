package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class WorkItemRelationships {
	
	private RelationGenericList assignees;
	
	private RelationGeneric creator;
	
	private RelationBaseType baseType;
	
	private RelationGeneric comments;
	
	private RelationGeneric iteration;
	
	private RelationGeneric area;
	
	private RelationGeneric children;
	
	private RelationSpaces space;
	
	public RelationGenericList getAssignees() {
		return assignees;
	}
	
	public RelationGeneric getCreator() {
		return creator;
	}
	
	public RelationBaseType getBaseType() {
		return baseType;
	}
	
	public RelationGeneric getComments() {
		return comments;
	}
	
	public RelationGeneric getIteration() {
		return iteration;
	}
	
	public RelationGeneric getArea() {
		return area;
	}
	
	public RelationGeneric getChildren() {
		return children;
	}
	
	public RelationSpaces getSpace() {
		return space;
	}
	
}
