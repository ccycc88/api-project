package com.api.project.common.ftp.commonsnet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class AFtpRemoteFile {

	protected FTPClient ftpClient = null;
	protected FTPFile ftpFile = null;
	protected String currDir = null;
	
	public AFtpRemoteFile(FTPFile rfile, FTPClient ftpClient, String currDir) 
		throws IOException {
		this.ftpClient = ftpClient;
		this.ftpFile = rfile;
		this.currDir = currDir;
	}
	
	/**
	 * 获得远程文件的文件大小
	 */
	public long getSize() {
		return ftpFile.getSize();
	}

	/**
	 * 获得远程文件的文件名称
	 */
	public String getFileName() {
		return ftpFile.getName();
	}
	
	/**
	 * 获得远程文件的全路径名
	 */
	public String getAbsFileName() {
		return currDir.concat(getFileName());
	}
	
	/**
	 * 判断当前的元素是否为目录
	 */
	public boolean isDirectory() {
		return ftpFile.isDirectory();
	}
	
	/**
	 * 判断当前的元素是否为文件
	 */
	public boolean isFile() {
		return ftpFile.isFile();
	}
	
	/**
	 * 获得远程文件的创建者
	 */
	public String getOwner() {
		return ftpFile.getUser();
	}

	/**
	 * 获得远程文件的最后一次修改时间。
	 * @return
	 */
	public Date getModifyDate() {
		return ftpFile.getTimestamp().getTime();
	}
	
	/**
	 * 修改远程文件/目录的名称
	 * 		此方法能够修改当前工作路径‘pwd()’下的文件/目录的名称
	 * 为新名称‘newName’，注意参数‘newName’只是元素名称，不得包
	 * 含路径。
	 * @throws IOException 
	 */
	public boolean renameTo(String newName) throws IOException {
		return ftpClient.rename(
				currDir.concat(getFileName()), newName);
	}

	/**
	 * 删除远程文件。
	 * 		此方法删除目录时会报出‘Delete operation failed.’
	 * 可能没有执行此操作的权限。
	 * 
	 * 注意：该方法不能删除一个非空目录。
	 * @throws IOException 
	 */
	public boolean remove() throws IOException {
		return ftpClient.deleteFile(
				currDir.concat(getFileName()));
	}
	
	/**
	 * 取得一个输出流
	 * @return
	 * @throws IOException 
	 */
	public InputStream getInputStream() throws IOException {
		return ftpClient.retrieveFileStream(this.getAbsFileName());
	}
	
	public void release() {
		ftpClient = null;
		ftpFile = null;
		currDir = null;
	}
}
