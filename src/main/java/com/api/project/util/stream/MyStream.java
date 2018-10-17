package com.api.project.util.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyStream {

	/**
	 * 移动流中的字节
	 * 		该方法将从数据流'input'中移动字节到输出流'output'中。参数off表示从数据流'input'中跳过的字节数量，默认为
	 * '-1'表示不跳过字节。参数len表示从流'input'中一共移动的字节数量，默认为'-1'表示全部移动。参数bufsize表示默认的
	 * 缓冲大小，默认是'1024'。如移动大文件建议加大缓冲，小文件则反之。
	 */
	public long moveBytes(InputStream input, OutputStream output, long off, long len, int bufsize) 
		throws IOException {
		if (off > 0)
			input.skip(off);
		
		long totalNum = 0;
		byte[] buf = new byte[bufsize];

		while (true) {
			// 如当前已读取要求读取的最大字节'len’，那么退出。
			if (len>0 && (len-totalNum)<=0)
				break;
			
			// 如当前能够读取的字节数量小于缓存区域，则修改缓存大小防止超界。
			else if (len>0 && bufsize>(len-totalNum))
				bufsize = (int)(len-totalNum);
			
			// 读取与缓存大小一致的字节数量，如没有可读字节则退出。
			int readNum = input.read(buf, 0, bufsize);
			if (readNum <= 0)
				break;
			
			// 移动读取的字节，并记录数量。
			output.write(buf, 0, readNum);
			totalNum += readNum;
		}
		buf = null;
		return totalNum;
	}
	
	public long moveBytes(InputStream input, OutputStream output, long off, long len) 
		throws IOException {
		return moveBytes(input, output, off, len, 1024);
	}
	
	public long moveBytes(InputStream input, OutputStream output, long off) 
		throws IOException {
		return moveBytes(input, output, off, -1, 1024);
	}

	public long moveBytes(InputStream input, OutputStream output) 
		throws IOException {
		return moveBytes(input, output, -1, -1, 1024);
	}
}
