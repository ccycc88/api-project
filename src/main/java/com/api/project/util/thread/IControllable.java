package com.api.project.util.thread;

public interface IControllable extends Runnable{

	// 心跳事件
	public void doHeartBeat(Class c);

	// 业务处理方法
	public void dispose();
}
