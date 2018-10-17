package com.api.project.common.shell.interfaces;

import java.io.File;
import java.util.Map;

public interface Shell {

	File getWorkingDir();
	
	void setWorkingDir(File workingDir);
	
	String getName();
	
	void setName(String name);
	
	public Map<String, String> getEnvironment();
	
	public void setEnvironment(Map<String, String> environment);
	
	public String getErrorMessage();
	
	public void setErroorMessage(String errorMessage);
	
	String[] getExecCommand();
	
	void setExecCommand(String[] command);
	
	boolean isCompleted();
	
	boolean isTimeOut();
	
	String apply() throws Exception;

	void destory();
	
	void kill(long pid);
	
	void kill(String name);
}
