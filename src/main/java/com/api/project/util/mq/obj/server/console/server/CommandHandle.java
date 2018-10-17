package com.api.project.util.mq.obj.server.console.server;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHandle {

	private static Logger log = LoggerFactory.getLogger(CommandHandle.class);
	/**
	 * @param lineCmd : cmd -parameterKey1 parameterValue1 -parameterKey2 parameterValue2 ...
	 * @return
	 */
	static String handle(String lineCmd) {
		if(lineCmd == null || lineCmd.trim().length() == 0) {
			return "";
		}
		lineCmd = lineCmd.trim();
		String [] array = lineCmd.split("-");
		String cmd = array[0].trim();
		Map<String, String> param = new HashMap<>();
		
		String pv[] = null;
		for(int i = 1; i < array.length; i++) {
			array[i] = array[i].trim();
			pv = array[i].split("\\s+");
			param.put(pv[0], array[i].substring(pv[0].length()).trim());
		}
		log.info("处理后的命令["+cmd+"]，参数：" + param);
		if(!commandMap.containsKey(cmd)) {
			return "[" + cmd + "] Command not found.";
		}
		
		//帮助命令的处理
		if(help.equals(cmd)) {
			if(pv != null) {
				Command c = commandMap.get(pv[0]);
				if(c == null) {
					return "[" + pv[0] + "] Command not found.";
				}else {
					return c.toString();
				}
			}else {
				return commandMap.keySet().toString();
			}
		}
		
		//其它命令的处理
		return MessageHandle.handle(cmd, param);
	}
	
	
	public static final Map<String,Command>commandMap = new HashMap<>();
	public static final String help = "man";
	public static final String queue = "queue";
	static {
		Command man = new Command(help);
		man.addParameter("command", "help for command. for example: "+help+" -"+help);
		commandMap.put(man.getCmd(), man);
		
		Command cmd = new Command(queue);
		cmd.addParameter("show", "show the details for the queue.if have no queue name,show all queue's details\n\t\tfor example : queue -show -qName");
		cmd.addParameter("get", "fetch the data from the queue\n\t\tfor example : queue -get qName");
		cmd.addParameter("clean", "clean the data int the queue\n\t\tfor example : queue -clean qName");
		cmd.addParameter("set", "set the length of a queue\n\t\tfor example : queue -set qName -length 30000");
		commandMap.put(cmd.getCmd(), cmd);
	}
}
