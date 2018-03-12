package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.io.InputStreamReader;
import java.lang.reflect.Type;

import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public class OSIORestGetWorkItem extends OSIORestGetRequest<OSIORestWorkItem>{

	public OSIORestGetWorkItem(CommonHttpClient client, String id) {
		super(client, "/workitems/" + id, null); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected OSIORestWorkItem parseFromJson(InputStreamReader in) {
		TypeToken<OSIORestWorkItem> type = new TypeToken<OSIORestWorkItem>() {
		};
		return new GsonBuilder().registerTypeAdapter(type.getType(), new JSonTaskDataDeserializer())
				.create()
				.fromJson(in, type.getType());
	}

	private class JSonTaskDataDeserializer implements JsonDeserializer<OSIORestWorkItem> {


		@Override
		public OSIORestWorkItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			OSIORestWorkItem response = new OSIORestWorkItem();
			
			JsonObject data = ((JsonObject)json.getAsJsonObject().get("data")); //$NON-NLS-1$
			String id = data.get("id").getAsString();
			response.setId(id);
			
			JsonObject attributes = (JsonObject)data.get("attributes"); //$NON-NLS-1$
			
			String title = attributes.get("system.title").getAsString(); //$NON-NLS-1$
			response.setTitle(title);
			
			String description = attributes.get("system.description.rendered").getAsString(); //$NON-NLS-1$
			response.setDescription(description);
			
			String number = attributes.get("system.number").getAsString(); //$NON-NLS-1$
			response.setNumber(number);
			
			return response;
		}
	}
}
