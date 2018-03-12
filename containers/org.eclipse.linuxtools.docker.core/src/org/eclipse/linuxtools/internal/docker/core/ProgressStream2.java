package org.eclipse.linuxtools.internal.docker.core;

import static com.google.common.io.ByteStreams.copy;
import static com.google.common.io.ByteStreams.nullOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.DockerTimeoutException;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ProgressMessage;

class ProgressStream2 implements Closeable {

	private static final Logger log = LoggerFactory
			.getLogger(ProgressStream2.class);
	private final InputStream stream;
	private final MappingIterator<ProgressMessage> iterator;

	private volatile boolean closed;

	ProgressStream2(final InputStream stream) throws IOException {
		this.stream = stream;
		final JsonParser parser = ObjectMapperProvider2.objectMapper()
				.getFactory()
				.createParser(stream);
		iterator = ObjectMapperProvider2.objectMapper().readValues(parser,
				ProgressMessage.class);
	}

	public boolean hasNextMessage(final String method, final URI uri)
			throws DockerException {
		try {
			return iterator.hasNextValue();
		} catch (SocketTimeoutException e) {
			throw new DockerTimeoutException(method, uri, e);
		} catch (IOException e) {
			throw new DockerException(e);
		}
	}

	public ProgressMessage nextMessage(final String method, final URI uri)
			throws DockerException {
		try {
			return iterator.nextValue();
		} catch (SocketTimeoutException e) {
			throw new DockerTimeoutException(method, uri, e);
		} catch (IOException e) {
			throw new DockerException(e);
		}
	}

	public void tail(ProgressHandler handler, final String method,
			final URI uri) throws DockerException {
		while (hasNextMessage(method, uri)) {
			handler.progress(nextMessage(method, uri));
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (!closed) {
			log.warn(this + " not closed properly");
			close();
		}
	}

	@Override
	public void close() throws IOException {
		closed = true;
		// Jersey will close the stream and release the connection after we read
		// all the data.
		// We cannot call the stream's close method because it an instance of
		// UncloseableInputStream,
		// where close is a no-op.
		copy(stream, nullOutputStream());
	}
}
