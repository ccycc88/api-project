package com.api.project.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class EasyRestUtil {
	
	private static PoolingHttpClientConnectionManager connMgr;  
    private static RequestConfig requestConfig;  
    private static final int MAX_TIMEOUT = 7000;  
    
    static {  
        // 设置连接池  
        connMgr = new PoolingHttpClientConnectionManager();  
        // 设置连接池大小  
        connMgr.setMaxTotal(100);  
        connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());  
  
        RequestConfig.Builder configBuilder = RequestConfig.custom();  
        // 设置连接超时  
        configBuilder.setConnectTimeout(MAX_TIMEOUT);  
        // 设置读取超时  
        configBuilder.setSocketTimeout(MAX_TIMEOUT);  
        // 设置从连接池获取连接实例的超时  
        configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);  
        // 在提交请求之前 测试连接是否可用  
        configBuilder.setStaleConnectionCheckEnabled(true);  
        requestConfig = configBuilder.build();  
    }  
  
	public static String post(String url, Map<String, String> params) throws Exception{
		
		HttpClientBuilder builder = HttpClientBuilder.create();
		CloseableHttpClient client = builder.build();
		
		HttpPost post = new HttpPost(url);
		
		try {
			
			if(params != null && params.size() != 0){
			
				List<NameValuePair> nvps = new ArrayList<>();
				
				Iterator<Entry<String, String>> it = 
						          params.entrySet().iterator();
				
				while(it.hasNext()){
					
					Entry<String, String> en = it.next();
					nvps.add(new BasicNameValuePair(en.getKey(), en.getValue()));
				}
				post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));				
			}
			
			return invoke(client, post);
		} finally{
			
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}
	public static String get(String url) throws Exception{
		
		HttpClientBuilder builder = HttpClientBuilder.create();
		CloseableHttpClient client = builder.build();
		
		HttpGet get = null;
		
		try {
			
			get = new HttpGet(url);
			return invoke(client, get);
		} finally{
			
			if(client != null){
				
				try {
					client.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
			}
		}
	}
	public static File get(String url, String dir, String name) throws Exception {
		
		HttpClientBuilder builder = HttpClientBuilder.create();
		CloseableHttpClient client = builder.build();
		
		CloseableHttpResponse response = null;
		HttpGet get = null;
		OutputStream outstream = null;
				
		try {
			
			get = new HttpGet(url);
			response = client.execute(get);
			int code = response.getStatusLine().getStatusCode();
			if(code == 404){
				
				throw new IllegalArgumentException("不存在的请求地址");
			}
			if(code == 500){
				
				throw new Exception("调用服务内部异常");
			}
			File file = new File(StringUtil.addSlash(dir) + name);
			HttpEntity entity = response.getEntity();
			outstream = new FileOutputStream(file);
			entity.writeTo(outstream);
			outstream.flush();
			EntityUtils.consume(entity);
			return file;
		} finally{
			
			try {
				if(outstream != null) {
					
					outstream.close();
				}
			} finally {
				
				try {
					
					if(response != null) {
						
						response.close();
					}
				} finally {
					
					if(client != null) {
						
						client.close();
					}
				}
			}
		}
	}
	private static String invoke(CloseableHttpClient client, HttpUriRequest request) throws Exception {
		
		CloseableHttpResponse response = null;
		String ret = null;
		try {
			
			response = client.execute(request);
			int code = response.getStatusLine().getStatusCode();
			if(code == 404){
				
				throw new IllegalArgumentException("不存在的请求地址");
			}
			if(code == 500){
				
				throw new Exception("调用服务内部异常");
			}
			HttpEntity entity = response.getEntity();
			ret = EntityUtils.toString(entity, "UTF-8");
			EntityUtils.consume(entity);
		} finally{
			
			if(response != null){
				try {
					response.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		return ret;
		
	}
	
	public static String postJson(String url, String json, boolean head) throws Exception{
		
		HttpClientBuilder builder = HttpClientBuilder.create();
		CloseableHttpClient client = builder.build();
		HttpPost post = null;
		try {
			post = new HttpPost(url);  
			
			if(head){
			
				post.addHeader(HTTP.CONTENT_TYPE,"application/json");
			}
			  
			//参数  
			StringEntity se = new StringEntity(json, HTTP.UTF_8);
			se.setContentEncoding("UTF-8"); 
			if(head){
				
				se.setContentType("application/json");//发送json需要设置contentType
			}
			//se.setContentType("application/json");//发送json需要设置contentType  
			post.setEntity(se);
			return invoke(client, post);
			
		} finally{
			
			if(client != null){
				client.close();
			}
		}
	}
	
    public static String postHttpsJson(String url, String json, boolean head) throws Exception{
		
		HttpClientBuilder builder = HttpClientBuilder.create();
		CloseableHttpClient client = HttpClients.custom()
				.setSSLSocketFactory(createSSLConnSocketFactory())
				.setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
		HttpPost post = null;
		try {
			post = new HttpPost(url);  
			
			if(head){
			
				post.addHeader(HTTP.CONTENT_TYPE,"application/json");
			}
			  
			//参数  
			StringEntity se = new StringEntity(json, HTTP.UTF_8);
			se.setContentEncoding("UTF-8"); 
			if(head){
				
				se.setContentType("application/json");//发送json需要设置contentType
			}
			//se.setContentType("application/json");//发送json需要设置contentType  
			post.setEntity(se);
			return invoke(client, post);
			
		} finally{
			
			if(client != null){
				client.close();
			}
		}
	}

    /** 
     * 创建SSL安全连接 
     * 
     * @return 
     */  
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {  
        SSLConnectionSocketFactory sslsf = null;  
        try {  
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {  
  
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {  
                    return true;  
                }  
            }).build();  
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {  
  
  
                @Override  
                public void verify(String host, X509Certificate cert) throws SSLException {  
                }  
  
                @Override  
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {  
                }

				@Override
				public boolean verify(String hostname, SSLSession session) {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public void verify(String arg0, SSLSocket arg1)
						throws IOException {
					// TODO Auto-generated method stub
					
				}  
            });  
        } catch (GeneralSecurityException e) {  
            e.printStackTrace();  
        }  
        return sslsf;  
    }  
}
