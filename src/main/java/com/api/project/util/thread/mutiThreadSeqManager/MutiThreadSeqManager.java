package com.api.project.util.thread.mutiThreadSeqManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MutiThreadSeqManager {

	private List<ExecuteThread> workerList = new ArrayList<>();
	private int size = 0;
	private int inPos = 0;
	private int outPos = 0;

	@SuppressWarnings("unchecked")
	public MutiThreadSeqManager(int n, Class worker) throws InstantiationException, IllegalAccessException {

		for (int i = 0; i < n; i++) {
			workerList.add(new ExecuteThread(worker));
		}

	}

	public void put(Object in) throws InterruptedException {

		ExecuteThread worker = workerList.get(inPos);
		worker.put(in);
		inPos += 1;
		if (inPos >= size) {
			inPos = 0;
		}

	}

	public Object get(long milisec) throws InterruptedException {

		ExecuteThread worker = workerList.get(outPos);
		Object out = worker.get(milisec);
		if (out == null) {
			return null;
		}
		outPos += 1;
		if (outPos >= size) {
			outPos = 0;
		}
		return out;

	}

	public void add(ExecuteThread worker) {
		workerList.add(worker);
	}

	public void start() {
		this.size = workerList.size();
		for (int i = 0; i < size; i++) {
			ExecuteThread t = workerList.get(i);
			t.setName("MutiThreadSeqWorker-" + i);
			t.start();
		}
	}

	public List<Object> getInObjects() {
		List<Object> list = new ArrayList<>();
		for (ExecuteThread thread : workerList) {
			Object[] inArray = thread.getInObjects();
			list.addAll(Arrays.asList(inArray));
		}
		return list;
	}

	public List<Object> getOutObjects() {
		List<Object> list = new ArrayList<>();
		for (ExecuteThread thread : workerList) {
			Object[] outArray = thread.getOutObjects();
			list.addAll(Arrays.asList(outArray));
		}
		return list;
	}

	public List<Object> getNowExcuteObjects() {
		List<Object> list = new ArrayList<>();
		for (ExecuteThread thread : workerList) {
			Object nowObject = thread.getNowExcuteObject();
			if (nowObject != null) {
				list.add(nowObject);
			}
		}
		return list;
	}

	public List<Object> getNonProcessedObjects() {
		List<Object> list = new ArrayList<>();
		for (ExecuteThread thread : workerList) {
			Object[] inArray = thread.getInObjects();
			list.addAll(Arrays.asList(inArray));
			Object nowObject = thread.getNowExcuteObject();
			if (nowObject != null) {
				list.add(nowObject);
			}
		}
		return list;
	}

	public void stop() {
		for (int i = 0; i < size; i++) {
			workerList.get(i).close();
		}
	}

	public static void main(String args[]) throws Exception {
		MutiThreadSeqManager mtsm = new MutiThreadSeqManager(10, new AbstractExecuteWorker() {
			public Object execute(Object in) {
				Integer i = (Integer) in;
				String out = "AAA" + i;
				if (i % 3 == 0) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return out;
			}
		}.getClass());
		mtsm.start();

		TestReceiver tr = new TestReceiver(mtsm, 10000);
		tr.start();

		for (int i = 0; i < 10000; i++) {
			mtsm.put(i);
		}
	}
}
