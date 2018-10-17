package com.api.project.util.quartz;
import java.util.Iterator;
import java.util.Map;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzManager {

	private static SchedulerFactory sf = new StdSchedulerFactory();
	private static final String JOB_GROUP_NAME = "DEFAULT_JOB_GROUP";
	private static final String TRIGGER_GROUP_NAME = "DEFAULT_TRIGGER_GROUP";
	/**
	 * 添加任务
	 * @param name
	 * @param clz
	 * @param cron
	 * @param context
	 * @throws Exception 
	 */
	public static void addJob(String name, String clz, 
			String cron, Map<String, Object> context) throws Exception{
		
		JobDetail detail = null;
		Scheduler scheduler = null;
		try {
			
			scheduler = sf.getScheduler();
			Class<Job> c = (Class<Job>) Class.forName(clz);
			detail = JobBuilder.newJob(c).withIdentity(name, JOB_GROUP_NAME).build();
			
			if(context != null && context.size() != 0){
				
				JobDataMap jobData = detail.getJobDataMap();
				Iterator<Map.Entry<String, Object>> it = 
					              context.entrySet().iterator();
				
				while(it.hasNext()){
					
					Map.Entry<String, Object> en = it.next();
					jobData.put(en.getKey(), en.getValue());
				}
			}
			//触发器
			CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
			CronTrigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(name, TRIGGER_GROUP_NAME).withSchedule(cronScheduleBuilder).build();
			scheduler.scheduleJob(detail, trigger);
			//启动调度器
			if(!scheduler.isShutdown()){
				scheduler.start();
			}
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}
	}
	/**
	 * 添加任务
	 * @param name
	 * @param job
	 * @param cron
	 * @param context
	 * @throws Exception 
	 */
	public static void addJob(String name, Job job, 
			String cron, Map<String, Object> context) throws Exception{
		
		JobDetail detail = null;
		Scheduler scheduler = null;
		try {
			
			scheduler = sf.getScheduler();
			detail = JobBuilder.newJob(job.getClass()).withIdentity(name, JOB_GROUP_NAME).build();
			
			if(context != null && context.size() != 0){
				
				JobDataMap jobData = detail.getJobDataMap();
				Iterator<Map.Entry<String, Object>> it = 
					              context.entrySet().iterator();
				
				while(it.hasNext()){
					
					Map.Entry<String, Object> en = it.next();
					jobData.put(en.getKey(), en.getValue());
				}
			}
			//触发器
			CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
			CronTrigger trigger = TriggerBuilder.newTrigger()
					.withIdentity(name, TRIGGER_GROUP_NAME).withSchedule(cronScheduleBuilder).build();
			scheduler.scheduleJob(detail, trigger);
			//启动调度器
			if(!scheduler.isShutdown()){
				scheduler.start();
			}
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}
	}
	/**
	 * 添加任务
	 * @param name
	 * @param job
	 * @param cron
	 * @param context
	 * @param job_group_name
	 * @param trigger_group_name
	 * @throws Exception 
	 */
	public static void addJob(String name, Job job, 
			String cron, Map<String, Object> context, 
			String job_group_name, String trigger_group_name) throws Exception{
		
		JobDetail detail = null;
		Scheduler scheduler = null;
		try {
			
			scheduler = sf.getScheduler();
			detail = JobBuilder.newJob(job.getClass()).withIdentity(name, job_group_name).build();
			
			JobDataMap jobData = detail.getJobDataMap();
			if(context != null && context.size() != 0){
				
				Iterator<Map.Entry<String, Object>> it = 
					              context.entrySet().iterator();
				while(it.hasNext()){
					
					Map.Entry<String, Object> en = it.next();
					jobData.put(en.getKey(), en.getValue());
				}
			}
			//触发器
			CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
			CronTrigger trigger = TriggerBuilder
					.newTrigger().withIdentity(name, trigger_group_name).withSchedule(cronScheduleBuilder).build();
			scheduler.scheduleJob(detail, trigger);
			//启动调度器
			if(!scheduler.isShutdown()){
				scheduler.start();
			}
		} catch (Exception e) {
			// TODO: handle exception
			throw e;
		}
	}
	/**
	 * 删除任务
	 * @param name
	 * @throws SchedulerException 
	 */
	public static void removeJob(String name) throws SchedulerException{
		
		Scheduler scheduler = null;
		try {
			scheduler = sf.getScheduler();
			scheduler.pauseTrigger(new TriggerKey(name, TRIGGER_GROUP_NAME));
			scheduler.unscheduleJob(new TriggerKey(name, TRIGGER_GROUP_NAME));
			scheduler.deleteJob(new JobKey(name, JOB_GROUP_NAME));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			throw e;
		}
		
	}
	/**
	 * 删除任务
	 * @param name
	 * @param trigger_group_name
	 * @param job_group_name
	 * @throws SchedulerException 
	 */
    public static void removeJob(String name, 
    		String trigger_group_name, String job_group_name) throws SchedulerException{
		
		Scheduler scheduler = null;
		try {
			scheduler = sf.getScheduler();
			scheduler.pauseTrigger(new TriggerKey(name, trigger_group_name));
			scheduler.unscheduleJob(new TriggerKey(name, trigger_group_name));
			scheduler.deleteJob(new JobKey(name, job_group_name));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
    /**
     * 修改任务
     * @param name
     * @param cron
     * @param context
     * @throws Exception 
     */
    public static void modifyJobTime(String name, String cron, Map<String, Object> context) throws Exception{
    	
    	Scheduler scheduler = null;
    	try {
			
    		scheduler = sf.getScheduler();
    		TriggerKey key = new TriggerKey(name, TRIGGER_GROUP_NAME);
    		CronTrigger trigger = (CronTrigger) scheduler.getTrigger(key);
    		if(trigger == null){
    			return;
    		}
    		JobDetail detail = scheduler.getJobDetail(new JobKey(name, JOB_GROUP_NAME));
    		Class<?> clz = detail.getJobClass();
    		removeJob(name);
    		addJob(name,clz.getName(), cron, context);
		} catch (Exception e) {
			throw e;
		}
    }
    /**
     * 修改任务
     * @param name
     * @param cron
     * @param trigger_group_name
     * @throws Exception 
     */
    public static void modifyJobTime(String name, String cron, String trigger_group_name) throws Exception{
    	
    	Scheduler scheduler = null;
    	try {
			
    		scheduler = sf.getScheduler();
    		TriggerKey key = new TriggerKey(name, TRIGGER_GROUP_NAME);
    		CronTrigger trigger = (CronTrigger) scheduler.getTrigger(key);
    		if(trigger == null){
    			return;
    		}
    		CronTrigger cronTrigger = (CronTrigger) trigger;
    		CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing();
    		trigger = trigger.getTriggerBuilder().withIdentity(key).withSchedule(cronScheduleBuilder).build();
    		scheduler.rescheduleJob(key, trigger);
		} catch (Exception e) {
			throw e;
		}
    }
}
