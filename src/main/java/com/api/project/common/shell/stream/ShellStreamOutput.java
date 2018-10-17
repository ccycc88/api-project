package com.api.project.common.shell.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;

public abstract class ShellStreamOutput extends Thread{

	private InputStream in = null;
	private String type = null;
	private Logger logger = null;
	
	public static final String STREAMERROR = "ERROE:";
	public static final String STREAMINPUT = "INPUT:";
	
	public ShellStreamOutput() {
		
	}
	public ShellStreamOutput(InputStream in, String type, Logger logger){
		
		this.in = in;
		this.type = type;
		this.logger = logger;
	}
	public void run() {
		
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		String line = null;
		try {
			
			isr = new InputStreamReader(in);
			br = new BufferedReader(isr);
			
			while((line = br.readLine()) != null && !isInterrupted()){
				
				this.resultParse(type.concat(line));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("shell类型[" + type + "]执行异常[" + com.api.project.util.StringUtil.createStackTrace(e) + "]");
		}finally{
			
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}finally{
				
				try {
					if(isr != null){
						isr.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}finally{
					try {
						if(in != null){
							in.close();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	public abstract void resultParse(String line);
}
