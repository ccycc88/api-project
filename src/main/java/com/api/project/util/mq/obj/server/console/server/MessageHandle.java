package com.api.project.util.mq.obj.server.console.server;

import java.util.Map;

import com.api.project.util.mq.obj.server.DataContainer;
import com.api.project.util.mq.obj.server.DataStore;

public class MessageHandle {

	/**
	 * 处理命令
	 * @param cmd
	 * @param param
	 * @return
	 */
	static String handle(String cmd,Map<String,String> param) {
		if(CommandHandle.queue.equals(cmd)) {
			if(param == null || param.size() == 0) {
				return "parameter required\n" + CommandHandle.commandMap.get(CommandHandle.queue);
			}
			return queue(param);
		}
		return cmd;
	}

	/**
	 * 处理queue 命令
	 * @param param
	 * @return
	 */
	private static String queue(Map<String, String> param) {
		String value = null;
		if(param.containsKey("show")) {
			value = param.get("show");
			if(value == null || value.trim().length() == 0) {
				return DataStore.getStore().toString();
			}else {
				return DataStore.getStore().size(value);
			}
		}else if(param.containsKey("get")) {
			value = param.get("get");
			if(value == null || value.trim().length() == 0) {
				return "a queue name required.";
			}else {
				DataContainer data = DataStore.getStore().get(value, null);
				return data == null ? "no queue named ["+value+"]" : data.toString();
			}
		}else if(param.containsKey("clean")) {
			value = param.get("clean");
			if(value == null || value.trim().length() == 0) {
				return "a queue name required.";
			}else {
				return DataStore.getStore().clear(value);
			}
		}else if(param.containsKey("set")) {
			value = param.get("set");
			if(value == null || value.trim().length() == 0) {
				return "a queue name required.";
			}else {
				String strLen = param.get("length");
				if(strLen == null || strLen.trim().length() == 0) {
					return "the length of the queue required.";
				}
				int intLen = -1;
				try {
					intLen = Integer.parseInt(strLen);
				} catch (NumberFormatException e) {
					return "the length must be number.wrong type : " + strLen;
				}
				DataStore.getStore().setMaxLength(value, intLen);
				return "set max length of ["+value+"] successed.";
			}
		}else {
			return "wrong parameter\n" + CommandHandle.commandMap.get(CommandHandle.queue);
		}
	}
}
