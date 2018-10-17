package com.api.project.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PropertiesUtil {

	public static String readValue(String filePath, String key) {
		Properties props = new Properties();
		try {

			InputStream in = new FileInputStream(filePath);
			props.load(in);

			String value = props.getProperty(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 读取properties的全部信息
	public static List<Map<String,String>> readProperties(String filePath) throws Exception {
		
		if(filePath==null || filePath.equals("")){
			return null;
		}
		Properties props = new Properties();
		List<Map<String,String>>  list = new ArrayList<>();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					filePath));
			props.load(in);
			Enumeration en = props.propertyNames();
			Map<String,String> map = new HashMap<>();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String property = props.getProperty(key);
				
				map.put(key, property);
				
			}
			list.add(map);
		} catch (Exception e) {
			throw e;
		}
		
		return list;
	}

	// 写入properties信息
	public static void writeProperties(String filePath, Map<String ,String> infoMap) throws IOException {
		
		if(filePath==null || filePath.equals("")||infoMap==null ||infoMap.isEmpty() ){
			return ;
		}
		Properties prop = new Properties();
		try {
			
			File f = new File(filePath);
			if (!f.exists()) {
				f.createNewFile();
			}
			InputStream fis = new FileInputStream(f);
		
			prop.load(fis);
			OutputStream fos = new FileOutputStream(filePath);
			
			
			Set<String> keySet = infoMap.keySet();
			for(String s:keySet){
				prop.setProperty(s, infoMap.get(s));
				
			}
			prop.store(fos, "");
		} catch (IOException e) {
			throw e;
		}
	}
}
