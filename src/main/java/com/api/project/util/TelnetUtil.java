package com.api.project.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.telnet.TelnetClient;

public class TelnetUtil {

	private TelnetClient telnet;
	private InputStreamReader in;
	private PrintStream out;

	/**
	 * 执行shell提示串的结尾字符
	 */
	private final char[] prompts = { '>', '$', '%', '#' };

	/**
	 * 需要telnet的服务ＩＰ
	 * @throws Exception 
	 */
	public TelnetUtil(String server) throws Exception {
		this(server, 23, -1);
	}

	public TelnetUtil(String server, int timeout) throws Exception {
		this(server, 23, timeout);
	}
	public TelnetUtil(String server, int port, int timeout) throws Exception {
		try {
			telnet = new TelnetClient();
			// Connect to the specified server
			telnet.connect(server, port);
			if (timeout > 0) {
				telnet.setDefaultTimeout(timeout);
				telnet.setSoTimeout(timeout);
			}
			// Get input and output stream references
			in = new InputStreamReader(telnet.getInputStream());
			out = new PrintStream(telnet.getOutputStream());
			
		} catch (Exception e) {
			throw e;
		}
	}
	/**
	 * 
	 * @param server
	 *            IP
	 * @param port
	 *            端口
	 * @param timeout
	 *            单位：秒
	 * @throws Exception 
	 */
	public TelnetUtil(String server, int port, int timeout,String encode) throws Exception {
		try {
			telnet = new TelnetClient();
			// Connect to the specified server
			telnet.connect(server, port);
			if (timeout > 0) {
				telnet.setDefaultTimeout(timeout);
				telnet.setSoTimeout(timeout);
			}
			// Get input and output stream references
			if(encode != null){
				in = new InputStreamReader (telnet.getInputStream(),encode);
				out = new PrintStream(telnet.getOutputStream(),false,encode);
			}else{
				in = new InputStreamReader(telnet.getInputStream());
				out = new PrintStream(telnet.getOutputStream());
			}
			
		} catch (Exception e) {
			throw e;
		}
	}

	public void connect(String user, String password) throws Exception{
		// Log the user on
		readUntil("login:");
		write(user);
		readUntil("Password:");
		write(password);
		readUntil();
	}

	public void su(String password) throws Exception {
		try {
			write("su");
			readUntil("Password: ");
			write(password);
			readUntil();
		} catch (Exception e) {
			throw e;
		}
	}

	public static void main(String[] args) {
		try{
		TelnetUtil telnet = new TelnetUtil("10.12.2.71",23,10);
		System.out.println("*************");
		telnet.connect("godu", "godu@123");
		System.out.println("$$$$$$$$$$$$$$$");
		telnet.write("tail -f /home/godu/test.log");
		while (true) {
			System.out.println(telnet.readUntil("\n",""));
		}
		}catch(Exception e) {
			System.out.println("key---------------");
			e.printStackTrace();
		}
		// telnet.sendCommand("./start.sh");
		// boolean ret = telnet.psProccess("gcp_v6" + "|grep " + "gdau");
		// System.out.println(ret);
	}

	public String readUntil(String pattern) throws Exception{
		char lastChar = pattern.charAt(pattern.length() - 1);
		StringBuffer sb = new StringBuffer();
		char ch = (char) in.read();
		while (true) {
			sb.append(ch);
			if (ch == lastChar) {
				if (sb.toString().endsWith(pattern)) {
					return sb.toString();
				}
			}
			int i = in.read();
			if(i==-1){
				throw new Exception("服务端主动断开连接");
			}
			ch = (char) i;
		}
	}

	public String readUntil(String pattern, String key) throws Exception {
		return readUntil(pattern);
	}

	public String readUntil() throws Exception{
		try {
			StringBuffer sb = new StringBuffer();
			char ch = (char) in.read();
			while (true) {
				sb.append(ch);
				if (isContains(ch, sb.toString())) {
					return sb.toString();
				}
				ch = (char) in.read();
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private boolean isContains(char var, String str) {
		for (char prompt : prompts) {
			if (var == prompt && str.endsWith(prompt + "")) {
				return true;
			}
		}
		return false;
	}

	public void write(String value) throws Exception {
		try {
			out.println(value);
			out.flush();
		} catch (Exception e) {
			throw e;
		}
	}

	public String sendCommand(String command) throws Exception {
		try {
			write(command);
			return readUntil();
		} catch (Exception e) {
			throw e;
		}
	}

	public void sendCommandNoScreen(String command) throws Exception {
		try {
			write(command);
		} catch (Exception e) {
			throw e;
		}
	}

	public void disconnect() throws Exception {
		try {
			telnet.disconnect();
		} catch (Exception e) {
			throw e;
		}
	}

	public void readScreen() throws IOException {
		BufferedReader reader = new BufferedReader(in);
		String value = "";
		try {
			while (!(value = reader.readLine()).equals("")) {
				System.out.println(value);
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public boolean psProccess(String keyWords) throws Exception {
		List<String> pids = new ArrayList<>();
		String result = sendCommand("ps -ef|grep " + keyWords
				+ " |grep -v grep");
		BufferedReader reader = new BufferedReader(new StringReader(result));
		try {
			String value = "";
			while ((value = reader.readLine()) != null) {
				if (StringUtil.isInteger(value)) {
					pids.add(value);
					return true;
				}
			}

		} catch (IOException e) {
			throw e;

		}
		return false;
	}
}
