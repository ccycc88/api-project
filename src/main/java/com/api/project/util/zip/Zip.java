package com.api.project.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.api.project.util.helper.DateHelper;
import com.api.project.util.helper.FileHelper;

public class Zip {

	protected int compressDirectoryCount = 0; // 总压缩文件夹数量
	protected int compressFileCount = 0;      // 总压缩文件数量
	
	protected int relativeAddrIdx = 0;        // 需要压缩文件夹或文件的地址起始位。
	protected int compressLevel = 6;          // 0～9的压缩比。0为最弱，9为最强。默认是6。
	protected String zipFilePath = null;      // 压缩包绝对路径名。
	protected String compressPath = null;     // 压缩文件夹或文件的绝对路径。
	
	private DateHelper datehelper = new DateHelper();
	protected ZipOutputStream zipOutput = null;
	
	/**
	 * 构造器，纠错并设置全局变量。
	 * @param compressPath 需要压缩的文件夹或文件的绝对路径
	 * @param zipFileName 压缩包绝对路径，允许为null
	 */
	public Zip(String compressPath, String zipFilePath) throws IOException{
		File compressFile = new File(compressPath);
		if (!compressFile.exists())
			throw new IOException("the file or directory '"+compressPath+"' not found!");
		
		// 按照当前系统的格式得到该压缩的路径。
		this.zipFilePath = zipFilePath;
		this.compressPath = compressFile.getAbsolutePath();

		// 用户未给出解压路径，将使用压缩路径。
		if (this.zipFilePath == null) {
			StringBuffer zipFilePathBuf = new StringBuffer(this.compressPath);
			int bufLen = zipFilePathBuf.length();
			if (zipFilePathBuf.charAt(bufLen-1) == '/')
				zipFilePathBuf.deleteCharAt(bufLen-1);
			this.zipFilePath = zipFilePathBuf.append(".zip").toString();
		}
		relativeAddrIdx = this.compressPath.lastIndexOf(File.separator)+1;
	}
	
	/**
	 * 构造器，纠错并设置全局变量。解析出来的zip内不包含目录
	 * 
	 * @param compressPath
	 *            需要压缩的文件夹或文件的绝对路径
	 * @param zipFileName
	 *            压缩包绝对路径，允许为null
	 */
	public Zip(File compressPath, String zipFilePath) throws IOException {
		if (!compressPath.exists()) {
			throw new IOException("the file or directory '" + compressPath.getAbsolutePath()
					+ "' not found!");
		}

		// 按照当前系统的格式得到该压缩的路径。
		this.zipFilePath = zipFilePath;
		this.compressPath = compressPath.getAbsolutePath();

		// 用户未给出解压路径，将使用压缩路径。
		if (this.zipFilePath == null) {
			StringBuffer zipFilePathBuf = new StringBuffer(this.compressPath);
			int bufLen = zipFilePathBuf.length();
			if (zipFilePathBuf.charAt(bufLen-1) == '/')
				zipFilePathBuf.deleteCharAt(bufLen-1);
			this.zipFilePath = zipFilePathBuf.append(".zip").toString();
		}
		relativeAddrIdx = this.compressPath.length() + 1;
	}

	public Zip(String compressPath) throws IOException {
		this(compressPath, null);
	}
	
	/**
	 * 递归压缩目录和其下的所有元素。
	 * @param directoryPath 需要压缩目录的绝对路径。
	 */
	protected void compressDirectory(File directoryPath) 
		throws IOException {
		if (directoryPath.isFile()) {
			compressFile(directoryPath.getAbsolutePath());
		}else{
			File listFiles[] = directoryPath.listFiles();
			for (int i=0; i<listFiles.length; i++)
				if (listFiles[i].isFile()) {
					compressFile(listFiles[i].getAbsolutePath());
				}else {
					compressDirectoryCount ++;
					compressDirectory(listFiles[i]);
				}
		}
	}
	
	/**
	 * 向压缩文档中添加一个文件。
	 * @param absolutePath 需要压缩的文件绝对路径。
	 */
	protected void compressFile(String absolutePath) throws IOException {
		compressFileCount ++;
		byte byteBuf[] = new byte[2048];
		zipOutput.putNextEntry(new ZipEntry(absolutePath.substring(relativeAddrIdx)));
		
		FileInputStream input= new FileInputStream(absolutePath);
		for (int count=0; (count=input.read(byteBuf,0,byteBuf.length))!=-1;)
			zipOutput.write(byteBuf, 0, count);
		input.close();
		zipOutput.closeEntry();
	}
	
	/**
	 * 向压缩文档加入压缩说明。
	 */
	protected void compressDesc() throws IOException {
		zipOutput.putNextEntry(new ZipEntry("detail.txt"));
		zipOutput.write(
				new StringBuffer(":) by bad bird. at ")
				.append(datehelper.randomDate(System.currentTimeMillis(),0,DateHelper.DAY,"yyyy/MM/dd HH:mm"))
				.append("\n\nfile:").append(compressFileCount)
				.append("\ndirectory:").append(compressDirectoryCount)
				.toString().getBytes()
				);
		zipOutput.closeEntry();
	}
	
	/**
	 * 当创建类后可调用此方法实现压缩。
	 */
	public void compress() throws IOException {
		new FileHelper(zipFilePath).enable();
		zipOutput = new ZipOutputStream(new FileOutputStream(zipFilePath));
		zipOutput.setMethod(ZipOutputStream.DEFLATED);
		zipOutput.setLevel(compressLevel);
		compressDirectory(new File(compressPath));
		//compressDesc();
		zipOutput.close();
	}
	
	/**
	 * 设置一个0～9的压缩比。0为最弱，9为最强。默认是6。
	 * 		该设置只改变“compressLevel”全局变量，只有在压缩之前
	 * 		完成设置才有效。
	 */
	public void setCompressLevel(int level) {
		compressLevel = level;
	}
}
