package com.api.project.util.dispatcher;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class TaskDispatcher {

	/**
	 * 任务分发器集合，<名称,任务分发器>
	 */
	private static Hashtable<String,TaskDispatcher> instanceList = new Hashtable<String,TaskDispatcher>();
	
	/**
	 * 配置线程组数量
	 */
	final private int GROUP_NUM;
	/**
	 * 配置每组线程数量
	 */
	final private int WORKER_NUM;
	/**
	 * 当前线程组数量
	 */
	private int currGroupNum = 0;
	
	/**
	 * 任务执行类
	 */
	final private Class iProc;
	/**
	 * 工人线程组列表
	 */
	private List<WorkerGroup> wList = new ArrayList<>();
	private TaskQueue queue = new TaskQueue();
		
	/**
	 * @param groupNum
	 * @param workerNum
	 * @param iProcess
	 */
	private TaskDispatcher(int groupNum, int workerNum, Class iProcess) {
		GROUP_NUM = groupNum;
		WORKER_NUM = workerNum;
		iProc = iProcess;
	}
	
	/**
	 * 创建并返回一个任务分发器
	 * @param instanceName 任务分发器实例名
	 * @param groupNum  线程组数量
	 * @param workerNum 每组线程数量
	 * @param iProcess 任务执行类
	 * @return
	 */
	public static TaskDispatcher crtInstance(String instanceName, int groupNum, int workerNum, Class iProcess) {
		if (instanceList.containsKey(instanceName) == false) 
			instanceList.put(instanceName, new TaskDispatcher(groupNum, workerNum, iProcess));
		return instanceList.get(instanceName);
	}
	
	/**
	 * 返回任务分发器实例
	 * @param instanceName 实例名
	 * @return
	 */
	public static TaskDispatcher getInstance(String instanceName) {
		return instanceList.get(instanceName);
	}
	
	/**
	 * 任务分发器实例名称数组
	 * @return
	 */
	public static String[] keys() {
		return instanceList.keySet().toArray(new String[instanceList.size()]);
	}
	
	/**
	 * 设置任务
	 * @param key  标识
	 * @param data  任务anyData
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
 
	public synchronized void setTask(String key, Object data) 
		throws InstantiationException, IllegalAccessException {
		queue.addTask(key, data);
		if (queue.size()>currGroupNum && currGroupNum<GROUP_NUM) {
			WorkerGroup wg = new WorkerGroup(
					"G"+(++currGroupNum), WORKER_NUM, iProc, queue);
			wg.srt();
			wList.add(wg);
		}
	}
	
	/**
	 * 返回当前任务的描述信息
	 * @return
	 */
	public String showTaskInfo() {
		return queue.showAll();
	}
	
	/**
	 * 关闭全部线程
	 */
	public void shutdown() {
		for (int i=0; i<wList.size(); i++)
			wList.remove(i).shutdown();
		wList.clear();
	}
}
