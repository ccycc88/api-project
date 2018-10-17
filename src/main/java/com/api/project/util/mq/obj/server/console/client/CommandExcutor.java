package com.api.project.util.mq.obj.server.console.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class CommandExcutor {

	private String cmdRecvrIP = null;
	private int cmdRecvrPort = -1;

	private Socket socket = null;
	private InputStream input = null;
	private OutputStream output = null;
	
	public CommandExcutor(String ip,int port) {
		this.cmdRecvrIP = ip;
		this.cmdRecvrPort = port;
	}

	private void connect() throws UnknownHostException, IOException {
		socket = new Socket(cmdRecvrIP, cmdRecvrPort);
		socket.setSoTimeout(1000 * 10);
		input = socket.getInputStream();
		output = socket.getOutputStream();
	}

	private void close() {
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

	public String execute(String cmd) {
		try {
			connect();
			output.write(cmd.getBytes());
			output.flush();
			
			byte[] buf = new byte[1024 * 1024];
			int readbytes = -1;
			try {
				readbytes = input.read(buf);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			if (readbytes == -1)
				return null;
			
			return new String(buf, 0, readbytes);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			close();
		}
	}
}
