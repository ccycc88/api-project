package com.api.project.util.dispatcher;

import java.util.ArrayList;
import java.util.List;

import com.api.project.util.helper.SysHelper;
import com.api.project.util.pool.ThreadPool;
import com.api.project.util.thread.ControllThread;

public class WorkerGroup {

	final private String GROUP_NAME;
	final private int WORKER_NUM;
	final private Class iProc;
	private TaskQueue queue = null;
	private List<TDWorker> wList = new ArrayList<>();
	private int procWorkerNum = 0;  // 正在执行任务的线程
	private String key = null;  // 绑定的Key
	
	/**
	 * @param groupName
	 * @param workerNum
	 * @param iProcess
	 * @param tl
	 * @param eList
	 */
	public WorkerGroup(String groupName, int workerNum, Class iProcess, TaskQueue queue) {
		GROUP_NAME = groupName;
		WORKER_NUM = workerNum;
		iProc = iProcess;
		this.queue = queue;
	}

	/**
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void srt() throws InstantiationException, IllegalAccessException {
		for (int i=0; i<WORKER_NUM; i++) {
			TDWorker w = new TDWorker((IProcess)iProc.newInstance(), this);
			ThreadPool.getInstance().execute(w);
			wList.add(w);
		}
	}
	
	public void shutdown() {
		for (int i=0; i<wList.size(); i++)
			wList.remove(i).interrupt();
		wList.clear();
		wList = null;
	}
	
	private void addProcNum() {
		synchronized (queue) {
			procWorkerNum ++;
		}
	}
	
	private void subtractProcNum() {
		synchronized (queue) {
			procWorkerNum --;
		}
	}
	
	public int getProcNum() {
		return procWorkerNum;
	}
	
	// 获取一个任务
	private Object get() {
		synchronized (queue) {
			if (key == null) {
				key = queue.tryLock();
				if (key == null)
					return null;
			}

			Object o = queue.takeTask(key);
			if (o==null && getProcNum()<=0) {
				queue.removeKey(key);
				key = null;
			}
			return o;
		}
	}
	
	
	/**
	 * 工人线程
	 *
	 */
	public class TDWorker extends ControllThread {
		private IProcess iProc = null;
		private WorkerGroup wg = null;
		private Object task = null;
		private boolean run = true;
		private byte[] lock = new byte[1];
		private String threadName = null;
		
		public TDWorker(IProcess iProc, WorkerGroup wg) {
			this.iProc = iProc;
			this.wg = wg;
		}
		
		public void init() {
		}
		
		public void dispose() {
			threadName = iProc.getClass().getSimpleName()
					+"@"+wg.GROUP_NAME+"@"+getSimpleID(getName());
			this.setName(threadName);

			String keyName = null;
			while (run == true) {
				
				synchronized (wg) {
					task = wg.get();
					keyName = wg.key;
				}
				if (task == null) {
					SysHelper.waitIt(lock, 500);
					continue;
				}
				wg.addProcNum();
				
				try {
					setName(threadName+"("+keyName+")");
					doHeartBeat(this.getClass());

					iProc.dispose(task);
				}catch(Exception ex) {
					ex.printStackTrace();
				}finally{
					iProc.release();
					this.setName(threadName);
					wg.subtractProcNum();
				}
			}
		}
		
		private String getSimpleID(String threadName) {
			return threadName.split("-")[1];
		}

		public boolean isResident() {
			return true;
		}
		
		public void interrupt() {
			run = false;
			SysHelper.notifyIt(lock);
		}
	}
}
