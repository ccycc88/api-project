package com.api.project.util.helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class FileHelper {

	private static final long serialVersionUID = 1L;

	protected File theFile = null;

	protected FileInputStream fileInputStream = null;
	protected FileOutputStream fileOutputStream = null;

	/**
	 * 构造方法
	 * 
	 * 该构造器将构造指定文件‘filePath’的File类 实例，无论该文件是否存在。
	 */
	public FileHelper(String filePath) {
		theFile = new File(filePath);
	}

	public FileHelper() {
	}

	public File getFileInstance() {
		return theFile;
	}

	/**
	 * 判断文件是否存在。
	 * 
	 * 当使用指定文件地址初始化本类后，可通过调用此 方来判断此文件是否存在。
	 */
	public boolean exists() {
		return theFile.exists();
	}

	/**
	 * 创建文件和其路径
	 * 
	 * 当使用指定文件地址初始化本类后，可通过调用此 方来创建文件的路径和文件，如文件路径存在则忽略创建 路径而创建文件本身。
	 */
	public void enable() throws IOException {
		if (!theFile.exists()) {
			String parentPath = theFile.getParent();
			if (parentPath != null)
				new File(parentPath).mkdirs();
			theFile.createNewFile();
		}
	}

	/**
	 * 创建路径
	 * 
	 * 当使用指定文件地址初始化本类后，可通过调用此 方来创建文件的路径和文件，如文件路径存在则忽略创建 路径而创建文件本身。
	 */
	public void enableDir() throws IOException {
		if (!theFile.exists()) {
			String parentPath = theFile.getParent();
			if (parentPath != null)
				new File(parentPath).mkdirs();
		}
	}

	/**
	 * 删除文件
	 * 
	 * 当使用指定文件地址初始化本类后，可通过调用此方 法来删除文件并保留其路径。如指定的文件并不存在则忽略。
	 */
	public void disable() throws IOException {
		if (theFile.exists()) {
			theFile.delete();
		}
	}

	/**
	 * 重建文件
	 * 
	 * 该方法能够将已有的文件删除并重新建立一个同名的文 件。当调用该方法时首先会判断之前是否已与此文件建立过输
	 * 入、输出流，如已建立则关闭且删除该文件如需要删除的文件 并不存在则忽略删除动作，之后再次创建该文件和其路径。当 调用该方法或之前文件中的数据将不复存在。
	 */
	public void remake() throws IOException {
		closeStream();
		disable();
		enable();
	}

	/**
	 * 得到文件的文件名和后缀
	 * 
	 * 当使用指定文件地址初始化本类后，可通过类实例调用 此方法来得到其文件名和后缀。
	 */
	public String getName() {
		return theFile.getName();
	}

	public boolean rename(String newName) {
		return theFile.renameTo(new File(newName));
	}

	/**
	 * 得到文件的后缀名
	 * 
	 * 调用该方法后能够得到当前指定文件的后缀名，返回的 后缀名不包含符号“.”,如果没有后缀名则返回“null”。如后 缀符号“.”后并无名称则返回“”。
	 */
	public String getSuffix() {
		String fileName = getName();
		int postfixIdx = fileName.lastIndexOf(".");
		if (postfixIdx == -1)
			return null;
		return fileName.substring(postfixIdx + 1);
	}

	/**
	 * 得到文件的长度
	 * 
	 * 当使用指定文件地址初始化本类后，可通过类实例调用 此方法来得到其长度（大小）。
	 */
	public long length() {
		return theFile.length();
	}

	/**
	 * 得到文件的绝对路径
	 * 
	 * 当使用指定文件地址初始化本类后，可通过类实例调用 此方法来得到其绝对路径。
	 */
	public String getAbsolutePath() {
		return theFile.getAbsolutePath();
	}

	/**
	 * 得到文件的输入流
	 * 
	 * 当使用指定文件地址初始化本类后，可通过调用此方法 获得该文件的输入流。如调用此方法果时指定的文件路径或文
	 * 件不存在则自动创建路径和文件，如果创建路径或文件失败或 建立输入流失败会抛出“IOException”异常，当成功与该文
	 * 件建立输入流后会将流实例返回给用户。
	 */
	public FileInputStream getInputStream() throws IOException {
		if (fileInputStream == null) {
			enable();
			fileInputStream = new FileInputStream(theFile);
		}
		return fileInputStream;
	}

	/**
	 * 得到文件的输出流
	 * 
	 * 当使用指定文件地址初始化本类后，可通过调用此方法 获得该文件的输出流。如调用此方法果时指定的文件路径或文
	 * 件不存在则自动创建路径和文件，如果创建路径或文件失败或 建立输出流失败会抛出“IOException”异常，当成功与该文
	 * 件建立输出流后会将流实例返回给用户。参数“append”标记 着是否创建一个追加数据的输出流。
	 */
	public FileOutputStream getOutputStream(boolean append) throws IOException {
		if (fileOutputStream == null) {
			enable();
			fileOutputStream = new FileOutputStream(theFile, append);
		}
		return fileOutputStream;
	}

	/**
	 * 关闭文件的输入输出流
	 * 
	 * 该方法能够关闭文件的输入输出流。在关闭流时分别判 断某文件之前是否创建过输入、输出流，如创建则关闭流并将
	 * 相关流对象赋为“null”否则将忽略。当调用此方法后之前得 到的文件输入、输出流将不再可用，如仍需要建立流则调用该
	 * 类的方法“getInputStream、getOutputStream”即可。
	 */
	public void closeStream() {
		if (fileInputStream != null) {
			try {
				fileInputStream.close();
			} catch (Exception ex) {
			}
			fileInputStream = null;
		}
		if (fileOutputStream != null) {
			try {
				fileOutputStream.close();
			} catch (Exception ex) {
			}
			fileOutputStream = null;
		}
	}

	/**
	 * 导入属性，来自文件‘fromFile’。
	 * 
	 * 文件不存在则返回‘null’
	 * 
	 * @return
	 * @throws Exception
	 */
	public static synchronized Properties loadProp(String fromFile) throws Exception {
		if (!new File(fromFile).exists())
			return null;

		Properties prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fromFile);
			prop.load(fis);
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (fis != null)
				fis.close();
		}
		return prop;
	}

	/**
	 * 持久化属性，保存在‘tofile’文件中
	 */
	public static synchronized void storeProp(Properties prop, String tofile) throws Exception {
		OutputStream fos = null;
		try {
			FileHelper fh = new FileHelper(tofile);
			fh.enableDir();
			fos = fh.getOutputStream(false);
			prop.store(fos, "");
		} catch (Exception ex) {
			throw ex;
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * 递归遍历某个文件夹下符合文件名要求的文件 add by yanghaoguang
	 * 
	 * @param path
	 * @param filter
	 */
	public static List<File> listFiles(String path, FilenameFilter filter) {
		ArrayList<File> return_list = new ArrayList<>();
		LinkedList<File> list = new LinkedList<>();
		File dir = new File(path);
		File file[] = dir.listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isDirectory()) {
				list.add(file[i]);
			} else {
				if (filter.accept(dir, file[i].getName())) {
					return_list.add(file[i]);
				}
			}
		}
		File tmp;
		while (!list.isEmpty()) {
			tmp = list.removeFirst();
			if (tmp.isDirectory()) {
				file = tmp.listFiles();
				if (file == null)
					continue;
				for (int i = 0; i < file.length; i++) {
					if (file[i].isDirectory())
						list.add(file[i]);
					else if (filter.accept(dir, file[i].getName())) {
						return_list.add(file[i]);
					}
				}
			} else {
				if (filter.accept(dir, tmp.getName())) {
					return_list.add(tmp);
				}
			}
		}
		return return_list;
	}

	/**
	 * 追加文件：使用FileWriter
	 * 
	 * @param fileName
	 * @param content
	 */
	// public static void strAppend2file(String fileName, String content)
	// throws Exception {
	// // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
	// FileWriter writer = new FileWriter(fileName, true);
	// writer.write(content);
	// writer.flush();
	// writer.close();
	// }
	public static void rename(String fileName, String newName) {
		File srcFile = new File(fileName);
		File newFile = new File(newName);
		srcFile.renameTo(newFile);
		srcFile = newFile = null;
	}

	public static Boolean copyFile(String from, String to) {
		if (!new File(from).exists()) {
			try {
				new File(from).createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		File fromFile, toFile;
		fromFile = new File(from);
		toFile = new File(to);
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			toFile.createNewFile();
			fis = new FileInputStream(fromFile);
			fos = new FileOutputStream(toFile);
			int bytesRead;
			byte[] buf = new byte[4 * 1024];// 4K buffer
			while ((bytesRead = fis.read(buf)) != -1) {
				fos.write(buf, 0, bytesRead);
			}
			fos.flush();
			fos.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 追加文件：使用FileWriter
	 * 
	 * @param fileName
	 * @param content
	 */
	public static void appendLine(String fileName, String content) {
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(fileName, true);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getLineNumber(String fileName) throws Exception {
		int lineCount = 0;
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.trim().equals("")) {
					continue;
				}
				lineCount++;
			}
			br.close();
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return lineCount;
	}

	// 合并多个小文件为一个大文件
	public static int merge(List<String> fileNames, String TargetFileName, String fileSeperator) throws Exception {
		File fin = null;
		// 构建文件输出流
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(TargetFileName));
		for (int i = 0; i < fileNames.size(); i++) {
			// 打开文件输入流
			fin = new File(fileNames.get(i));
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(fin));
			// 从输入流中读取数据，并写入到文件数出流中
			int buffersize = 4096;
			byte[] b = new byte[buffersize];
			int length = 0;
			while ((length = in.read(b)) == buffersize) {
				out.write(b);
				out.flush();
			}
			if (length > 0) {
				for (int j = 0; j < length; j++) {
					out.write(b[j]);
				}
				out.flush();
			}
			in.close();
			// 文件和文件之间是否要写分割符，例如\n
			if (fileSeperator != null) {
				out.write(fileSeperator.getBytes());
				out.flush();
			}
		}
		out.close();
		return FileHelper.getLineNumber(TargetFileName);

	}

	// 清空文件
	public static void clearFile(String filename) throws Exception {
		File f = new File(filename);
		FileWriter fw = new FileWriter(f);
		fw.write("");
		fw.close();
	}

	// 判断文件为空
	public static boolean isEmpty(String filename) {
		File file = new File(filename);
		if (!file.exists()) {
			return true;
		}
		if (file.length() == 0) {
			return true;
		}
		return false;
	}

	public static boolean copyDirToDes(String sourcePath, String desPath) throws Exception {

		File file = new File(sourcePath);

		if (file.isDirectory()) {
			File files[] = file.listFiles();
			File tempFile = null;
			for (int i = 0; i < files.length; i++) {
				tempFile = files[i];
				if (tempFile.exists()) {
					File desFile = new File(desPath);
					if (!desFile.exists()) {
						desFile.mkdirs();
					}
					FileInputStream fis = new FileInputStream(tempFile);
					FileOutputStream fos = new FileOutputStream(desPath + File.separator + tempFile.getName());
					byte[] b = new byte[fis.available()];
					int len = 0;
					while ((len = fis.read(b)) != -1) {
						fos.write(b, 0, len);
						fos.flush();
					}
					fos.close();
					fis.close();
				}
			}
		}

		System.out.println("import finish");
		return true;

	}

	public static String getFileName(String str) {
		File tempFile = new File(str.trim());
		String fileName = tempFile.getName();
		return fileName;
	}

	public static String getFilePath(String str) {
		File tempFile = new File(str.trim());
		String filePath = tempFile.getParent();
		return filePath;
	}

	/*
	 * Java文件操作 获取文件扩展名 /opt/Gcp/GcpCollector/cmd/111.txt txt
	 */
	public static String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length() - 1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}

	/*
	 * Java文件操作 获取不带扩展名的文件名 /opt/Gcp/GcpCollector/cmd/111.txt
	 * /opt/Gcp/GcpCollector/cmd/111
	 */
	public static String getFilePathNameNoEx(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf('.');
			if ((dot > -1) && (dot < (filename.length()))) {
				return filename.substring(0, dot);
			}
		}
		return filename;
	}

	/*
	 * Java文件操作 获取不带路径及扩展名的文件名 /opt/Gcp/GcpCollector/cmd/111.txt 111
	 */
	public static String getFileNameNoEx(String filename) {
		String strtmp = getFileName(filename);
		String str = getFilePathNameNoEx(strtmp);
		return str;

	}
}
