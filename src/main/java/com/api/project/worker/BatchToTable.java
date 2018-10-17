package com.api.project.worker;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.StringUtil;
import com.api.project.util.helper.SysHelper;
import com.api.project.util.thread.ControllThread;

public abstract class BatchToTable extends ControllThread{

	private java.util.Set<Object> datas = new HashSet<>();
	private String queueName = null;
	private volatile boolean run = true;
	private AtomicInteger index = new AtomicInteger(0);
	private Set<Object> store = new HashSet<>();
	
	private Logger logger =  LoggerFactory.getLogger(BatchToTable.class);
	public BatchToTable(String queueName){
		
		this.queueName = queueName;
	}
	
	public boolean isRun() {
		return run;
	}


	public void setRun(boolean run) {
		this.run = run;
	}

    public boolean isFinish(){
    	
    	int size = -1;//NativeChannelFactory.size(queueName);
    	if(size > 0){
    		
    		return false;
    	}
    	if(datas.size() > 0){
    		
    		return false;
    	}
    	if(store.size() > 0){
    		
    		return false;
    	}
		return true;
    }
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
        while(run){
			
			try {
				
				//NativeChannelFactory.drainTo(queueName, 500, datas);
				
				if(datas.size() == 0){
					
					if(index.incrementAndGet() == 2){
						
						if(store.size() > 0){
							
							//数据入库
							try {
								
								this.batchToTable(store);
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}finally{
								
								index.set(0);
								store.clear();
							}
						}
					}
					SysHelper.waitIt(this, 500);
					continue;
				}
				index.set(0);
				if(store.size() + datas.size() > 300){
					
					//数据插入
					store.addAll(datas);
					//依据通道名称
					this.batchToTable(store);
					index.set(0);
					store.clear();
				}else{
					
					//缓存数据
					store.addAll(datas);
				}
			} catch (Exception e) {
				// TODO: handle exception
				logger.error("数据异常[" + StringUtil.createStackTrace(e)+ "]将进行单条插入");
				try {
					
					Iterator<Object> it = store.iterator();
					while(it.hasNext()){
						
						try {
							
							this.singleToTable(it.next());
						} catch (Exception e2) {
							// TODO: handle exception
							e2.printStackTrace();
						}
					}
				} catch (Exception e2) {
					// TODO: handle exception
				}finally{
					store.clear();
				}
			}finally{
				
				datas.clear();
			}
		}
	}
	public abstract void batchToTable(Set<Object> datas);
	
	public abstract void singleToTable(Object data);
}
