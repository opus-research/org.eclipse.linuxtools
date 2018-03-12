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
package org.eclipse.linuxtools.internal.docker.core;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.spotify.docker.client.LogReader;
import com.spotify.docker.client.LogStream;

/**
 * This is a workaround for lack of HTTP Hijacking support in Apache
 * HTTPClient. The assumptions made in Apache HTTPClient are that a
 * response is an InputStream and so we have no sane way to access the
 * underlying OutputStream (which exists at the socket level)
 *
 * References :
 * https://docs.docker.com/reference/api/docker_remote_api_v1.16/#32-hijacking
 * https://github.com/docker/docker/issues/5933
 */
public class HttpHijackWorkaround {

	public static WritableByteChannel getOutputStream(LogStream stream, String uri) throws Exception {
		final String[] fields = new String[] { "reader", "stream", "original",
				"input", "in", "in", "wrappedStream", "in", "instream" };
		final String[] declared = new String[] {
				LogStream.class.getName(),
				LogReader.class.getName(),
				"org.glassfish.jersey.message.internal.ReaderInterceptorExecutor$UnCloseableInputStream",
				"org.glassfish.jersey.message.internal.EntityInputStream",
				FilterInputStream.class.getName(),
				FilterInputStream.class.getName(),
				"org.apache.http.conn.EofSensorInputStream",
				"org.apache.http.impl.io.IdentityInputStream",
				"org.apache.http.impl.io.SessionInputBufferImpl" };

		List<String[]> list = new LinkedList<>();
		for (int i = 0; i < fields.length; i++) {
			list.add(new String[] { declared[i], fields[i] });
		}

		if (uri.startsWith("unix:")) {
			list.add(new String[] { "sun.nio.ch.ChannelInputStream", "ch" });
		} else {
			list.add(new String[] { "java.net.SocketInputStream", "socket" });
		}

		Object res = getInternalField(stream, list);
		if (res instanceof WritableByteChannel) {
			return (WritableByteChannel) res;
		} else if (res instanceof Socket) {
			return Channels.newChannel(((Socket) res).getOutputStream());
		} else {
			return null;
		}
	}

	/*
	 * We could add API for this in com.spotify.docker.client since there is
	 * access to the underlying InputStream but better wait and see what
	 * happens with the HTTP Hijacking situation.
	 */
	public static InputStream getInputStream(LogStream stream) throws Exception {
		final String[] fields = new String[] { "reader", "stream" };
		final String[] declared = new String[] { LogStream.class.getName(), LogReader.class.getName()};

		List<String[]> list = new LinkedList<>();
		for (int i = 0; i < fields.length; i++) {
			list.add(new String[] { declared[i], fields[i] });
		}
		return (InputStream) getInternalField(stream, list);
	}

	/*
	 * Access arbitrarily nested internal fields.
	 */
	private static Object getInternalField (Object input, List<String []> set) {
		Object curr = input;
		try {
			for (String [] e : set) {
				Field f = loadClass(e[0]).getDeclaredField(e[1]);
				f.setAccessible(true);
				curr = f.get(curr);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return curr;
	}

	/*
	 * Avoid explicitly depending on certain classes that are requirements
	 * of the docker-client library (com.spotify.docker.client).
	 */
	private static Class<?> loadClass(String key) {
		Class<?> k;
		try {
			k = Class.forName(key);
		} catch (ClassNotFoundException e) {
			Bundle b = Platform.getBundle("com.spotify.docker.client");
			try {
				k = b.loadClass(key);
			} catch (ClassNotFoundException e1) {
				k  = null;
			}
		}
		return k;
	}

}
