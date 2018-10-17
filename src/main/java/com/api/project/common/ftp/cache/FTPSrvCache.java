package com.api.project.common.ftp.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.StringUtil;
import com.api.project.common.ftp.commonsnet.AFtpRemoteFile;
import com.api.project.common.ftp.commonsnet.FTPSrv;
import com.api.project.util.helper.FileHelper;
import com.api.project.util.helper.SysHelper;

public class FTPSrvCache extends FTPSrv{

	private Logger log = null;
	private SearchCache sc = SearchCache.getInstance();
	private RmtFileCache tfc = RmtFileCache.getInstance();
	private String host = null;
	//取代ip地址作为存放文件的目录
	private String ftpDirName;
	
	public String getFtpDirName() {
		return ftpDirName;
	}
	public void setFtpDirName(String ftpDirName) {
		this.ftpDirName = ftpDirName;
	}
	public FTPSrvCache(Logger l, String dir){
		if(l == null){
			log = LoggerFactory.getLogger(FTPSrvCache.class);
		}else{
			log = l;
		}
		this.CACHE_DIR = dir;
	}
	public FTPSrvCache(String dir){
		log = LoggerFactory.getLogger(FTPSrvCache.class);
		this.CACHE_DIR = dir;
	}

	final private int LST_TIME_OUT = 1000 * 15;
	public String CACHE_DIR = null;

	public void login(String host, int port, String user, String pwd,
			String encode, int timeout) throws Exception {
		this.host = host;
		super.login(host, port, user, pwd, encode, timeout);
	}

	// 缓存15秒list
	public AFtpRemoteFile[] list4Cache(String expr) throws Exception {
		AFtpRemoteFile[] frf = sc.tryGet(host, super.dir, "LIST", LST_TIME_OUT);
		if (frf == null) {
			try {
				frf = super.list(expr, 1000 * 60 * 10);
			} catch (Exception e) {

				boolean load = false;
				log.info("ftp重新操作：ftp从[" + host + "]列举文件[" + expr
						+ "]失败，接下来将有三次以内的重复列举:" + e.getMessage());
				for (int m = 1; m < 4 && (!load); m++) {
					try {
						Thread.sleep(60 * 1000);
						try {
							logout();
						} catch (Exception e2) {
							log.info("ftp重新操作：ftp从[" + host + "]列举[" + expr
									+ "]失败后，又退出失败:" + e2.getMessage());
						}

						super.initFtpDeamon();
						login(host, port, user, password, "gbk", timeout);
						log.info("ftp重新操作：ftp从[" + host + "]列举[" + expr + "]：第[" + m
								+ "]次重新登陆成功.");
						if (isPassiveMode)
							entryPassiveMode();
						//20150902 新增再次进入所配目录，
						if (super.chdir(super.dir)) {
							frf = super.list(expr, 1000 * 60 * 10);
							log.info("ftp重新操作：ftp从[" + host + "]列举[" + expr + "]：第[" + m
									+ "]次重新列举成功");
							load = true;
						}
						
					} catch (Exception e1) {
						log.info("ftp重新操作：ftp第[" + m + "]次重新列举文件失败:"+e1.getMessage());
					}
				}
				if (!load) {
					throw new Exception("从[" + host + "]下载[" + expr + "]失败:"
							+ StringUtil.createStackTrace(e));
				}
			}
			sc.put(host, super.dir, "LIST", frf);
		} else {
			// System.out.println("FTPSrvCache list from cache "+frf.length);
		}
		return frf;
	}

	// 缓存15秒nlst
	public AFtpRemoteFile[] nlst4Cache(String expr) throws Exception {
		AFtpRemoteFile[] frf = sc.tryGet(host, super.dir, "NLST", LST_TIME_OUT);
		if (frf == null) {
			frf = super.nlst(expr, 1000 * 60 * 10);
			sc.put(host, super.dir, "NLST", frf);
		} else {
			// System.out.println("FTPSrvCache nlst from cache "+frf.length);
		}
		return frf;
	}

	public String retrive4Cache(AFtpRemoteFile rmtFile) throws Exception {
		return retrive4Cache(rmtFile, 10 * 60);
	}

	// 本地文件适配器调用方法
	/**
	 * 可以重新登陆下载 timeout:单位：秒
	 */
	public String retrive4Cache(AFtpRemoteFile rmtFile, int timeout)
			throws Exception {
		String cacheFile = tfc.tryGet(host, rmtFile.getAbsFileName(),
				rmtFile.getSize(), rmtFile.getModifyDate().getTime());
		if (cacheFile != null) {
			// System.out.println("FTPSrvCache get file from cache "+cacheFile);
			// 如果存在就返回，不存在就下载
			if (new File(cacheFile).exists()) {
				return cacheFile;
			}
		}

		// 下载远程
		Random rand = new Random();
		int rnd = rand.nextInt(100000);
		if(this.ftpDirName==null){
			this.ftpDirName = host;
		}
		cacheFile = CACHE_DIR + this.ftpDirName + "/" + rmtFile.getFileName() + "_" +rnd+rmtFile.getModifyDate().getTime();
		try {
			new FileHelper(cacheFile).enable();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileOutputStream fos = new FileOutputStream(cacheFile);

		try {
			try {
				boolean sucessFlag = super.retrive(rmtFile.getAbsFileName(), fos, timeout);
				//重试
				for (int i=0; !sucessFlag&&i<3; i++) {
					if (!sucessFlag) {
						SysHelper.waitIt(this, 30);
						sucessFlag = super.retrive(rmtFile.getAbsFileName(), fos, timeout);
						
						if(sucessFlag){
							log.info(rmtFile.getAbsFileName() + "第["+(i+2)+"]下载成功！");
						}
					}
				}
			} catch (Exception e) {

				boolean load = false;
				log.info("ftp重新操作：ftp从[" + host + "]下载[" + rmtFile.getAbsFileName()
						+ "]失败，接下来将有三次以内的重复下载:" + e.getMessage());
				for (int m = 1; m < 4 && (!load); m++) {
					try {
						Thread.sleep(60 * 1000);
						try {
							logout();
						} catch (Exception e2) {
							log.info("ftp重新操作：ftp从[" + host + "]下载["
									+ rmtFile.getAbsFileName() + "]失败后，又退出失败:"
									+ e2.getMessage());
						}

						super.initFtpDeamon();
						login(host, port, user, password, "gbk", timeout);
						log.info("ftp重新操作：ftp从[" + host + "]下载["
								+ rmtFile.getAbsFileName() + "]：第[" + m
								+ "]次重新登陆成功.");
						if (isPassiveMode)
							entryPassiveMode();

						super.retrive(rmtFile.getAbsFileName(), fos, timeout);
						log.info("ftp重新操作：ftp从[" + host + "]下载["
								+ rmtFile.getAbsFileName() + "]：第[" + m
								+ "]次重新下载成功");
						load = true;
					} catch (Exception e1) {
						log.info("ftp重新操作：ftp第[" + m + "]次重新下载["
									+ rmtFile.getAbsFileName() + "]失败:"+e1.getMessage());
					}
				}
				if (!load) {
					throw new Exception("从[" + host + "]下载["
							+ rmtFile.getAbsFileName() + "]失败:"
							+ StringUtil.createStackTrace(e));
				}
			}
			tfc.put(host, rmtFile.getAbsFileName(), rmtFile.getSize(), rmtFile
					.getModifyDate().getTime(), cacheFile);
		} finally {
			fos.close();
		}
		return cacheFile;
	}
	
	public boolean store4Reconn(String lfileName, String toRfileName) throws Exception{
		try {
			return this.store(lfileName, toRfileName);
		}catch(Exception e) {

			boolean load = false;
			log.info("ftp重新操作：ftp从[" + host + "]上传[" + lfileName + "]失败，接下来将有三次以内的重复上传:" + e.getMessage());
			for (int m = 1; m < 4 && (!load); m++) {
				try {
					Thread.sleep(60 * 1000);
					try {
						logout();
					} catch (Exception e2) {
					}

					super.initFtpDeamon();
					login(host, port, user, password, "gbk", timeout);
					log.info("ftp重新操作：ftp从[" + host + "]上传["
							+ lfileName + "]：第[" + m
							+ "]次重新登陆成功.");
					if (isPassiveMode)
						entryPassiveMode();

					return this.store(lfileName, toRfileName);
				} catch (Exception e1) {
					log.info("ftp重新操作：ftp第[" + m + "]次重新上传["
								+ lfileName + "]失败:"+e1.getMessage());
				}
			}

			throw new Exception("从[" + host + "]上传["
					+ lfileName + "]失败:"
					+ StringUtil.createStackTrace(e));
		
		}
	}

	public String retriveNoCache(AFtpRemoteFile rmtFile) throws Exception {
		// 下载远程
		Random rand = new Random();
		int rnd = rand.nextInt(100000);
		if(this.ftpDirName==null){
			this.ftpDirName = host;
		}
		String cacheFile = CACHE_DIR + this.ftpDirName + "/" + rmtFile.getFileName() + "_" +rnd+rmtFile.getModifyDate().getTime();
		try{
			new FileHelper(cacheFile).enable();
		}catch(Exception e){
			e.printStackTrace();
		}
		FileOutputStream fos = new FileOutputStream(cacheFile);
		
		try {
			super.retrive(rmtFile.getAbsFileName(), fos);
		}finally {
			fos.close();
		}
		return cacheFile;
	}
	
	public String retriveNoCache4TryWhithTemp(AFtpRemoteFile rmtFile) throws Exception {
		

		// 下载远程
		Random rand = new Random();
		int rnd = rand.nextInt(100000);
		if(this.ftpDirName==null){
			this.ftpDirName = host;
		}
		String cacheFile = CACHE_DIR + this.ftpDirName + "/" + rmtFile.getFileName() + "_" +rnd+rmtFile.getModifyDate().getTime();
		String cacheFileTemp = cacheFile + ".temp";
		try {
			new FileHelper(cacheFileTemp).enable();
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileOutputStream fos = new FileOutputStream(cacheFileTemp);

		try {
			try {
				boolean sucessFlag = super.retrive(rmtFile.getAbsFileName(), fos, timeout);
				//重试
				for (int i=0; !sucessFlag&&i<3; i++) {
					if (!sucessFlag) {
						SysHelper.waitIt(this, 30);
						sucessFlag = super.retrive(rmtFile.getAbsFileName(), fos, timeout);
						
						if(sucessFlag){
							log.info(rmtFile.getAbsFileName() + "第["+(i+2)+"]下载成功！");
						}
					}
				}
			} catch (Exception e) {

				boolean load = false;
				log.info("ftp重新操作：ftp从[" + host + "]下载[" + rmtFile.getAbsFileName()
						+ "]失败，接下来将有三次以内的重复下载:" + e.getMessage());
				for (int m = 1; m < 4 && (!load); m++) {
					try {
						Thread.sleep(60 * 1000);
						try {
							logout();
						} catch (Exception e2) {
							log.info("ftp重新操作：ftp从[" + host + "]下载["
									+ rmtFile.getAbsFileName() + "]失败后，又退出失败:"
									+ e2.getMessage());
						}

						super.initFtpDeamon();
						login(host, port, user, password, "gbk", timeout);
						log.info("ftp重新操作：ftp从[" + host + "]下载["
								+ rmtFile.getAbsFileName() + "]：第[" + m
								+ "]次重新登陆成功.");
						if (isPassiveMode)
							entryPassiveMode();
						fos = new FileOutputStream(cacheFileTemp);
						super.retrive(rmtFile.getAbsFileName(), fos, timeout);
						log.info("ftp重新操作：ftp从[" + host + "]下载["
								+ rmtFile.getAbsFileName() + "]：第[" + m
								+ "]次重新下载成功");
						load = true;
					} catch (Exception e1) {
						log.info("ftp重新操作：ftp第[" + m + "]次重新下载["
									+ rmtFile.getAbsFileName() + "]失败:"+e1.getMessage());
					}
				}
				if (!load) {
					throw new Exception("从[" + host + "]下载["
							+ rmtFile.getAbsFileName() + "]失败:"
							+ StringUtil.createStackTrace(e));
				}
			}
			
		} finally {
			fos.close();
		}
		
		File tempFile = new File(cacheFileTemp);
		File toFile = new File(cacheFile);
		tempFile.renameTo(toFile);
		
		return cacheFile;
	}
	
	// 本地文件适配器调用方法
	public String retriveNoCache(String remoteDir, String remoteFileName)
			throws Exception {

		// 下载远程
		Random rand = new Random();
		int rnd = rand.nextInt(100000);
		if(this.ftpDirName==null){
			this.ftpDirName = host;
		}
		String localFile = CACHE_DIR + this.ftpDirName + "/" + remoteFileName + "_" +  rnd+System.currentTimeMillis();
		FileHelper helper = null;
		try {
			helper = new FileHelper(localFile);
			helper.enable();
		} catch (Exception e) {
			e.printStackTrace();
		}
		boolean success = false;
		FileOutputStream fos = new FileOutputStream(localFile);

		try {
			String fileName = StringUtil.addSlash(remoteDir) + remoteFileName;
			try {
				success = super.retrive(fileName, fos);
				//下载文件不存在
				if(!super.replycode()){
					helper.disable();
					return null;
				}
				//重试
				for (int i=0; !success&&i<3; i++) {
					if (!success) {
						SysHelper.waitIt(this, 1500);
						success = super.retrive(fileName, fos);
						
						if(success){
							log.info(fileName + "第["+(i+2)+"]次下载成功！");
							break;
						}
					}
				}
				if(success){
					return localFile;
				}else{
					helper.disable();
					return null;
				}
			} catch (Exception e) {

				boolean load = false;
				log.info("ftp重新操作：ftp从[" + host + "]下载[" + fileName + "]失败，接下来将有三次以内的重复下载:" + e.getMessage());
				for (int m = 1; m < 4 && (!load); m++) {
					try {
						Thread.sleep(60 * 1000);
						try {
							logout();
						} catch (Exception e2) {
							log.info("ftp重新操作：ftp从[" + host + "]下载[" + fileName
									+ "]失败后，又退出失败:" + e2.getMessage());
						}

						super.initFtpDeamon();
						login(host, port, user, password, "gbk", timeout);
						log.info("ftp重新操作：ftp从[" + host + "]下载[" + fileName + "]：第["
								+ m + "]次重新登陆成功.");
						if (isPassiveMode)
							entryPassiveMode();

						success = super.retrive(fileName, fos);
						
						log.info("ftp重新操作：ftp从[" + host + "]下载[" + fileName + "]：第["
								+ m + "]次重新下载成功");
						load = true;
                        if(!super.replycode()){
                        	helper.disable();
							return null;
						}else{
							return localFile;
						}
					} catch (Exception e1) {
						log.info("ftp重新操作：ftp第[" + m + "]次重新从[" + host + "]下载[" + fileName + "]失败:"+e1.getMessage());
					}
				}
				if (!load) {
					throw new Exception("从[" + host + "]下载[" + fileName
							+ "]失败:" + StringUtil.createStackTrace(e));
				}

			}
		} finally {
			fos.close();
		}
		return null;
	}

	public void login4Reconn(String host, int port, String user, String pwd,
			String encode, int timeout) throws Exception {
		this.host = host;
		try {
			super.login(host, port, user, pwd, encode, timeout);
		} catch (Exception e) {

			try {
				logout();
			} catch (Exception e2) {
				log.info("ftp重新操作：ftp重新连接前退出时出现异常：" + e.getMessage());
			}
			boolean login = false;
			log.info("ftp重新操作：ftp登陆[" + host + "]时出现异常，，接下来将有三次以内的重复登陆:" + e.getMessage());
			for (int m = 1; m < 4 && (!login); m++) {
				try {
					Thread.sleep(60 * 1000);
					try {
						logout();
					} catch (Exception e2) {
					}
					
					super.initFtpDeamon();
					login(host, port, user, password, "gbk", timeout);
					log.info("ftp重新操作：ftp在第[" + m + "]次重新登陆成功");
					login = true;
				} catch (Exception e1) {
					log.info("ftp重新操作：ftp在第[" + m + "]次重新登陆["+host+"]失败:" + e1.getMessage());
				}
			}
			if (!login) {
				throw new Exception("连接设备[" + host + "]失败"
						+ StringUtil.createStackTrace(e));
			}
		}
	}
	
	public boolean chdir4Reconn(String dir) throws Exception {
		try {
			return chdir(dir, 1000*30);
		} catch (Exception e) {

			boolean load = false;
			log.info("ftp重新操作：ftp从[" + host + "]变换目录[" + dir + "]失败，接下来将有三次以内的重复操作:" + e.getMessage());
			for (int m = 1; m < 4 && (!load); m++) {
				try {
					Thread.sleep(60 * 1000);
					try {
						logout();
					} catch (Exception e2) {
					}

					super.initFtpDeamon();
					login(host, port, user, password, "gbk", timeout);
					log.info("ftp重新操作：ftp从[" + host + "]变换目录[" + dir + "]：第[" + m + "]次重新登陆成功.");
					if (isPassiveMode)
						entryPassiveMode();

					super.chdir(dir, 1000*30);
					log.info("ftp重新操作：ftp从[" + host + "]变换目录[" + dir + "]：第[" + m + "]次重新操作成功");
					load = true;
				} catch (Exception e1) {
					log.info("ftp重新操作：ftp第[" + m + "]次重新从[" + host + "]变换目录[" + dir + "]失败:"+e1.getMessage());
				}
			}
			if (!load) {
				throw new Exception("ftp从[" + host + "]变换目录[" + dir + "]失败:" + StringUtil.createStackTrace(e));
			}else{
				return true;
			}

		}
	}
	
	
	
	public boolean downloadFileToPath(AFtpRemoteFile remoteFile, String toPath)
			throws Exception {

		File loaclFile = new File(toPath + remoteFile.getFileName());
		if (loaclFile.exists()) {
			loaclFile.delete();
		}

		String toFile = toPath + remoteFile.getFileName();
		try {
			new FileHelper(toFile).enable();
		} catch (Exception e) {

		}
		FileOutputStream fos = new FileOutputStream(toFile);

		try {
			try {
				super.retrive(remoteFile.getAbsFileName(), fos);
			} catch (Exception e) {
				boolean load = false;
				log.info("sftp重新操作：sftp从[" + host + "]下载["
						+ remoteFile.getAbsFileName() + "]失败，接下来将有三次以内的重复下载:"
						+ e.getMessage());
				for (int m = 1; m < 4 && (!load); m++) {
					try {
						Thread.sleep(60 * 1000);
						try {
							logout();
						} catch (Exception e2) {
							log.info("ftp重新操作：ftp从[" + host + "]下载["
									+ remoteFile.getAbsFileName() + "]失败后，又退出失败:"
									+ e2.getMessage());
						}

						super.initFtpDeamon();
						login(host, port, user, password, "gbk", timeout);
						log.info("ftp重新操作：ftp从[" + host + "]下载["
								+ remoteFile.getAbsFileName() + "]：第[" + m
								+ "]次重新登陆成功.");
						if (isPassiveMode)
							entryPassiveMode();

						super.retrive(remoteFile.getAbsFileName(), fos, timeout);
						log.info("ftp重新操作：ftp从[" + host + "]下载["
								+ remoteFile.getAbsFileName() + "]：第[" + m
								+ "]次重新下载成功");
						load = true;
					} catch (Exception e1) {
						log.info("ftp重新操作：ftp第[" + m + "]次重新下载["
									+ remoteFile.getAbsFileName() + "]失败:"+e1.getMessage());
					}
				}
				if (!load) {
					log.error("从[" + host + "]下载["
							+ remoteFile.getAbsFileName() + "]失败:"
							+ StringUtil.createStackTrace(e));
					return false;
				}
			}
		} finally {
			if (fos != null)
				fos.close();
		}

		return true;
	}
}
