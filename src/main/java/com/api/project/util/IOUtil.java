package com.api.project.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOUtil {

	public static byte[] getStreamByte(InputStream in) throws IOException {
		
		ByteArrayOutputStream bos = null;
		
		try {
		
			bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int length = -1;
			while((length = in.read(buf)) != -1) {
				
				bos.write(buf, 0, length);
			}
			return bos.toByteArray();
		} finally {
			
			try {
				if(bos != null) {
					
					bos.close();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
