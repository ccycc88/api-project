package com.api.project.util.mq.obj.server.console.client;

import java.util.Scanner;

public class ConsoleClient {

	public static void main(String [] config) {
		config = new String[] {"4445"};
		CommandExcutor excutor = null;
		if(config == null || config.length == 0) {
			System.out.println("启动时请添加需要连接的服务器端IP和端口，如果只有一个参数，系统认为是端口数据，IP默认为127.0.0.1");
			System.exit(-1);
		}
		String ip = null;
		int port = -1;
		if(config.length == 1) {
			port = Integer.parseInt(config[0]);
			ip = "127.0.0.1";
		}else {
			port = Integer.parseInt(config[1]);
			ip = config[0];
		}
		excutor = new CommandExcutor(ip,port);
		while(true) {
			System.out.print("gcpmq# ");
			Scanner input = new Scanner(System.in);
			String cmd = input.nextLine().trim();
			if (cmd.length() <= 0)
				continue;
			
			if("exit".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
				System.out.println("bye bye!");
				break;
			}
			
			System.out.print(excutor.execute(cmd));
		}
	}

}
