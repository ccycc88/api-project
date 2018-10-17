package com.api.project.util.dispatcher;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class TaskQueue {

	private byte[] lock = new byte[1];
	private Hashtable<String,List<Object>> procMap = new Hashtable<String,List<Object>>();
	private Hashtable<String,List<Object>> waitMap = new Hashtable<String,List<Object>>();
	
	public void addTask(String key, Object o) {
		synchronized (lock) {
			List<Object> queue = procMap.get(key);
			if (queue == null)
				queue = waitMap.get(key);
			if (queue == null) {
				queue = new ArrayList<>();
				waitMap.put(key, queue);
			}
			queue.add(o);
		}
	}
	
	public Object takeTask(String key) {
		synchronized (lock) {
			List<Object> queue = procMap.get(key);
			if (queue==null || queue.size()<=0)
				return null;
			return queue.remove(0);
		}
	}
	
	public String tryLock() {
		synchronized (lock) {
			if (waitMap.size() <= 0)
				return null;
			
			String keys[] = waitMap.keySet().toArray(new String[waitMap.size()]);
			if (keys!=null && keys.length>0) {
				List<Object> queue = waitMap.remove(keys[0]);
				procMap.put(keys[0], queue);
				return keys[0];
			}
			keys = null;
			return null;
		}
	}
	
	public void removeKey(String key) {
		synchronized (lock) {
			List<Object> queue = procMap.remove(key);
			if (queue != null) {
				if (queue.size() > 0)
					waitMap.put(key, queue);
				else {
					queue.clear();
					queue = null;
				}
			}
		}
	}
	
	public int size() {
		synchronized (lock) {
			return waitMap.size()+procMap.size();
		}
	}
	
	public int size(String key) {
		synchronized (lock) {
			List<Object> queue = procMap.get(key);
			if (queue == null)
				queue = waitMap.get(key);
			if (queue == null)
				return 0;
			return queue.size();
		}
	}
	
	public String showAll() {
		synchronized (lock) {
			StringBuffer procLog = new StringBuffer();
			Enumeration<String> e = procMap.keys();
			String key = null;
			while (e.hasMoreElements()) {
				key = e.nextElement();
				procLog.append(key).append("=").append(procMap.get(key).size()).append(";");
			}
			
			e = null;
			StringBuffer waitLog = new StringBuffer();
			e = waitMap.keys();
			while (e.hasMoreElements()) {
				key = e.nextElement();
				waitLog.append(key).append("=").append(waitMap.get(key).size()).append(";");
			}
			e = null;
			
			try {
				if (procLog.length()<=0 && waitLog.length()<=0)
					return null;
	
				return (procLog.length()>0?" PROC:"+procLog.toString():"")+
					(waitLog.length()>0?" WAIT:"+waitLog.toString():"");
			}finally{
				procLog = null;
				waitLog = null;
			}
		}
	}
}
