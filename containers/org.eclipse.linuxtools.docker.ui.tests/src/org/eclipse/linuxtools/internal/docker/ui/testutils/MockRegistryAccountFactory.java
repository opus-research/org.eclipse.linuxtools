/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.testutils;

import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.mockito.Mockito;

/**
 * Utility class to get mocked instances of {@link RegistryAccount}
 */
public class MockRegistryAccountFactory {

	public static MockRegistryAccountBuilder url(final String url) {
		return new MockRegistryAccountBuilder().url(url);
	}

	public static class MockRegistryAccountBuilder {

		private String url;

		public MockRegistryAccountBuilder url(String url) {
			this.url = url;
			return this;
		}

		public IRegistryAccount build() {
			final IRegistryAccount mockRegistryAccount = Mockito.mock(IRegistryAccount.class);
			Mockito.when(mockRegistryAccount.getServerAddress()).thenReturn(url);
			return mockRegistryAccount;
		}

	}

}
