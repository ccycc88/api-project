package com.api.project.util.mq.obj.queue;

import java.io.Serializable;

public class QueueRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id = null;
	
	private String correlationId = null;
	
	private RequestType requestType = null;
	
	//队列名称
	private String queueName = null;
	
	private Object data = null;
	
	public QueueRequest(RequestType type) {
		this(null,type);
	}
	
	public QueueRequest(String qName,RequestType type) {
		this.queueName = qName;
		this.requestType = type;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public void setRequestType(RequestType type) {
		this.requestType = type;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("queueType:")
			.append(requestType)
			.append(";id:")
			.append(id)
			.append(";correlationId:")
			.append(correlationId)
			.append(";queueName:")
			.append(queueName)
			.append(";data:")
			.append(data);
		return sb.toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}
}
