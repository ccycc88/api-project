package com.api.project.util.gzip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import com.api.project.util.helper.FileHelper;
import com.api.project.util.stream.MyStream;

public class Gzip {

	/**
	 * 将字符串压缩到gz文件
	 */
	public void compress(String srcStr, String toGzFile) 
		throws IOException {
		compress(srcStr.getBytes(), toGzFile);
	}
	
	/**
	 * 将字节数组压缩到gz文件
	 */
	public void compress(byte[] srcBytes, String toGzFile) 
		throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(srcBytes);
		compress(bais, toGzFile);
		bais.close();
	}
	
	/**
	 * 将文件压缩到gz文件
	 */
	public void compress(String srcFileName, String toGzFile, boolean delSrc) 
		throws IOException {
		FileInputStream fileInput = new FileInputStream(srcFileName);
		compress(fileInput, toGzFile);
		fileInput.close();
		
		// 完成压缩后，是否保留源文档
		if (delSrc == true)
			new File(srcFileName).delete();
	}
	
	/**
	 * 将流中的数据压缩到gz文件
	 */
	public void compress(InputStream src, String toGzFile) 
		throws IOException {
		GZIPOutputStream gzOutput = new GZIPOutputStream(
				new FileHelper(toGzFile).getOutputStream(false));
		
		// 从源流中读取数据，并写入到压缩流中（即写入到压缩文件中）
		new MyStream().moveBytes(src, gzOutput);
		gzOutput.close();
	}
	
	public OutputStream getCompressOutputStream(String toGzFile) 
		throws IOException {
		GZIPOutputStream gzOutput = new GZIPOutputStream(
				new FileHelper(toGzFile).getOutputStream(false));
		return gzOutput;
	}
	
	/**
	 * 压缩对象，返回压缩后的字节数组
	 *    对应 Gunzip.unCompressObj
	 */
	public byte[] compressObj(Object o) throws IOException {
		/* 压缩对象‘obj’并将压缩后的字节写入管道中。
		 * 注关闭gzip流后才能算完成压缩，否则压缩是不完整的！！ */
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream(); 
		ObjectOutputStream objOut = new ObjectOutputStream(
				new GZIPOutputStream(byteArrayOut));
		objOut.writeObject(o);
		objOut.close();
		
		// 从管道中得到压缩字节并写入到文件中。
		byte[] compressObj = byteArrayOut.toByteArray();
		byteArrayOut.close();
		
		return compressObj;
	}
	
	/**
	 * 将对象压缩到gz文件
	 *    对应 Gunzip.unCompressObj
	 */
	public void compressObj(Object o, String toGzFile) 
		throws IOException {
		byte[] compressB = compressObj(o);
		ByteArrayInputStream bais = new ByteArrayInputStream(compressB);
		FileOutputStream fos = new FileHelper(toGzFile).getOutputStream(false);
		
		new MyStream().moveBytes(bais, fos);
		bais.close();
		fos.close();
	}
	
	/*
    * 字符串的压缩  
    *   
    * @param str  
    *            待压缩的字符串  
    * @return    返回压缩后的字符串  
    * @throws IOException  
    */  
   public static String compress(String str) throws IOException {  
       if (null == str || str.length() <= 0) {  
           return str;  
       }  
       // 创建一个新的 byte 数组输出流  
       ByteArrayOutputStream out = new ByteArrayOutputStream();  
       // 使用默认缓冲区大小创建新的输出流  
       GZIPOutputStream gzip = new GZIPOutputStream(out);  
       // 将 b.length 个字节写入此输出流  
       gzip.write(str.getBytes());  
       gzip.close();  
       // 使用指定的 charsetName，通过解码字节将缓冲区内容转换为字符串  
       return out.toString("ISO-8859-1");  
//       return out.toString("GB2312"); 
   }   
}
