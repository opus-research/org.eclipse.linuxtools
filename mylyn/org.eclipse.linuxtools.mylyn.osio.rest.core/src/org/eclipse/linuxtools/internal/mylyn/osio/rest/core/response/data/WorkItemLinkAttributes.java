package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class WorkItemLinkAttributes {
	
	@SerializedName("created-at")
	private String created_at;

	@SerializedName("updated-at")
	private String updated_at;
	
	private int version;
	
	public String getCreated_at() {
		return created_at;
	}
	
	public String getUpdated_at() {
		return updated_at;
	}
	
	public Date getCreatedAt() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSSSSS"); //$NON-NLS-1$
		Date d = null;
		try {
			d = sdf.parse(created_at.replace("Z", "").replace("T", "-")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}
	
	public Date getUpdatedAt() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSSSSS"); //$NON-NLS-1$
		Date d = null;
		try {
			d = sdf.parse(updated_at.replace("Z", "").replace("T", "-")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;

	}
	
	public int getVersion() {
		return version;
	}

}
