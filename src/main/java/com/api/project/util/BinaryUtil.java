package com.api.project.util;

import java.io.UnsupportedEncodingException;


public class BinaryUtil {
	private static String hexStr =  "0123456789ABCDEF";
	/*
	 * 二进制在转换为数字的时候会存在大端方式和小端方式的区别,但是这种区别指挥发生在多个字节的情况下,如果只有一个字节就不会发生这种情况
	 * 0x1234 = 4660
	 * 大端方式:  {0x12,0x34}
	 * 小端方式:  {0x34,0x12}
	 * 一般的方式为大端{高位字节在前,地位字节在后}
	 * 
	 * 大小端区别:字段内部的比特高低次序相同（左高右低），而字段之间的高低次序相反
	 */
	//下面部分是大端的方式转换
	/**
	 * 大端方式的字节转换为整型
	 * @param target 大端方式的二进制数组
	 * @return 转换后的整型
	 * @throws Exception 
	 */		
	public static int convertBidBytesToInt(byte[] target) throws Exception{
		if(target.length > 4){
			throw new Exception("超过int的取值范围!");
		}

		byte[] results = new byte[]{0x0,0x0,0x0,0x0};
		int targetIndex = 0;
		for(int i = 0; i < results.length ; i++){
			if(i >= results.length - target.length){
				results[i] = target[targetIndex++];
			}
		}

		return (
					(results[3] & 0xFF) | 
					((results[2] << 8) & 0xFF00) | 
					((results[1] << 16) & 0xFF0000) | 
					((results[0] << 24) & 0xFF000000)
				);
	}
	/**
	 * 大端方式的字节转换为长整型
	 * @param target 大端方式的二进制数组
	 * @return 转换后的长整型
	 * @throws Exception 
	 */
	public static long convertBidBytesToLong(byte[] target) throws Exception{

		if(target.length > 8){
			throw new Exception("超过long的取值范围!");
		}

		byte[] results = new byte[]{0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
		int targetIndex = 0;
		for(int i = 0; i < results.length ; i++){
			if(i >= results.length - target.length){
				results[i] = target[targetIndex++];
			}
		}
		
		long value = (
				(((long)results[7]) & 0xFFl) | 
				(((long)results[6] << 8) & 0xFF00l) | 
				(((long)results[5] << 16) & 0xFF0000l) | 
				(((long)results[4] << 24) & 0xFF000000l) |
				(((long)results[3] << 32) & 0xFF00000000l) |
				(((long)results[2] << 40) & 0xFF0000000000l) |
				(((long)results[1] << 48) & 0xFF000000000000l) |
				(((long)results[0] << 56) & 0xFF00000000000000l)
			);
		return value;
	}
	
	//下面部分是小端的方式转换
	/**
	 * 小端方式的字节转换为整型
	 * @param target 小端方式的二进制数组
	 * @return 转换后的整型
	 * @throws Exception 
	 */	
	public static int convertLittleBytesToInt(byte[] target) throws Exception{

		if(target.length > 4){
			throw new Exception("超过int的取值范围!");
		}

		byte[] results = new byte[]{0x0,0x0,0x0,0x0};
		int targetIndex = 0;
		//高低位换位
		for(int i= results.length; i>0; i--){
			if(i > results.length - target.length){
				results[i-1] = target[targetIndex++];
			}
		}

		return (
					(results[3] & 0xFF) | 
					((results[2] << 8) & 0xFF00) | 
					((results[1] << 16) & 0xFF0000) | 
					((results[0] << 24) & 0xFF000000)
				);
			
	}
	
	/**
	 * 小端方式的字节转换为长整型
	 * @param target 小端方式的二进制数组
	 * @return 转换后的长整型
	 * @throws Exception 
	 */
	public static long convertLittleBytesToLong(byte[] target) throws Exception{

		if(target.length > 8){
			throw new Exception("超过long的取值范围!");
		}

		byte[] results = new byte[]{0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0};
		int targetIndex = 0;
		//高低位换位
		for(int i= results.length; i>0; i--){
			if(i > results.length - target.length){
				results[i-1] = target[targetIndex++];
			}
		}
		
		long value = (
				(((long)results[7]) & 0xFFl) | 
				(((long)results[6] << 8) & 0xFF00l) | 
				(((long)results[5] << 16) & 0xFF0000l) | 
				(((long)results[4] << 24) & 0xFF000000l) |
				(((long)results[3] << 32) & 0xFF00000000l) |
				(((long)results[2] << 40) & 0xFF0000000000l) |
				(((long)results[1] << 48) & 0xFF000000000000l) |
				(((long)results[0] << 56) & 0xFF00000000000000l)
			);
		return value;
	}
    public static String biStringNot0x(byte b){
		
		StringBuffer sb = new StringBuffer();
		int v = b & 0xFF;          
		String hv = Integer.toHexString(v);          
		if (hv.length() < 2) {              
			sb.append(0);          
			}          
		sb.append(hv);  
		return sb.toString().toUpperCase();
	}
	public static String combineBidBytes(byte[] target){
		return "0x" + combineBiStringNot0x(target);
	}
	
	public static String combineBiStringNot0x(byte[] target){
		StringBuffer sb = new StringBuffer();
		for(byte b : target){
			int v = b & 0xFF;          
			String hv = Integer.toHexString(v);          
			if (hv.length() < 2) {              
				sb.append(0);          
				}          
			sb.append(hv);  
		}
		return sb.toString().toUpperCase();
	}
	
	//字符串转换

	/**
	 * 大端方式的二进制转换为字符串(指定的编码)
	 * target大端方式的二进制数组
	 * @param charsetName 指定的编码格式
	 * @return 转换后的字符串
	 * @throws UnsupportedEncodingException
	 */
	public static String convertBidBytesToString(byte[] target, String charsetName) throws UnsupportedEncodingException{
		return new String(target,charsetName);
	}
	/**
	 * 大端方式的二进制转换为字符串
	 * target小端方式的二进制数组
	 * @return 转换后的字符串
	 */
	public static String convertBidBytesToString(byte[] target){
		return new String(target);
	}
	/**
	 * 小端方式的二进制转换为字符串(指定的编码)
	 * target小端方式的二进制数组
	 * @param charsetName 指定的编码格式
	 * @return 转换后的字符串
	 * @throws UnsupportedEncodingException
	 */
	public static String convertLittleBytesToString(byte[] target, String charsetName) throws UnsupportedEncodingException{
		
		byte[] results = new byte[target.length];
		for(int i = 0;i<target.length;i++){
			results[i] = 0x0;
		}
		int targetIndex = 0;
		//高低位换位
		for(int i= results.length; i>0; i--){
			if(i > results.length - target.length){
				results[i-1] = target[targetIndex++];
			}
		}
		return convertBidBytesToString(results,charsetName);
	}
	/**
	 * 小端方式的二进制转换为字符串
	 * @param target 小端方式的二进制数组
	 * @return 转换后的字符串
	 */
	public static String convertLittleBytesToString(byte[] target){
		
		byte[] results = new byte[target.length];
		for(int i = 0;i<target.length;i++){
			results[i] = 0x0;
		}
		int targetIndex = 0;
		//高低位换位
		for(int i= results.length; i>0; i--){
			if(i > results.length - target.length){
				results[i-1] = target[targetIndex++];
			}
		}
		return convertBidBytesToString(results);
	}
	
	//正常字符往二进制转换
	/**
	 * 将int类型转换为byte[]
	 * @param 	target	要转换的int
	 * @return 	byte[]	转换后的结果	(大端方式)
	 */
	public static byte[] intToBytes(int target){
		
		byte[] digit = new byte[4];
		
		digit[3] = (byte) target;
		digit[2] = (byte) (target >> 8);
		digit[1] = (byte) (target >> 16);
		digit[0] = (byte) (target >> 24);
		
		return digit;
	}
	
	/**
	 * 将long类型转换为byte[]
	 * @param 	target	要转换的long
	 * @return 	byte[]	转换后的结果	(大端方式)
	 */
	public static byte[] longToBytes(long target){
		
		byte[] digit = new byte[8];
		
		digit[7] = (byte) target;
		digit[6] = (byte) (target >> 8);
		digit[5] = (byte) (target >> 16);
		digit[4] = (byte) (target >> 24);
		digit[3] = (byte) (target >> 32);
		digit[2] = (byte) (target >> 40);
		digit[1] = (byte) (target >> 48);
		digit[0] = (byte) (target >> 56);
		
		return digit;
	}
	
	/**
	 * 将String类型转换为byte[](使用默认字符集)
	 * @param 	target	要转换的String
	 * @return 	byte[]	转换后的结果	(大端方式)
	 */
	public static byte[] stringToBytes(String target){
		return target.getBytes();
	}
	
	/**
	 * 将String类型转换为byte[]
	 * @param 	target	要转换的String
	 * @param	charsetName	字符集名称
	 * @return 	byte[]	转换后的结果	(大端方式)
	 * @throws UnsupportedEncodingException 
	 */
	public static byte[] stringToBytes(String target, String charsetName) 
		throws UnsupportedEncodingException{
		
		return target.getBytes(charsetName);
	}
	
	public static byte[] HexStringToBinary(String hexString){
		//hexString的长度对2取整，作为bytes的长度
		int len = hexString.length()/2;
		byte[] bytes = new byte[len];
		byte high = 0;//字节高四位
		byte low = 0;//字节低四位

		for(int i=0;i<len;i++){
			 //右移四位得到高位
			 high = (byte)((hexStr.indexOf(hexString.charAt(2*i)))<<4);
			 low = (byte)hexStr.indexOf(hexString.charAt(2*i+1));
			 bytes[i] = (byte) (high|low);//高地位做或运算
		}
		return bytes;
	}
	
}
