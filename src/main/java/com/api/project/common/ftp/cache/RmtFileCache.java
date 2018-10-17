package com.api.project.common.ftp.cache;

import java.util.Hashtable;

import com.api.project.util.MD5;

public class RmtFileCache {

	private static RmtFileCache tfc = new RmtFileCache();
	private MD5 md5 = new MD5();
	private Hashtable<String,Hashtable<String,String>> cache = 
		new Hashtable<String,Hashtable<String,String>>();
	private long lastClearTime = 0;
	private RmtFileCache() {
	}
	
	public static RmtFileCache getInstance() {
		return tfc;
	}
	
	public String tryGet(String ipAddr, String absFileName, long fileSize, long modifyAt) {
		synchronized (cache) {
			Hashtable<String,String> map = cache.get(ipAddr);
			if (map == null)
				return null;
			return map.get(md5.toMD5Str(absFileName+fileSize+modifyAt));
		}
	}
	
	public void put(String ipAddr, String absFileName, long fileSize, long modifyAt, String filePath) {
		synchronized (cache) {
			Hashtable<String,String> map = cache.get(ipAddr);
			if (map == null) {
				map = new Hashtable<String,String>();
				cache.put(ipAddr, map);
			}
			map.put(md5.toMD5Str(absFileName+fileSize+modifyAt), filePath);
		}
	}
	
	public void clear(String ipAddr) {
		synchronized (cache) {
			Hashtable<String,String> map = cache.get(ipAddr);
			if (map != null)
				map.clear();
		}
	}
	
	public void clear() {
		synchronized (cache) {
			for (String ipAddr: cache.keySet())
				clear(ipAddr);
			cache.clear();
		}
	}

	/**
	 * @return the lastClearTime
	 */
	public long getLastClearTime() {
		return lastClearTime;
	}

	/**
	 * @param lastClearTime the lastClearTime to set
	 */
	public void setLastClearTime(long lastClearTime) {
		this.lastClearTime = lastClearTime;
	}
	
	public int getCacheSize(){
		return cache.size();
	}
}
