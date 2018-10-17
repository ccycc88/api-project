package com.api.project.common.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.common.shell.impl.AbstractShell;

public class ShellRuntime extends AbstractShell{

	private static Logger logger = LoggerFactory.getLogger(ShellRuntime.class);

	private Exception exception = null;

	public ShellRuntime() {
		super(logger);
		// TODO Auto-generated constructor stub
	}
	public ShellRuntime(int delay) {
		super(delay, logger);
		// TODO Auto-generated constructor stub
	}
	public ShellRuntime(String[] command, String name) {
		this();
		// TODO Auto-generated constructor stub
		super.setExecCommand(command);
		super.setName(name);
	}
	public ShellRuntime(int delay, String[] command, String name) {
		this(delay);
		// TODO Auto-generated constructor stub
		super.setExecCommand(command);
		super.setName(name);
	}

	@Override
	public String[] getExecCommand() {
		// TODO Auto-generated method stub
		return command;
	}

	@Override
	public void kill(long pid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void kill(String name) {
		// TODO Auto-generated method stub

	}
	public void exec() throws Exception {

		try {
			super.apply();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.exception = e;
			throw e;
		}
	}
	public void exec(String[] command) throws Exception {

		super.setExecCommand(command);
		try {
			super.apply();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.exception = e;
			throw e;
		}
	}
	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
