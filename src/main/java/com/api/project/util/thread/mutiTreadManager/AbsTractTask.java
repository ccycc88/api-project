package com.api.project.util.thread.mutiTreadManager;

public abstract class AbsTractTask {

	private String taskName = Thread.currentThread().getName();
	public abstract void execute();
	public void setTaskName(String taskName){
		this.taskName = taskName;
	}
	public String  getTaskName(){
		return taskName;
	}
}
