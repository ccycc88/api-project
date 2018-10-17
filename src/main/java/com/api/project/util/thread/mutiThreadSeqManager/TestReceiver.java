package com.api.project.util.thread.mutiThreadSeqManager;

public class TestReceiver extends Thread{

	MutiThreadSeqManager mtsm;
	int num;

	public TestReceiver(MutiThreadSeqManager mtsm, int n) {
		this.mtsm = mtsm;
		this.num = n;
	}

	public void run() {

		long start = System.currentTimeMillis();

		for (int i = 0; i < num; i++) {
			try {
				String out = (String) mtsm.get(1);
				if(out==null){
					i--;
					continue;
				}
				System.out.println(out);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		long end = System.currentTimeMillis();

		System.out.println("time lasted: " + (end - start) + "毫秒");

	}
}
