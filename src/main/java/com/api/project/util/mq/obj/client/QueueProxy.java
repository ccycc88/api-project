package com.api.project.util.mq.obj.client;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.mq.obj.exception.ServerConnectionException;
import com.api.project.util.mq.obj.queue.QueueRequest;
import com.api.project.util.mq.obj.queue.QueueResponse;

public class QueueProxy {

	private Logger log = LoggerFactory.getLogger(QueueProxy.class);

	private QueueClient client = null;

	private String host = null;

	private int port = -1;
	private String extra = null;

	QueueProxy(String h, int p, String extra) {
		this.setHost(h);
		this.setPort(p);
		this.setExtra(extra);
		if (client == null) {
			try {
				if (!connect()) {
					log.debug("没有连接成功：" + host + ":" + port);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public synchronized boolean write(Object data, String channel) throws ServerConnectionException {

		if (client == null) {
			try {
				if (!connect()) {

				}
			} catch (UnknownHostException e) {
				throw new ServerConnectionException(e);
			} catch (IOException e) {
				throw new ServerConnectionException(e);
			}
		}
		try {
			client.writeData(channel, data);
			// log.debug("发送数据:"+data);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			if (client != null) {
				client.shutdown();
				client = null;
			}
			// 重联，继续发送数据
			try {
				log.debug("第一次发送失败,5秒重新连接...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e2) {
				}
				if (connect()) {
					try {
						client.writeData(channel, data);
						log.debug("第二次发送成功,发送数据:" + data);
					} catch (IOException e1) {
						log.error(e.getMessage(), e);
						if (client != null) {
							client.shutdown();
							client = null;
						}
						return false;
					}
				} else {
					log.debug("重新连接失败...");
					return false;
				}
			} catch (UnknownHostException e1) {
				if (client != null) {
					client.shutdown();
					client = null;
				}
				throw new ServerConnectionException(e1);
			} catch (IOException e1) {
				if (client != null) {
					client.shutdown();
					client = null;
				}
			}
		}
		return true;

	}

	public synchronized Object read(String channel) throws ServerConnectionException, ClassNotFoundException {

		if (client == null) {
			try {
				if (!connect()) {

				}
			} catch (UnknownHostException e) {
				throw new ServerConnectionException(e);
			} catch (IOException e) {
				throw new ServerConnectionException(e);
			}
		}
		Object data = null;
		try {
			data = client.readData(channel);
			if (data != null) {
				// log.debug("读取数据:" + data);
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			if (client != null) {
				client.shutdown();
				client = null;
			}
			// 重联，继续发送数据
			try {
				log.debug("第一次读取失败,5秒后重新连接....");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e2) {
				}
				if (connect()) {
					try {
						data = client.readData(channel);
						log.debug("第二次读取成功,读取数据:" + data);
					} catch (IOException e1) {
						if (client != null) {
							client.shutdown();
							client = null;
						}
						return data;
					}
				} else {
					log.debug("重新连接失败");
					return data;
				}
			} catch (UnknownHostException e1) {
				if (client != null) {
					client.shutdown();
					client = null;
				}
				throw new ServerConnectionException(e1);
			} catch (IOException e1) {
				if (client != null) {
					client.shutdown();
					client = null;
				}
			}
		}
		return data;

	}

	public synchronized QueueResponse sendRequest(QueueRequest req)
			throws ServerConnectionException, ClassNotFoundException {
		if (client == null) {
			try {
				if (!connect()) {

				}
			} catch (UnknownHostException e) {
				throw new ServerConnectionException(e);
			} catch (IOException e) {
				throw new ServerConnectionException(e);
			}
		}
		QueueResponse res = null;
		try {
			res = client.sendRequest(req);
			if (res != null) {
				// log.debug("读取数据:" + data);
			}
		} catch (IOException e) {
			if (client != null) {
				client.shutdown();
				client = null;
			}
			// 重联，继续发送数据
			try {
				log.debug("第一次读取失败,5秒后重新连接....");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e2) {
				}
				if (connect()) {
					try {
						res = client.sendRequest(req);
						log.debug("第二次读取成功,读取数据:" + res);
					} catch (IOException e1) {
						if (client != null) {
							client.shutdown();
							client = null;
						}
						return res;
					}
				} else {
					log.debug("重新连接失败");
					return res;
				}
			} catch (UnknownHostException e1) {
				if (client != null) {
					client.shutdown();
					client = null;
				}
				throw new ServerConnectionException(e1);
			} catch (IOException e1) {
				if (client != null) {
					client.shutdown();
					client = null;
				}
			}
		}
		return res;
	}

	public void setResetPeriod(int period) {
		if (client == null)
			try {
				connect();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		this.client.setResetPeriod(period);
	}

	public void close() {
		if (client != null) {
			client.close();
		}
	}

	private boolean connect() throws UnknownHostException, IOException {
		try {
			client = QueueFactory.getInstance(host, port, this.extra).connect();
		} catch (ClassNotFoundException e) {
			log.error("接收到服务器端返回不可知的对象：" + e.getMessage(), e);
		}
		return true;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}
}
