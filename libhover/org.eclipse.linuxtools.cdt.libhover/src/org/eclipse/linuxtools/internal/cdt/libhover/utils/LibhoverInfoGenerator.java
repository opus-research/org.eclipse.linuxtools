package org.eclipse.linuxtools.internal.cdt.libhover.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.w3c.dom.Document;

public abstract class LibhoverInfoGenerator {
	
	protected Document document;
	
	public void generate(String outputFile){
		LibHoverInfo hoverInfo = doGenerate();
        save(hoverInfo, outputFile);
	}
	
	protected abstract LibHoverInfo doGenerate();
	
	protected void save(LibHoverInfo hoverInfo, String fileName){
        try (FileOutputStream f = new FileOutputStream(fileName);
                ObjectOutputStream out = new ObjectOutputStream(f)) {
            out.writeObject(hoverInfo);
        }catch(IOException ioException){
        	ioException.printStackTrace();
        }
	}
}
