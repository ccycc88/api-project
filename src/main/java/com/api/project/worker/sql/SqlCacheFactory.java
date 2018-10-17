package com.api.project.worker.sql;

import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SqlCacheFactory {
	
	private LinkedHashMap<String, String> sqllist = new LinkedHashMap<>();
	
	private ReentrantLock lock = new ReentrantLock();
	
	private static final int MAX_DEPTH = 5000;
	
	private static final Log LOG = LogFactory.getFactory().getLog(SqlCacheFactory.class);
	
	private SqlCacheFactory(){
		
	}
	private static SqlCacheFactory factory = new SqlCacheFactory();
	
	public static SqlCacheFactory getInstance(){
		
		return factory;
	}
	public void addBatch(String sqlI, String sqlU){
		
		if(StringUtils.isBlank(sqlI)){
			
			return;
		}
		lock.lock();
		try {
			
			if(sqllist.size() > MAX_DEPTH){
				
				LOG.warn("缓存sql队列大于[" + MAX_DEPTH + "]深度,sql将被丢弃[" + sqlI + "]");
			}
			sqllist.put(sqlI, sqlU);
		} finally{
			
			lock.unlock();
		}
	}
	public LinkedHashMap<String, String> taskBatch(){
		
		lock.lock();
		try {
			if(sqllist.size() == 0){
				
				return null;
			}
			LinkedHashMap<String, String> batchSql = sqllist;
			sqllist = new LinkedHashMap<>();
			return batchSql;
		} finally{
			
			lock.unlock();
		}
	}
	public void clearBatch(){
		lock.lock();
		try {
			
			sqllist.clear();
		} finally{
			lock.unlock();
		}
	}
}
