package com.api.project.util.mq.obj.server;

import java.util.Date;

public class DataContainer {

private String messageID = null;
	
	private String correlationId = null;
	
	private Object data = null;
	
	//重发次数
	private int sendTimes = 0;
	
	private Date timestamps = new Date();
	
	public int getSendTimes() {
		return sendTimes;
	}
	public Date getTimestamps() {
		return timestamps;
	}
	/**
	 * sendTimes plusplus
	 */
	public void pp(){
		sendTimes++;
	}
	
	public DataContainer(String id,String cId,Object data) {
		this.setCorrelationId(cId);
		this.setData(data);
		this.setMessageID(id);
	}
	
	public String getMessageID() {
		return messageID;
	}

	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	@Override
	public int hashCode() {
		if(this.getCorrelationId() == null) return super.hashCode();
		return this.getCorrelationId().hashCode();
	}
	
	@Override
	public boolean equals(Object p) {
		if(p == null) return false;
		if(p instanceof DataContainer) {
			DataContainer c = (DataContainer)p;
			if(this.getCorrelationId() == null) return false;
			return this.getCorrelationId().equals(c.getCorrelationId());
		}
		return false;
	}
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append("messageID:").append(this.getMessageID())
		  .append(";correlationId:").append(this.getCorrelationId())
		  .append(";data:").append(this.getData())
		  .append(";sendTimes:").append(sendTimes)
		  .append(";timestamps:").append(this.getTimestamps());
		return  sb.toString();
	}
}
