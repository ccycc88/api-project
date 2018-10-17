package com.api.project.util.mq.obj.client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.mq.obj.queue.QueueRequest;
import com.api.project.util.mq.obj.queue.RequestType;

public class QueueFactory {

private static Logger log = LoggerFactory.getLogger(QueueFactory.class);
	
	private QueueFactory(String host,int port) {
		this.host = host;
		this.port = port;
	}
	private QueueFactory(String host,int port, String extra) {
		this.host = host;
		this.port = port;
		this.extra = extra;
	}
	private static Map<String,QueueFactory> factory = new ConcurrentHashMap<>();
	private String host = null;
	private int port = -1;
	private String extra = null;
	
	public static synchronized QueueFactory getInstance(String host,int port) {
		QueueFactory f = factory.get(host+port);
		if(f == null) {
			f = new  QueueFactory(host,port);
			factory.put(host+port,f);
		}
		return f;
	}
	public static synchronized QueueFactory getInstance(String host,int port, String extra) {
		QueueFactory f = null;
		if(extra != null){
			f = factory.get(host+port + extra);
			if(f == null) {
				f = new  QueueFactory(host,port, extra);
				factory.put(host+port + extra,f);
			}
		}else{
		
			f = factory.get(host+port);
			if(f == null) {
				f = new  QueueFactory(host,port, null);
				factory.put(host+port,f);
			}
		}
		return f;
	}
	
	public QueueClient connect() throws IOException, ClassNotFoundException{
		Socket socket = new Socket();
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		String localIP = null;
		int localPort = -1;
		try {
			socket.setTcpNoDelay(true);
			socket.setSoTimeout(30000); //60秒读不到数据,异常退出
			socket.setKeepAlive(false);
			socket.connect(new InetSocketAddress(host, port));
			oos = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()));
			ois = new ObjectInputStream(new DataInputStream(new BufferedInputStream(socket.getInputStream())));
			
			localIP = socket.getInetAddress().getHostAddress();
			localPort = socket.getLocalPort();
			
			//发送注册消息
			log.debug("开始发送注册消息");
			oos.writeObject(new QueueRequest(null,RequestType.regist));
			oos.flush();
			log.debug("开始接收反馈消息");
			Object res = ois.readObject();
			log.debug("收到反馈消息：" + res);
		} catch (SocketException e) {
			if(socket != null && socket.isConnected()) {
				try {
					socket.shutdownInput();
					socket.shutdownOutput();
				} catch (Exception e1) {
				}
			}
			throw e;
		} catch (IOException e) {
			if(socket != null) {
				socket.shutdownInput();
				socket.shutdownOutput();
			}
			throw e;
		} catch (ClassNotFoundException e) {
			if(socket != null) {
				socket.shutdownInput();
				socket.shutdownOutput();
			}
			throw e;
		}catch(Exception e) {
			log.error(e.getMessage(), e);
			if(socket != null) {
				socket.shutdownInput();
				socket.shutdownOutput();
			}
			return null;
		}
		
		return new QueueClient(oos,ois,localIP,localPort);
	}
	
	private Map<String,MessageQueue> cache = new ConcurrentHashMap<>();
	private Map<String,QueueProxy> proxys = new ConcurrentHashMap<>();
	
	public synchronized MessageQueue createMessageQueue(String queue) throws UnknownHostException, IOException {
		if(queue == null || queue.trim().length() == 0) return null;
		MessageQueue mq = cache.get(queue);
		if(mq == null) {
			mq = new MessageQueue(queue);
			
			QueueProxy proxy = proxys.get(host+port);
			if(proxy == null) {
				proxy = new QueueProxy(host,port, extra);
				proxys.put(host+port, proxy);
			}
			
			mq.setProxy(proxy);
			cache.put(queue, mq);
		}
		return mq;
	}
	
	
	public synchronized MessageQueue newMessageQueue(String queue) throws UnknownHostException, IOException {
		MessageQueue mq = new MessageQueue(queue);
		QueueProxy proxy = new QueueProxy(host,port, extra);
		mq.setProxy(proxy);
		return mq;
	}
}
