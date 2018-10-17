package com.api.project.util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.logicalcobwebs.proxool.ConnectionListenerIF;
import org.logicalcobwebs.proxool.ConnectionPoolFacade;
import org.logicalcobwebs.proxool.Prototyper;
import org.logicalcobwebs.proxool.ProxoolConstants;
import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.MD5;
import com.api.project.util.helper.SysHelper;

public class ProxoolPool {

	protected static Logger log = LoggerFactory.getLogger(ProxoolPool.class);

	protected static List<String> poolContainer=new ArrayList<>();
	//配置文件只能加载一次
	private boolean isInit=false;
	
	protected static ProxoolPool pool = new ProxoolPool();
	private ProxoolPool(){}
	
	public static ProxoolPool getInstance() {
		return pool;
	}
	
	/**
	 * 初始化配置文件
	 * @param fileName
	 * @throws ProxoolException 
	 * @throws Exception
	 */
	public synchronized void configure(String fileName) throws ProxoolException{
		if (!isInit) { 
			PropertyConfigurator.configure(fileName);
			isInit=true;
		}
	}
	
	/**
	 * 初始化配置文件
	 * @param fileName
	 * @throws ProxoolException 
	 * @throws Exception
	 */
	public void configure(Properties properties) throws ProxoolException{
		PropertyConfigurator.configure(properties);
	}
	
	/**
	 * 添加连接监听器
	 * @param alias
	 * @param connectionListener
	 * @throws ProxoolException 
	 */
	public void  addConnectionListener(String alias, ConnectionListenerIF connectionListener) throws ProxoolException{
		ProxoolFacade.addConnectionListener(alias,connectionListener);
	}
	/**
	 * 添加连接监听器
	 * @param filePath 数据库配置文件
	 * @param connectionListener
	 * @throws ProxoolException 
	 */
	public void  addConnectionListener() throws Exception{
		String[] names=ConnectionPoolFacade.getConnectionPoolNames();
		for (String alias : names) {
			addConnectionListener(alias, new DBConnListener());
			ProxoolFacade.killAllConnections(alias,"Set dirty read!");
		}
	}
	
	/**
	 *  通过参数创建一个连接池 
	 * @param driver
	 * @param url
	 * @param user
	 * @param pwd
	 * @param startWith
	 * @return Connection
	 */
	public synchronized Connection getConnection(String driver, String url, String user, String pwd){
		String poolName = driver.concat("_").concat(url).concat("_").concat(user).concat("_").concat(pwd);
		String alias=new MD5().toMD5Str(poolName);
		if (poolContainer.contains(poolName) == false) {			
			// 创建连接池，并返回引用
			String fullUrl = buildProxoolUrl(alias,driver,url);
	        Properties properties = new Properties();
	        //用户名
	        properties.setProperty(ProxoolConstants.USER_PROPERTY, user);
	        //密码
	        properties.setProperty(ProxoolConstants.PASSWORD_PROPERTY, pwd);
	        //最大连接数
	        properties.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_COUNT_PROPERTY, "100");
	        //最小连接数
	        properties.setProperty(ProxoolConstants.MINIMUM_CONNECTION_COUNT_PROPERTY, "3");
	        //最大存活日时间，单位毫秒，默认4小时，目前设置2小时
	        properties.setProperty(ProxoolConstants.MAXIMUM_CONNECTION_LIFETIME_PROPERTY, "7200000");
	        //最大并发连接数默认10,目前设置为最大连接数
	        properties.setProperty(ProxoolConstants.SIMULTANEOUS_BUILD_THROTTLE_PROPERTY, "100");
	        //最大active时间，单位毫秒，默认1分钟,目前设置1小时
	        properties.setProperty(ProxoolConstants.MAXIMUM_ACTIVE_TIME_PROPERTY, "3600000");
	        //千万不能配置,否则sql异常后,吊死无返回值
	        //properties.setProperty(ProxoolConstants.FATAL_SQL_EXCEPTION_PROPERTY, "Fatal error");
	        //最小空闲连接数，低于次数自动增加，默认0
	        properties.setProperty(ProxoolConstants.PROTOTYPE_COUNT_PROPERTY, "2");
	        try {
				ProxoolFacade.registerConnectionPool(fullUrl, properties);
			} catch (ProxoolException e) {
				log.error("注册连接池"+poolName+"异常",e);
			}
			log.info("注册连接池"+poolName+"成功,别名:"+alias);
			poolContainer.add(alias);
			//添加脏读设置
			try {
				addConnectionListener(alias, new DBConnListener());
			} catch (ProxoolException e) {
				log.error("连接池"+poolName+"添加脏读异常",e);
			}
		}
		Connection conn=getConnection(alias);
		//连接失败,删除连接池
		if (conn==null) removeConnectionPool(alias);
		return conn;
	}
	
	/**
	 * 数据库链接
	 */
	public synchronized Connection getConnection(String alias) {
				
		try {
			Prototyper prototyper=ConnectionPoolFacade.getConnectionPoolPrototyper(alias);
		
			int i=0;
			while(true){
				long connectionCount=prototyper.getConnectionCount();
				int availableConnectionCount=ConnectionPoolFacade.getAvailableConnectionCount(alias);
				int maxinumConnectionCount=ConnectionPoolFacade.getMaximumConnectionCount(alias);
				
//				System.out.println(" ***************** maxinumConnectionCount="+maxinumConnectionCount+
//				", connectionCount="+connectionCount+
//				", availableConnectionCount="+availableConnectionCount);
				if (connectionCount >= maxinumConnectionCount &&availableConnectionCount < 1) {
					log.info("连接池:"+alias+"可用连接达到上限,等待可以用连接："+i);
					log.info("maxinumConnectionCount="+maxinumConnectionCount+
					", connectionCount="+connectionCount+
					", availableConnectionCount="+availableConnectionCount);
					i++;
					SysHelper.waitIt(this, 1000);
					continue;
				}else {
					break;
				}
			}
		} catch (ProxoolException e1) {
			log.info("查看连接池:"+alias+"可用连接是否达到上限异常",e1);
		}
		
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(ProxoolConstants.PROPERTY_PREFIX + alias);
		} catch (SQLException e) {
			log.error("创建数据库连接错误:",e);
//			连接失败不删除连接池,不停的重连,提示其修改配置
//			removeConnectionPool(alias);
		}
		return connection;
	}
	/**
	 * 删除连接池alias
	 */
	public void removeConnectionPool(String alias){
		try {
	        log.info("删除连接池:"+alias);
			ProxoolFacade.removeConnectionPool(alias);
			poolContainer.remove(alias);
		} catch (ProxoolException e) {
			log.error("删除连接池"+alias+"异常",e);
		}
	}
	/**
     * Build a valid Proxool URL
     * @param alias identifies the pool
     * @param driver the delegate driver
     * @param delegateUrl the url to send to the delegate driver
     * @return proxool.alias:driver:delegateUrl
     */
    private  String buildProxoolUrl(String alias, String driver, String delegateUrl) {
        String url = ProxoolConstants.PROXOOL
                + ProxoolConstants.ALIAS_DELIMITER
                + alias
                + ProxoolConstants.URL_DELIMITER
                + driver
                + ProxoolConstants.URL_DELIMITER
                + delegateUrl;
        return url;
    }
}
