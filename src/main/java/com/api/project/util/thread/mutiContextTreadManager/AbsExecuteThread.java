package com.api.project.util.thread.mutiContextTreadManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.helper.SysHelper;

public abstract class AbsExecuteThread extends Thread{

	protected Logger log = LoggerFactory.getLogger(AbsExecuteThread.class);
	protected boolean run = true;
	protected boolean busy = false;
	protected List<AbsTractTask> taskList = null;
	protected String threadName=null;
	protected Map<String,Object> contextmap = new HashMap<>();
	private ReentrantLock lock = null;
	private Condition empty = null;

	protected void setTaskList(List<AbsTractTask> taskList, ReentrantLock lock, Condition empty){
		this.taskList = taskList;
		this.lock = lock;
		this.empty = empty;
	}
	
	protected void setContext(String key ,Object value){
		contextmap.put(key, value);
	}
	
	protected Object getContext(String key){
		return contextmap.get(key);
	}
	
	protected Map<String,Object> getContextMap(){
		return contextmap;
	}
	
	abstract public void init();
	
	abstract public void clean();
	
	@Override
	public void run(){
		threadName = this.getClass().getSimpleName()+"_IDLE";
		while(run){
			AbsTractTask task = null;
			try{

                lock.lock();
				try{
					if (taskList.size() == 0){
						empty.await();
						continue;
					}
					task = taskList.remove(0);
				}finally {
					lock.unlock();
				}
					
				this.setName(task.getTaskName());
				busy = true;
				task.execute(contextmap);
			} catch (Exception e){
				log.error("任务"+task);
				SysHelper.waitIt(this, 5000);
			} finally {
				busy = false;
				this.setName(threadName);
			}
		}
	}
	
	public void interrupt() {
		run = false;
		this.clean();
		SysHelper.notifyIt(this);
	}
	
	public boolean isBusy() {
		return busy;
	}
}
