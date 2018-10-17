package com.api.project.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CloneUtil {

	/**
	 * 如果克隆失败，可以再重新clone一次
	 * 
	 * @param obj
	 * @return
	 */
	public static Object clone(Serializable obj) {
		Object clone = null;
		try {
			clone = cloneObject(obj);
			if (clone == null) {
				clone = cloneObject(obj);
			}
		} catch (Exception e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {

			}
			clone = cloneObject(obj);
			if (clone == null) {
				clone = cloneObject(obj);
			}
		}

		return clone;
	}

	/**
	 * 
	 * Object
	 * 
	 * @param obj
	 * @return Mar 16, 2012
	 */
	public static Object cloneObject(Serializable obj) {
		try {
			return toObject(toByte(obj));
		} catch (IOException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} catch (StackOverflowError error) {
			System.out.println("stack length " + error.getStackTrace().length);
			error.printStackTrace();
			return null;
		}
	}

	public static Object toObject(byte[] array) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(array);
		ObjectInputStream ois = null;

		try {
			ois = new ObjectInputStream(bais);
			return ois.readObject();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (Exception e) {
				}
			}
			if (bais != null) {
				try {
					bais.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public static byte[] toByte(Serializable obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;

		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			return baos.toByteArray();
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
				}
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (Exception e) {
				}
			}
		}
	}

	// 获得对象内存大小，采用序列化方式
	public static int getObjectSize(Serializable obj) {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ObjectOutputStream os = null;
		int ret = 0;
		try {
			os = new ObjectOutputStream(bs);
			os.writeObject(obj);
			os.flush();
			ret = bs.size();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (bs != null) {
				try {
					bs.close();
					bs = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (os != null) {
				try {
					os.close();
					os = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	public static String Object2str(Serializable obj) throws Exception {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
		oos.writeObject(obj);
		String serStr = byteArrayOutputStream.toString("ISO-8859-1");
		serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
		oos.close();
		byteArrayOutputStream.close();
		return serStr;
	}

	public static Serializable unSerialize(String serStr) throws Exception {
		String redStr = java.net.URLDecoder.decode(serStr, "UTF-8");
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redStr.getBytes("ISO-8859-1"));
		ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
		Object obj = objectInputStream.readObject();
		objectInputStream.close();
		byteArrayInputStream.close();
		return (Serializable) obj;
	}
}
