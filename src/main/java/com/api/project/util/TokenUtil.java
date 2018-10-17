package com.api.project.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * 
 * @author v-chenyangchao-os
 *使用token认真resquest的header中包含有认证的token信息，
 *可以扩充超时   等等…
 */
public class TokenUtil {

	private static final String ALG = "DES";
	private final String BLUR = "SMLS";
	private RandomNum randomNum = RandomNum.getInstance();
	
	public static final String NAME = "name";
	
	public static final String CREATE = "create";
	
	private static TokenUtil util = new TokenUtil();
	
	private TokenUtil(){
		
	}
	/**
	 * 创建token
	 * @param name
	 * @param key 签证 可为空
	 * @return
	 * @throws Exception
	 */
	public static String buildToken(String name, String key) throws Exception{
		
		return util.build(name, key);
	}
	/**
	 * 验证接受到的token
	 * @param token
	 * @param key 签证可为空
	 * @return
	 * @throws Exception
	 */
	public static JsonObject validateToken(String token, String key) throws Exception{
		
		return util.validate(token, key);
	}
//	public static String getHeaderKey(String token) {
//		
//		return util.getKey(token);
//	}
	private String build(String name, String key) throws Exception{
		
		JsonObject jsonObject = this.createPlayLoad();
		
		jsonObject.addProperty(NAME, name);
		jsonObject.addProperty(CREATE, System.currentTimeMillis());
		
		String palyLoad = this.encodePlayLoad(jsonObject);
		
		Encrypt encrypt = null;
		if(StringUtil.isBlank(key)){
			
			encrypt = new Encrypt();
		}else{
			
			encrypt = new Encrypt(key);
		}
		String epl = encrypt.encrypt(palyLoad);
		int length = epl.length();
		String header = this.encodeHeader(length);
		String blur = null;
		blur = encrypt.encrypt(BLUR.concat(randomNum.getSeriesNum()));
		return header.concat(".").concat(epl.concat(blur));
	}
//    private String getKey(String token) {
//    	
//    	String header = token.substring(0, StringUtils.indexOf(token, "."));
//    	String jsonStr = this.decodeHeader(header);
//    	
//    	Gson gson = new Gson();
//    	JsonObject jsonObject = gson.fromJson(jsonStr, JsonObject.class);
//    	return jsonObject.get("key").getAsString();
//    }
    private JsonObject validate(String token, String key) throws Exception{
    	
    	String header = token.substring(0, StringUtils.indexOf(token, "."));
    	String jsonStr = this.decodeHeader(header);
    	
    	Gson gson = new Gson();
    	JsonObject jsonObject = gson.fromJson(jsonStr, JsonObject.class);
    	String alg = jsonObject.get("alg").getAsString();
    	if(StringUtil.isBlank(alg)){
    		
    		return null;
    	}
    	int length = jsonObject.get("length").getAsInt();
    	int index = StringUtils.indexOf(token, ".")+1;
    	String playLoad = token.substring(index, index + length);
    	if(ALG.equals(alg)){
    		
    		Encrypt encrypt = null;
        	if(StringUtil.isBlank(key)){
        		
        		encrypt = new Encrypt();
        	}else{
        		
        		encrypt = new Encrypt(key);
        	}
        	
        	String dpl = encrypt.decrypt(playLoad);
        	String jsonPL = this.decodePlayLoad(dpl);
        	return gson.fromJson(jsonPL, JsonObject.class);
    	}else{
    		
    		throw new Exception("错误的算法");
    	}
    }
	/**
	 * 编码头
	 * @return
	 */
	private String encodeHeader(int length){
		
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("type", "JWT");
		jsonObject.addProperty("alg", ALG);
		jsonObject.addProperty("length", length);
		
		String header = jsonObject.toString();
		String ret = Base64.encodeBase64String(header.getBytes());
	    return ret;
	}
	/**
	 * 解码头
	 * @param header
	 * @return
	 */
	private String decodeHeader(String header){
		
		byte[] b = Base64.decodeBase64(header);
		return new String(b);
	}
	/**
	 * 编码palyLoad
	 * @param object
	 * @return
	 */
	private String encodePlayLoad(JsonObject object){
		
		String pl = object.toString();
		String ret = Base64.encodeBase64String(pl.getBytes());
		return ret;
	}
	/**
	 * 解码palyLoad
	 * @param playLoad
	 * @return
	 */
	private String decodePlayLoad(String playLoad){
		
		byte[] b = Base64.decodeBase64(playLoad);
		return new String(b);
	}
	/**
	 * 后面扩充用
	 * @return
	 */
	private JsonObject createPlayLoad(){
		
		JsonObject jsonObject = new JsonObject();
		
		jsonObject.addProperty("iss", "");  //jwt签发者
		jsonObject.addProperty("sub", "");  //jwt所面向的用户
		jsonObject.addProperty("aud", "");  //接收jwt的一方
		jsonObject.addProperty("exp", "");  //jwt的过期时间，这个过期时间必须要大于签发时间
		jsonObject.addProperty("nbf", "");  //定义在什么时间之前，该jwt都是不可用的.
		jsonObject.addProperty("iat", "");  //jwt的签发时间
		jsonObject.addProperty("jti", "");  //jwt的唯一身份标识，主要用来作为一次性token,从而回避重放攻击。
		
		return jsonObject; 
	}
	static class RandomNum {
		
		private char firstNum = 65;
		private char secondNum = 65;
		private String headTag = "AA";
		private int series = 0;
		
		private static RandomNum rn = new RandomNum();
		private RandomNum(){
		}
		public synchronized static RandomNum getInstance() {
			return rn;
		}
		
		public synchronized String getSeriesNum() {
			if (++series > 9999999) {
				series = 0;
				if (++secondNum > 90) {
					secondNum = 65;
					if (++firstNum > 90)
						firstNum = 65;
				}
				headTag = String.valueOf(firstNum).concat(
						String.valueOf(secondNum));
			}
			
			return headTag+series;
		}
	}
//	public static void main(String[] args) {
//		
//		TokenUtil util = new TokenUtil();
//		try {
//			String token = util.buildToken("chenyangchao",  "");
//			System.out.println(token);
//			JsonObject jsonObject = util.validateToken(token, null);
//	        System.out.println(jsonObject.get("name").getAsString());
//	        System.out.println(jsonObject.get("create").getAsLong());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
