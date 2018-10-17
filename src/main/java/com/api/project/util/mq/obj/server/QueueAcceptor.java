package com.api.project.util.mq.obj.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.mq.obj.exception.RestartServerException;
import com.api.project.util.mq.obj.server.console.server.ConsoleAcceptor;

public class QueueAcceptor extends Thread {

	private static Logger log = LoggerFactory.getLogger(QueueAcceptor.class);
	//端口
	private int port = -1;
	
	private QueueAcceptor()  {
		this.setName(this.getClass().getName());
	}
	private static QueueAcceptor acceptor = new QueueAcceptor();
	public static QueueAcceptor getInstance() {return acceptor;}
	public void setPort(int port) {this.port = port;}
	
	private boolean run = false;
	private ServerSocket svrSocket = null;
	
	private void accept() throws RestartServerException {
		if(run) {
			throw new RestartServerException("");
		}
		run = true;
		
		while (run && svrSocket==null) {
			try {
				log.debug("开始绑定端口:" + port);
				svrSocket = new ServerSocket(port);
				log.debug("已绑定端口["+port+"]。");
			}catch(Exception ex) {
				if (svrSocket != null)
					try {
						svrSocket.close();
					} catch (IOException e) {
					}
				svrSocket = null;
				log.error("绑定端口["+port+"]失败，消息["+ex.getMessage()+"]。5秒后重试...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
			}
		}
		
		ClientListner clientListner = ClientListner.getInstance();
		clientListner.start();
		while (run) {
			try {
				final Socket socket = svrSocket.accept();
				log.debug("远程地址:" + socket.getRemoteSocketAddress());
				ClientOperator oper = new ClientOperator(socket);
				oper.start();
				clientListner.addClient(oper);
			} catch (IOException e) {
				log.error("等待客户端注册超时，消息["+e.getMessage()+"]。");
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
				}
			}
		}
		run = false;
		log.debug("队列管理启线程退出");
	}
	
	public void run() {
		try {
			accept();
		} catch (RestartServerException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static void main(String[] conf) {
		conf = new String[] {"4444"};
		if(conf == null || conf.length == 0) {
			log.error("请配置需要绑定的端口");
			return;
		}
		
		//启动主服务监听线程
		int port = Integer.parseInt(conf[0]);
		QueueAcceptor acp = QueueAcceptor.getInstance();
		acp.setPort(port);
		System.out.println("开始主服务监听服务，绑定端口：" + port);
		acp.start();
		
		//启动控制台监听线程,默认控制台监听端口是主服务监听    端口+1
		if(conf.length > 1) {
			port = Integer.parseInt(conf[1]);
		}else {
			port ++ ;
		}
		System.out.println("开始启动控制台监听服务，绑定端口：" + port);
		new Thread(new ConsoleAcceptor(port)).start();
	}
}
