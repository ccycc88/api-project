package com.api.project.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * 方法签名认证
 * @author v-chenyangchao-os
 *
 */
public class ApiSignUtil {

	/**
	 * 创建签名串
	 * @param data
	 * @param keyt
	 * @return
	 */
	public static String buildSign(Map<String, String> data, String keyt) {

		//删除空值
		for(Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();it.hasNext();){

			Map.Entry<String, String> en = it.next();
			if(StringUtil.isBlank(en.getValue())){

				it.remove();
			}
		}
		//仅对值进行排序
		Ordering<Map.Entry<String, String>> ordering = Ordering.from(new Comparator<Map.Entry<String,String>>(){

			@Override
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
				return -o1.getKey().compareTo(o2.getKey());
			}
		});
		//仅对值进行排序

		List<Map.Entry<String, String>> sorted = ordering.sortedCopy(data.entrySet());

		StringBuilder sb = new StringBuilder();
		//排序后数据进行拼接加密
		sb.append(sorted.get(0).getKey());
		sb.append(sorted.get(0).getValue());
		List<Map.Entry<String, String>> over = sorted.subList(1, sorted.size());
		for(int i=0; i<over.size(); i++) {
			
			Map.Entry<String, String> sm = over.get(i);
			sb.append("&");
			sb.append(sm.getKey());
			sb.append(sm.getValue());
		}
		sb.append("&");
		sb.append(keyt);
		return new MD5().toMD5Str(sb.toString());
	}

	/**
	 * 验证签名串
	 * @param params
	 * @param keyt
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static boolean validateSign(Map<String, String> params, String keyt) {

		String sign = params.remove("sign");

		//删除空值
		for(Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();it.hasNext();){

			Map.Entry<String, String> en = it.next();
			if(StringUtil.isBlank(en.getValue())){

				it.remove();
			}
		}
		//仅对值进行排序
		Ordering<Map.Entry<String, String>> ordering = Ordering.from(new Comparator<Map.Entry<String,String>>(){

			@Override
			public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
				return -o1.getKey().compareTo(o2.getKey());
			}
		});
		//仅对值进行排序

		List<Map.Entry<String, String>> sorted = ordering.sortedCopy(params.entrySet());

		StringBuilder sb = new StringBuilder();
		//排序后数据进行拼接加密
		sb.append(sorted.get(0).getKey());
		sb.append(sorted.get(0).getValue());
		List<Map.Entry<String, String>> over = sorted.subList(1, sorted.size());
		for(int i=0; i<over.size(); i++) {

			Map.Entry<String, String> sm = over.get(i);
			sb.append("&");
			sb.append(sm.getKey());
			sb.append(sm.getValue());
		}
		sb.append("&");
		sb.append(keyt);
		if(new MD5().toMD5Str(sb.toString()).equalsIgnoreCase(sign)) {
			
			return true;
		}
		return false;
	}
}
