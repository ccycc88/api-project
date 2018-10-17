package com.api.project.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public abstract class FunctionTimeoutUtil<V> implements Callable<V>{

	public V exec(int timeoutSeconds) throws InterruptedException, ExecutionException, TimeoutException{
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<V> future = executor.submit(this);
		V obj = null;
		try{
			obj = future.get(1000 * timeoutSeconds, TimeUnit.MILLISECONDS);
		}catch(TimeoutException ex){
			throw ex;
		}finally{
			future.cancel(true);
			executor.shutdown();
		}
		return obj;
	}
	
	
	public abstract V call();
}
