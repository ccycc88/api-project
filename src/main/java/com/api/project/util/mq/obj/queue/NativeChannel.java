package com.api.project.util.mq.obj.queue;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeChannel<T> {

	private Logger log = LoggerFactory.getLogger(NativeChannel.class);
	private List<T> store = Collections.synchronizedList(new LinkedList<T>());
	private String channelName = null;
	public NativeChannel(String chnlName) {
		this.channelName = chnlName;
	}
	
	private long lastVisitTime = System.currentTimeMillis();
	
	private int maxLength = 30000;
	public synchronized void add(T obj) {
		lastVisitTime = System.currentTimeMillis();
		if(store.size() >= maxLength) {
			log.error("["+channelName+"]达到上限［"+maxLength+"］，将要删除旧数据:" + store.remove(store.size() - 1));
		}
		store.add(obj);
	}
	
	public synchronized T get() {
		lastVisitTime = System.currentTimeMillis();
		if(store.size() == 0) {
			return null;
		}
		return store.remove(0);
	}
	
	public synchronized int indexOf(Object o) {
		return store.indexOf(o);
	}
	
	public synchronized T get(int index) {
		lastVisitTime = System.currentTimeMillis();
		if (index < 0) {
			return null;
		}else {
			return store.remove(index);
		}
	}
	
	public synchronized int size() {
		return store.size();
	}
	
	public synchronized void clear() {
		store.clear();
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	public long getLastVisitTime() {
		return lastVisitTime;
	}
}
