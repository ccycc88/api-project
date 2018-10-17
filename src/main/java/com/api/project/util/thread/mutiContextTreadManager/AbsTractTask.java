package com.api.project.util.thread.mutiContextTreadManager;

import java.util.Map;

public abstract class AbsTractTask {

	private String taskName = Thread.currentThread().getName();
	public abstract void execute(Map<String,Object> contextmap);
	public void setTaskName(String taskName){
		this.taskName = taskName;
	}
	public String  getTaskName(){
		return taskName;
	}
}
