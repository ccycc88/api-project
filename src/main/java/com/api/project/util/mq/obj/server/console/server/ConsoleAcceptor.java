package com.api.project.util.mq.obj.server.console.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleAcceptor implements Runnable {

	private static Logger log = LoggerFactory.getLogger(ConsoleAcceptor.class);
	public ConsoleAcceptor(int port) {
		this.controlPort = port;
	}
	
	private ServerSocket server = null;
	private Socket socket = null;
	private InputStream input = null;
	private OutputStream output = null;
	private int controlPort = -1;
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if(!bind(controlPort)) {
			log.error("控制台监听绑定端口[" + controlPort + "]失败，请检查端口是否被占用。");
			return;
		}

		//循环监听控制台
		while(true) {
			try {
				Thread.sleep(100);
				
				if(!createSession()) {
					continue;
				}
				
				String terminalIP = socket.getInetAddress().getHostAddress();
				String cmd = recMsg();
				if (cmd == null) {
					closeSession();
					continue;
				}
				
				log.debug("将要处理终端["+terminalIP+"]的命令["+cmd+"]");
				
				sendMsg(CommandHandle.handle(cmd));
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}finally {
				closeSession();
			}
		}
	}

	private boolean bind(int port) {
		try {
			server = new ServerSocket(port);
			server.setSoTimeout(1000 * 60 * 15);
		} catch (IOException e) {
			e.printStackTrace();
			try {
				server.close();
			} catch (Exception e1) {
			}
			return false;
		}
		return true;
	}
	
	private boolean createSession() {
		try {
			socket = server.accept();
			socket.setSoTimeout(1000 * 20);
			input = socket.getInputStream();
			output = socket.getOutputStream();
		} catch (Exception ex) {
			log.warn("与终端建立连接失败，消息：".concat(ex.getMessage()));
			closeSession();
			return false;
		}
		return true;
	}

	private void closeSession() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
			}
		}
		if (output != null) {
			try {
				output.close();
			} catch (IOException e) {
			}
		}
	}
	
	private String recMsg() {
		byte[] buf = new byte[1024];
		int readbytes = -1;
		try {
			readbytes = input.read(buf);
		} catch (IOException e) {
			log.error("接收中端消息失败，异常信息：".concat(e.getMessage()));
		}
		if (readbytes == -1)
			return null;
		return new String(buf, 0, readbytes);
	}
	
	private void sendMsg(String msg) {
		try {
			if (msg == null)
				msg = "null";
			output.write(msg.concat("\n").getBytes());
			output.flush();
		} catch (IOException e) {
			log.error("向终端发送消息失败，异常信息：".concat(e.getMessage()));
		}
	}

}
