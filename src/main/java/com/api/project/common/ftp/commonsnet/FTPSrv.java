package com.api.project.common.ftp.commonsnet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.api.project.util.helper.SysHelper;

public class FTPSrv {

	private FTPDaemon ftpDaemon = null;

	public int timeout;
	public int port;
	public String host;
	public String user;
	public String password;
	public String encode;
	public String dir;
	public boolean isPassiveMode = false;

	private long crtTime = 0; // ftp 创建时间

	public FTPSrv() {
		initFtpDeamon();
	}

	public void initFtpDeamon() {
		ftpDaemon = new FTPDaemon();
		ftpDaemon.start();
		crtTime = System.currentTimeMillis();
	}

	/**
	 * 登陆FTP
	 * 
	 * @param host
	 * @param port
	 * @param user
	 * @param pwd
	 * @param encode
	 * @param timeout
	 * @throws Exception
	 */
	public void login(String host, int port, String user, String pwd, String encode, int timeout) throws Exception {
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = pwd;
		this.encode = encode;
		this.timeout = timeout;
		String[] args = new String[] { host, String.valueOf(port), user, pwd, encode, String.valueOf(timeout) };
		ftpDaemon.setName(ftpDaemon.getName() + "(" + host + ":" + port + ")");
		ftpDaemon.doIt(FTPDaemon.LOGIN, args);
		getReturnValue("login", timeout);
	}

	public long getCrtTime() {
		return crtTime;
	}

	/**
	 * 登陆FTP（默认端口21、编码GBK、超时2分钟）
	 * 
	 * @param host
	 * @param user
	 * @param pwd
	 * @throws Exception
	 */
	public void login(String host, String user, String pwd) throws Exception {
		login(host, 21, user, pwd, "GBK", 1000 * 60 * 2);
	}

	/**
	 * 登出FTP
	 * 
	 * @param immediate
	 *            是否立即关闭连接
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @throws Exception
	 */
	public void logout(boolean immediate, int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.LOGOUT, new String[] { String.valueOf(immediate) });
		try {
			getReturnValue("logout", timeout);
		} catch (Exception e) {
			throw e;
		} finally {
			ftpDaemon.interrupt();
		}
	}

	/**
	 * 登出FTP（默认超时10秒）
	 * 
	 * @throws Exception
	 */
	public void logout() throws Exception {
		logout(false, 5000);
	}

	/**
	 * 改为被动模式
	 * 
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @throws Exception
	 */
	public void entryPassiveMode(int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.ENTRY_PASSIVE_MODE, null);
		getReturnValue("entryPassiveMode", timeout);
		isPassiveMode = true;
	}

	/**
	 * 改为被动模式（默认超时10秒）
	 * 
	 * @throws Exception
	 */
	public void entryPassiveMode() throws Exception {
		entryPassiveMode(1000 * 30);
	}

	/**
	 * 获取远程主机IP
	 * 
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 */
	public String getRemoteHostIp(long timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.GET_REMOTE_HOST_IP, null);
		return getReturnValue("getRemoteHostIp", timeout);
	}

	/**
	 * 获取远程主机IP（默认超时10秒）
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getRemoteHostIp() throws Exception {
		return getRemoteHostIp(1000 * 30);
	}

	/**
	 * 获取encoding
	 * 
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public String getEncoding(long timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.GET_ENCODING, null);
		return getReturnValue("getEncoding", timeout);
	}

	/**
	 * 获取encoding（默认超时10秒）
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getEncoding() throws Exception {
		return getEncoding(1000 * 30);
	}

	/**
	 * 删除指定目录
	 * 
	 * @param dir
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public boolean rmdir(String dir, int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.REMOVE_DIR, new String[] { dir });
		if ("true".equalsIgnoreCase(getReturnValue("rmdir", timeout))) {
			return true;
		}
		return false;
	}

	public boolean rename(String srcName, String to) throws Exception {
		ftpDaemon.doIt(FTPDaemon.RENAME, new String[] { srcName, to });
		if ("true".equalsIgnoreCase(getReturnValue("rename", timeout))) {
			return true;
		}
		return false;
	}

	/**
	 * 删除指定目录（默认超时10秒）
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public boolean rmdir(String dir) throws Exception {
		return rmdir(dir, 1000 * 30);
	}

	/**
	 * 删除指定文件
	 * 
	 * @param filepath
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public boolean rmfile(String filepath, int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.REMOVE_FILE, new String[] { filepath });
		if ("true".equalsIgnoreCase(getReturnValue("rmfile", timeout))) {
			return true;
		}
		return false;
	}

	/**
	 * 删除指定文件（默认超时10秒）
	 * 
	 * @param filepath
	 * @return
	 * @throws Exception
	 */
	public boolean rmfile(String filepath) throws Exception {
		return rmfile(filepath, 1000 * 30);
	}

	/**
	 * 创建目录
	 * 
	 * @param dir
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public boolean mkdir(String dir, int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.MAKE_DIR, new String[] { dir });
		if ("true".equalsIgnoreCase(getReturnValue("mkdir", timeout))) {
			return true;
		}
		return false;
	}

	/**
	 * 创建目录（默认超时10秒）
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public boolean mkdir(String dir) throws Exception {
		return mkdir(dir, 1000 * 30);
	}

	/**
	 * 改变当前工作目录
	 * 
	 * @param dir
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public boolean chdir(String dir, int timeout) throws Exception {
		this.dir = dir;
		ftpDaemon.doIt(FTPDaemon.CHANGE_DIR, new String[] { dir });
		if ("true".equalsIgnoreCase(getReturnValue("chdir", timeout))) {
			return true;
		}
		return false;
	}

	/**
	 * 改变当前工作目录（默认超时10秒）
	 * 
	 * @param dir
	 * @return
	 * @throws Exception
	 */
	public boolean chdir(String dir) throws Exception {
		return chdir(dir, 1000 * 30);
	}

	/**
	 * 得到当前路径
	 * 
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public String pwd(int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.PWD, null);
		return getReturnValue("pwd", timeout);
	}

	/**
	 * 得到当前路径（默认超时10秒）
	 * 
	 * @return
	 * @throws Exception
	 */
	public String pwd() throws Exception {
		return pwd(1000 * 30);
	}

	/**
	 * 列出当前工作目录下的所有文件
	 * 
	 * @param expr
	 *            文件名表达式
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public AFtpRemoteFile[] list(String expr, long timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.LIST, new String[] { expr });
		return getFtpRemoteFiles("list", timeout);
	}

	/**
	 * 列出当前工作目录下的所有文件
	 * 
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public AFtpRemoteFile[] list(long timeout) throws Exception {
		return list(null, timeout);
	}

	/**
	 * 列出当前工作目录下的所有文件（默认10分钟）
	 * 
	 * @return
	 * @throws Exception
	 */
	public AFtpRemoteFile[] list() throws Exception {
		return list(null, 1000 * 60 * 10);
	}

	/**
	 * 获取远程指定文件
	 * 
	 * @param fileName
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public AFtpRemoteFile getAFtpRemoteFile(String fileName, int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.GET_REMOTE_FILE, new String[] { fileName });
		long startTime = System.currentTimeMillis();
		while (true) {
			SysHelper.waitIt(this, 50);
			if (!ftpDaemon.isDoIt()) {
				if (ftpDaemon.getException() != null) {
					ftpDaemon.interrupt();
					throw ftpDaemon.getException();
				} else {
					return ftpDaemon.getFtpRemoteFile();
				}
			}

			/** 检查是否超时 */
			if (System.currentTimeMillis() - startTime > timeout) {
				ftpDaemon.interrupt();
				throw new Exception("ftp operation[getAFtpRemoteFile] timeout [" + timeout + "] milliseconds");
			}
		}
	}

	/**
	 * 获取远程指定文件（默认10分钟）
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public AFtpRemoteFile getAFtpRemoteFile(String fileName) throws Exception {
		return getAFtpRemoteFile(fileName, 1000 * 60 * 10);
	}

	/**
	 * 列出当前工作目录下的所有文件
	 * 
	 * @param expr
	 * @param fast
	 * @param currDir
	 * @param timeOffset
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public AFtpRemoteFile[] nlst(String expr, boolean fast, String currDir, long timeOffset, long timeout)
			throws Exception {
		ftpDaemon.doIt(FTPDaemon.NLST,
				new String[] { expr, String.valueOf(fast), currDir, String.valueOf(timeOffset) });
		return getFtpRemoteFiles("nlst", timeout);
	}

	/**
	 * 列出当前工作目录下的所有文件（默认超时10分钟）
	 * 
	 * @param expr
	 * @return
	 * @throws IOException
	 */
	public AFtpRemoteFile[] nlst(String expr) throws Exception {
		return nlst(expr, false, null, -1, 1000 * 60 * 10);
	}

	public AFtpRemoteFile[] nlst(String expr, long timeout) throws Exception {
		return nlst(expr, false, null, -1, timeout);
	}

	/**
	 * 上传文件
	 * 
	 * @param lfileInput
	 * @param toRfileName
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public boolean store(InputStream lfileInput, String toRfileName, int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.STORE, new String[] { toRfileName }, lfileInput);
		if ("true".equalsIgnoreCase(getReturnValue("store", timeout))) {
			return true;
		}
		return false;
	}

	/**
	 * 上传文件（默认超时10分钟）
	 * 
	 * @param lfileInput
	 * @param toRfileName
	 * @return
	 * @throws Exception
	 */
	public boolean store(InputStream lfileInput, String toRfileName) throws Exception {
		return store(lfileInput, toRfileName, 1000 * 60 * 10);
	}

	/**
	 * 上传文件
	 * 
	 * @param lfileName
	 * @param toRfileName
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public boolean store(String lfileName, String toRfileName, int timeout) throws Exception {
		FileInputStream lfileInput = null;
		try {
			lfileInput = new FileInputStream(lfileName);
			return store(lfileInput, toRfileName, timeout);
		} catch (Exception ioe) {
			throw ioe;
		} finally {
			if (lfileInput != null)
				lfileInput.close();
		}
	}

	/**
	 * 上传文件（默认超时10分钟）
	 * 
	 * @param lfileName
	 * @param toRfileName
	 * @return
	 * @throws Exception
	 */
	public boolean store(String lfileName, String toRfileName) throws Exception {
		return store(lfileName, toRfileName, 1000 * 60 * 10);
	}

	/**
	 * 上传文件（默认超时10分钟，文件名与本地文件相同）
	 * 
	 * @param lfileName
	 * @return
	 * @throws Exception
	 */
	public boolean store(String lfileName) throws Exception {
		return store(lfileName, new File(lfileName).getName());
	}

	public boolean replycode(int timeout) throws Exception {

		ftpDaemon.doIt(FTPDaemon.GET_REPLYCODE, null);
		if ("true".equalsIgnoreCase(getReturnValue("replycode", timeout))) {
			return true;
		}
		return false;
	}

	public boolean replycode() throws Exception {

		return this.replycode(1000 * 5);
	}

	/**
	 * 下载文件
	 * 
	 * @param rfileName
	 * @param toLfileOutput
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public boolean retrive(String rfileName, OutputStream toLfileOutput, int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.RETRIVE, new String[] { rfileName }, toLfileOutput);
		if ("true".equalsIgnoreCase(getReturnValue("retrive", timeout))) {
			return true;
		}
		return false;
	}

	/**
	 * 下载文件（默认超时10分钟）
	 * 
	 * @param rfileName
	 * @param toLfileOutput
	 * @return
	 * @throws Exception
	 */
	public boolean retrive(String rfileName, OutputStream toLfileOutput) throws Exception {
		return retrive(rfileName, toLfileOutput, 1000 * 60 * 10);
	}

	/**
	 * 下载文件
	 * 
	 * @param rfileName
	 * @param toLfileName
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public boolean retrive(String rfileName, String toLfileName, int timeout) throws Exception {
		FileOutputStream toLfileOutput = null;
		try {
			toLfileOutput = new FileOutputStream(toLfileName);
			return retrive(rfileName, toLfileOutput, timeout);
		} catch (Exception ioe) {
			throw ioe;
		} finally {
			if (toLfileOutput != null)
				toLfileOutput.close();
		}
	}

	/**
	 * 下载文件（默认超时10分钟）
	 * 
	 * @param rfileName
	 * @param toLfileName
	 * @return
	 * @throws Exception
	 */
	public boolean retrive(String rfileName, String toLfileName) throws Exception {
		return retrive(rfileName, toLfileName, 1000 * 60 * 10);
	}

	/**
	 * 下载文件（默认超时10分钟，文件名与远程文件相同）
	 * 
	 * @param rfileName
	 * @return
	 * @throws Exception
	 */
	public boolean retrive(String rfileName) throws Exception {
		return retrive(rfileName, new File(rfileName).getName());
	}

	/**
	 * 获取远程输入流
	 * 
	 * @param rfileName
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public InputStream retriveByStream(String rfileName, int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.RETRIVE_BY_STREAM, new String[] { rfileName });
		long startTime = System.currentTimeMillis();
		while (true) {
			SysHelper.waitIt(this, 50);
			if (!ftpDaemon.isDoIt()) {
				if (ftpDaemon.getException() != null) {
					ftpDaemon.interrupt();
					throw ftpDaemon.getException();
				} else {
					return ftpDaemon.getInputStream();
				}
			}

			/** 检查是否超时 */
			if (System.currentTimeMillis() - startTime > timeout) {
				ftpDaemon.interrupt();
				throw new Exception("ftp operation[retriveByStream] timeout [" + timeout + "] milliseconds");
			}
		}
	}

	/**
	 * 获取远程输入流（默认超时10分钟）
	 * 
	 * @param rfileName
	 * @return
	 * @throws Exception
	 */
	public InputStream retriveByStream(String rfileName) throws Exception {
		return retriveByStream(rfileName, 1000 * 60 * 10);
	}

	/**
	 * 获取远程输出流
	 * 
	 * @param rfileName
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public OutputStream storeByStream(String rfileName, int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.STORE_BY_STREAM, new String[] { rfileName });
		long startTime = System.currentTimeMillis();
		while (true) {
			SysHelper.waitIt(this, 50);
			if (!ftpDaemon.isDoIt()) {
				if (ftpDaemon.getException() != null) {
					ftpDaemon.interrupt();
					throw ftpDaemon.getException();
				} else {
					return ftpDaemon.getOutputStream();
				}
			}

			/** 检查是否超时 */
			if (System.currentTimeMillis() - startTime > timeout) {
				ftpDaemon.interrupt();
				throw new Exception("ftp operation[storeByStream] timeout [" + timeout + "] milliseconds");
			}
		}
	}

	/**
	 * 获取远程输出流（默认超时10分钟）
	 * 
	 * @param rfileName
	 * @return
	 * @throws Exception
	 */
	public OutputStream storeByStream(String rfileName) throws Exception {
		return storeByStream(rfileName, 1000 * 60 * 10);
	}

	/**
	 * 命令结束
	 * 
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	public boolean completePendingCommand(int timeout) throws Exception {
		ftpDaemon.doIt(FTPDaemon.COMPLETE, null);
		if ("true".equalsIgnoreCase(getReturnValue("completePendingCommand", timeout))) {
			return true;
		}
		return false;
	}

	/**
	 * 设置文件属性配置
	 * 
	 * @param timeout
	 * @throws Exception
	 */
	public void configure(int timeout) throws Exception {

		ftpDaemon.doIt(FTPDaemon.CONFIGURE, null);
		getReturnValue("configure", timeout);
	}

	/**
	 * 增加配置默认10秒
	 * 
	 * @throws Exception
	 */
	public void Configure() throws Exception {

		this.configure(10 * 1000);
	}

	/**
	 * 命令结束（默认超时10秒）
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean completePendingCommand() throws Exception {
		return completePendingCommand(1000 * 30);
	}

	/**
	 * 获取FTP操作的返回值
	 * 
	 * @param operation
	 *            操作名称
	 * @param timeout
	 *            超时时间（单位：毫秒）
	 * @return 当无返回值时，返回null
	 * @throws Exception
	 */
	private String getReturnValue(String operation, long timeout) throws Exception {
		long startTime = System.currentTimeMillis();
		while (true) {
			SysHelper.waitIt(this, 50);
			if (!ftpDaemon.isDoIt()) {
				if (ftpDaemon.getException() != null) {
					ftpDaemon.interrupt();
					throw ftpDaemon.getException();
				} else {
					return ftpDaemon.getReturnValue();
				}
			}

			/** 检查是否超时 */
			if (System.currentTimeMillis() - startTime > timeout) {
				ftpDaemon.interrupt();
				throw new Exception("ftp operation[" + operation + "] timeout [" + timeout + "] milliseconds");
			}
		}
	}

	/**
	 * 克隆FTP连接
	 * 
	 * @return
	 * @throws Exception
	 */
	public FTPSrv cloneFTPSrv() throws Exception {
		FTPSrv ftpSrv = new FTPSrv();
		ftpSrv.login(host, port, user, password, encode, timeout);
		ftpSrv.chdir(dir);
		if (ftpDaemon.isPassiveMode()) {
			ftpSrv.getFtpDaemon().setPassiveMode(true);
		}
		return ftpSrv;
	}

	/**
	 * 列出当前工作目录下的所有文件
	 * 
	 * @param operation
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	protected AFtpRemoteFile[] getFtpRemoteFiles(String operation, long timeout) throws Exception {
		long startTime = System.currentTimeMillis();
		while (true) {
			SysHelper.waitIt(this, 50);
			if (!ftpDaemon.isDoIt()) {
				if (ftpDaemon.getException() != null) {
					ftpDaemon.interrupt();
					throw ftpDaemon.getException();
				} else {
					return ftpDaemon.getFtpRemoteFiles();
				}
			}

			/** 检查是否超时 */
			if (System.currentTimeMillis() - startTime > timeout) {
				ftpDaemon.interrupt();
				throw new Exception("ftp operation[" + operation + "] timeout [" + timeout + "] milliseconds");
			}
		}
	}

	public FTPDaemon getFtpDaemon() {
		return ftpDaemon;
	}

	public static void main(String[] args) throws Exception {
		/*
		 * FTPSrv ftpSrv = new FTPSrv(); ftpSrv.login("10.0.2.50", "gcp", "gcp");
		 * System.out.println(ftpSrv.getRemoteHostIp());
		 * System.out.println(ftpSrv.getEncoding()); System.out.println(ftpSrv.pwd());
		 * //System.out.println(ftpSrv.chdir("/home/gdau/test"));
		 * System.out.println(ftpSrv.pwd());
		 * //System.out.println(ftpSrv.store("d:/dnms.sql"));
		 * System.out.println(ftpSrv.retrive("~/check/checkCpuAndDisk1.jar",
		 * "d:/bbb.xml")); System.out.println(ftpSrv.list()[0].getFileName());
		 * ftpSrv.logout();
		 */
		// String aaa ="/ccc/aaaa";
		// System.out.println(aaa.substring(0,aaa.lastIndexOf("/")+1));
		// FTPSrv srv = new FTPSrv();
		// srv.login("192.168.6.46", 21, "gcp", "gcp", "gbk", 1000*60*2);
		// //srv.login("192.168.3.33", "gcp", "gcp");
		// ///opt/Gcp/datasource/ftptest/
		// srv.chdir("/opt/Gcp/GcpServerCore/cfg");
		// AFtpRemoteFile[] list = srv.list();

		// for(AFtpRemoteFile file : list){
		//
		// System.out.println(file.getFileName());
		// }

		// System.out.println(srv.pwd());
		// AFtpRemoteFile[] afs = srv.list("/opt/Gcp/Ftp4HUAWEIWAP/cmd/listen.sh",
		// 1000);
		//
		// for(AFtpRemoteFile af : afs){
		//
		// System.out.println(af.getAbsFileName());
		// }

		// int i=0;
		// while(true){
		//
		// i++;
		// File file = new
		// File("C:\\Users\\Death\\Desktop\\ceshidy\\[欧美][剧情][幸福终点站][高清RMVB][1280×720][中英双字幕]
		// - 副本 - 副本.RMVB");
		// InputStream in = new FileInputStream(file);
		// boolean ret = srv.store(in, file.getName() + "_" + i);
		//
		// if(ret == false){
		//
		// System.out.println("下载失败。");
		// }
		// in.close();
		// in = null;
		// }
	}
}
