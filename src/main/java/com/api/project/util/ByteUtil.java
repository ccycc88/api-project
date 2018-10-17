package com.api.project.util;

import java.util.Arrays;

public class ByteUtil {

	public static void main(String[]a) {
		String s1 = "ab中文ccc";
		String s2 = "中文";
		byte[] b1 = s1.getBytes();
		byte[] b2 = s2.getBytes();
		print(b1);
		print(b2);
		print(Arrays.copyOfRange(b1, 1,b1.length));
		System.out.println(find(b1,b2));
	}
	private static void print(byte[] b) {
		for(int i = 0; i < b.length ; i++) {
			System.out.print(b[i] + ",");
		}
		System.out.println();
	}

	/**
	 * 从一个字节数组里，找出另一个字节数组的位置
	 * @param src
	 * @param con
	 * @return
	 */
	public static int find(byte[] src, byte[]con) {
		if(src.length < con.length) return -1;
		
		for(int i = 0; i < src.length; i++) {
			if(src[i] != con[0]) {
				continue;
			}
			boolean e = true;
			for(int j=1;j < con.length;j++) {
				if(i+j >= src.length || src[i+j] != con[j]) {
					e = false;
					break;
				}
			}
			if(e) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 不会在源数组上直接删除，返回删除后的
	 * @param src
	 * @param start
	 * @return 删除后的字节数组
	 */
	public static byte[] subByte(byte[] src,int start) {
		//要删除的下标大于原的最大下标时，把所有的删除
		if(start > src.length) return new byte[0];
		
		if(start < 0) {
			start = 0;
		}
		return Arrays.copyOfRange(src, start, src.length);
	}
	
	/**
	 * 
	 * @param src
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] subByte(byte[] src,int start,int end) {
		if(start > src.length) return new byte[0];
		
		if(start < 0) {
			start = 0;
		}
		if(end > src.length) {
			end = src.length;
		}
		return Arrays.copyOfRange(src, start, end);
	}
}
