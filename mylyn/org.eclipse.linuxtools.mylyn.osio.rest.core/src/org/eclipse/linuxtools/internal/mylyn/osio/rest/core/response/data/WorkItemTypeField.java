package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class WorkItemTypeField {
	
	private String description;
	
	private String label;
	
	private Boolean required;
	
	private WorkItemTypeFieldType type;
	
	public String getDescription() {
		return description;
	}
	
	public String getLabel() {
		return label;
	}
	
	public Boolean getRequired() {
		return required;
	}
	
	public WorkItemTypeFieldType getType() {
		return type;
	}

}