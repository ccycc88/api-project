package com.api.project.util.thread.mutiContextTreadManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadContextManagerFactory {

	private static Map<String,ThreadContextManagerFactory> threadManagerFactoryMap = new HashMap<>();
	private List<ExecuteContextThreadManager> list = new ArrayList<>();
	private ThreadContextManagerFactory(){	
	}
	
	public static ThreadContextManagerFactory getInstance(String managerFactoryName){
		if(threadManagerFactoryMap.get(managerFactoryName)==null){

			synchronized(threadManagerFactoryMap){

				if(threadManagerFactoryMap.get(managerFactoryName)==null){

					ThreadContextManagerFactory threadManagerFactory = new ThreadContextManagerFactory();
					threadManagerFactoryMap.put(managerFactoryName, threadManagerFactory);
				}
			}
		}
		return threadManagerFactoryMap.get(managerFactoryName);
	}
	public ExecuteContextThreadManager initThreadManager(int maxThreadNum){
		ExecuteContextThreadManager manager = new ExecuteContextThreadManager(maxThreadNum);
		synchronized(list){
			list.add(manager);
		}
		return manager;
	}
	public ExecuteContextThreadManager[] getThreadManager(){

		synchronized(list){
			return list.toArray(new ExecuteContextThreadManager[0]);
		}
	}
	
	public void clearThreadManager(ExecuteContextThreadManager manager){
		synchronized(list){
			list.remove(manager);
		}
	}
	public void clear(long savetime){
		synchronized(list){
			for(ExecuteContextThreadManager executeThreadManager:list){
				if(System.currentTimeMillis()-executeThreadManager.getCreatetime()>savetime){
					list.remove(executeThreadManager);
				}
			}
		}
	}
}
