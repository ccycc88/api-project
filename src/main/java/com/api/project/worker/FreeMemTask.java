package com.api.project.worker;

import com.api.project.util.helper.SysHelper;

public class FreeMemTask extends Thread{

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			while(true){
		
				SysHelper.waitIt(this, 4*60*1000);
				Runtime.getRuntime().gc();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
