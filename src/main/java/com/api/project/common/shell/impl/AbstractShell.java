package com.api.project.common.shell.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;

import com.api.project.common.shell.exception.InvalidValueException;
import com.api.project.common.shell.interfaces.Shell;
import com.api.project.common.shell.stream.ShellStreamOutput;

public abstract class AbstractShell implements Shell{

	private File workingDir = null;
	private String name = null;
	private Map<String, String> environment = null;
	protected String[] command = null;
	private AtomicBoolean completed = new AtomicBoolean(false);
	private AtomicBoolean timeOut = new AtomicBoolean(false);
	private Process process = null;
	private int delay = 0;
	private Logger logger = null;
	private StringBuffer error = new StringBuffer();
	
	public AbstractShell(Logger logger){
		this(0, logger);
	}
	public AbstractShell(int delay, Logger logger){
		this.delay = delay;
		this.logger = logger;
	}
	@Override
	public File getWorkingDir() {
		// TODO Auto-generated method stub
		return workingDir;
	}

	@Override
	public void setWorkingDir(File workingDir) {
		// TODO Auto-generated method stub
		this.workingDir = workingDir;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		this.name = name;
	}

	@Override
	public Map<String, String> getEnvironment() {
		// TODO Auto-generated method stub
		return environment;
	}

	@Override
	public void setEnvironment(Map<String, String> environment) {
		// TODO Auto-generated method stub
		this.environment = environment;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return error.toString();
	}

	@Override
	public void setErroorMessage(String errorMessage) {
		// TODO Auto-generated method stub
		error.append(errorMessage);
		error.append("\n");
	}

	@Override
	public void setExecCommand(String[] command) {
		// TODO Auto-generated method stub
		this.command = command;
	}

	@Override
	public boolean isCompleted() {
		// TODO Auto-generated method stub
		return completed.get();
	}

	@Override
	public boolean isTimeOut() {
		// TODO Auto-generated method stub
		return timeOut.get();
	}

	@Override
	public String apply() throws Exception{
		// TODO Auto-generated method stub
		
		return runCommand();
	}

	private String runCommand() throws IOException, InterruptedException, InvalidValueException {
		// TODO Auto-generated method stub
		ProcessBuilder builder = new ProcessBuilder(this.getExecCommand());
		if(this.environment != null){
			
			builder.environment().putAll(environment);
		}
		if(this.workingDir == null){
			
			builder.directory(workingDir);
		}
		Timer shellTimer = null;
		ShellStreamOutput outputErro = null;
		ShellStreamOutput outputInput = null;
		try {
			//开始运行
			process = builder.start();
			
			if(delay > 0){
				
				shellTimer = new Timer();
				shellTimer.schedule(new ShellTimerTask(this), delay);
			}
			outputErro = new ShellStreamOutput(process.getErrorStream(), ShellStreamOutput.STREAMERROR, logger) {
				
				@Override
				public void resultParse(String line) {
					// TODO Auto-generated method stub
					setErroorMessage(line);
				}
			};
			outputErro.start();
			outputInput = new ShellStreamOutput(process.getInputStream(), ShellStreamOutput.STREAMINPUT, logger) {
				
				@Override
				public void resultParse(String line) {
					// TODO Auto-generated method stub
					logger.info(name.concat("----").concat(line));
				}
			};
			outputInput.start();
			
			//执行中
			int exitCode = process.waitFor();
			//执行完成
			try {
				
				outputErro.join();
				outputInput.join();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(exitCode != 0){
				
				throw new InvalidValueException("进程["+ name +"]执行失败,异常Id[" + exitCode + "]");
			}
			completed.set(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			throw e;
		} catch (InvalidValueException e) {
			// TODO Auto-generated catch block
			throw e;
		}finally{
			
			if(shellTimer != null){
				shellTimer.cancel();
			}
			if(!completed.get()){
				
				if(outputErro != null) {
				
					outputErro.interrupt();
				}
				if(outputInput != null) {
				
					outputInput.interrupt();
				}
				
			}
			this.destory();
		}
		return name;
	}

	@Override
	public void destory() {
		// TODO Auto-generated method stub
		if(process != null){
			process.destroy();
		}
	}
	private class ShellTimerTask extends TimerTask{

		AbstractShell shell = null;
		public ShellTimerTask(AbstractShell shell){
			
			this.shell = shell;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Process process = shell.process;
			
			try {
				//
				process.exitValue();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				if(process != null && !shell.isCompleted()){
					shell.timeOut.set(true);
					shell.destory();
				}
			}
		}
		
	}  
}
