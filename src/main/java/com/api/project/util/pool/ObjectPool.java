package com.api.project.util.pool;

import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ObjectPool {

	private Logger log = LoggerFactory.getLogger(ObjectPool.class);
	private ObjectContainer container = new ObjectContainer();
	private int increment = 0;   // 增长数
	private int maxConnNum = 0;  // 最大连接数
	private long timeout = 0;    // 超时时间
	
	public ObjectPool(int increment, int maxConnNum, long timeout){
		this.increment = increment;
		this.maxConnNum = maxConnNum;
		this.timeout = timeout;
	}
	
	public ObjectPool() {
		this(1, 100, 1000*60*30);
	}

	abstract protected Object create() throws Exception;
	abstract protected void close(Object object);
	abstract protected boolean isClosed(Object object);
	
	/**
	 * 向对象池增加一批对象
	 * 当初始化对象池或池中的对象不能满足对象需要时，会通过“getObject”方法调用该方法用
	 * 来创建指定数量“increment”个连接，并依次将新创建的对象添加到对象容器中。如
	 * 果在创建批对象中途出错则会打印错误信息并立即停止批创建。
	 * @throws Exception 
	 */
	protected void increaseObject(int num) throws Exception {
//		try {
		for (int i=0; i<num; i++) {
			Object o = create();
			if (o != null)
				container.addObject(o);
		}
//		}catch(Exception e) {
//			log.error("increaseObject["+num+"]."+e.getMessage());
//		}
	}
	
	/**
	 * 关闭一个对象
	 * 该方法能够关闭指定的对象，参数“reason”表示关闭的理由。方法会根据该
	 * 理由通过连接池日志类打印出提示信息。关闭对象的同时调用对象容器的
	 * “remove”方法将该对象在对象容器和对象描述容器的记录删除。
	 */
	protected void closeObject(Object object, String reason){
		if (object == null)
			return;
		container.remove(object);		
		try{
			close(object);
			System.out.println(new Date()+" close connection reason:"+reason+" obj:"+object);
		}catch(Exception e){
			System.out.println(new Date()+" close connection exception msg:"+
					e.getMessage()+" reason:"+reason+" obj:"+object);
		}
	}
	
	/**
	 * 向对象池中交付一个对象
	 * 当调用此方法交付对象的时候对象池会认为这是压力低峰期，在真正交付给对象
	 * 容器时需要执行一系列的验证。使用对象自带的方法“isClosed”判断该对象是否已关
	 * 闭，如没有关闭则使用“REPAIRE”变量判断该对象是否已经超时（当前的时间毫秒减去
	 * 对象的创建时间大于“REPAIRE”表示超时）。如其中任意验证结果为真则关闭对象,否
	 * 则添加到对象容器中继续留用。
	 */
	protected void feeObject(Object object){
		synchronized (container) {
			if (object == null)
				return;
			
			try {
				if (object==null || isClosed(object)) {
					closeObject(object, "closed by user");
				}else{
					container.addObject(object);
				}
			}catch(Exception ex){
				closeObject(object, ex.getMessage());
			}
		}
	}
	
	/**
	 * 从对象池中获得一个空闲的对象
	 * 在获取时首先使用方法“getIdleObjectCount()”判断对象容器中是否存在可用
	 * 的对象,如果返回“0”则表示没有可用的对象,这时使用方法“increaseObject()”
	 * 创建“num”个对象。无论创建是否成功都试图从对象容器中获取，在获取
	 * 出对象时判断如果该对象为空“null”则表示无可用对象。如参数“wait”大于“0”时表
	 * 示用户愿意在无可用对象时等候其他线程释放的对象，方法会自动冻结“wait”毫秒后使
	 * 用递归的方式在此调用自身“return getObject(wait)”直到能够得到可用的对象为止。
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	protected Object getObject(long wait) throws Exception {
		synchronized (container) {
			try {
				if (getIdleObjectCount() <= 0) {
					if (getAllObjectCount() >= maxConnNum)
						throw new Exception("["+maxConnNum+"] too many open object.");
					increaseObject(increment);
				}
				
				Object object = container.getObject();
				if (object != null) {
					if (timeout>0 && (System.currentTimeMillis()-container.getEnrolTime(object))>timeout) {
						closeObject(object, "object timeout["+timeout+"]. close by connecter.");
						return getObject(wait);
					}
					
					try {
						if (isClosed(object))
							throw new Exception("the object is closed.");
					}catch(Exception e) {
						closeObject(object, e.getMessage());
						return getObject(wait);
					}
				}else if (wait > 0){
					try {
						this.wait(wait);
					} catch (InterruptedException e) {
					}
					return getObject(wait);
				}
				return object;
			}catch(Exception ex) {
				log.error("getObject, wait:["+wait+"]. "+ex.getMessage());
				throw ex;
			}
		}
	}

	/**
	 * 关闭对象容器中所有的对象
	 * 
	 * 		调用该方法能够关闭对象容器中所有的空闲对象，但如果某个对象
	 * 没有被回收那么该对象不能被关闭。最初考虑使用“mapsConn”容器来做
	 * 为关闭对象的对象容器，该容器中记录了所有空闲和忙碌的对象。但使用
	 * 该容器关闭存在一个问题就是它会突然中断一项正在处理任务中的对象造
	 * 成数据的丢失。
	 *      需要注意的是，该方法只是关闭了当前可用的对象并没有改变对象
	 * 池的内存地址，也就是说当调用完该方法后类实例仍然有效，而且处于忙
	 * 碌中的对象在完成任务后仍然能够被回收。
	 */
	public void closeAllObject() {
		synchronized (container) {
			while (getIdleObjectCount() != 0)
				closeObject(container.getObject(), "pool shutdown.");
		}
	}
	
	public int getIdleObjectCount(){
		synchronized (container) {
			return container.idleConn.size();
		}
	}
	
	public int getAllObjectCount() {
		synchronized (container) {
			return container.mapsConn.size();
		}
	}
	
	/**
	 * 对象容器
	 * 
	 *    该类中含有两个容器，一是保存可用对象的容器，二是保存着这些对象
	 * 所对应的的注册时间。因考虑到某个时间集体释放对象不仅给系统增加压力而且也增加
	 * 了数据库对象管理的复杂性，特此考虑在对象池中增加一个容器保存着当前所有可用对
	 * 象的注册时间，当对象被服务交付时判断该对象的注册时间是否大于指定的存活期，如
	 * 大于存活期则关闭，否则继续留用。
	 *    在管理对象方面，每当建立一个新的对象会通过该类的“addObject”方法添加到
	 * 对象容器中，同时判断该对象是否在描述容器中记录了注册时间，如果没有记录则在描
	 * 述容器中添加一个新的记录。获取可用对象时直接在对象容器取出即可，删除的时候不
	 * 仅要删除对象容器的记录同时也要删除对象描述容器中的记录。
	 * 		
	 *    注意：该类只负责使用两个容器管理对象和其所对应的注册时间，具体是否释放
	 * 或定义一个对象超时不由该类负责。只是为了给对象池提供所有对象更具体的信息并
	 * 为其是否留用作出依据。
	 * 
	 *      free or new
	 *            |              idleConn
	 *    	      |     －－－－－－－－－－－－－－－－－－ 
	 *   add conn |-->  | conn 1 | conn 2 | conn n...  --> get conn to system
	 *   		  |     －－－－－－－－－－－－－－－－－－
	 *            |
	 *            |              mapsConn
	 *            |     －－－－－－－－－－－－－－－－－－
	 *   reg time |-->  | time 1 | time 2 | time n...
	 *            |     -----------------------------
	 * 	  	      \-->  | conn 1 | conn 2 | conn n...
	 * 		  	        －－－－－－－－－－－－－－－－－－
	 */
	private class ObjectContainer {
		public LinkedList<Object> idleConn = new LinkedList<>();
		public Hashtable<Object,Long> mapsConn = new Hashtable<Object,Long>();
		
		/**
		 * 向对象容器中添加一个可用的对象。
		 * 
		 *    方法会将用户指定的对象“object”添加到对象容器中，新的对象将
		 * 添加到对象容器的头部。同时将判断该对象是否存在于对象描述容器中，如
		 * 该对象在对象描述容器中没有记录，则添加且注册时间是当前的毫秒时间。
		 */
		public void addObject(Object object) {
			synchronized(this) {
				idleConn.addFirst(object);
				/*if (!mapsConn.containsKey(object))
					mapsConn.put(object, System.currentTimeMillis());*/
				mapsConn.put(object, System.currentTimeMillis());
			}
		}
		
		/**
		 * 从对象容器中获得一个可用的对象。
		 * 
		 *    该方法将从对象容器尾部取出一个可用的对象，如果对象容器
		 * 中没有可用对象则返回null。
		 */
		public Object getObject() {
			synchronized(this) {
				if (idleConn.size() <= 0)
					return null;
				return idleConn.removeLast();
			}
		}
		
		/**
		 * 获得某个对象的注册时间
		 * 
		 *    从对象描述容器种搜索指定的对象，如果该对象不再对象描述容器中
		 * 则返回的注册时间是-1，否则将返回容器中与该对象对应的注册时间。该时
		 * 间是以毫秒形式返回。
		 */
		public long getEnrolTime(Object object) {
			synchronized (this) {
				Long enrolTime = mapsConn.get(object);
				if (enrolTime != null)
					return enrolTime.longValue();
				return -1;
			}
		}
		
		/**
		 * 从容器中删除一个对象，及其描述
		 * 
		 * 		从对象容器中删除用户指定的对象，如果该对象不存在在容器中也不
		 * 会报出任何异常。之后从对象描述容器种删除该对象对应的注册时间和记录。
		 */
		public void remove(Object object) {
			synchronized(this) {
				idleConn.remove(object);
				mapsConn.remove(object);
			}
		}
	}
}
