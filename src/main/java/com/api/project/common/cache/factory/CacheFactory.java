package com.api.project.common.cache.factory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.api.project.common.cache.Cache;

public class CacheFactory {

	private static Map<String, CacheFactory> factories = new HashMap<>();

	private Cache cache = null;

	private String name = null;

	private Class<?> clz = null;

	private Long delay = null;


	public Long getDelay() {
		return delay;
	}
	public void setDelay(Long delay) {
		this.delay = delay;
	}
	private CacheFactory(Class<?> clz) {

		try {

			cache = (Cache) clz.newInstance();
			cache.setName(cache.getClass().getSimpleName() + "_cache");
			name = cache.getClass().getSimpleName()+"_factory";
			this.clz = clz;
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static CacheFactory getInstance(Class<?> clz) {

		String simpleName = clz.getSimpleName();
		if(factories.get(simpleName) == null) {

			synchronized (factories) {

				if(factories.get(simpleName) == null) {

					CacheFactory factory = new CacheFactory(clz);
					if(factory.cache != null) {

						factories.put(simpleName, factory);
					}
				}

			}
		}

		return factories.get(simpleName);
	}
	public void refresh() {

		cache.refresh();
	}
	public void init() {

		cache.init();
	}
	//	public static Map<String, CacheFactory> getTotalFactory(){
//
//		Map<String, CacheFactory> tmp = Maps.newHashMap(factories);
//		return tmp;
//	}
	public static Map<String, CacheFactory> getTaskFactory(){

		synchronized (factories) {

			Iterator<Entry<String, CacheFactory>> it =
					factories.entrySet().iterator();
			Map<String, CacheFactory> m = new HashMap<>();

			while(it.hasNext()) {

				Entry<String, CacheFactory> en = it.next();
				if(en.getValue().getDelay() == null || en.getValue().getDelay() < 0) {

					continue;
				}
				m.put(en.getKey(), en.getValue());
			}

			return m;
		}
	}
	public static Map<String, CacheFactory> getTotalFactory(){

		synchronized (factories) {

			Map<String, CacheFactory> tmp = new HashMap<>(factories);
			return tmp;
		}

	}
	public Cache getCache() {
		return cache;
	}
	public String getName() {
		return name;
	}

}
