package com.api.project.util.thread.mutiThreadSeqManager;

import java.util.HashMap;
import java.util.Map;

public class MutiThreadSeqManagerFactory {

	private static Map<String,MutiThreadSeqManager> map = new HashMap<>();
	private static final int fileZipWorkerNum = 10;
	
	@SuppressWarnings("unchecked")
	public static synchronized MutiThreadSeqManager getManager(String managername,Class worker) throws Exception{
		return getManager(managername,worker,fileZipWorkerNum);
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized MutiThreadSeqManager getManager(String managername,Class worker,int workernum) throws Exception{
		MutiThreadSeqManager mtsm = map.get(managername);
		if(mtsm==null){
			mtsm = new MutiThreadSeqManager(workernum,worker);
			map.put(managername, mtsm);
		}
		return mtsm;
	}
}
