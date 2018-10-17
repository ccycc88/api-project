package com.api.project.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHUtil {

	private Connection conn;

	private Session session;

	private String ip;

	private String usr;

	private String psword;
	private String charset = "GB2312";

	private int TIME_OUT = 1000 * 5 * 60;

	public SSHUtil(String ip, String usr, String ps) {
		this.ip = ip;
		this.usr = usr;
		this.psword = ps;
	}

	public SSHUtil(String ip, String usr, String ps, int TIME_OUT) {
		this.ip = ip;
		this.usr = usr;
		this.psword = ps;
		this.TIME_OUT = TIME_OUT;
	}

	/** */
	/**
	 * login
	 * 
	 * @return
	 * @throws IOException
	 */
	public boolean login() throws Exception {
		for (int i = 0; i < 3; i++) {
			try {
				conn = new Connection(ip);
				conn.connect();
				return conn.authenticateWithPassword(usr, psword);
			} catch (IOException e) {
				if (conn != null) {
					conn.close();
				}
				conn = null;
				if (i == 2) {
					throw new Exception("IOException" + e.getMessage());
				}
				// throw new GcpException("IOException" + e.getMessage());
				try {
					Thread.currentThread().sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		return false;

	}

	/** */
	/**
	 * exec
	 * 
	 * @param cmds
	 * @return
	 * @throws Exception
	 */
	public String exec(String cmds) throws Exception {
		InputStream stdOut = null;
		InputStream stdErr = null;
		String outStr = "";
		String outErr = "";
		int ret = -1;
		try {

			// Open a new {@link Session} on this connection

			session = conn.openSession();

			// Execute a command on the remote machine.
			session.execCommand(cmds);

			stdOut = new StreamGobbler(session.getStdout());
			outStr = processStream(stdOut, charset);

			stdErr = new StreamGobbler(session.getStderr());
			outErr = processStream(stdErr, charset);

			session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);

			// System.out.println("outStr=" + outStr);
			// System.out.println("outErr=" + outErr);

			ret = session.getExitStatus();

			session.close();
		} catch (Exception e) {
			throw new Exception("exec exception" + e);
		} finally {

			IOUtils.closeQuietly(stdOut);
			IOUtils.closeQuietly(stdErr);
		}
		return ret + "out:" + outStr + " error:" + outErr;
	}

	/** */
	/**
	 * exec
	 * 
	 *  cmds
	 * @return
	 * @throws Exception
	 */
	public String exec(List<String> cmdList) throws Exception {
		InputStream stdOut = null;
		InputStream stdErr = null;
		String outStr = "";
		String outErr = "";
		int ret = -1;
		try {

			// Open a new {@link Session} on this connection

			session = conn.openSession();

			for (String cmds : cmdList) {
				// Execute a command on the remote machine.
				session.execCommand(cmds);
				stdOut = new StreamGobbler(session.getStdout());
				outStr = processStream(stdOut, charset);

				stdErr = new StreamGobbler(session.getStderr());
				outErr = processStream(stdErr, charset);

				session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);

				System.out.println("outStr=" + outStr);
				System.out.println("outErr=" + outErr);
			}

			ret = session.getExitStatus();

			session.close();
		} catch (Exception e) {
			throw new Exception("exec exception" + e);
		} finally {

			IOUtils.closeQuietly(stdOut);
			IOUtils.closeQuietly(stdErr);
		}
		return ret + "out:" + outStr + " error:" + outErr;
	}

	public void close() {
		if (session != null) {
			session.close();
		}
		if (conn != null) {
			conn.close();
		}
	}

	/** */
	/**
	 * @param in
	 * @param charset
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private String processStream(InputStream in, String charset) throws Exception {
		StringBuilder sb = new StringBuilder();
		try {
			byte[] buf = new byte[1024];

			while (in.read(buf) != -1) {
				sb.append(new String(buf));
			}
		} catch (UnsupportedEncodingException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		}
		return sb.toString();
	}

	public static void main(String args[]) throws Exception {
		SSHUtil exe = new SSHUtil("10.0.3.67", "gcp", "gcp");
		boolean loginFlag = exe.login();

		List<String> cmdList = new ArrayList<String>();
		System.out.println(exe.exec("cd /opt/gcp/temp;ls"));
		System.out.println(exe.exec("cd /opt/gcp/temp; unzip GcpCollector.zip"));
		System.out.println(exe.exec("cd /opt/gcp/temp/GcpCollector; ls"));

	}
}
