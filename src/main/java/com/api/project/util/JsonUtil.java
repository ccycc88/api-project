package com.api.project.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

public class JsonUtil {

	/**
	 * 
	 * @param json
	 * @param clz 目前仅支持 map  list string
	 * @return 数据按照json数据的数据顺序
	 */
	public static List<Map<String, String>> jsonToOut(String json, Class<?> clz) {

		Gson gson = new Gson();
		
		Object obj = gson.fromJson(json, clz);
		List<Map<String, String>> result = new LinkedList<>();
		jsongToMap(obj, null, null, null, null,result);
		return result;
	}

	private static void jsongToMap(Object source, Map map, 
			List list, String str, String key, List<Map<String, String>> result) {

		if(source != null){
			
			if(source instanceof Map){
				
				map = (Map) source;
			}else if(source instanceof List){
				
				list = (List) source;
			}else if(source instanceof String){
				
				str = (String) source;
			}
		}
		if (map != null) {

			Iterator<Entry> it = map.entrySet().iterator();
			while (it.hasNext()) {

				Entry en = it.next();
				String km = (String) en.getKey();
				Object v = en.getValue();
				if (v instanceof List) {

					List l = (List) v;
					jsongToMap(null, null, l, null, km, result);
				} else if (v instanceof Map) {

					Map m = (Map) v;
					jsongToMap(null, m, null, null, km, result);
				} else if (v instanceof String) {

					String s = (String) v;
					jsongToMap(null, null, null, s, km, result);
				}
			}
		} else if (list != null) {

			Iterator it = list.iterator();
			while (it.hasNext()) {

				Object o = it.next();

				if (o instanceof Map) {

					Map m = (Map) o;
					jsongToMap(null, m, null, null, null, result);
				} else if (o instanceof List) {

					List l = (List) o;
					jsongToMap(null, null, l, null, null, result);
				} else if (o instanceof String) {

					String s = (String) o;
					jsongToMap(null, null, null, s, null, result);
				}
			}
		} else if (str != null) {

			Map<String, String> res = new HashMap<>();
			res.put(key, str);
			result.add(res);
		}
	}
}
