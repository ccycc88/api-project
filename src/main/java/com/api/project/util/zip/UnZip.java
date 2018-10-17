package com.api.project.util.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.api.project.util.helper.FileHelper;

public class UnZip {

	protected String deCompressPath = null; // 解压的绝对路径
	protected String zipFilePath = null; // 压缩包绝对路径。

	/**
	 * 构造方法，纠错并设置全局变量。
	 * 
	 * @param toDirectory
	 *            解压的绝对路径
	 * @param zipFileName
	 *            需要解压的zip文件绝对路径。
	 */
	public UnZip(String zipFilePath, String toPath) throws IOException {
		File zipFile = new File(new File(zipFilePath).getAbsolutePath());
		if (!zipFile.isFile())
			throw new IOException("not found file '" + zipFilePath + "'");

		// 按照当前系统的格式得到该压缩的路径。
		this.deCompressPath = toPath;
		this.zipFilePath = zipFile.getAbsolutePath();

		// 用户没有指定，使用压缩包所在的路径。
		if (deCompressPath == null)
			deCompressPath = zipFile.getParent() + "/";

		else if (deCompressPath.charAt(deCompressPath.length() - 1) != '/')
			deCompressPath = deCompressPath + "/";
	}

	public UnZip(String zipFileName) throws IOException {
		this(zipFileName, null);
	}

	/**
	 * 当创建该类后可调用此方法具体解压一个zip文件。
	 */
	@SuppressWarnings("unchecked")
	public void deCompress() throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipFilePath);
			Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zipFile.entries();
			for (ZipEntry entry; e.hasMoreElements();) {
				if (!(entry = e.nextElement()).isDirectory()) {
					String toPath = new StringBuffer(deCompressPath).append(entry.getName()).toString();
					toPath = toPath.replace("\\", File.separator);
					deCompressFile(zipFile.getInputStream(entry), toPath);
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (zipFile != null) {
				zipFile.close();
			}
		}
	}

	/**
	 * 将某个压缩项目的数据复制到指定的解压路径。
	 * 
	 * @param input
	 *            压缩包中具体项目的数据读取流
	 * @param toPath
	 *            解压的绝对路径。
	 */
	protected void deCompressFile(InputStream input, String toPath) throws IOException {
		// if (SysEvnVar.SYS_FILE_SPARATOR.equals("/"))
		// toPath = toPath.replace("\\", "/");
		// else
		// toPath = toPath.replace("/", "\\");

		new FileHelper(toPath).enable();
		byte byteBuf[] = new byte[2048];
		FileOutputStream output = new FileOutputStream(toPath, false);
		try {
			for (int count = 0; (count = input.read(byteBuf, 0, byteBuf.length)) != -1;)
				output.write(byteBuf, 0, count);
		} catch (IOException e) {
			throw e;
		} finally {
			output.close();
			input.close();
		}
	}

	/**
	 * 获得压缩包中所有项目的基本描述
	 * 
	 * @param showDetail
	 *            是否将结果打印到屏幕上，true/false 是/否。
	 */
	@SuppressWarnings("unchecked")
	public CompressDetail[] getCompressDetail() throws IOException {
		ZipFile zipFile = new ZipFile(zipFilePath);
		try {
			LinkedList<CompressDetail> itemsContainer = new LinkedList<>();

			Enumeration<ZipEntry> e = (Enumeration<ZipEntry>) zipFile.entries();
			for (ZipEntry entry; e.hasMoreElements();) {
				entry = e.nextElement();
				itemsContainer.add(new CompressDetail(entry.getName(), entry.getSize(), entry.getCompressedSize(),
						entry.getTime(), entry.isDirectory()));
			}

			CompressDetail compressItems[] = new CompressDetail[itemsContainer.size()];
			System.arraycopy(itemsContainer.toArray(), 0, compressItems, 0, compressItems.length);
			return compressItems;
		} finally {
			zipFile.close();
		}

	}

	/**
	 * 该类记录了压缩文档中的项目的基本信息。
	 * 
	 * @param itemPathName
	 *            某压缩项目在压缩文档中的相对路径。
	 * @param itemIsDirectory
	 *            该项目是否是文件夹 true/false 是/否
	 * @param itemOriginalSize
	 *            该项目的实际大小
	 * @param itemCompressSize
	 *            压缩后的大小
	 * @param itemLastUpdateTime
	 *            该项目最后更新的时间
	 */
	public class CompressDetail {
		public String itemPathName = null;
		public long itemOriginalSize = 0;
		public long itemCompressSize = 0;
		public long itemLastUpdateTime = 0;
		public boolean itemIsDirectory = false;

		public CompressDetail(String name, long size, long compressSize, long time, boolean isDirectory) {
			itemPathName = name;
			itemOriginalSize = size;
			itemCompressSize = compressSize;
			itemLastUpdateTime = time;
			itemIsDirectory = isDirectory;
		}
	}
}
