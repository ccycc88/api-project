package com.api.project.util.gzip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import com.api.project.util.helper.FileHelper;
import com.api.project.util.stream.MyStream;

public class Gunzip {

	/**
	 * 从gz流中释放数据到文件
	 */
	public void unCompress(InputStream gzInput, String toFile) 
		throws IOException {
		GZIPInputStream gzIn = new GZIPInputStream(gzInput);
		FileOutputStream fileOutput = new FileHelper(toFile).getOutputStream(false);
		
		// 从压缩文档流中读取数据，并写入解压文档流中
		new MyStream().moveBytes(gzIn, fileOutput);
		
		gzIn.close();
		fileOutput.close();
	}
	
	/**
	 * 从压缩字节数组中释放数据到文件
	 */
	public void unCompress(byte[] gzBytes, String toFile)
		throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(gzBytes);
		unCompress(bais, toFile);
		bais.close();
	}

	/**
	 * 从压缩文件中释放数据到文件
	 */
	public void unCompress(String gzFileName, String toFile, boolean delSrc) 
		throws IOException {
		FileInputStream fileInput = new FileInputStream(gzFileName);
		unCompress(fileInput, toFile);
		fileInput.close();
		
		// 完成解压缩后，是否保留压缩文档
		if (delSrc == true)
			new File(gzFileName).delete();
	}

	/**
	 * 从压缩文件中释放对象
	 *    对应 Gzip.compressObj
	 */
	public Object unCompressObj(String gzFileName) 
		throws IOException, ClassNotFoundException {
		FileInputStream fileInput = new FileInputStream(gzFileName);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		// 从压缩文档流中读取数据，并写入解压文档流中
		new MyStream().moveBytes(fileInput, baos);
	
		Object o = unCompressObj(baos.toByteArray());
		
		fileInput.close();
		baos.close();
		return o;
	}

	/**
	 * 从压缩字节数组中释放对象
	 *    对应 Gzip.compressObj
	 */
	public Object unCompressObj(byte[] gzBytes) 
		throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(gzBytes);
		ObjectInputStream objIn = new ObjectInputStream(
				new GZIPInputStream(bais)); 
		Object srcObj = objIn.readObject();
		
		bais.close();
		objIn.close();
		return srcObj;
	}
	
	/**  
	    * 字符串的解压  
	    *   
	    * @param str  
	    *            对字符串解压  
	    * @return    返回解压缩后的字符串  
	    * @throws IOException  
	    */  
	   public static String unCompress(String str) throws IOException {  
	       if (null == str || str.length() <= 0) {  
	           return str;  
	       }  
	       // 创建一个新的 byte 数组输出流  
	       ByteArrayOutputStream out = new ByteArrayOutputStream();  
	       // 创建一个 ByteArrayInputStream，使用 buf 作为其缓冲区数组  
	       ByteArrayInputStream in = new ByteArrayInputStream(str  
	               .getBytes("ISO-8859-1"));  
	       // 使用默认缓冲区大小创建新的输入流  
	       GZIPInputStream gzip = new GZIPInputStream(in);  
	       byte[] buffer = new byte[256];  
	       int n = 0;  
	       while ((n = gzip.read(buffer)) >= 0) {// 将未压缩数据读入字节数组  
	           // 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此 byte数组输出流  
	           out.write(buffer, 0, n);  
	       }  
	       // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串  
	       return out.toString("GBK");  
	   }
}
