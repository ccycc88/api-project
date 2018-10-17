package com.api.project.util.timeJob.crt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.timeJob.Job;
import com.api.project.util.timeJob.JobGroup;

public class TimerService {

private static TimerService  timeService=null;
	
	private TimerService(){
	}
	
	public static synchronized TimerService getInstance(){
		if(timeService==null){
			timeService = new TimerService();
		}
		return timeService;
		
	}
	private Logger log = LoggerFactory.getLogger(TimerService.class);
	
	// 待执行任务列表
	private List<Job> wait2RunJobList = new ArrayList<>();
	// 工人线程列表
	private List<TimerWorker> workerList = new ArrayList<>();
	// 工人线程列表
	private Map<String,JobGroup> JobGroupMap = new HashMap<>();

	public void startService() {
		
		boolean start = true;
		if(!start){
			return;
		}
		// 启动监控线程
		new TimerMonitor(wait2RunJobList, workerList, JobGroupMap).start();
		
		// 启动可执行线程
		for (int i=0; i<250; i++) {
			TimerWorker tw = new TimerWorker(wait2RunJobList);
			tw.start();
			workerList.add(tw);
		}
		
		// 启动备用线程
		for (int i=0; i<50; i++) {
			TimerWorker tw = new TimerWorker(wait2RunJobList);
			tw.setRunType(TimerWorker.SUSPEND);
			tw.start();
			workerList.add(tw);
		}
	}
	
	public void putJob(Job job) {
		boolean start = true;
		if(!start){
			return;
		}
		synchronized(JobGroupMap){
			log.debug("定时器准备添加了一个任务");
			String groupname = job.getJobGroupName();
			if(!JobGroupMap.containsKey(groupname)){
				JobGroup jobGroup = new JobGroup();
				jobGroup.setGroupName(groupname);
				JobGroupMap.put(groupname, jobGroup);
			}
			JobGroupMap.get(groupname).putJob(job);
			log.debug("定时器添加了一个任务");
		}
		
	}
	
	public JobGroup getJobGroup(String groupName){
		return JobGroupMap.get(groupName);
	}
}
