package com.api.project.common.ftp.cache;

import java.util.Hashtable;

import com.api.project.util.MD5;
import com.api.project.common.ftp.commonsnet.AFtpRemoteFile;

public class SearchCache {

	private static SearchCache sc = new SearchCache();
	private MD5 md5 = new MD5();
	private Hashtable<String, FileDesc> cache = new Hashtable<String, FileDesc>();

	private SearchCache() {
	}

	public static SearchCache getInstance() {
		return sc;
	}

	public AFtpRemoteFile[] tryGet(String ipAddr,String fileDir, String lstType, int timeout) {
		synchronized (cache) {
			//FileDesc fd = cache.get(md5.toMD5Str(ipAddr +fileDir+ lstType));
			FileDesc fd = cache.get(ipAddr +fileDir+ lstType);
			if(fd==null){
				return null;
			}
			if (System.currentTimeMillis() - fd.updateTime > timeout){
				fd.clear();
				return null;
			}	
			return fd.ftf;

		}
	}

	public void put(String ipAddr, String fileDir,String lstType, AFtpRemoteFile[] ftf) {
		synchronized (cache) {
			FileDesc fd = new FileDesc();
			fd.ftf = ftf;
			fd.updateTime = System.currentTimeMillis();
			//cache.put(md5.toMD5Str(ipAddr +fileDir+ lstType), fd);
			cache.put(ipAddr +fileDir+ lstType, fd);
		}
	}

	public void clear() {
		synchronized (cache) {
			for (String key : cache.keySet())
				cache.get(key).clear();
			cache.clear();
		}
	}

	public class FileDesc {
		private  AFtpRemoteFile[] ftf = null;
		public long updateTime = 0;

		public void clear() {
			ftf = null;
		}
	}

	/**
	 * @return the cache
	 */
	public Hashtable<String, FileDesc> getCache() {
		return cache;
	}
}
