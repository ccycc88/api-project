package com.api.project.util.thread.mutiContextTreadManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ExecuteContextThreadManager {

	private List<AbsExecuteThread> threadList = new ArrayList<>();
	private List<AbsTractTask> taskList = new ArrayList<>();
	private long createtime = System.currentTimeMillis();
	private ReentrantLock lock = null;
	private Condition empty = lock.newCondition();

	public long getCreatetime() {
		return createtime;
	}

	private int MAX_THREAD_NUM = 5;

	public ExecuteContextThreadManager(int max_thread_num){
		this.MAX_THREAD_NUM = max_thread_num;
	}

	public void setThread(AbsExecuteThread thread) throws Exception{

		synchronized (threadList){

			if(MAX_THREAD_NUM > threadList.size()){
				thread.setTaskList(taskList, lock, empty);
				thread.start();
				threadList.add(thread);
			}
		}
	}

	public void executeTask(AbsTractTask task){

		lock.lock();
		try{

			taskList.add(task);
			empty.signalAll();
		}finally {
			lock.unlock();
		}
	}

	public boolean taskIsFinish() {
		if (taskList.size() > 0)
			return false;

		for (AbsExecuteThread thread: threadList){
			if (thread.isBusy())
				return false;
		}
		return true;
	}

	public int getTaskSize() {

		lock.lock();
		try{

			return this.taskList.size();
		}finally {
			lock.unlock();
		}
	}

	public int getThreadSize() {
		return threadList.size();
	}

	public void clear() {
		synchronized(threadList){
			if (threadList != null) {
				for (AbsExecuteThread thread: threadList)
					thread.interrupt();
				threadList.clear();
			}
		}
		lock.lock();
		try{
			if (taskList != null)
				taskList.clear();
		}finally {
			lock.unlock();
		}
	}
}
