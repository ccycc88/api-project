package com.api.project.util.mq.obj.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.mq.obj.queue.QueueRequest;
import com.api.project.util.mq.obj.queue.QueueResponse;
import com.api.project.util.mq.obj.queue.RandomUtil;
import com.api.project.util.mq.obj.queue.RequestType;

public final class QueueClient {

	private static Logger log = LoggerFactory.getLogger(QueueClient.class);
	private ObjectOutputStream oos = null;
	private ObjectInputStream ois = null;
	
	private String host = null;
	private int port = -1;
	
	private long period = 100;
	
	QueueClient (ObjectOutputStream oos, ObjectInputStream ois,String host,int port) {
		this.oos = oos;
		this.ois = ois;
		this.host = host;
		this.port = port;
	}
	/**
	 * 读数据
	 * Object
	 * @param qName
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * Jun 18, 2012
	 */
	Object readData(String qName) throws IOException, ClassNotFoundException {
		oos.writeObject(new QueueRequest(qName,RequestType.read));
		oos.flush();
		reset();
		return ((QueueResponse)ois.readObject()).getResponse();
	}
	
	void writeData(String qName,Object obj) throws IOException {
		QueueRequest request = new QueueRequest(qName,RequestType.write);
		request.setData(obj);
		oos.writeObject(request);
		oos.flush();
		reset();
		log.debug("发送数据到通道["+qName+"]：" + obj);
	}
	QueueResponse sendRequest(QueueRequest req) throws IOException, ClassNotFoundException {
		if(req == null) return new QueueResponse("请求对象为空");
		
		RequestType type = req.getRequestType();
		if(type == null) {
			return new QueueResponse("请求对象类型为空");
		}
		
		req.setId(createRequestID());
		
		oos.writeObject(req);
		oos.flush();
		reset();
		if(type.equals(RequestType.write) || type.equals(RequestType.close)) {
			return new QueueResponse("发送成功"); 
		}else {
			return (QueueResponse)ois.readObject();
		}
	}
	
	void close() {
		try {
			oos.writeObject(new QueueRequest(null,RequestType.close));
			oos.flush();
			shutdown();
		} catch (IOException e) {
		}
	}
	
	void shutdown() {
		if(oos != null) {
			try {
				oos.close();
			} catch (IOException e) {
			}
		}
		if(ois != null) {
			try {
				ois.close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * 必须大于0
	 * @param period
	 */
	void setResetPeriod(int period) {
		if(period <= 0) period = 5000;
		this.period = period;
	}
	
	private int times = 0;
	private void reset() throws IOException {
		times++;
		if(times == period) {
			times = 0;
			oos.flush();
			oos.reset();
		}
	}
	
	private String createRequestID() {
		String local = host + ":" + port + ":";
		String random = RandomUtil.getRandomNormalString(32 - local.length());
		return local + random;
	}
}
