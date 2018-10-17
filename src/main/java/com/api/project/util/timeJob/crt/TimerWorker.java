package com.api.project.util.timeJob.crt;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.StringUtil;
import com.api.project.util.helper.SysHelper;
import com.api.project.util.thread.ControllThread;
import com.api.project.util.timeJob.Job;

public class TimerWorker extends ControllThread{

	private Logger log = LoggerFactory.getLogger(TimerWorker.class);
	// 待执行任务列表
	private List<Job> jobList = null;
	
	final static public int SUSPEND = 0;
	final static public int RUN = 1;
	final static public int END = 2;
	private volatile int runType = RUN;
	private String threadName = this.getName();
	
	private long heartbeatTime = 0;
	private Job job = null;
	
	public TimerWorker(List<Job> jobList) {
		this.jobList = jobList;
	}
	
	public void dispose() {
		threadName ="Detect-idle";
		this.setName(threadName);
		while (runType==SUSPEND || runType==RUN) {
			doHeartBeat(this.getClass());
			job=null;
			heartbeatTime = System.currentTimeMillis();
			// 暂停
			if (runType == SUSPEND) {
				SysHelper.waitIt(this, 1000);
				continue;
			}
			
			// 执行
			synchronized (jobList) {
				if (jobList.size() > 0) {
					job = jobList.remove(0);
				}
			}
			
			if (job != null) {
				try{
					log.debug("执行任务"+job.getJobGroupName());
					super.setName("Detecting("+job.getJobGroupName()+")");
					job.executeUpdateTime();	
				}catch(Exception e){
					log.error(StringUtil.createStackTrace(e));
					e.printStackTrace();			
				}finally{
					    this.setName(threadName);
						synchronized(this){
							heartbeatTime = System.currentTimeMillis();
							//状态设置为空闲
							job.setStatus(0);
							job.setIsTimeOut(false);
						}
						
				}
				
			}
			SysHelper.waitIt(this, 300);
		}
	}
	
	public void setRunType(int type) {
		runType = type;
	}

	public  int getRunType() {
		return runType;
	}
	
	public  long getHeartbeatTime() {
		return heartbeatTime;
	}
	
	public Job getJob() {
		return job;
	}
}
