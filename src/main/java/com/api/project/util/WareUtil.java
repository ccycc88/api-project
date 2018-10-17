package com.api.project.util;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class WareUtil {

	/**
	 * 获取本服务器ip
	 * 
	 * @return
	 */
	public static final String getLoaclIP(String filter) {

		Enumeration<NetworkInterface> netInterfaces = null;
		try {

			netInterfaces = NetworkInterface.getNetworkInterfaces();

			while (netInterfaces.hasMoreElements()) {

				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> address = ni.getInetAddresses();
				while (address.hasMoreElements()) {

					String ip = address.nextElement().getHostAddress();
					if (ip.matches("\\d*\\.\\d*\\.\\d*\\.\\d*")) {

						if (StringUtil.isBlank(filter)) {
							return ip;
						}
						boolean matche = false;
						for (String rex : filter.split(",")) {

							if (ip.startsWith(rex)) {
								matche = true;
							}
						}
						if (!matche) {
							return ip;
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "127.0.0.1";
	}

	/**
	 * 获取主机名称
	 * 
	 * @return
	 */
	public static final String getHostName() {

		Enumeration<NetworkInterface> netInterfaces = null;
		try {

			netInterfaces = NetworkInterface.getNetworkInterfaces();

			while (netInterfaces.hasMoreElements()) {

				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> address = ni.getInetAddresses();
				while (address.hasMoreElements()) {

					return address.nextElement().getHostName();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "hostName";
	}
	public String getIp(HttpServletRequest request) {

		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static void main(String[] args) {
		System.out.println(getHostName());
	}
}
