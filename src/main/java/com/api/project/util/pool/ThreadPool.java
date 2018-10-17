package com.api.project.util.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {

	final static private int CORE_SIZE = 100;
	final static private int MAX_SIZE = 1000;
	final static private int ALIVE_TIME = 20;
	
	private static ThreadPool threadPool = null;
	private ThreadPoolExecutor serverThreadPool = null;
	
//	 queue capacity (core pool size + max pool size)/2;
	private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>((CORE_SIZE+MAX_SIZE)/2); 
	private RejectedExecutionHandlerImp reh = new RejectedExecutionHandlerImp();

	private ThreadPool() {
	}
	
	public static synchronized ThreadPool getInstance(){
		if(threadPool==null){
			threadPool = new ThreadPool();
			threadPool.serverThreadPool = new ThreadPoolExecutor(
					CORE_SIZE, // core pool size;
					MAX_SIZE, // max pool size;
					ALIVE_TIME, // keep alive time;
					TimeUnit.SECONDS, // time unit;
					threadPool.workQueue, // pool queue
					threadPool.reh); // RejectedExecutionHandler
		}
		return threadPool;
	}
	
	public void execute(Runnable runnable){
		
		serverThreadPool.execute(runnable);
	}
	
	public int getQueueSize() {
		return serverThreadPool.getQueue().size();
	}
	
	public int getMaximumPoolSize() {
		return serverThreadPool.getMaximumPoolSize();
	}
	
	public long getCurrTaskCount() {
		return serverThreadPool.getTaskCount();
	}
	
	public void shutdown() {
		serverThreadPool.shutdown();
	}
	
	class RejectedExecutionHandlerImp implements RejectedExecutionHandler {
		public RejectedExecutionHandlerImp() {
		}
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		}
	}
}
