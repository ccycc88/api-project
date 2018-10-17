package com.api.project.common.ftp.commonsnet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;

import com.api.project.common.ftp.ExtendsDefaultFTPFileEntryParserFactory;

public class FTPDaemon extends Thread{

private boolean run = true;
	
	private boolean passiveMode = false;

	private boolean doIt = false; //是否执行

	private int type; //操作类型

	private String returnValue = null; //返回值

	private AFtpRemoteFile[] ftpRemoteFiles = null;

	private AFtpRemoteFile ftpRemoteFile = null;

	private String[] args = null; //参数
	
	private InputStream inputStream = null;

	private OutputStream outputStream = null;

	private FTPClient ftpClient = null; //Apache FTP客户端

	private Exception exception = null; //异常

	public final static int LOGIN = 1;

	public final static int LOGOUT = 2;

	public final static int ENTRY_PASSIVE_MODE = 3;
	
	public final static int GET_REMOTE_HOST_IP = 4;
	
	public final static int GET_ENCODING = 5;
	
	public final static int REMOVE_DIR = 6;
	
	public final static int MAKE_DIR = 7;
	
	public final static int CHANGE_DIR = 8;
	
	public final static int PWD = 9;
	
	public final static int LIST = 10;
	
	public final static int GET_REMOTE_FILE = 11;
	
	public final static int NLST = 12;
	
	public final static int STORE = 13;
	
	public final static int RETRIVE = 14;
	
	public final static int RETRIVE_BY_STREAM = 15;
	
	public final static int STORE_BY_STREAM = 16;
	
	public final static int COMPLETE = 17;
	
	public final static int REMOVE_FILE = 18;
	
	public final static int RENAME = 19;
	
	public final static int CONFIGURE = 20;
	
	public final static int GET_REPLYCODE = 21;

	public void run() {
		while (run) {
			try {
				sleep(100);
			} catch (InterruptedException e) {
			}
			if (!run)
				continue;
			if(!doIt){
				continue;
			}
			try{
				switch (type) {
				case LOGIN: {
					ftpClient = new FTPClient();
					//add for huawei Dopra linux OS
					ftpClient.setParserFactory(new ExtendsDefaultFTPFileEntryParserFactory());
					if(args[4]!=null && args[4].trim().length()>0){
						ftpClient.setControlEncoding(args[4]);
					}
					ftpClient.connect(args[0], Integer.parseInt(args[1]));
					ftpClient.setRemoteVerificationEnabled(false);
					ftpClient.setSoTimeout(Integer.parseInt(args[5]));
					if(!ftpClient.login(args[2], args[3])){
						throw new Exception("login["+args[0]+"],port["+args[1]+"] fail, please check user and password");
					}
//					System.out.println(this.getClass()+" ftp conn ["+args[0]+"],port["+args[1]+"] succ!");
					break;
				}
				case LOGOUT: {
					if(ftpClient != null){
						try {
							ftpClient.logout();
						}catch(Exception e){
						}
						
						if ("true".equalsIgnoreCase(args[0])) {
//							try {
//								ftpClient.closeSocket1();
//							} catch (Exception e) {
//							}
							try {
								ftpClient.disconnect();
							}catch(Exception e) {
							}
						}else {
							try {
								ftpClient.disconnect();
							}catch(Exception e) {
							}
//							try {
//								ftpClient.closeSocket1();
//							} catch (Exception e) {
//							}
						}
						ftpClient = null;
					}
					break;
				}
				case ENTRY_PASSIVE_MODE: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before entryPassiveMode");
					}else{
						ftpClient.enterLocalPassiveMode();
						passiveMode = true;
					}
					break;
				}
				case GET_REMOTE_HOST_IP: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before getRemoteHostIp");
					}else{
						returnValue = ftpClient.getRemoteAddress().getHostAddress();
					}
					break;
				}
				case GET_ENCODING: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before getEncoding");
					}else{
						returnValue = ftpClient.getControlEncoding();
					}
					break;
				}
				case REMOVE_DIR: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before rmdir");
					}else{
						if(ftpClient.removeDirectory(args[0])){
							returnValue = "true";
						}else{
							returnValue = "false";
						}
					}
					break;
				}
				case REMOVE_FILE: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before rmfile");
					}else{
						if(ftpClient.deleteFile(args[0])){
							returnValue = "true";
						}else{
							returnValue = "false";
						}
					}
					break;
				}
				case RENAME: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before rmfile");
					}else{
						if(ftpClient.rename(args[0], args[1])){
							returnValue = "true";
						}else{
							returnValue = "false";
						}
					}
					break;
				}
				case MAKE_DIR: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before mkdir");
					}else{
						if(ftpClient.makeDirectory(args[0])){
							returnValue = "true";
						}else{
							returnValue = "false";
						}
					}
					break;
				}
				case CHANGE_DIR: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before chdir");
					}else{
						if(ftpClient.changeWorkingDirectory(args[0])){
							returnValue = "true";
						}else{
							returnValue = "false";
						}
					}
					break;
				}
				case PWD: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before pwd");
					}else{
						returnValue = ftpClient.printWorkingDirectory();
					}
					break;
				}
				case LIST: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before list");
					}else{
						String currdir = ftpClient.printWorkingDirectory();
						if (currdir.endsWith("/") == false) {
							currdir = currdir + "/";
						}
						FTPFile[] rfileList = null;
						ftpClient.listNames(currdir);
						if(args[0]==null || args[0].length()==0){
							rfileList = ftpClient.listFiles(currdir);
						}else{
							//为了兼容Apache 2.0的包 commons-net-2.0.jar 2016-1-11
//							rfileList = ftpClient.exprListFiles(currdir, args[0]);
							ftpClient.changeWorkingDirectory(currdir);
							rfileList = ftpClient.listFiles(args[0]);
						}
						ftpRemoteFiles = new AFtpRemoteFile[rfileList.length];
						for (int i=0; i<rfileList.length; i++){
							ftpRemoteFiles[i] = new AFtpRemoteFile(rfileList[i], ftpClient, currdir);
						}
					}
					break;
				}
				case GET_REMOTE_FILE: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before getAFtpRemoteFile");
					}else{
						FTPFile[] rfileList = ftpClient.listFiles(args[0]);
						if(rfileList==null || rfileList.length<1 || rfileList[0]==null){
							ftpRemoteFile = null;
						}else{
							ftpRemoteFile = new AFtpRemoteFile(rfileList[0], ftpClient, ftpClient.printWorkingDirectory());
						}
					}
					break;
				}
				case NLST: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before nlst");
					}else{
						
						String names[] = ftpClient.listNames(args[0]);
						String currdir = ftpClient.printWorkingDirectory();
						if (currdir.endsWith("/") == false) {
							currdir = currdir + "/";
						}
						// 快速遍历，不获取文件属性。
						if ("true".equalsIgnoreCase(args[1])) {
							Calendar c = Calendar.getInstance();
							c.setTimeInMillis(System.currentTimeMillis() + Long.parseLong(args[3]));
							
							ftpRemoteFiles = new AFtpRemoteFile[names.length];
							for (int i=0; i<names.length; i++) {
								FTPFile fFile = new FTPFile();
								fFile.setGroup("");
								fFile.setName(names[i]);
								fFile.setSize(0);
								fFile.setTimestamp(c);
								fFile.setType(0);
								fFile.setUser("Unknown");
								ftpRemoteFiles[i] = new AFtpRemoteFile(fFile, ftpClient, currdir);
							}
						}else{
							// 完全遍历
							List<AFtpRemoteFile> rfileList = new ArrayList<>();
							for (int i=0; names!=null&&i<names.length; i++) {
								FTPFile[] rfile = ftpClient.listFiles(names[i]);
								if (rfile!=null && rfile.length>0)
									rfileList.add(new AFtpRemoteFile(rfile[0], ftpClient, currdir));
							}
							
							if (rfileList.size() <= 0){
								ftpRemoteFiles = new AFtpRemoteFile[0];
							}else{
								ftpRemoteFiles = new AFtpRemoteFile[rfileList.size()];
								rfileList.toArray(ftpRemoteFiles);
							}
						}
					}
					break;
				}
				case STORE: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before store");
					}else{
						ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
						if (ftpClient.storeFile(args[0], inputStream)){
							returnValue = "true";
						}else{
							returnValue = "false";
						}
						
					}
					break;
				}
				case RETRIVE: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before retrive");
					}else{
						ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
						if (ftpClient.retrieveFile(args[0], outputStream)){
							returnValue = "true";
						}else{
							returnValue = "false";
						}
						
					}
					break;
				}
				case RETRIVE_BY_STREAM: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before retriveByStream");
					}else{
						ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
						inputStream = ftpClient.retrieveFileStream(args[0]);
					}
					break;
				}
				case STORE_BY_STREAM: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before storeByStream");
					}else{
						ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
						outputStream = ftpClient.storeFileStream(args[0]);
					}
					break;
				}
				case COMPLETE: {
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before completePendingCommand");
					}else{
						if(ftpClient.completePendingCommand()){
							returnValue = "true";
						}else{
							returnValue = "false";
						}
					}
					break;
				}
				case CONFIGURE : {
					
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before completePendingCommand");
					}else{
						ftpClient.configure(new FTPClientConfig("ExtendsConfigurableFTPFileEntryParserImpl"));
					    returnValue = "true";
					}
					break;
				}
				case GET_REPLYCODE : {
					
					if(ftpClient == null){
						exception = new Exception("Please login with user and password before completePendingCommand");
					}else{
						
						//后期再加吧
						if(ftpClient.getReplyCode() == 550){
							returnValue = "false";
						}else{
							returnValue = "true";
						}
					    
					}
					break;
				}
				default:
					break;

				}
			}catch(Exception e){
				exception = e;
			}finally{
				doIt = false;
			}
		}
	}
	
	public void interrupt(){
		run = false;
		if(ftpClient != null){
			try {
				ftpClient.logout();
			} catch (Exception e) {
			}
//			try {
//				ftpClient.closeSocket1();
//			} catch (Exception e) {
//			}
			try {
				ftpClient.disconnect();
			}catch(Exception e) {
			}
		}
		super.interrupt();
	}

	public void doIt(int type, String[] args) {
		this.type = type;
		this.args = args;
		returnValue = null;
		ftpRemoteFiles = null;
		ftpRemoteFile = null;
		exception = null;
		doIt = true;
	}

	public void doIt(int type, String[] args, InputStream inputStream) {
		this.type = type;
		this.args = args;
		this.inputStream = inputStream;
		returnValue = null;
		ftpRemoteFiles = null;
		ftpRemoteFile = null;
		exception = null;
		doIt = true;
	}

	public void doIt(int type, String[] args, OutputStream outputStream) {
		this.type = type;
		this.args = args;
		this.outputStream = outputStream;
		returnValue = null;
		ftpRemoteFiles = null;
		ftpRemoteFile = null;
		exception = null;
		doIt = true;
	}

	public boolean isDoIt() {
		return doIt;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public Exception getException() {
		return exception;
	}

	public boolean isPassiveMode() {
		return passiveMode;
	}
	
	public void setPassiveMode(boolean passiveMode) {
		this.passiveMode = passiveMode;
	}

	public AFtpRemoteFile[] getFtpRemoteFiles() {
		return ftpRemoteFiles;
	}

	public AFtpRemoteFile getFtpRemoteFile() {
		return ftpRemoteFile;
	}
	
	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public FTPClient getFtpClient() {
		return ftpClient;
	}
}
