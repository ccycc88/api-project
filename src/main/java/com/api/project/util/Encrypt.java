package com.api.project.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class Encrypt {

	private String DEFAULT_CRYPT_KEY = "api_server";
	private final  String DES = "DES";
	private static final String charset = "UTF-8";
	private static final String KEY_SHA = "SHA";
	private static final char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
	
	/**
	 * 构造函数
	 */
	public Encrypt() {
	}
	
	/**
	 * 构造函数
	 * @key 密钥
	 */
	public Encrypt(String key) {
		this.DEFAULT_CRYPT_KEY=key;
	}
	
	
	/**
	 * 加密
	 * @param src 数据源
	 * @param key 密钥，长度必须是8的倍数
	 * @return 返回加密后的数据
	 * @throws Exception
	 */
	private  byte[] encrypt(byte[] src, byte[] key) throws Exception {

		// DES算法要求有一个可信任的随机数源
		SecureRandom sr = new SecureRandom();
		// 从原始密匙数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);
		// 创建一个密匙工厂，然后用它把DESKeySpec转换成
		// 一个SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance(DES);
		// 用密匙初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
		// 现在，获取数据并加密, 正式执行加密操作
		return cipher.doFinal(src);
		
	}

	/**
	 * 解密
	 * @param src 数据源
	 * @param key 密钥，长度必须是8的倍数
	 * @return 返回解密后的原始数据
	 * @throws Exception
	 */

	private  byte[] decrypt(byte[] src, byte[] key) throws Exception {

		// DES算法要求有一个可信任的随机数源
		SecureRandom sr = new SecureRandom();
		// 从原始密匙数据创建一个DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);
		// 创建一个密匙工厂，然后用它把DESKeySpec对象转换成一个SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
		SecretKey securekey = keyFactory.generateSecret(dks);
		// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance(DES);
		// 用密匙初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
		// 现在，获取数据并解密,正式执行解密操作
		return cipher.doFinal(src);
	}

	/**
	 * 密码解密
	 * @param data
	 * @return
	 * @throws Exception
	 */

	public   String decrypt(String data) throws Exception {
		try {
			
			byte[] bs=decrypt(hex2byte(data.getBytes()),DEFAULT_CRYPT_KEY.getBytes());				
			return new String(bs);
		} catch (Exception e) {
			
			throw new Exception("字符串解密失败",e);
		}
	}

	/**
	 * 
	 * 密码加密
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public   String encrypt(String password) throws Exception {

		try {
			
			byte[] bs=encrypt(password.getBytes(), DEFAULT_CRYPT_KEY.getBytes());
			return byte2hex(bs);
		} catch (Exception e) {
			
			throw new Exception("字符串加密失败",e);
		}
	}

	/**
	 * 二行制转字符串
	 */
	private  String byte2hex(byte[] b) {

		String hs = "";
		String stmp = "";
		
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1){
				hs = hs + "0" + stmp;
			}else{
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}
	/**
	 * 十六进制转换为二行制
	 */
	private  byte[] hex2byte(byte[] b) {

		if ((b.length % 2) != 0)
			throw new IllegalArgumentException("长度不是偶数");
		
		byte[] b2 = new byte[b.length / 2];
		
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	/**
     * SHA-1算法
     *
     * @param paramString
     * @return
     */
    public static String getStringSHA(String paramString) {
        try {
            byte[] tmp = paramString.getBytes(charset);
            return getByteSHA(tmp);
        } catch (Exception ex) {
            //logger.error("getStringSHA error: " + ex.getLocalizedMessage(), ex.fillInStackTrace());
        	ex.printStackTrace();
        }
        return null;
    }
    /**
     * SHA-1算法
     *
     * @param paramString
     * @return
     */
    public static String getByteSHA(byte[] paramString) {
        return getByteSHA(paramString, KEY_SHA);
    }
    private static String getByteSHA(byte[] paramString, String strType) {
        String str = null;
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance(strType);
            localMessageDigest.update(paramString);
            str = toHexString(localMessageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return str;
    }
    private static String toHexString(byte[] paramArrayOfByte) {
        StringBuilder localStringBuilder = new StringBuilder(paramArrayOfByte.length * 2);
        for (int i = 0; i < paramArrayOfByte.length; ++i) {
            localStringBuilder.append(hexChar[((paramArrayOfByte[i] & 0xF0) >>> 4)]);
            localStringBuilder.append(hexChar[(paramArrayOfByte[i] & 0xF)]);
        }
        return localStringBuilder.toString();
    }
//	public static void main(String[] args) {
//		try {
//			Encrypt encryptString=new Encrypt();
//			String str=encryptString.encrypt("1233");
//			System.out.println("加密后的字符串是       " +str );
//			System.out.println("解密后的字符串是       " + encryptString.decrypt(str));
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
