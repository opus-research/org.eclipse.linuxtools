/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc. and others.
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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

/**
 * A class for managing the environments of various vagrant boxes.
 * 
 * The implementation will use secure storage to save the environments for all
 * environments. It will keep all environments stored in one property key,
 * serialized, and load / store via a byte array.
 * 
 * While it's definitely possible that some environments do not need to be
 * secured, it seems prudent in this case that the possibility that credentials
 * will be passed through the environment make it more than reasonable to secure
 * all environments.
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
		private static final String ENVIRONMENT_ATTRIBUTE = "org.eclipse.linuxtools.internal.vagrant.core.EnvironmentsManager.EnvironmentKey"; //$NON-NLS-1$
		private static final String SECURE_PREFERNCES_BASEKEY = Activator.PLUGIN_ID;
		private static final String ENVIRONMENT_KEY = "environment"; //$NON-NLS-1$

		private static final boolean READ = true;
		private static final boolean WRITE = false;

		private Map<String, Map<String, String>> backing;

		public Environment() {
			load();
		}

		private ISecurePreferences getPreferenceNode() {
			ISecurePreferences root = SecurePreferencesFactory.getDefault();
			ISecurePreferences node = root.node(SECURE_PREFERNCES_BASEKEY);
			return node.node(ENVIRONMENT_KEY);
		}

		@SuppressWarnings("unchecked")
		private void load() {
			ISecurePreferences prefs2 = getPreferenceNode();
			try {
				byte[] read = prefs2.getByteArray(ENVIRONMENT_ATTRIBUTE,
						(byte[]) null);
				backing = new HashMap<>();
				if (read == null) {
					return;
				}
				ByteArrayInputStream bis = new ByteArrayInputStream(read);
				ObjectInputStream ois = new ObjectInputStream(bis);
				backing = (Map<String, Map<String, String>>) ois.readObject();
				ois.close();
				bis.close();
			} catch (IOException ioe) {
				Activator.getDefault().getLog().log(createStatus(ioe, READ));
			} catch (ClassNotFoundException cnfe) {
				Activator.getDefault().getLog().log(createStatus(cnfe, READ));
			} catch (StorageException se) {
				Activator.getDefault().getLog().log(createStatus(se, READ));
			}
		}

		private void save() {
			if (backing == null) {
				backing = new HashMap<>();
			}
			ISecurePreferences prefs = getPreferenceNode();
			try {
				byte[] toSave = null;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(backing);
				oos.close();
				bos.close();
				toSave = bos.toByteArray();
				prefs.putByteArray(ENVIRONMENT_ATTRIBUTE, toSave, true);
				prefs.flush();
			} catch (IOException ioe) {
				Activator.getDefault().getLog().log(createStatus(ioe, WRITE));
			} catch (StorageException se) {
				Activator.getDefault().getLog().log(createStatus(se, WRITE));
			}
		}

		private IStatus createStatus(Throwable t, boolean action) {
			// TODO externalize?
			if (action == READ)
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						Messages.EnvironmentsManager_error_read, t);
			else
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						Messages.EnvironmentsManager_error_write, t);
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


	/*
	 * Convert a string/string hashmap into an array of string environment
	 * variables as required by java.lang.Runtime This will super-impose the
	 * provided environment variables ON TOP OF the existing environment in
	 * eclipse, as users may not know *all* environment variables that need to
	 * be set, or to do so may be tedious.
	 */
	public static String[] convertEnvironment(final Map<String, String> env) {
		// Create a new map based on pre-existing environment of Eclipse
		final Map<String, String> environment = System.getenv();
		Map<String, String> result = new HashMap<>(environment);
		// Add all new environments on top of existing
		if (env != null) {
			result.putAll(env);
		}
		// also update the PATH to include the path to the Vagrant executable
		final StringBuilder path = new StringBuilder();
		final String newEnvPath = path.append(environment.get("PATH")) //$NON-NLS-1$
				.append(File.pathSeparator)
				.append(VagrantConnection.getUserDefinedVagrantPath())
				.toString();
		result.put("PATH", newEnvPath); //$NON-NLS-1$
		// Convert the combined map into a form that can be used to launch
		// process
		ArrayList<String> ret = new ArrayList<>();
		Iterator<String> it = result.keySet().iterator();
		String working = null;
		while (it.hasNext()) {
			working = it.next();
			ret.add(working + "=" + result.get(working)); //$NON-NLS-1$
		}
		return ret.toArray(new String[ret.size()]);
	}

}
