package com.api.project.common.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.api.project.util.StringUtil;

public abstract class Cache {

	private String name = null;

	public String getName() {

		if(StringUtil.isBlank(name)) {

			return Thread.currentThread().getName()+"_cache";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	/**
	 * 初始化
	 */
	public abstract void init();
	/**
	 * 刷新
	 */
	public abstract void refresh();

	public abstract boolean exist(String... key) throws Exception;

	public abstract boolean exist(String hkey, String... key) throws Exception;

	public abstract boolean exist(String[] hkeys, String... key) throws Exception;

	public abstract boolean existObject(String obj, String... key) throws Exception;

	public abstract boolean existObject(String[] objs, String... key) throws Exception;

	public abstract <T> T get(Class<T> clz, String... key) throws Exception;

	public abstract <T> T get(Class<T> clz, String hkey, String... key) throws Exception;

	public abstract <T> T get(Class<T> clz, int index, String... key) throws Exception;

	public abstract void delete(String... key) throws Exception;

	public abstract void delete(String hkey, String... key) throws Exception;

	public abstract void delete(String[] hkeys, String... key) throws Exception;

	public abstract void deleteObject(String obj, String... key) throws Exception;

	public abstract void deleteObject(String[] objs, String... key) throws Exception;

	public abstract void put(String value, String... key) throws Exception;

	public abstract void put(TimeUnit unit, long expire_in, String value, String... key) throws Exception;

	public abstract void put(Map<String, String> value, String... key) throws Exception;

	public abstract void put(TimeUnit unit, long expire_in, Map<String, String> value, String... key) throws Exception;

	public abstract void put(List<String> value, String... key) throws Exception;

	public abstract void put(TimeUnit unit, long expire_in, List<String> value, String... key) throws Exception;

	public abstract void put(Set<String> value, String... key) throws Exception;

	public abstract void put(TimeUnit unit, long expire_in, Set<String> value, String... key) throws Exception;

	public abstract String[] keys(String pattern) throws Exception;
}
