package com.api.project.common.ftp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.parser.ConfigurableFTPFileEntryParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

public class ExtendsConfigurableFTPFileEntryParserImpl extends ConfigurableFTPFileEntryParserImpl{

	private Logger log = LoggerFactory.getLogger(ExtendsConfigurableFTPFileEntryParserImpl.class);
	
	//, , , , , , , , , , , 
	private static ImmutableMap<String, String> monthM = null;
	static {
	
		ImmutableMap.Builder<String, String> months = ImmutableMap.builder();
		months.put("Jan", "01");
		months.put("Feb", "02");
		months.put("Mar", "03");
		months.put("Apr", "04");
		months.put("May", "05");
		months.put("Jun", "06");
		months.put("Jul", "07");
		months.put("Aug" ,"08");
		months.put("Sep", "09");
		months.put("Oct", "10");
		months.put("Nov", "11");
		months.put("Dec", "12");
		
		monthM = months.build();
	}
	
	public ExtendsConfigurableFTPFileEntryParserImpl(){
		this("");
	}
	public ExtendsConfigurableFTPFileEntryParserImpl(String regex) {
		super(regex);
		// TODO Auto-generated constructor stub
	}

	public FTPFile parseFTPEntry(String entry) {
		// TODO Auto-generated method stub
		FTPFile file = new FTPFile();
	    file.setRawListing(entry);
	    //获取文件属性
	    String[] attribut = entry.split("\\s+");

	    if(attribut == null || attribut.length == 0){
	    	return null;
	    }
	    if(attribut.length != 8 && attribut.length != 9){
	    	
	    	return null;
	    }
	    //处理文件类型
	    String fileType = attribut[0].substring(0, 1);

	    if ("d".equals(fileType)) {

	      file.setType(FTPFile.DIRECTORY_TYPE);
	    } else {

	      //文件类型
	      file.setType(FTPFile.FILE_TYPE);
	      file.setSize(Integer.valueOf(attribut[4]));
	    }
	    //设置用户
	    file.setUser(attribut[3]);
	    //-rw-rw-r--    1 root     system       422448  9月20 19时00 ztecounter_3113_201609201900.zip
	    Calendar calendar = Calendar.getInstance();
	    if(attribut.length == 8){
	    	
	    	//匹配到文件
	    	if(attribut[6].matches("\\d{4}")){
	    		
	    	  if(attribut[5].indexOf("月") != -1){
	    		  
	    		  SimpleDateFormat format = new SimpleDateFormat("yyyyMM月dd");
				  try {
					  Date date = format.parse(attribut[6] + attribut[5]);
					  calendar.setTime(date);
					  file.setName(attribut[7]);
				  } catch (ParseException e) {
					  // TODO Auto-generated catch block
					  e.printStackTrace();
				  }
	    	  }
	    	}else if(attribut[6].indexOf("时") !=-1){
	    		
	    		if(attribut[5].indexOf("月") != -1){
	    			
	    			SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd天HH时mm");
	    			try {
						Date date = format.parse(calendar.get(Calendar.YEAR)+"年"+attribut[5]+"天" + attribut[6]);
						calendar.setTime(date);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    			file.setName(attribut[7]);
	    		}
	    	}else{
	    		
	    		log.error("不可处理数据[" + entry + "]");
	    		return null;
	    	}
	    }else if(attribut.length == 9){
	    	
	    	if(attribut[7].matches("\\d{4}")){
	    		
	    		//匹配到日期
	    		String month = monthM.get(attribut[5]);
	    		if(month != null){
	    		
	    			if(attribut[6].matches("\\d{1,2}")){
	    				
	    				SimpleDateFormat format = new SimpleDateFormat("yyyyMM月dd");
	    				  try {
	    					  Date date = format.parse(attribut[7] + month + "月" + attribut[6]);
	    					  calendar.setTime(date);
	    					  file.setName(attribut[8]);
	    				  } catch (ParseException e) {
	    					  // TODO Auto-generated catch block
	    					  e.printStackTrace();
	    				  }
	    			}
	    		}
	    	}else if(attribut[7].indexOf(":") != -1){
	    		
	    		String month = monthM.get(attribut[5]);
	    		if(month != null){
	    			
	    			if(attribut[6].matches("\\d{1,2}")){
	    				
	    				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    				try {
							Date date = format.parse(calendar.get(Calendar.YEAR)+"-" + month + "-"+attribut[6] + " " + attribut[7]);
						    calendar.setTime(date);
						    file.setName(attribut[8]);
	    				} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
	    		}
	    	}else{
	    		log.error("不可处理数据[" + entry + "]");
	    		return null;
	    	}
	    	
	    }else{
	    	log.error("不可处理数据[" + entry + "]");
	    	return null;
	    }
	    file.setTimestamp(calendar);
	    return file;
	}

	@Override
	protected FTPClientConfig getDefaultConfiguration() {
		// TODO Auto-generated method stub
		Class clazz = ExtendsConfigurableFTPFileEntryParserImpl.class;
		return new FTPClientConfig(clazz.getPackage().getName()

			      + clazz.getSimpleName(), "", "", "", "", "");
	}
}
