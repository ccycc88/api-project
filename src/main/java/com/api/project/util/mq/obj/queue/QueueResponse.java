package com.api.project.util.mq.obj.queue;

import java.io.Serializable;

public class QueueResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Object response = null;
	private String sendID = null;
	private String correlationId = null;
	public QueueResponse() {
		this(null,null);
	}
	
	public QueueResponse(String id,Object res) {
		this.setSendID(id);
		this.setResponse(res);
	}
	
	public QueueResponse(Object res) {
		this(null,res);
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}
	
	public String getSendID() {
		return sendID;
	}

	public void setSendID(String sendID) {
		this.sendID = sendID;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append("sendID:").append(sendID)
		  .append(";correlationId:").append(correlationId)
		  .append(";response:").append(response);
		return  sb.toString();
	}
}
