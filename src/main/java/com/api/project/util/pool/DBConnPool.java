package com.api.project.util.pool;

import java.sql.Connection;
import java.sql.Driver;
import java.util.Hashtable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.StringUtil;

public class DBConnPool extends ObjectPool{

	private Hashtable<Connection,Class> useLogs = new Hashtable<Connection,Class>();
	protected Logger log = LoggerFactory.getLogger(DBConnPool.class); 
	private String key_url = null;   // 数据库连接串
	private String key_name = null;  // 登陆数据库名称
	private String key_pwd = null;   // 登陆数据库口令
	private String key_driver = null; // 驱动
	private String key_ipaddr = null;
	
	public DBConnPool(String dirver, String userName, String pwd, String url,
			int increment, int securityNum, long timeout) {
		super(increment, securityNum, timeout);
		
		this.key_url = url;
		this.key_name = userName;
		this.key_pwd = pwd;
		this.key_driver = dirver;
		this.key_ipaddr = StringUtil.parseUrlIpAddr(key_url);
		if (key_ipaddr.indexOf("hsqldb") > 0)
			key_ipaddr = "hsqldb";
		
		try {
			Class.forName(dirver);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public DBConnPool(String dirver, String userName, String pwd, String url) {
		this(dirver, userName, pwd, url, 3, 100, 1000*60*30);
	}
	
	public String getDBHostIP() {
		return key_ipaddr;
	}

	@Override
	public void close(Object object) {
		if (object == null)
			return;

		Connection conn = (Connection)object;
		log.debug("release database connection. ["+key_url+"] ID:"+conn.toString());
		try{
			conn.close();
		}catch(Exception e){
		}
	}

	@Override
	public Object create() throws Exception {
//		ConnWorker cw = new ConnWorker(key_url, key_name, key_pwd);
//		cw.setName(cw.getName()+" -> "+key_name+"/"+key_pwd+" -> "+key_url);
//		cw.start();
//		
//		long srtAt = System.currentTimeMillis();
//		while (true) {
//			SysHelper.waitIt(this, 500);
//			if (cw.isFinish() == true) {
//				log.debug("create database connection("+((System.currentTimeMillis()-srtAt)/1000)
//						+" sec). ["+key_url+"] ID:"+cw.getConn());
//				return cw.getConn();
//			}
//			
//			if (System.currentTimeMillis()-srtAt>1000*60*5) {  // 5 minute
//				cw.interrupt();
//				log.warn("create database connection timeout. ["+key_url+"]");
//				return null;
//			}
//		}
		
		Properties props = new Properties();
		props.put("user", key_name);
		props.put("password", key_pwd);
		Driver driver = (Driver) Class.forName(key_driver).newInstance();
		try {
			return driver.connect(key_url, props);
		}finally {
			props.clear();
			props = null;
			driver = null;
		}
	}

	@Override
	public boolean isClosed(Object object) {
		Connection conn = (Connection)object;
		try {
			if (conn.isClosed() == true)
				return true;
			conn.createStatement().close();
		}catch(Exception ex) {
			return true;
		}
		return false;
	}

	public Connection getConnection(int wait, Class c) 
		throws Exception {
		Connection conn = (Connection)getObject(wait);
		synchronized (useLogs) {
			useLogs.put(conn, c);
		}
		return conn;
	}
	
	public Connection getConnection(Class c) 
		throws Exception {
		return getConnection(0, c);
	}
	
	public void freeConnection(Connection conn) {
		if (conn == null)
			return;
		
		synchronized (useLogs) {
			useLogs.remove(conn);
		}
		feeObject(conn);
	}
	
	public String getUsedLogs() {
		synchronized (useLogs) {
			StringBuffer logs = new StringBuffer();
			for (Class c: useLogs.values())
				logs.append(c.getSimpleName()).append(";");
			try {
				return logs.toString();
			}finally {
				logs = null;
			}
		}
	}
	
//	// 数据库连接线程
//	class ConnWorker extends Thread {
//		private String url,user,pwd;
//		private Connection conn = null;
//		private boolean finish = false;
//		
//		public ConnWorker(String url, String user, String pwd) {
//			this.url = url;
//			this.user = user;
//			this.pwd = pwd;
//		}
//		
//		public void run() {
//			try {
//				log.debug("connection database ["+key_url+"] ...");
//				conn = DriverManager.getConnection(url,user,pwd);
//			}catch(Exception sqle) {
//				sqle.printStackTrace();
//			}finally {
//				finish = true;
//			}
//		}
//		
//		public Connection getConn() {
//			return conn;
//		}
//		
//		public boolean isFinish() {
//			return finish;
//		}
//	}
}
