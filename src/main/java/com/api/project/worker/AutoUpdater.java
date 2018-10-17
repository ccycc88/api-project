package com.api.project.worker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.api.project.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.common.cache.factory.CacheFactory;

/**
 * 系统缓存 自动更新的
 * @author v-chenyangchao-os
 *
 */
public class AutoUpdater {

	private static byte[] lock = new byte[1];
	
	private static AutoUpdater automaticUpdate = null;
	
	private Map<String, TimerTask> tasks = new HashMap<String, TimerTask>();
	
	private ScheduledExecutorService pool = null;
	
	private Logger logger = LoggerFactory.getLogger(AutoUpdater.class);
	
	public static AutoUpdater getInstance(){
		
		if(automaticUpdate == null){
			
			synchronized (lock) {
				
				if(automaticUpdate == null){
					
					 automaticUpdate = new AutoUpdater();
				}
			}
		}
		return automaticUpdate;
	}
	private AutoUpdater(){

	}
	//手动更新
	public void refresh(){
		
		Map<String, CacheFactory> map = CacheFactory.getTotalFactory();
		for(CacheFactory factory : map.values()){
			
			factory.refresh();
		}
	}
	public void automaticUpdate(){
		
		Map<String, CacheFactory> taskFactory = CacheFactory.getTaskFactory();
		
		int size = taskFactory.size();

		logger.info("需要刷新的缓存个数[" + size + "]");
		if(size <= 0){
			return;
		}
		size = ((size/5 == 0) ? 1 : size/5);
		pool = Executors.newScheduledThreadPool(size);
		
		Iterator<Entry<String, CacheFactory>> it =
				taskFactory.entrySet().iterator();
		while(it.hasNext()){
			
			Entry<String, CacheFactory> en = it.next();
			
			Long delay = en.getValue().getDelay();
			String taskName = en.getKey();
			if(delay == null){
				
				logger.error("任务[" + taskName + "],周期任务将不再调度");
				continue;
			}
			UpdateTask task = new UpdateTask(en.getValue());
			
			synchronized (tasks) {
			
				tasks.put(en.getValue().getName(), task);
			}
			//首次执行延时5秒
			pool.scheduleAtFixedRate(task, 5, delay, TimeUnit.SECONDS);
		}
		
	}
	public void addUpdateTask(CacheFactory factory){
		
		if(pool != null){
			
			Long delay = factory.getDelay();
			String taskName = factory.getName();
			if(delay == null){
				
				logger.error("任务[" + taskName + "],周期任务将不再调度");
				return;
			}
            UpdateTask task = new UpdateTask(factory);
			synchronized (tasks) {
			
				tasks.put(factory.getName(), task);
			}
			//首次执行不再延时
			pool.scheduleAtFixedRate(task, 0, delay, TimeUnit.SECONDS);
		}
	}
	public void deleteTask(Class<?> clz){
		
		TimerTask task = null;
		synchronized (tasks) {
			
			task = tasks.remove(clz.getSimpleName());
		}
		
		//取消任务调度
		if(task != null){
			task.cancel();
		}
	}
	private class UpdateTask extends TimerTask{

		private CacheFactory factory = null;
		
		public UpdateTask(CacheFactory factory){
		
			this.factory = factory;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			logger.debug("更新[" + factory.getName() + "]的缓存信息");
			try {
				factory.refresh();
			} catch (Exception e) {
				logger.error("任务执行异常[" + StringUtil.createStackTrace(e));
			}
		}
		
	}
}
