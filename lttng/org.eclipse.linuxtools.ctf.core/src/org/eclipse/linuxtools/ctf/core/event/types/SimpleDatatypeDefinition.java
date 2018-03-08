package org.eclipse.linuxtools.ctf.core.event.types;

public abstract class SimpleDatatypeDefinition extends Definition {

    public SimpleDatatypeDefinition(IDefinitionScope definitionScope,
            String fieldName) {
        super(definitionScope, fieldName);
    }

    public Long getIntegerValue(){
        return null;
    }

    public String getStringValue(){
        return null;
    }

}
