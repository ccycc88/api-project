package com.api.project.util.helper;

public class SysHelper {

	/**
	 * 冻结线程
	 */
	public static void waitIt(Object o, long time) {
		synchronized(o){
			try{
				if (time == -1)
					o.wait();
				else
					o.wait(time);
			}catch(InterruptedException iex){
				System.err.println(iex.getMessage());
			}
		}
	}

	public static void waitIt(Object o) {
		waitIt(o, -1);
	}

	/**
	 * 唤醒线程
	 */
	public static void notifyIt(Object o) {
		synchronized(o){
			o.notify();
		}
	}
}
