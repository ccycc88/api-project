package com.api.project.util.mq.obj.client;

import com.api.project.util.mq.obj.exception.ServerConnectionException;
import com.api.project.util.mq.obj.queue.QueueRequest;
import com.api.project.util.mq.obj.queue.QueueResponse;

public class MessageQueue {

	private String queueName = null;
	private QueueProxy proxy = null;

	MessageQueue(String qName) {
		this.queueName = qName;
	}

	public Object read() throws ServerConnectionException, ClassNotFoundException {
		return proxy.read(queueName);
	}

	public void write(Object obj) throws ServerConnectionException {
		proxy.write(obj, queueName);
	}

	public QueueResponse sendRequest(QueueRequest req) throws ServerConnectionException, ClassNotFoundException {
		req.setQueueName(queueName);
		return proxy.sendRequest(req);
	}

	public QueueResponse sendRequest(QueueRequest req, long timeout)
			throws ServerConnectionException, ClassNotFoundException {
		req.setQueueName(queueName);
		QueueResponse res = proxy.sendRequest(req);
		long start = System.currentTimeMillis();
		while (res.getResponse() == null) {
			if (System.currentTimeMillis() - start > timeout)
				break;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
			res = proxy.sendRequest(req);
		}
		return res;
	}

	public void close() {
		proxy.close();
	}

	public void setResetPeriod(int period) {
		this.proxy.setResetPeriod(period);
	}

	public QueueProxy getProxy() {
		return proxy;
	}

	public void setProxy(QueueProxy proxy) {
		this.proxy = proxy;
	}
}
