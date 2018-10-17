package com.api.project.util.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class OpenUrl {

	private String open(String url) throws MalformedURLException, IOException {
		HttpURLConnection huc=(HttpURLConnection)new URL(url).openConnection();
		huc.setRequestMethod("GET");
        huc.setUseCaches(true);
        huc.connect();
        
        InputStream is=huc.getInputStream();
        BufferedReader reader=new BufferedReader(
        		new InputStreamReader(is,huc.getContentType().equals("text-html; charset=gb2312")?"gb2312":"UTF-8"));
        StringBuilder temp=new StringBuilder();
        String str;
        while((str=reader.readLine())!=null){
            temp.append(str+"\n");
        }
        return temp.toString();

	}
}
