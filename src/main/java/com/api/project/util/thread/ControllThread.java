package com.api.project.util.thread;

import java.util.Map;

import com.api.project.util.pool.ThreadPool;

public abstract class ControllThread implements IControllable{

	private Thread t = null;
	private String name = null;
	private int priority = Thread.NORM_PRIORITY;

	@SuppressWarnings("unchecked")
	public void doHeartBeat(Class c) {
		if (t != null)
			ThreadMonitor.getInstance().updateHeartBeatTime(t, c);
	}

	public synchronized void start() {
		ThreadPool.getInstance().execute(this);
	}

	public void setName(String name) {
		this.name = name;
		if (t != null)
			t.setName(name);
	}

	public String getName() {
		if (t != null)
			return t.getName();
		return name;
	}

	final public void run() {
		t = Thread.currentThread();
		if (name != null)
			t.setName(name);
		t.setPriority(priority);
		ThreadMonitor.getInstance().regThread(t, this.getClass());

		try {
			dispose();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void interrupt() {
		if (t != null)
			t.interrupt();
	}

	public void setStart(boolean start) {

	}

	public boolean isStart() {
		return true;
	}

	@SuppressWarnings("unchecked")
	public void setParas(Map paras) {
	}

	@SuppressWarnings("unchecked")
	public void setCommonParas(Map commonParas) {
	}

	public void setDescription(String description) {
	}
	public void setPriority(int priority){
		this.priority = priority;
	}
	public int getPriority(){
		return this.priority;
	}
}
