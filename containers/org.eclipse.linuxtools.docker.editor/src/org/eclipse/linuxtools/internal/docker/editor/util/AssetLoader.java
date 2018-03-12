/*******************************************************************************
 * Copyright (c) 2015, 2017 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.docker.editor.Activator;

public class AssetLoader {

	private Map<String, String> additionalInfos = new HashMap<>();
	private static final String ADDITIONAL_INFO_PATH = "assets/additional-info";

	public AssetLoader() {
		additionalInfos = new HashMap<>();
	}

	public String getInfo(String instruction) {
		if (additionalInfos.containsKey(instruction))
			return additionalInfos.get(instruction);

		String additionalInfo = "";
		String targetFile = ADDITIONAL_INFO_PATH + "/" + instruction + ".html";
		try {
			additionalInfo = AssetLoader.loadAsset(targetFile);
		} catch (IOException e) {
			Activator.log(IStatus.WARNING, "Failed to load additional info file for instruction " + instruction, e);
		}

		additionalInfos.put(instruction, additionalInfo);
		return additionalInfo;
	}

	public static String loadAsset(String path) throws IOException {
		URL url = FileLocator.find(Activator.getDefault().getBundle(), new Path(path), null);
		if (url == null) {
			return null;
		} else {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try (InputStream input = url.openStream()) {

				byte[] buffer = new byte[4096];
				int len = 0;
				do {
					len = input.read(buffer, 0, buffer.length);
					if (len > 0)
						out.write(buffer, 0, len);
				} while (len >= 0);
			}
			return new String(out.toByteArray());
		}
	}
}
