/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.structures;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IGobblerListener;



public class LoggingStreamDaemon implements IGobblerListener {
	
	/**
	 * Sets up an output to log to.
	 */
	public LoggingStreamDaemon() {
		output = new StringBuilder();
		try {
			outputFile = File.createTempFile(this.toString(), ".tmp");
			writer = new FileWriter(outputFile, true);
		} catch(IOException ioe) {
			outputFile = null;
		}
		saveLog = false;
	}
	
	/**
	 * Pushes output to log.
	 */
	protected void pushData() {
		if(null != writer) {
			try {
				writer.write(output.toString());
				output.delete(0, output.length());
				writer.flush();
			} catch(IOException ioe) {}
		}
	}

	/**
	 * Outputs one line.
	 */
	public void handleDataEvent(String line) {
		output.append(line);
		this.pushData();
	}

	/**
	 * Reads in and returns the output produced.
	 * 
	 * @return The logged data.
	 */
	public String getOutput() {
		if(null == outputFile)
			return null;
		try {
			if(output.length() > 0) pushData();
			FileReader reader = new FileReader(outputFile);
			char[] buffer = new char[BUFFER_SIZE];
			int count;
			StringBuilder builder = new StringBuilder();
			while(-1 != (count = reader.read(buffer))) 
				builder.append(buffer, 0, count);
			reader.close();
			return builder.toString();
		} catch(IOException ioe) {}
		return null;
	}
	
	/**
	 * Saves the logfile.
	 * 
	 * @param file The file to save the log data to.
	 * 
	 * @return True if the save was successful.
	 */
	public boolean saveLog(File file) {
		try {
			if(!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileReader r = new FileReader(outputFile);
			FileWriter w = new FileWriter(file, true);
			char[] buffer = new char[BUFFER_SIZE];
			int count;
			while(-1 != (count = r.read(buffer))) 
				w.write(new String(buffer, 0, count));
			w.flush();
			writer.close();
			writer = w;
			r.close();
			outputFile.delete();
			outputFile = file;
			saveLog = true;
		} catch(IOException ioe) {
			return false;
		}
		return true;
	}
	
	public void dispose() {
		if(null != outputFile && !saveLog)
			outputFile.delete();
		outputFile = null;
		if(null != writer) {
			try {
				writer.close();
			} catch(IOException ioe) {}
		}
		writer = null;
	}
	
	protected StringBuilder output;
	protected File outputFile;
	protected FileWriter writer;
	private boolean saveLog;

	private static final int BUFFER_SIZE = 1024;
}
