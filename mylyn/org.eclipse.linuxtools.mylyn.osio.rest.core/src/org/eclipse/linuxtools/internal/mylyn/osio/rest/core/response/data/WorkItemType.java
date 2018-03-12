package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class WorkItemType implements Named {
	
	private WorkItemTypeData workItemTypeData;
	
	private PagingLinks pagingLinks;
	
	private WorkItemTypeListMeta workItemTypeListMeta;
	
	public WorkItemTypeData getWorkItemTypeData() {
		return workItemTypeData;
	}
	
	public String getName() {
		return workItemTypeData.getName();		
	}
	
	public PagingLinks getPagingLinks() {
		return pagingLinks;
	}
	
	public WorkItemTypeListMeta getWorkItemTypeListMeta() {
		return workItemTypeListMeta;
	}

}
