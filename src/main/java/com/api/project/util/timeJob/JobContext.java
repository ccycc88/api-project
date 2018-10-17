package com.api.project.util.timeJob;

import java.util.HashMap;
import java.util.Map;

public class JobContext {

	private Map<String, Object> attrMap = new HashMap<>();

	public synchronized void put(String str, Object obj) {
		attrMap.put(str, obj);
	}

	public synchronized Object get(String str) {
		return attrMap.get(str);
	}
}
