package com.api.project.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.StringUtil;

public class ServiceShutdownHook extends Thread{

	private static Logger logger = LoggerFactory.getLogger(ServiceShutdownHook.class.getName());
	 public void run(){  
		 try{
			 logger.info("cobertura flush start");
			 String className = "net.sourceforge.cobertura.coveragedata.ProjectData";
			 String methodName = "saveGlobalProjectData";
			 Class saveClass = Class.forName(className);
			 java.lang.reflect.Method saveMethod = saveClass.getDeclaredMethod(methodName, new Class[0]);
			 saveMethod.invoke(null, new Object[0]);
			 logger.info("cobertura flush end");
		 }catch(Exception e){
			 logger.error("进程退出之前运行清理程序失败"+StringUtil.createStackTrace(e));
		 }
	 
	 }
}
