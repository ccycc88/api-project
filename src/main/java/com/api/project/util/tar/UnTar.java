package com.api.project.util.tar;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

public class UnTar {

	public void unCompress(String tarFilePath, String destPath) 
			throws IOException {
			FileInputStream fileIn = null;
			BufferedInputStream bufIn = null;
			TarInputStream taris = null;
			try {
				fileIn = new FileInputStream(tarFilePath);
				bufIn = new BufferedInputStream(fileIn);
				taris = new TarInputStream(bufIn);
				TarEntry entry = null;
				while ((entry = taris.getNextEntry()) != null) {
					if (entry.isDirectory()) 
						continue;
					
					FileOutputStream output = new FileOutputStream(
							destPath+entry.getName(), false);
					taris.copyEntryContents(output);
					output.close();
				}
			}finally {
				if (taris != null) {
					try {
						taris.close();
					}catch(Exception ex) {
					}
				}
			}
		}
}
