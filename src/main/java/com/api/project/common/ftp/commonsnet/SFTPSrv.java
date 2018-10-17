package com.api.project.common.ftp.commonsnet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.StringUtil;
import com.api.project.common.ftp.cache.FTPSrvCache;
import com.api.project.util.helper.FileHelper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

public class SFTPSrv extends FTPSrvCache{

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private ChannelSftp ftpClient = null;
	private JSch jsch = null;
	private Session session = null;

	private String host = "";
	private int port = 22;
	private String user = "";
	private String pwd = "";
	private int timeout = 0;
	private String encode = null;
	private String[] kex = null;

	// 取代ip地址作为存放文件的目录
	private String ftpDirName = null;

	public SFTPSrv(String dir) {
		
		super(dir);
	}
    
	public SFTPSrv(String dir, String... kex){
		
		super(dir);
		if(kex != null && kex.length > 0){
			this.kex = kex;
		}
		
	}
	public SFTPSrv(Logger log, String dir) {
		
		super(dir);
		if (log != null) {
			this.log = log;
		}
	}
	public SFTPSrv(String dir, Logger log, String... kex){
		
		super(dir);
		if(log != null){
			this.log = log;
		}
		if(kex != null && kex.length > 0){
			
			this.kex = kex;
		}
	}
	public boolean mkdir(String path) throws Exception {
		ftpClient.mkdir(path);
		return true;
	}
	public boolean mkdir(String path,long timeout) throws Exception {
		ftpClient.mkdir(path);
		return true;
	}
	public boolean mkdir(String path,int timeout) throws Exception {
		ftpClient.mkdir(path);
		return true;
	}
	
	public boolean rename(String src,String to) throws Exception{
		ftpClient.rename(src, to);
		return true;
	}
	public String getRemoteHostIp()throws Exception{
		return host;
	}
	public String getRemoteHostIp(long timeout)throws Exception{
		return host;
	}
	
	public boolean completePendingCommand() throws Exception{
		return true;
	}
	public boolean completePendingCommand(int timeout)throws Exception {
		return true;
	}

	public boolean chdir4Reconn(String dir) throws Exception {
		try {
			ftpClient.cd(dir);
			return true;
		} catch (Exception e) {
			log.error("进入目录[" + host + "][" + dir + "]失败,接下来将有三次以内的重试操作，异常信息："
					+ e.getMessage(), e);
			boolean load = false;
			for (int m = 1; m < 4 && (!load); m++) {
				try {
					Thread.sleep(60 * 1000);
					try {
						logout();
					} catch (Exception e2) {
					}

					createFTPClient();

					ftpClient.cd(dir);
					load = true;
					log.info("sftp重新操作：ftp从[" + host + "]变换目录[" + dir + "]：第["
							+ m + "]次重新登陆成功.");
				} catch (Exception e1) {
					log.info("sftp重新操作：sftp第[" + m + "]次重新从[" + host + "]变换目录["
							+ dir + "]失败:" + e1.getMessage());
				}
			}

			if (!load) {
				throw new Exception("sftp从[" + host + "]变换目录[" + dir + "]失败:"
						+ StringUtil.createStackTrace(e));
			} else {
				return true;
			}
		}
	}

	public String getFtpDirName() {
		return ftpDirName;
	}

	@SuppressWarnings("unchecked")
	public AFtpRemoteFile[] list4Cache(String expr) throws Exception {

		Vector<LsEntry> vector = null;
		try {
			if (expr == null || expr.trim().length() == 0) {
				vector = ftpClient.ls("*");
			} else {
				vector = ftpClient.ls(expr);
			}

			String currDir = ftpClient.pwd() + File.separator;
			AFtpRemoteFile[] rfile = new ASFtpRemoteFile[vector.size()];
			for (int i = 0; i < vector.size(); i++) {
				LsEntry ftpFile = vector.get(i);
				rfile[i] = new ASFtpRemoteFile(ftpFile, ftpClient, currDir);
			}

			return rfile;
		} catch (Exception e) {
			log.error("listFiles error!", e);
			ftpClient = createFTPClient();
		}

		return null;
	}

	public void login(String host, int port, String user, String pwd,
			String encode, int timeout) throws Exception {
		this.host = host;
		this.port = port;
		this.user = user;
		this.pwd = pwd;
		this.timeout = timeout;
		this.encode = encode;
		ftpClient = createFTPClient();
	}

	private static byte[] lock = new byte[0];
	private ChannelSftp createFTPClient() throws Exception {
		synchronized (lock) {
			// 断开连接
			logout();
			// 连接
			jsch = new JSch();
			com.jcraft.jsch.Logger logger = new SettleLogger();
			jsch.setLogger(logger);
			session = jsch.getSession(user, host, port);
			Properties config = new Properties();
			//config.put("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
			config.put("StrictHostKeyChecking", "no");
			config.put("userauth.gssapi-with-mic", "no");
			if(kex != null && kex.length > 0){
				
				for(int i=0; i<kex.length; i++){
					config.put("kex", kex[i]);
				}
			}
			session.setConfig(config);
			//session = jsch.getSession(user, host, port);
			session.setTimeout(timeout);
			session.setUserInfo(new MyUserInfo(pwd));
			session.connect();
			ftpClient = (ChannelSftp) session.openChannel("sftp");
			ftpClient.connect();
			// time
			//增加sftp编码 解决汉语乱码
			if(!StringUtil.isBlank(encode)){
				Class clz = ChannelSftp.class;  
				Field field =clz.getDeclaredField("server_version");  
				field.setAccessible(true);  
				field.set(ftpClient, 2); 
				ftpClient.setFilenameEncoding(encode);
			}
			return ftpClient;
		}
	}

	public void login4Reconn(String host, int port, String user, String pwd,
			String encode, int timeout) throws Exception {
		try {
			this.login(host, port, user, pwd, encode, timeout);
		} catch (Exception e) {
			log.error("连接[" + host + "]失败，下面将有三次重连，每次重连间隔一分钟!", e);

			boolean login = false;
			for (int m = 1; m < 4 && (!login); m++) {
				try {
					Thread.sleep(60 * 1000);
					try {
						logout();
					} catch (Exception e2) {
					}

					this.login(host, port, user, pwd, encode, timeout);
					log.info("sftp重新操作：sftp在第[" + m + "]次重新登陆成功");
					login = true;
				} catch (Exception e1) {
					log.info("sftp重新操作：sftp在第[" + m + "]次重新登陆[" + host + "]失败:"
							+ e1.getMessage());
				}
			}

			if (!login) {
				throw new Exception("连接设备[" + host + "]失败"
						+ StringUtil.createStackTrace(e));
			}
		}
	}

	public AFtpRemoteFile[] nlst4Cache(String expr) throws Exception {
		return list4Cache(expr);
	}

	public String retrive4Cache(AFtpRemoteFile rmtFile, int timeout)
			throws Exception {
		return this.retrive4Cache(rmtFile);
	}

	public boolean retrive(String rfileName, OutputStream toLfileOutput,
			int timeout) throws Exception {
		ftpClient.get(rfileName, toLfileOutput);
		return true;
	}

	public boolean retrive(String rfileName, OutputStream toLfileOutput)
			throws Exception {
		ftpClient.get(rfileName, toLfileOutput);
		return true;
	}

	public boolean retrive(String rfileName, String toLfileName, int timeout)
			throws Exception {
		ftpClient.get(rfileName, toLfileName);
		return true;
	}

	public boolean retrive(String rfileName, String toLfileName)
			throws Exception {
		ftpClient.get(rfileName, toLfileName);
		return true;
	}

	public boolean retrive(String rfileName) throws Exception {
		ftpClient.get(rfileName);
		return true;
	}

	public String retrive4Cache(AFtpRemoteFile rmtFile) throws Exception {

		// 下载远程
		Random rand = new Random();
		int rnd = rand.nextInt(100000);
		if (this.ftpDirName == null || ftpDirName.trim().length() == 0) {
			this.ftpDirName = host;
		}
		String toFile = CACHE_DIR + this.ftpDirName + "/"
				+ rmtFile.getFileName() + "_" + rnd
				+ rmtFile.getModifyDate().getTime();
		try {
			new FileHelper(toFile).enable();
		} catch (Exception e) {

		}
		FileOutputStream fos = new FileOutputStream(toFile);

		try {
			try {
				ftpClient.get(rmtFile.getAbsFileName(), fos);
			} catch (Exception e) {
				boolean load = false;
				log.info("sftp重新操作：sftp从[" + host + "]下载["
						+ rmtFile.getAbsFileName() + "]失败，接下来将有三次以内的重复下载:"
						+ e.getMessage());
				for (int m = 1; m < 4 && (!load); m++) {
					try {
						Thread.sleep(60 * 1000);
						try {
							fos.close();
							fos = null;
							logout();
						} catch (Exception e2) {
							log.info("sftp重新操作：sftp从[" + host + "]下载["
									+ rmtFile.getAbsFileName() + "]失败后，又退出失败:"
									+ e2.getMessage());
						}

						createFTPClient();

						log.info("sftp重新操作：sftp从[" + host + "]下载["
								+ rmtFile.getAbsFileName() + "]：第[" + m
								+ "]次重新登陆成功.");

						fos = new FileOutputStream(toFile);
						ftpClient.get(rmtFile.getAbsFileName(), fos);

						log.info("sftp重新操作：sftp从[" + host + "]下载["
								+ rmtFile.getAbsFileName() + "]：第[" + m
								+ "]次重新下载成功");
						load = true;
					} catch (Exception e1) {
						log.info("sftp重新操作：sftp第[" + m + "]次重新下载["
								+ rmtFile.getAbsFileName() + "]失败:"
								+ e1.getMessage());
					}
				}
				if (!load) {
					throw new Exception("从[" + host + "]下载["
							+ rmtFile.getAbsFileName() + "]失败:"
							+ StringUtil.createStackTrace(e));
				}
			}
		} finally {
			if (fos != null)
				fos.close();
		}
		return toFile;

	}

	public String retriveNoCache(AFtpRemoteFile rmtFile) throws Exception {
		return this.retrive4Cache(rmtFile);
	}

	public String retriveNoCache4TryWhithTemp(AFtpRemoteFile rmtFile) throws Exception {

		// 下载远程
		Random rand = new Random();
		int rnd = rand.nextInt(100000);
		if (this.ftpDirName == null || ftpDirName.trim().length() == 0) {
			this.ftpDirName = host;
		}
		String cacheFile = CACHE_DIR + this.ftpDirName + "/"
				+ rmtFile.getFileName() + "_" + rnd
				+ rmtFile.getModifyDate().getTime();
		String cacheFileTemp = cacheFile + ".temp";
		try {
			new FileHelper(cacheFileTemp).enable();
		} catch (Exception e) {

		}
		FileOutputStream fos = new FileOutputStream(cacheFileTemp);

		try {
			try {
				ftpClient.get(rmtFile.getAbsFileName(), fos);
			} catch (Exception e) {
				boolean load = false;
				log.info("sftp重新操作：sftp从[" + host + "]下载["
						+ rmtFile.getAbsFileName() + "]失败，接下来将有三次以内的重复下载:"
						+ e.getMessage());
				for (int m = 1; m < 4 && (!load); m++) {
					try {
						Thread.sleep(60 * 1000);
						try {
							fos.close();
							fos = null;
							logout();
						} catch (Exception e2) {
							log.info("sftp重新操作：sftp从[" + host + "]下载["
									+ rmtFile.getAbsFileName() + "]失败后，又退出失败:"
									+ e2.getMessage());
						}

						createFTPClient();

						log.info("sftp重新操作：sftp从[" + host + "]下载["
								+ rmtFile.getAbsFileName() + "]：第[" + m
								+ "]次重新登陆成功.");

						fos = new FileOutputStream(cacheFileTemp);
						ftpClient.get(rmtFile.getAbsFileName(), fos);

						log.info("sftp重新操作：sftp从[" + host + "]下载["
								+ rmtFile.getAbsFileName() + "]：第[" + m
								+ "]次重新下载成功");
						load = true;
					} catch (Exception e1) {
						log.info("sftp重新操作：sftp第[" + m + "]次重新下载["
								+ rmtFile.getAbsFileName() + "]失败:"
								+ e1.getMessage());
					}
				}
				if (!load) {
					throw new Exception("从[" + host + "]下载["
							+ rmtFile.getAbsFileName() + "]失败:"
							+ StringUtil.createStackTrace(e));
				}
			}
		} finally {
			if (fos != null)
				fos.close();
		}
		
		File tempFile = new File(cacheFileTemp);
		File toFile = new File(cacheFile);
		tempFile.renameTo(toFile);
		
		return cacheFile;

	
	}
	
	public String retriveNoCache(String remoteDir, String remoteFileName)
			throws Exception {
		// 下载远程
		Random rand = new Random();
		int rnd = rand.nextInt(100000);
		if (this.ftpDirName == null || ftpDirName.trim().length() == 0) {
			this.ftpDirName = host;
		}
		String toFile = CACHE_DIR + this.ftpDirName + "/" + remoteFileName
				+ "_" + rnd;
		try {
			new FileHelper(toFile).enable();
		} catch (Exception e) {

		}
		FileOutputStream fos = new FileOutputStream(toFile);
		ftpClient.get(remoteDir + File.separator + remoteFileName, fos);
		return toFile;
	}

	public void setFtpDirName(String ftpDirName) {
		this.ftpDirName = ftpDirName;
	}

	public void logout() throws Exception {
		if (session != null) {
			try {
				session.disconnect();
			} catch (Exception e) {
			}
		}
		if (ftpClient != null) {
			try {
				ftpClient.disconnect();
			} catch (Exception e) {
			}
		}
	}
	/**
	 * 列出当前工作目录下的所有文件
	 * @param expr 文件名表达式
	 * @param timeout 超时时间（单位：毫秒）
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<ASFtpRemoteFile> list(String expr,int timeout) { 
		List<ASFtpRemoteFile> ftpFileNameList = new ArrayList<>();
		Vector<ChannelSftp.LsEntry> sftpFile = null;
		try {
			sftpFile = ftpClient.ls(expr);
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if(sftpFile == null) {
			return ftpFileNameList;
		}
		LsEntry isEntity = null; 
		Iterator<LsEntry> sftpFileNames = sftpFile.iterator(); 
		while (sftpFileNames.hasNext()) { 
			isEntity = sftpFileNames.next(); 
			if(isNull(isEntity.getFilename())) {
				try {
					ftpFileNameList.add(new ASFtpRemoteFile(isEntity,ftpClient,expr));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} 
		return ftpFileNameList; 
	}
	
	private boolean isNull(String regex) {
		if(regex.equalsIgnoreCase(".") || regex.equalsIgnoreCase("..")) {
			return false;
		}else {
			return true;
		}
	}

	public boolean store(InputStream lfileInput, String toRfileName, int timeout)
			throws Exception {
		ftpClient.put(lfileInput, toRfileName);
		return true;
	}

	public boolean store(InputStream lfileInput, String toRfileName)
			throws Exception {
		ftpClient.put(lfileInput, toRfileName);
		return true;
	}

	public boolean store(String lfileName, String toRfileName, int timeout)
			throws Exception {
		ftpClient.put(lfileName, toRfileName);
		return true;
	}

	public boolean store(String lfileName, String toRfileName) throws Exception {
		ftpClient.put(lfileName, toRfileName);
		return true;
	}
	
	public boolean store4Reconn(String lfileName, String toRfileName) throws Exception{
		try {
			return this.store(lfileName, toRfileName);
		}catch(Exception e) {
			if(e.getMessage() != null && e.getMessage().toLowerCase().indexOf("timeout") >= 0) {


				boolean load = false;
				log.info("sftp重新操作：sftp从[" + host + "]上传[" + lfileName + "]失败，接下来将有三次以内的重复上传:" + e.getMessage());
				for (int m = 1; m < 4 && (!load); m++) {
					try {
						Thread.sleep(60 * 1000);
						try {
							logout();
						} catch (Exception e2) {
						}

						this.login(host, port, user, pwd, encode, timeout);
						log.info("sftp重新操作：sftp从[" + host + "]上传["
								+ lfileName + "]：第[" + m
								+ "]次重新登陆成功.");

						return this.store(lfileName, toRfileName);
					} catch (Exception e1) {
						log.info("sftp重新操作：sftp第[" + m + "]次重新上传["
									+ lfileName + "]失败:"+e1.getMessage());
					}
				}
				if (!load) {
					throw new Exception("从[" + host + "]上传["
							+ lfileName + "]失败:"
							+ StringUtil.createStackTrace(e));
				}
			
			}
			throw e;
		}
	}

	public boolean store(String lfileName) throws Exception {
		ftpClient.put(lfileName);
		return true;
	}

	public void logout(boolean immediate, int timeout) throws Exception {
		logout();
	}

	public void entryPassiveMode() {
	}
	public void entryPassiveMode(int timeout) {
	}

	public String pwd() throws SftpException {
		
		return ftpClient.pwd();
	}

	public boolean chdir(String dir) throws Exception {
		try {
			ftpClient.cd(dir);
			return true;
		} catch (SftpException e) {
			//throw new Exception(e);
			return false;
		}
	}
	public boolean chdir(String dir,int timeout) throws Exception {
		try {
			ftpClient.cd(dir);
			return true;
		} catch (SftpException e) {
			throw new Exception(e);
		}
	}

	@Override
	public FTPSrv cloneFTPSrv() throws Exception {
		throw new Exception("unsurpport");
	}

	@Override
	public AFtpRemoteFile getAFtpRemoteFile(String fileName, int timeout)
			throws Exception {
		throw new Exception("unsurpport");
	}

	@Override
	public AFtpRemoteFile getAFtpRemoteFile(String fileName) throws Exception {
		throw new Exception("unsurpport");
	}

	@Override
	public long getCrtTime() {
		return System.currentTimeMillis();
	}

	@Override
	public String getEncoding() throws Exception {
		throw new Exception("unsurpport");
	}

	@Override
	public String getEncoding(long timeout) throws Exception {
		throw new Exception("unsurpport");
	}

	@Override
	public FTPDaemon getFtpDaemon() {
		return null;
	}

	@Override
	protected AFtpRemoteFile[] getFtpRemoteFiles(String operation, long timeout)
			throws Exception {
		throw new Exception("unsurpport");
	}

	@Override
	public void initFtpDeamon() {
	}

	@Override
	public AFtpRemoteFile[] list() throws Exception {
		return this.list4Cache(null);
	}

	@Override
	public AFtpRemoteFile[] list(long timeout) throws Exception {
		return this.list4Cache(null);
	}

	@Override
	public AFtpRemoteFile[] list(String expr, long timeout) throws Exception {
		return this.list4Cache(null);
	}

	@Override
	public void login(String host, String user, String pwd) throws Exception {
		this.login4Reconn(host, 22, user, pwd, null, 10 * 60 * 1000);
	}

	@Override
	public AFtpRemoteFile[] nlst(String expr, boolean fast, String currDir,
			long timeOffset, long timeout) throws Exception {
		throw new Exception("unsurpport");
	}

	@Override
	public AFtpRemoteFile[] nlst(String expr, long timeout) throws Exception {
		throw new Exception("unsurpport");
	}

	@Override
	public AFtpRemoteFile[] nlst(String expr) throws Exception {
		throw new Exception("unsurpport");
	}

	@Override
	public String pwd(int timeout) throws Exception {
		return ftpClient.pwd();
	}

	@Override
	public InputStream retriveByStream(String rfileName, int timeout)
			throws Exception {
		return ftpClient.get(rfileName);
	}

	@Override
	public InputStream retriveByStream(String rfileName) throws Exception {
		return ftpClient.get(rfileName);
	}

	@Override
	public boolean rmdir(String dir, int timeout) throws Exception {
		ftpClient.rmdir(dir);
		return true;
	}

	@Override
	public boolean rmdir(String dir) throws Exception {
		ftpClient.rmdir(dir);
		return true;
	}

	@Override
	public boolean rmfile(String filepath, int timeout) throws Exception {
		ftpClient.rm(filepath);
		return true;
	}

	@Override
	public boolean rmfile(String filepath) throws Exception {
		ftpClient.rm(filepath);
		return true;
	}

	@Override
	public OutputStream storeByStream(String rfileName, int timeout)
			throws Exception {
		return ftpClient.put(rfileName);
	}

	@Override
	public OutputStream storeByStream(String rfileName) throws Exception {
		return ftpClient.put(rfileName);
	}

	/**
	 * 登录ssh用户信息
	 */
	private static class MyUserInfo implements UserInfo {
		protected String passwd = null;

		public MyUserInfo(String passwd) {
			this.passwd = passwd;
		}

		public String getPassword() {
			return passwd;
		}

		public String getPassphrase() {
			return null;
		}

		public void showMessage(String message) {
		}

		public boolean promptYesNo(String str) {
			return true;
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		public boolean promptPassword(String message) {
			return true;
		}
	}
	
	class SettleLogger implements com.jcraft.jsch.Logger{

		public boolean isEnabled(int level) {
			// TODO Auto-generated method stub
			return true;
		}

		public void log(int level, String msg) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
