package com.api.project.util.mq.obj.exception;

public class ServerConnectionException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ServerConnectionException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param paramString
	 */
	public ServerConnectionException(String paramString) {
		super(paramString);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param paramThrowable
	 */
	public ServerConnectionException(Throwable paramThrowable) {
		super(paramThrowable);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param paramString
	 * @param paramThrowable
	 */
	public ServerConnectionException(String paramString,
			Throwable paramThrowable) {
		super(paramString, paramThrowable);
		// TODO Auto-generated constructor stub
	}
}
