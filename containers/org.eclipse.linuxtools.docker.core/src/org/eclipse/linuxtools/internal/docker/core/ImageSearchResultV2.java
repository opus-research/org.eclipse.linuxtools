package org.eclipse.linuxtools.internal.docker.core;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.spotify.docker.client.messages.ImageSearchResult;

/**
 * Binding for Image Search Results from a Registry V2.
 * 
 * Currently the returned image search results lack the same degree of
 * information returned from the V1 registries.
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ImageSearchResultV2 {

	@JsonProperty("repositories") //$NON-NLS-1$
	private List<String> repositories;

	public List<ImageSearchResult> getRepositories() {
		List<ImageSearchResult> result = new ArrayList<>();
		for (String repo : repositories) {
			result.add(new ImageResultV2(repo));
		}
		return result;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("results", getRepositories()).toString(); //$NON-NLS-1$
	}

	private class ImageResultV2 extends ImageSearchResult {

		private String name;

		public ImageResultV2(String name) {
			this.name = name;
		}

		@Override
		public String getDescription() {
			return "";
		}

		@Override
		public boolean isOfficial() {
			return false;
		}

		@Override
		public boolean isAutomated() {
			return false;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public int getStarCount() {
			return 0;
		}
	}

}
