package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

import com.google.gson.annotations.SerializedName;

public class SpaceRelationships {
	
	@SerializedName("owned-by")
	private SpaceOwnedBy owned_by;

	private RelationGeneric iterations;
	
	private RelationGeneric areas;
	
	private RelationGeneric workitemlinktypes;
	
	private RelationGeneric workitemtypes;
	
	private RelationGeneric workitems;
	
	private RelationGeneric codebases;
	
	private RelationGeneric collaborators;
	
	private RelationGeneric labels;
	
	public SpaceOwnedBy getOwnedBy() {
		return owned_by;
	}
	
	public RelationGeneric getIterations() {
		return iterations;
	}
	
	public RelationGeneric getWorkItemLinkTypes() {
		return workitemlinktypes;
	}
	
	public RelationGeneric getWorkItemTypes() {
		return workitemtypes;
	}
	
	public RelationGeneric getWorkItems() {
		return workitems;
	}
	
	public RelationGeneric getCodebases() {
		return codebases;
	}
	
	public RelationGeneric getCollaborators() {
		return collaborators;
	}
	
	public RelationGeneric getLabels() {
		return labels;
	}
	
}
