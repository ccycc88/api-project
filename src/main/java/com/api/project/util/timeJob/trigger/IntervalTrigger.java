package com.api.project.util.timeJob.trigger;

public class IntervalTrigger implements ITrigger{

	private long interval;
	public IntervalTrigger(long interval){
		this.interval=interval;
	}
	public Boolean isTimeOn(long lastTime){
		if(interval<=0){
			return true;
		}
		if(System.currentTimeMillis()-lastTime>interval){
			return true;
		}
		return false;
	}
}
