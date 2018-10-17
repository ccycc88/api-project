package com.api.project.util.helper;

import java.util.Enumeration;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadHelper {

private static Logger log = LoggerFactory.getLogger(ThreadHelper.class);
	
	public synchronized Thread[] findAllThreads() {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		ThreadGroup topGroup = group;

		// 遍历线程组树，获取根线程组
		while (group != null) {
			topGroup = group;
			group = group.getParent();
		}
		// 激活的线程数加倍
		int estimatedSize = topGroup.activeCount() * 2;
		Thread[] slackList = new Thread[estimatedSize];
		// 获取根线程组的所有线程
		int actualSize = topGroup.enumerate(slackList);
		Thread[] list = new Thread[actualSize];
		System.arraycopy(slackList, 0, list, 0, actualSize);
		return list;
	}
	
	public synchronized StackTraceElement[] findStackTraces(Thread t) {
		StackTraceElement[] ste = t.getStackTrace();
		return ste;
	}

	public static void run(ControllRunnable r[], int threadMaxNum) {
		run(r, threadMaxNum, -1, Thread.currentThread().getName());
	}
		
	public static void run(ControllRunnable r[], int threadMaxNum, long timeout) {
		run(r, threadMaxNum, timeout, Thread.currentThread().getName());
	}
		
	public static void run(ControllRunnable r[], int threadMaxNum, long timeout, String threadName) {
		Hashtable<Worker, Long> runMap = new Hashtable<Worker, Long>();

		long currTime = 0;
		int procIdx = 0;
		while (procIdx<r.length || runMap.size()>0) {
			currTime = System.currentTimeMillis();
			if (procIdx<r.length && runMap.size()<threadMaxNum) {
				Worker w = new Worker(r[procIdx]);
				procIdx ++;
				runMap.put(w, currTime);
				w.start();
				continue;
			}
			
			SysHelper.waitIt(runMap, 500);
			Enumeration<Worker> e = runMap.keys();
			while (e.hasMoreElements()) {
				Worker w = e.nextElement();
				if (w.isRun() == false) 
					runMap.remove(w);
				
				else if (timeout!=-1 && (currTime-runMap.get(w).longValue())>timeout){
					w.interrupt();
					runMap.remove(w);
					log.debug(threadName+" check. subthread:"+w.getName()+" timeout");
				}
				log.debug(threadName+" check. subthread:"+w.getName()+" isrun:"+w.isRun());
			}
		}
	}
	
	protected static class Worker extends Thread {
		private boolean running = false;
		private ControllRunnable runnable = null;
		
		public Worker(ControllRunnable r) {
			runnable = r;
			running = true;
		}
		
		public void run() {
			runnable.run();
			running = false;
		}
		
		public void interrupt() {
			runnable.release();
			super.interrupt();
		}
		
		public boolean isRun() {
			return running;
		}
	}
	public interface ControllRunnable extends Runnable {
		public void release() ;
	}
}
