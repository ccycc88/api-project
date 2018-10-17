package com.api.project.util.mq.obj.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientListner extends Thread {

	private Logger log = LoggerFactory.getLogger(ClientListner.class);
	private static ClientListner instance = new ClientListner();
	private ClientListner() {
		this.setDaemon(true);
	}
	public static ClientListner getInstance() {
		return instance;
	}
	
	private final List<ClientOperator> clients = new ArrayList<>();
	
	private SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
	private boolean start = false;
	public void run() {
		if(start) {
			log.warn("监听已经启动!");
			return;
		}
		
		start = true;
		this.setName(this.getClass().getSimpleName());
		
		try {
			Iterator<ClientOperator> iter = null;
			ClientOperator co = null;
			while(true) {
				try {
					Thread.sleep(60 * 1000);
				} catch (Exception e1) {
				}
				
				try {
					iter = clients.iterator();
					while(iter.hasNext()) {
						co = iter.next();
						if(co.getState().equals(Thread.State.TERMINATED)) {
							log.info(co.host + ":" + co.qNames + ":" + co.reqType + ": 客户端终止!");
							iter.remove();
						}
					}
					iter = null;
					co = null;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		}
	}
	
	void addClient(ClientOperator co){
		synchronized(clients) {
			clients.add(co);
		}
	}
	
	public String toString() {
		if(clients.size() == 0) {
			return "no clients connect!";
		}
		StringBuffer sb = new StringBuffer();
		sb.append("state\thost\tlastRequest\trequestType\tqueueName\n");
		synchronized(clients) {
			for(ClientOperator co : clients) {
				sb.append(co.getState().name()).append("\t")
					.append(co.host).append("\t")
					.append(format.format(new Date(co.lastRequest))).append("\t")
					.append(co.reqType).append("\t")
					.append(co.qNames).append("\n");
			}
		}
		return sb.toString();
	}
}
