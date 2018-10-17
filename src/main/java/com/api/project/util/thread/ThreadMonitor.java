package com.api.project.util.thread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.helper.DateHelper;
import com.api.project.util.helper.SysHelper;
import com.api.project.util.helper.ThreadHelper;
import com.api.project.util.pool.ThreadPool;

public class ThreadMonitor implements IControllable{

	private Logger log = LoggerFactory.getLogger(ThreadMonitor.class);
	private Hashtable<Long,ThreadDesc> threadMap = new Hashtable<Long,ThreadDesc>();
	private List<Long> threadSequence = new ArrayList<>();
	
	private Thread currT = null;
	private static final long exeCycle = 1000 * 60 * 10;
	private boolean run = true;
	private ThreadHelper thelper = new ThreadHelper();
	
	private long keepSize = 200 * 1024 * 1024;
	private boolean toutAppend = true;
	
	final private int[] LEN_TAG = new int[]{4,4,12,45,0};
	private StringBuilder threadOut = new StringBuilder("none.");
	private StringBuilder threadDetail = new StringBuilder();

	private static ThreadMonitor tm = new ThreadMonitor();
	private ThreadMonitor() {
	}
	public static ThreadMonitor getInstance() {
		return tm;
	}
	
	private boolean start = false;
	public synchronized void start() {
		if(start) return;
		ThreadPool.getInstance().execute(this);	
		start = true;
	}
	
	public void regThread(Thread t, Class c) {
		if (threadMap.containsKey(t.hashCode()) == true) {
			log.warn("线程["+t.getName()+"]实例["
					+t.getClass().getSimpleName()+"]重复注册，停止并拒绝。");
			return;
		}
		
		synchronized (threadOut) {
			long currTime = System.currentTimeMillis();
			ThreadDesc tinfo = new ThreadDesc();
			tinfo.regTime = currTime;
			tinfo.heartBeatTime = currTime;
	//		tinfo.threadName = t.getName();
			tinfo.threadObj = t;
			tinfo.className = c.toString().substring(6);
			threadMap.put(t.getId(), tinfo);
			threadSequence.add(t.getId());
		}
	}
	
	public void cancelMonitor(Thread t) {
		synchronized (threadOut) {
			threadMap.remove(t.getId());
			threadSequence.remove(t.getId());
		}
	}
	
	public void updateHeartBeatTime(Thread t, Class c) {
		ThreadDesc tinfo = threadMap.get(t.getId());
		if (tinfo != null)
			tinfo.heartBeatTime = System.currentTimeMillis();
		else {
			regThread(t, c);
		}
	}
	
	public String getName() {
		if (currT != null)
			return currT.getName();
		return "";
	}
	
	public String getThreadInfo() {
		refresh();
		return threadOut.append("\n\n\n").append(threadDetail).toString();
	}
	
	public void doHeartBeat(Class c) {
		updateHeartBeatTime(currT, c);
	}
	
	final public void run() {
		currT = Thread.currentThread();
		regThread(currT, this.getClass());
		dispose();
		cancelMonitor(currT);
	}

	public void dispose() {
		currT.setName("ThreadMonitor");
		while (run == true) {
			SysHelper.waitIt(this, exeCycle);
			this.doHeartBeat(this.getClass());
			
			refresh();
			threadOut.append("update at:"+new Date()+"\n");
			FileOutputStream fos = null;
			try {
				File file = new File(".JSTACK.dat");
				
				boolean append = toutAppend && file.exists() && file.length() < keepSize; 
				fos = new FileOutputStream(file, append);
				String time = DateHelper.getCurrentDateString("yyyy-MM-dd HH:mm:ss");
				fos.write(time.getBytes());
				fos.write("\n\n\n".getBytes());
				fos.write(threadOut.toString().getBytes());
				fos.write("\n\n\n".getBytes());
				fos.write(threadDetail.toString().getBytes());
			}catch(Exception ex) {
				ex.printStackTrace();
			}finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
				threadDetail.setLength(0);
			}
		}
	}
	
	private void refresh() {
		synchronized (threadOut) {
			threadOut.setLength(0);
			threadDetail.setLength(0);
			int threadNum = 0;
			long currtime = System.currentTimeMillis();
			String datas[] = new String[5];
			for (long id: threadSequence) {
				ThreadDesc td = threadMap.get(id);
				datas[0] = timeToStr(currtime - td.regTime);
				datas[1] = timeToStr(currtime - td.heartBeatTime);
				datas[2] = curtailState(td.threadObj.getState().toString());
				datas[3] = td.threadObj.getName();
				datas[4] = td.className;
				append2buf(threadOut, datas);
				append2buf(threadDetail, datas);
				++ threadNum;
				
				StackTraceElement[] allChild = thelper.findStackTraces(td.threadObj);
				for (StackTraceElement child: allChild) {
					datas[0] = "";
					datas[1] = "";
					datas[2] = child.toString();
					datas[3] = "";
					datas[4] = "";
					append2buf(threadDetail, datas);
				}
				allChild = null;
			}
			
			Thread[] allthreads = thelper.findAllThreads();
			for (Thread t: allthreads) {
				if (threadSequence.contains(t.getId()))
					continue;
				datas[0] = "--";
				datas[1] = "";
				datas[2] = curtailState(t.getState().toString());
				datas[3] = t.getName();
				datas[4] = t.getClass().toString().substring(6);
				append2buf(threadOut, datas);
				append2buf(threadDetail, datas);
				++ threadNum;
				
				StackTraceElement[] allChild = thelper.findStackTraces(t);
				for (StackTraceElement child: allChild) {
					datas[0] = "";
					datas[1] = "";
					datas[2] = child.toString();
					datas[3] = "";
					datas[4] = "";
					append2buf(threadDetail, datas);
				}
				allChild = null;
			}
			allthreads = null;
			threadOut.append(threadNum+" rows.\n");
			datas = null;
		}
	}
	
	private void append2buf(StringBuilder outBuf, String[] datas) {
		try {
			for (int i=0; i<datas.length; i++) {
				outBuf.append(datas[i]);
				for (int j=0; i!=(datas.length-1)&&j<(LEN_TAG[i]-datas[i].length()); j++)
					outBuf.append(" ");
			}
			outBuf.append("\n");
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private String timeToStr(long l) {
		if (l > 1000*60*60*24) {
			return l/(1000*60*60*24) + "D";			
		}else if (l > 1000*60*60) {
			return l/(1000*60*60) + "H";
		}else if (l > 1000*60) {
			return l/(1000*60) + "m";
		}else if (l > 1000) {
			return l/1000 + "s";
		}
		return "1s>";
	}
	
	private String curtailState(String str) {
		if (str.equals("TIMED_WAITING"))
			return "T_WAITING";
		return str;
	}
	
	public void interrupt() {
		if (currT != null)
			currT.interrupt();
	}
	
	public void release() {
		run = false;
		threadMap.clear();
		SysHelper.notifyIt(this);
	}
	
	public class ThreadDesc {
		public long regTime = 0;
		public long heartBeatTime = 0;
//		public String threadName = null;
		public String className = null;
		public Thread threadObj = null;
	}
	
	public int getThreadNum(){
		return threadMap.size();
	}
	
	public Collection<ThreadDesc> getAllThread(){
		return threadMap.values();
	}
}
