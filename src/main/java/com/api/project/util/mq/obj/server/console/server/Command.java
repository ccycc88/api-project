package com.api.project.util.mq.obj.server.console.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Command {

	Command(String c){
		this.cmd = c;
	}
	Command(){
		this(null);
	}
	//命令
	private String cmd = null;
	
	//paramKey,parameter description
	private final Map<String,String> param = new HashMap<>();
	
	/**
	 * 
	 * @param p : parameter key
	 * @param description : paramter description
	 */
	public void addParameter(String p,String description) {
		if(p == null) return;
		param.put(p, description);
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(cmd);
		if(param != null && param.size() > 0) {
			Iterator<String> keyIter = param.keySet().iterator();
			String key = null;
			while(keyIter.hasNext()) {
				key = keyIter.next();
				sb.append("\n").append("\t-").append(key).append("\t").append(param.get(key)).append("\n");
			}
		}else {
			sb.append("\t").append("command only with no parameter.");
		}
		return sb.toString();
	}
	
	public int hashCode() {
		if(cmd == null) {
			return super.hashCode();
		}else {
			return cmd.hashCode();
		}
	}
	
	public boolean equals(Object obj) {
		if(this.cmd == null) {
			return super.equals(obj);
		}

		if(obj == null) {
			return false;
		}else {
			if(obj instanceof Command) {
				return cmd.equals(((Command)obj).getCmd());
			}else {
				return super.equals(obj);
			}
		}
	}
}
