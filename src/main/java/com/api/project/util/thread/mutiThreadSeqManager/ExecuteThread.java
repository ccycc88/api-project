package com.api.project.util.thread.mutiThreadSeqManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.thread.ControllThread;

public class ExecuteThread extends ControllThread {

	private boolean run = true;
	private BlockingQueue<Object> inQueue = new LinkedBlockingQueue<Object>();
	private BlockingQueue<Object> outOueue = new LinkedBlockingQueue<Object>();
	private Class processer = null;
	private AbstractExecuteWorker worker;
	private Logger log = LoggerFactory.getLogger(ExecuteThread.class);
	private Object nowobject;

	public ExecuteThread(Class processer) {
		this.processer = processer;
	}

	public void dispose() {
		try {
			worker = (AbstractExecuteWorker) processer.newInstance();
		} catch (Exception e) {
			log.error("实例化类" + processer.getName() + "异常" + e.getMessage());
		}
		long starttime = System.currentTimeMillis();
		while (run) {
			Object in = null;
			try {
				doHeartBeat(this.getClass());
				in = this.inQueue.poll(1, TimeUnit.SECONDS);
				if (System.currentTimeMillis() - starttime > 300 * 1000) {
					starttime = System.currentTimeMillis();
					if (inQueue.size() > 0 || outOueue.size() > 0) {
						log.debug(Thread.currentThread().getName() + "入队列长度[" + inQueue.size() + "],出队列长度["
								+ outOueue.size() + "]");
					}
				}
				if (in == null)
					continue;

				nowobject = in;
				Object out = worker.execute(in);
				if (out == null) {
					out = "";
				}
				this.outOueue.put(out);
				nowobject = null;
			} catch (Exception e) {
				log.error("处理消息[" + in + "]异常" + e.getMessage(), e);
			}
		}

	}

	public Object[] getInObjects() {
		return inQueue.toArray();
	}

	public Object[] getOutObjects() {
		return outOueue.toArray();
	}

	public Object getNowExcuteObject() {
		return nowobject;
	}

	public void close() {
		this.run = false;
	}

	public void put(Object in) throws InterruptedException {
		this.inQueue.put(in);
	}

	public Object get(long milisec) throws InterruptedException {
		return this.outOueue.poll(milisec, TimeUnit.MILLISECONDS);
	}
}
