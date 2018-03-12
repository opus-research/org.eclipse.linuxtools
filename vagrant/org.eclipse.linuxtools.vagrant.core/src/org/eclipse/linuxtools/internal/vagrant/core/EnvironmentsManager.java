/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.vagrant.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A class for managing the environments of various vagrant boxes.
 * 
 * The implementation will use a vagrantfile's folder as part of the key for
 * storing the environment, so if a vagrantfile can launch multiple VMs, they
 * will share an environment.
 *
 */
public class EnvironmentsManager {
	private static EnvironmentsManager singleton = new EnvironmentsManager();

	public static EnvironmentsManager getSingleton() {
		return singleton;
	}

	private EnvironmentsManager() {
		getEnvironment(); // pre-load from preferences
	}

	private Environment env;

	private synchronized Environment getEnvironment() {
		if (env == null)
			env = new Environment();
		return env;
	}

	private class Environment {
		private final String key = "org.eclipse.linuxtools.internal.vagrant.core.EnvironmentsManager.PREFERENCE_KEY";
		private Map<String, Map<String, String>> backing;

		public Environment() {
			load();
		}

		private void load() {
			IEclipsePreferences prefs2 = InstanceScope.INSTANCE
					.getNode(Activator.PLUGIN_ID);
			try {
				byte[] read = prefs2.getByteArray(key, (byte[]) null);
				if (read == null) {
					backing = new HashMap<String, Map<String, String>>();
					return;
				}
				ByteArrayInputStream bis = new ByteArrayInputStream(read);
				ObjectInputStream ois = new ObjectInputStream(bis);
				backing = (Map) ois.readObject();
				ois.close();
				bis.close();
			} catch (IOException ioe) {
				// TODO log error
				ioe.printStackTrace();
				return;
			} catch (ClassNotFoundException cnfe) {
				// TODO log error
				cnfe.printStackTrace();
				return;
			}
		}

		private void save() {
			IEclipsePreferences prefs = InstanceScope.INSTANCE
					.getNode(Activator.PLUGIN_ID);
			try {
				byte[] toSave = null;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(backing);
				oos.close();
				bos.close();
				toSave = bos.toByteArray();
				prefs.putByteArray(key, toSave);
				prefs.flush();
			} catch (IOException ioe) {
				// TODO log
				ioe.printStackTrace();
			} catch (BackingStoreException bse) {
				// TODO log
				bse.printStackTrace();
			}
		}

		public void setEnvironmentForFolder(File folder,
				Map<String, String> env) {
			if (backing != null) {
				backing.put(folder.getAbsolutePath(), env);
			}
		}

		public Map<String, String> getEnvironmentForFolder(File folder) {
			if (backing != null) {
				return backing.get(folder.getAbsolutePath());
			}
			return null;
		}
	}

	public Map<String, String> getEnvironment(File folder) {
		return getEnvironment().getEnvironmentForFolder(folder);
	}

	public void setEnvironment(File folder, Map<String, String> environment) {
		Environment env = getEnvironment();
		env.setEnvironmentForFolder(folder, environment);
		env.save();
	}

	// Convert a string/string hashmap into an array of string environment
	// variables as required by java.lang.Runtime
	public static String[] convertEnvironment(Map<String, String> env) {
		if (env == null || env.size() == 0)
			return null;

		// Create a new map based on pre-existing environment
		Map<String, String> original = new HashMap<String, String>(
				System.getenv());

		// Add new environment on top of existing
		Iterator<String> additonal = env.keySet().iterator();
		String k;
		while (additonal.hasNext()) {
			k = additonal.next();
			original.put(k, env.get(k));
		}

		// Convert the combined map into a form that can be used to launch
		// process
		ArrayList<String> ret = new ArrayList<>();
		Iterator<String> it = original.keySet().iterator();
		String working = null;
		while (it.hasNext()) {
			working = it.next();
			ret.add(working + "=" + original.get(working));
		}
		return ret.toArray(new String[ret.size()]);
	}

}
