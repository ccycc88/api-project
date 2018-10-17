package com.api.project.util.thread.mutiTreadManager;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.helper.SysHelper;

public class ExecuteThread extends Thread{

	private Logger log = LoggerFactory.getLogger(ExecuteThread.class);
	private boolean run = true;
	private boolean busy = false;
	private List<AbsTractTask> taskList = null;
	private String threadName=null;
	private ReentrantLock lock = null;
	private Condition empty = null;
	//暂停标志
	private boolean halt = false;

	public ExecuteThread(List<AbsTractTask> taskList, ReentrantLock lock, Condition empty){
		this.taskList = taskList;
		this.empty = empty;
		this.lock = lock;
	}

	@Override
	public void run(){
		threadName = this.getClass().getSimpleName()+"_IDLE";
		while(run){

			if(halt == true){
				SysHelper.waitIt(this, 1000);
				continue;
			}
			AbsTractTask task = null;
			try{

				lock.lock();
				try {

					if(taskList.size() == 0) {

						empty.await();
						continue;
					}
					task = taskList.remove(0);
				} finally {
					// TODO: handle finally clause
					lock.unlock();
				}
//				synchronized (taskList) {
//					if (taskList.size() > 0)
//						task = taskList.remove(0);
//				}
//				if(task == null) {
//					SysHelper.waitIt(this, 1000);
//					continue;
//				}

				this.setName(task.getTaskName());
				busy = true;
				task.execute();
			} catch (Exception e){
				e.printStackTrace();
				log.error("任务"+task,e);
				SysHelper.waitIt(this, 5000);
			} finally {
				busy = false;
				this.setName(threadName);
			}
		}
	}

	public void interrupt() {
		run = false;
		SysHelper.notifyIt(this);
	}

	public boolean isBusy() {
		return busy;
	}

	public void halt(){
		halt = true;
	}

	public void go_on(){
		halt = false;
	}
}
