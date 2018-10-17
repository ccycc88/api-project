package com.api.project.common.msg;

import java.util.Properties;

import com.api.project.common.msg.listener.MessageListener;

public interface Receiver {

	void connect(Properties property);
	
	byte[] receive();
	
	void close();
	
	void receive(MessageListener listener);
}
