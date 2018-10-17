package com.api.project.util.mq.obj.server;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.mq.obj.queue.QueueRequest;
import com.api.project.util.mq.obj.queue.QueueResponse;
import com.api.project.util.mq.obj.queue.RequestType;

public class ClientOperator extends Thread {

	private Logger log = LoggerFactory.getLogger(ClientOperator.class);
	private Socket socket = null;
	String host = null;
	private boolean run = true;
	private long lastReset = 0;

	public static int maxSendTimes = 3;

	public ClientOperator(Socket s) {
		this.socket = s;
		host = s.getInetAddress().getHostAddress();
		lastReset = System.currentTimeMillis();
		this.setName(this.getClass().getSimpleName() + "[" + host + "]");
	}

	Set<String> qNames = new HashSet<>();
	String reqType = "";
	long lastRequest = 0;

	public void run() {
		log.debug(host + ":处理线程启动");
		try {
			socket.setTcpNoDelay(true);
			// 0表示读数据时一直处在阻塞状态,如果设置成1000表示,1秒内读不到数据就异常退出
			socket.setSoTimeout(0);
			socket.setKeepAlive(true);
			ObjectOutputStream oos = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()));
			ObjectInputStream ois = new ObjectInputStream(
					new DataInputStream(new BufferedInputStream(socket.getInputStream())));

			while (run) {
				Object obj = null;
				try {
					if (System.currentTimeMillis() - lastReset > 10 * 1000) {
						lastReset = System.currentTimeMillis();
						oos.reset();
					}
					obj = ois.readObject();
					// log.debug("收到请求: " + obj);
				} catch (ClassNotFoundException e) {
					log.error(host + "收到不能反序列化的对象", e);
					oos.writeObject(new QueueResponse("收到不能反序列化的对象"));
					oos.flush();
					continue;
				} catch (Exception se) {
					log.debug(host + "连接异常退出:" + se.getMessage());
					if (se.getMessage() != null && se.getMessage().indexOf("serialVersionUID") >= 0) {
						log.error(se.getMessage(), se);
					}
					return;
				}

				lastRequest = System.currentTimeMillis();

				if (obj instanceof QueueRequest) {
					QueueRequest req = (QueueRequest) obj;
					RequestType type = req.getRequestType();
					this.reqType = type.toString();
					String qName = req.getQueueName();
					if (qName != null && !"null".equalsIgnoreCase(qName))
						qNames.add(qName);
					if (RequestType.regist.equals(type)) {
						oos.writeObject(new QueueResponse("注册成功"));
						oos.flush();
						log.debug(host + "来注册");
					} else if (RequestType.write.equals(type)) {
						Object data = req.getData();
						DataStore.getStore().put(qName, new DataContainer(req.getId(), req.getCorrelationId(), data));
						log.debug("[" + host + "]写入[" + qName + "]数据：" + data);
					} else if (RequestType.read.equals(type)) {
						DataContainer data = DataStore.getStore().get(qName, req.getCorrelationId());
						QueueResponse res = new QueueResponse();
						if (data != null) {
							res.setResponse(data.getData());
							res.setSendID(data.getMessageID());
							res.setCorrelationId(data.getCorrelationId());
						}
						try {
							oos.writeObject(res);
							oos.flush();
							if (data != null) {
								log.debug("[" + host + "]读取数据[" + qName + "]：" + data);
							}
						} catch (IOException e) {
							if (data != null) {
								if (data.getSendTimes() < maxSendTimes) {
									data.pp();
									DataStore.getStore().put(qName, data);
								} else {
									log.error("数据已经超过重发次数[" + maxSendTimes + "]，将要丢弃数据：" + data);
								}
							}
							throw e;
						}
					} else if (RequestType.close.equals(type)) {
						log.debug("[" + host + "]客户端请求退出");
						break;
					} else {
						oos.writeObject(new QueueResponse("请求类型错误"));
						oos.flush();
					}
				} else {
					log.debug("收到[" + host + "]不能处理的对象：" + obj);
					oos.writeObject(new QueueResponse("收到不能处理的对象：" + obj));
					oos.flush();
				}
			}
		} catch (IOException e) {
			log.error("[" + host + "]" + e.getMessage(), e);
			run = false;
		} finally {
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				log.debug("[" + host + "]客户端退出");
			} catch (IOException e1) {
			}
		}
	}
}
