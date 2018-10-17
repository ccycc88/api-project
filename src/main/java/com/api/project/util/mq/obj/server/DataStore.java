package com.api.project.util.mq.obj.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.api.project.util.mq.obj.queue.NativeChannel;

public class DataStore {

	private DataStore() {}
	private static DataStore store = new DataStore();
	public static  DataStore getStore() {return store;}
	
	//<队列管理器,<队列,数据列表>>
	private final Map<String,NativeChannel<DataContainer>> datas = new HashMap<>();
	
	public void put(String qName,DataContainer data) {
		if(data == null || qName == null || qName.trim().length() == 0) return;
		
		NativeChannel<DataContainer> chnl = datas.get(qName);
		if(chnl == null) {
			chnl = new NativeChannel<DataContainer>(qName);
			datas.put(qName, chnl);
		}
		
		chnl.add(data);
	}
	
	public DataContainer get(String qName,String correlationId) {
		NativeChannel<DataContainer> chnl = datas.get(qName);
		if(chnl == null) {
			return null;
		}
		
		if(correlationId == null || correlationId.trim().length() == 0) {
			return chnl.get();
		}else {
			return chnl.get(chnl.indexOf(new DataContainer(null,correlationId,null)));
		}
	}
	
	
	public String clear(String qName) {
		NativeChannel<DataContainer> chnl = datas.get(qName);
		if(chnl == null) {
			return "no channel named ["+qName+"]";
		}
		chnl.clear();
		return "clear ["+qName+"] finished!";
	}
	
	public String size(String qName) {
		NativeChannel<DataContainer> chnl = datas.get(qName);
		if(chnl == null) {
			return "no channel named ["+qName+"]";
		}else {
			return new StringBuffer(qName).append("\t").append(chnl.size()).append("\t").append(chnl.getMaxLength()).toString();
		}
	}
	
	public String setMaxLength(String qName,int maxLength) {
		NativeChannel<DataContainer> chnl = datas.get(qName);
		if(chnl == null) {
			return "no channel named ["+qName+"]";
		}else {
			chnl.setMaxLength(maxLength);
			return "set channel["+qName+"] max length ["+maxLength+"]";
		}
	}
	
	public String toString() {
		if(datas.size() == 0) {
			return "empty";
		}
		StringBuffer sb = new StringBuffer();
		NativeChannel<DataContainer> chnl = null;
		Iterator<String> keys = datas.keySet().iterator();
		sb.append("channel\tcurrDepth\tmaxDepth\n");
		while(keys.hasNext()) {
			String key = keys.next();
			chnl = datas.get(key);
			sb.append(key).append("\t").append(chnl.size()).append("\t").append(chnl.getMaxLength()).append("\n");
		}
		return sb.toString();
	}
}
