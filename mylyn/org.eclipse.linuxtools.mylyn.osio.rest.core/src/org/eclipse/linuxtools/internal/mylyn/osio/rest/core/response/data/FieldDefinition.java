package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class FieldDefinition {
	
	private boolean required;
	
	private FieldType fieldType;
	
	private String label;
	
	private String description;
	
	public boolean isRequired() {
		return required;
	}
	
	public FieldType getFieldType() {
		return fieldType;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getDescription() {
		return description;
	}

}
