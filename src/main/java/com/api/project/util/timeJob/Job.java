package com.api.project.util.timeJob;

import com.api.project.util.timeJob.trigger.ITrigger;
import com.api.project.util.timeJob.trigger.IntervalTrigger;

public abstract class Job {

	protected JobContext jobContext = new JobContext();
	protected ITrigger trigger = new IntervalTrigger(60*1000);
	//任务在任务容器超时时间 4个小时
	protected  long timeOut=4*60*60*1000;
	//任务执行是否超时
	protected  boolean isTimeOut=false;
	//任务上一次被调度时间
	protected  long lastUpdateTime;
	//内部状态，由外部查看是否应该被删除
	protected  boolean isActive=true;
	//0代表空闲 1代表待执行
	protected  int status;
	
	protected  String jobGroupName;
	
	private  long newJobTime;
	
	//执行方法
	protected abstract void execute();
	
	public Job(){
		newJobTime = System.currentTimeMillis();
	}
	
	public  void  executeUpdateTime(){	
		try{
			execute();
		}finally{
			//状态设置为空闲
			lastUpdateTime = System.currentTimeMillis();
		}
		
	}
	
	//得到运行上下文
	public  JobContext getJobContext(){
		return jobContext;
	}
	//是否到达触发时间
	public  boolean isTimeOn(){
		return trigger.isTimeOn(lastUpdateTime);
	}
	
	//得到上次被调度时间
	public  long getLastUpdateTime(){
		return lastUpdateTime;
	}
	//设置是否活动状态
	public void setNoActive(){
		this.isActive=false;
	}
	//得到活动状态
	public  boolean  getIsActive(){
		return this.isActive;
	}
	//设置超时
	public  void setIsTimeOut(boolean isTimeOut){
		this.isTimeOut=isTimeOut;
	}
	//得到是否超时状态
	public  boolean  getIsTimeOut(){
		return this.isTimeOut;
	}
	//设置trigger
	public void setTrigger(ITrigger  trigger){
		this.trigger = trigger;
	}
	
	public void setJobGroupName(String jobGroupName){
		this.jobGroupName=jobGroupName;
	}
	
	public  String getJobGroupName(){
		return jobGroupName;
	}

	/**
	 * @return the status
	 */
	public  int getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public  void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @return the timeOut
	 */
	public long getTimeOut() {
		return timeOut;
	}

	/**
	 * @param timeOut the timeOut to set
	 */
	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * @return the newJobTime
	 */
	public long getNewJobTime() {
		return newJobTime;
	}

	/**
	 * @param newJobTime the newJobTime to set
	 */
	public void setNewJobTime(long newJobTime) {
		this.newJobTime = newJobTime;
	}
}
