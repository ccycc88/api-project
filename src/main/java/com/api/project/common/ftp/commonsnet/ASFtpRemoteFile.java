package com.api.project.common.ftp.commonsnet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

public class ASFtpRemoteFile extends AFtpRemoteFile {

	private LsEntry ftpFile = null;
	private ChannelSftp ftpClient = null;
	
	public ASFtpRemoteFile(FTPFile rfile, FTPClient ftpClient, String currDir)
			throws IOException {
		super(rfile, ftpClient, currDir);
	}
	
	public ASFtpRemoteFile(LsEntry ftpFile,ChannelSftp ftpClient,String currDir) throws Exception {
		super(null,null,null);
		this.ftpClient = ftpClient;
		this.ftpFile = ftpFile;
		this.currDir = currDir;
	}

	@Override
	public String getAbsFileName() {
		return currDir + File.separator + ftpFile.getFilename();
	}

	@Override
	public String getFileName() {
		return ftpFile.getFilename();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return ftpClient.getInputStream();
	}

	@Override
	public Date getModifyDate() {
		return new Date(ftpFile.getAttrs().getMTime());
	}

	@Override
	public String getOwner() {
		return ftpFile.getAttrs().getUId() + "";
	}

	@Override
	public long getSize() {
		return ftpFile.getAttrs().getSize();
	}

	@Override
	public boolean isDirectory() {
		return ftpFile.getAttrs().isDir();
	}

	@Override
	public boolean isFile() {
		return !isDirectory();
	}

	@Override
	public void release() {
		ftpClient = null;
		ftpFile = null;
		currDir = null;
	}

	@Override
	public boolean remove() throws IOException {
		try {
			ftpClient.rm(currDir + File.separator + ftpFile.getFilename());
		} catch (SftpException e) {
			throw new IOException(e);
		}
		return true;
	}

	@Override
	public boolean renameTo(String newName) throws IOException {
		try {
			ftpClient.rename(ftpFile.getFilename(), newName);
		} catch (SftpException e) {
			throw new IOException(e);
		}
		return true;
	}
}
