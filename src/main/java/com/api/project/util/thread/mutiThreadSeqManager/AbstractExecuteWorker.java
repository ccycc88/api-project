package com.api.project.util.thread.mutiThreadSeqManager;

public abstract class AbstractExecuteWorker {

	private String taskName = Thread.currentThread().getName();
	public abstract Object execute(Object in);
	public void setTaskName(String taskName){
		this.taskName = taskName;
	}
	public String  getTaskName(){
		return taskName;
	}
}
