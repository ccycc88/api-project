package com.api.project.util.thread.mutiTreadManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreadManagerFactory {

	private static Map<String,ThreadManagerFactory> threadManagerFactoryMap = new HashMap<>();
	private List<ExecuteThreadManager> list = new ArrayList<>();
	private ThreadManagerFactory(){	
	}
	
	public static ThreadManagerFactory getInstance(String managerFactoryName){

		if(threadManagerFactoryMap.get(managerFactoryName)==null){

			synchronized(threadManagerFactoryMap){

				if(threadManagerFactoryMap.get(managerFactoryName) == null){

					ThreadManagerFactory threadManagerFactory = new ThreadManagerFactory();
					threadManagerFactoryMap.put(managerFactoryName, threadManagerFactory);
				}
			}

		}
		return threadManagerFactoryMap.get(managerFactoryName);
	}
	public ExecuteThreadManager initThreadManager(int maxThreadNum){

		ExecuteThreadManager manager = new ExecuteThreadManager(maxThreadNum);
		synchronized(list){
			list.add(manager);
		}
		return manager;
	}
	public ExecuteThreadManager[] getThreadManager(){

		synchronized(list) {
			return list.toArray(new ExecuteThreadManager[0]);
		}
	}
	
	public void clearThreadManager(ExecuteThreadManager manager){
		synchronized(list){
			list.remove(manager);
		}
	}
	
	public void halt(){
		synchronized(list){
			for(ExecuteThreadManager executeThreadManager:list){
				executeThreadManager.halt();
			}
		}
	}
	
	public void go_on(){
		synchronized(list){
			for(ExecuteThreadManager executeThreadManager:list){
				executeThreadManager.go_on();
			}
		}
	}
	
	public void clear(long savetime){
		synchronized(list){
			for(ExecuteThreadManager executeThreadManager:list){
				if(System.currentTimeMillis()-executeThreadManager.getCreatetime()>savetime){
					list.remove(executeThreadManager);
				}
			}
		}
	}
}
