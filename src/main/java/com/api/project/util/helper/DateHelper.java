package com.api.project.util.helper;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

public class DateHelper {

	private static final long serialVersionUID = 1L;
	final static public long SECOND = 1000; // 一秒的毫秒数
	final static public long MINUTE = SECOND * 60; // 一分钟的毫秒数
	final static public long HOUR = MINUTE * 60; // 一小时的毫秒数
	final static public long DAY = HOUR * 24; // 一天的毫秒数
	final static public long MONTH = DAY * 30; // 30天的毫秒数
	final static public long YEAR = MONTH * 12; // 12*30天的毫秒数

	private SplDateFormatAgent sdfa = new SplDateFormatAgent();
	static protected Calendar calendar = Calendar.getInstance();
	static Calendar tempMonthUse = Calendar.getInstance();

	/**
	 * 参数'l'单位必须是毫秒， 方法能够将该数自动进位，并以'200ms'、'10s'、'15h'...的形式输出。 时间单位：ms s m H d M y
	 */
	public synchronized String str2time(long l) {
		if (l > YEAR)
			return l / YEAR + "y";
		else if (l > MONTH)
			return l / MONTH + "M";
		else if (l > DAY)
			return l / DAY + "d";
		else if (l > HOUR)
			return l / HOUR + "H";
		else if (l > MINUTE)
			return l / MINUTE + "m";
		else if (l > SECOND)
			return l / SECOND + "s";
		return l + "ms";
	}

	/**
	 * 移动某个指定时间的任意时间元素 通过此方法能够在指定时间戳‘startWith’的基础上，随意移动年/月/日/小时/分钟
	 * ，并以long类型返回最终移动的结果。
	 * 
	 * 例如：指定时间“1070467200000” 就是“2003/12/04” randomeDate(1070467200000, -25,
	 * DateHelper.MONTH); // 2001/11/04
	 * 
	 * randomeDate(1070467200000, 34, DateHelper.DAY); //2004/01/29
	 *
	 * 注意：小时是12制，而非24
	 */
	public synchronized long randomDate(long startWith, double cursor, long dateType) {
		calendar = Calendar.getInstance();

		if (dateType == SECOND)
			startWith = startWith + (long) (cursor * SECOND);
		else if (dateType == MINUTE)
			startWith = startWith + (long) (cursor * MINUTE);
		else if (dateType == HOUR)
			startWith = startWith + (long) (cursor * HOUR);
		else if (dateType == DAY)
			startWith = startWith + (long) (cursor * DAY);

		// 对年或月进行特殊处理
		else {
			calendar.setTimeInMillis(startWith);
			if (dateType == MONTH) {
				calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + (int) cursor);
				return calendar.getTimeInMillis();
			} else if (dateType == YEAR) {
				calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + (int) cursor);
				return calendar.getTimeInMillis();
			} else {
				return startWith;
			}
		}

		// 这里针对秒、分钟、小时和天进行处理，只为支持浮点
		calendar.setTimeInMillis(startWith);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		calendar.set(year, month, day, hour, minute, second);
		long ltime = calendar.getTimeInMillis();
		return ltime;
	}

	public synchronized String randomDate(long startWith, int cursor, long dateType, String dateFormat) {
		long freewill = randomDate(startWith, cursor, dateType);
		return sdfa.format(dateFormat, new Date(freewill));
	}

	/**
	 * 生成Date类实例
	 * 
	 * 例如： toDate(2007, -2, 15, 1, 1); // 2006/10/15 1:1
	 *
	 * 注意：小时是12制，而非24
	 */
	public synchronized long toDate(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
		if (calendar == null)
			calendar = Calendar.getInstance();

		calendar.set(year, month - 1, dayOfMonth, hourOfDay, minute);
		return calendar.getTimeInMillis();
	}

	public synchronized long toDate(int year, int month, int dayOfMonth) {
		if (calendar == null)
			calendar = Calendar.getInstance();

		calendar.set(year, month - 1, dayOfMonth);
		return calendar.getTime().getTime();
	}

	/**
	 * 生成Date类实例
	 * 
	 * 将使用已知的时间格式‘format’解析字符串时间‘strTime’，如成功则返回Date类 实例，否则报出ParseException异常。
	 * 
	 * 例如： Date date = DateHelper.toDate("02/10/15", "yy/MM/dd");
	 */
	public synchronized Date toDate(String strTime, String format) throws ParseException {
		return sdfa.parse(format, strTime);
	}

	public synchronized String dateFormat(long timestamp, String formatStr) {
		return sdfa.format(formatStr, new Date(timestamp));
	}

	public synchronized String dateFormat(long timestamp, Locale locale, String formatStr) {
		return sdfa.format(formatStr, locale, new Date(timestamp));
	}

	/**
	 * 生成Date类实例
	 * 
	 * 此方法能够解析多种标准的时间格式，并以Date类实例形式返回结果。如果所提供的时
	 * 间格式未在方法内定义（不能解析）则会报出ParseException异常。
	 * 
	 * 例如： Date date = DateHelper.toDate("10 Sep 2006 16:00:00 GMT"); Date date =
	 * DateHelper.toDate("Wednesday, 20 September 2006 00:00 GMT");
	 */
	public synchronized Date toDate(String strTime) throws ParseException {
		if (strTime == null)
			return null;

		StringBuffer strb = new StringBuffer();
		for (int i = 0; i < strTime.length(); i++) {
			if (strTime.charAt(i) != '?')
				strb.append(strTime.charAt(i));
			else
				strb.append(" ");
		}
		if (strTime.indexOf("UTC") > 0) {
			strb = new StringBuffer(strb.substring(0, strb.indexOf("UTC")));
			if (strb.charAt(strb.length() - 1) == ' ')
				strb = new StringBuffer(strb.substring(0, strb.length() - 1));
		}
		if (strTime.indexOf("EDT") > 0) {
			strb = new StringBuffer(strb.substring(0, strb.indexOf("EDT")));
			if (strb.charAt(strb.length() - 1) == ' ')
				strb = new StringBuffer(strb.substring(0, strb.length() - 1));
		}
		if (strTime.indexOf("T") > 0 && strTime.indexOf("Z") > 0)
			strb = new StringBuffer(strb.toString().replace('T', ' ').replace('Z', ' '));
		if (strTime.indexOf("CDT") > 0) {
			strb = new StringBuffer(strb.substring(0, strb.indexOf("CDT")));
			if (strb.charAt(strb.length() - 1) == ' ')
				strb = new StringBuffer(strb.substring(0, strb.length() - 1));
		}
		if (strTime.indexOf("GMT") > 0) {
			strb = new StringBuffer(strb.substring(0, strb.indexOf("GMT")));
			if (strb.charAt(strb.length() - 1) == ' ')
				strb = new StringBuffer(strb.substring(0, strb.length() - 1));
		}
		if (strTime.indexOf("CST") > 0) {
			strb = new StringBuffer(strb.substring(0, strb.indexOf("CST")));
			if (strb.charAt(strb.length() - 1) == ' ')
				strb = new StringBuffer(strb.substring(0, strb.length() - 1));
		}
		if (strTime.indexOf("PDT") > 0) {
			strb = new StringBuffer(strb.substring(0, strb.indexOf("PDT")));
			if (strb.charAt(strb.length() - 1) == ' ')
				strb = new StringBuffer(strb.substring(0, strb.length() - 1));
		}

		/** 通过正则表达式，来解析目前已知的所有时间格式。 */
		String dateformat = "EEE MMM dd HH:mm:ss z yyyy";
		if (strTime.matches("\\d{14}"))
			dateformat = "yyyyMMddHHmmss";
		else if (strTime.matches("\\d{17}"))
			dateformat = "yyyyMMddHHmmsssss";
		else if (strTime.matches("\\d{8}"))
			dateformat = "yyyyMMdd";
		else if (strTime.matches("\\d{1,4}\\-[a-zA-Z]{1,}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}"))
			dateformat = "yyyy-MMM-dd HH:mm:ss";
		else if (strTime.matches("\\d{1,4} [a-zA-Z]{1,} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} [a-zA-Z]{1,}"))
			dateformat = "dd MMM yyyy HH:mm:ss"; // 10 Sep 2006 16:00:00 GMT
		else if (strTime.matches("[a-zA-Z]{1,},\\d{2} \\d{2} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}"))
			dateformat = "EEE,dd MM yyyy hh:mm:ss"; // Sat,23 09 2006 13:19:52
		else if (strTime.matches("[a-zA-Z]{3}, \\d{2} [a-zA-Z]{3} \\d{4} \\d{1,2}:\\d{1,2}"))
			dateformat = "EEE, dd MMM yyyy hh:mm"; // Sat, 23 Sep 2006 01:41
		else if (strTime.matches("\\d{1},\\d{1,2} [a-zA-Z]{3} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			dateformat = "dd MMM yyyy hh:mm:ss"; // 0,17 Sep 2006 22:30:16
			strb = new StringBuffer(strTime.substring(strTime.indexOf(",") + 1, strTime.length()));
		} else if (strTime.matches("\\d{2}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}"))
			dateformat = "dd/MM/yy hh:mm:ss"; // 01/01/01 16:10:01
		else if (strTime.matches("\\d{1,2}/\\d{1,2}/\\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} [a-zA-Z]{2}"))
			dateformat = "MM/dd/yyyy hh:mm:ss a"; // 11/3/2002 10:02:13 PM
		else if (strTime.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2} [a-zA-Z]{3}"))
			dateformat = "yyyy-MM-dd hh:mm:ss"; // 2006-9-13 15:17:25 GMT
		else if (strTime.matches("\\d{4}-\\d{1,2}-\\d{1,2}[a-zA-Z]{1}\\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			dateformat = "yyyy-MM-dd hh:mm:ss"; // 2006-09-16T22:16:00
			strb = new StringBuffer(strTime.replace("T", " "));
		} else if (strTime.matches("[a-zA-Z]{3,}, \\d{2} [a-zA-Z]{3,} \\d{4} \\d{1,2}:\\d{1,2} [a-zA-Z]{3}"))
			dateformat = "EEE, dd MMM yyyy hh:mm"; // Wednesday, 20 September 2006 00:00 GMT
		else if (strTime.matches("\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}-\\d{1,2}:\\d{1,2}")) {
			dateformat = "yyyy-MM-dd hh:mm:ss"; // 2005-02-27T08:36:00-05:00
			strb = new StringBuffer(strTime.replace("T", " ").substring(0, strTime.lastIndexOf("-")));
		} else if (strTime.matches("\\d{4}--\\d{1,2}-\\d{1,2}-T\\d{1,2}: \\d{1,2}:\\d{1,2}:-\\d{1,2}:\\d{1,2}")) {
			dateformat = "yyyy--MM-dd-hh:mm:ss";
			strb = new StringBuffer(strTime.replace("T", " ").replace(" ", "").substring(0, strTime.lastIndexOf("-")));
		} else if (strTime.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2}"))
			dateformat = "yyyy-MM-dd hh:mm:ss";
		else if (strTime.matches("[a-zA-Z]{3} \\w{4} \\d{4} \\d{1,2}:\\d{1,2}[a-zA-Z]{2}")) {
			dateformat = "MMM dd yyyy hh:mma"; // Jul 31st 2006 10:08PM
			strb = new StringBuffer(strTime.replace("st", "").replace("nd", "").replace("th", ""));
		} else if (strTime.matches("[a-zA-Z]{1,2}, \\d{1,2} [a-zA-Z]{3} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} -\\d{4}")) {
			strb = new StringBuffer(strTime.substring(strTime.indexOf(",") + 2, strTime.length()));
			dateformat = "dd MMM yyyy HH:mm:ss Z";
		} else if (strTime
				.matches("[a-zA-Z]{3,}, \\d{1,2} [a-zA-Z]{3,} \\d{4}, \\d{1,2}:\\d{1,2}:\\d{1,2} [a-zA-Z]{2}ST"))
			dateformat = "EEE, dd MMM yyyy, HH:mm:ss Z"; // Saturday, 23 September 2006, 11:32:29 SAST
		else if (strTime.matches("\\d{4}-\\d{1,2}-\\d{1,2} [^\\x00-\\xff]{1,} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			dateformat = "yyyy-MM-dd  HH:mm:ss"; // 2006-9-22 锟斤拷锟斤拷 08:40:18
			strb = new StringBuffer(strTime.replaceAll("[^\\x00-\\xff]{1,}", ""));
		} else if (strTime.matches("[a-zA-Z]{3}, \\d{1,2} [a-zA-Z]{3} \\d{4} \\d{1,2}:\\d{1,2} [a-zA-Z]{3,}"))
			dateformat = "EEE, dd MMM yyyy HH:mm"; // Thu, 7 Sep 2006 22:23 GMT
		else if (strTime.matches("\\d{4}/\\d{1,2}/\\d{1,2} \\d{1,2}:\\d{1,2}"))
			dateformat = "yyyy/MM/dd hh:mm"; // 2006/09/23 21:54
		else if (strTime.matches("[a-zA-Z]{3}, \\d{1,2} [a-zA-Z]{3} \\d{4}"))
			dateformat = "EEE, dd MMM yyyy"; // Tue, 19 Sep 2006
		else if (strTime.matches("\\d{4}[^\\x00-\\xff]{1,}\\d{1,2}[^\\x00-\\xff]{1,}\\d{1,2}[^\\x00-\\xff]{1,}")) {
			strb = new StringBuffer(strTime.replaceAll("[^\\x00-\\xff]{1,}", "-"));
			dateformat = "yyyy-MM-dd";
		} else if (strTime.matches("[a-zA-Z]{1,}, \\d{1,2} [a-zA-Z]{1,} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			dateformat = "dd MMM yyyy hh:mm:ss"; // Sa, 23 Sep 2006 13:49:25
			if (strTime.indexOf(", ") > 0)
				strb = new StringBuffer(strTime.substring(strTime.indexOf(", ") + 2, strTime.length()));
		} else if (strTime
				.matches("[a-zA-Z]{1,} [a-zA-Z]{1,} \\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2} [a-zA-Z ]{1,} \\d{4}")) {
			if (strTime.matches("[a-zA-Z]{1,} [a-zA-Z]{1,} \\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2} [a-zA-Z]{1,}  \\d{4}")) {
				dateformat = "EEE MMM dd hh:mm:ss yyyy"; // Sat Sep 23 15:29:45 CD 2006
				strb = new StringBuffer(strTime.replaceAll("[a-zA-Z]{1,}  ", " "));
			} else
				dateformat = "EEE MMM dd hh:mm:ss 'U C' yyyy"; // Wed Sep 14 13:14:23 U C 2005
		} else if (strTime.matches("[a-zA-Z]{1,} \\d{1,2}, \\d{4}, \\d{1,2}:\\d{1,2}[a-zA-Z]{2}"))
			dateformat = "MMM dd, yyyy, hh:mma"; // September 20, 2006, 11:56AM
		else if (strTime.matches("[a-zA-Z]{1,}, \\d{1,2} [a-zA-Z]{1,} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}")) {
			dateformat = "dd MMM yyyy hh:mm:ss"; // We, 26 Jul 2006 00:55:30
			strb = new StringBuffer(strTime.replace("[a-zA-Z]{1,}, ", ""));
		} else if (strTime.matches("\\d{4}[^\\x00-\\xff]{1,}"))
			dateformat = "yyyy";
		else if (strTime.matches("[a-zA-Z]{1,} [a-zA-Z]{1,} \\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2} \\d{4}"))
			dateformat = "EEE MMM dd hh:mm:ss yyyy"; // Sat Sep 23 09:51:06 2006
		else if (strTime.matches("[a-zA-Z]{1,}, \\d{1,2}, [a-zA-Z]{1,} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} -\\d{1,}")) {
			dateformat = "EEE, dd, MMM yyyy hh:mm:ss z"; // Fri, 22, Sept 2006 11:54:00 -0700
			if (strTime.split("[a-zA-Z]{4} ").length > 1)
				strb = new StringBuffer(strTime.replaceAll("[a-zA-Z] ", " "));
		} else if (strTime.matches("\\d{1,2} [a-zA-Z]{3} \\d{4}"))
			dateformat = "dd MMM yyyy";
		else if (strTime.matches("\\d{1,2}/\\d{1,2}/\\d{4}"))
			dateformat = "dd/MM/yyyy";
		else if (strTime.matches("[a-zA-Z]{3,} \\d{1,2} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}."))
			dateformat = "MMM dd yyyy hh:mm:ss."; // October 12 2005 16:01:49.
		else if (strTime.matches("[a-zA-Z]{3,} \\d{1,2} \\d{4}"))
			dateformat = "MMM dd yyyy";
		else if (strTime.matches("[a-zA-Z]{3,}, [a-zA-Z]{3,} \\d{1,2} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2} -\\d{1,}"))
			dateformat = "EEE, MMM dd yyyy hh:mm:ss z"; // Thu, September 7 2006 12:40:02 -0700
		else if (strTime.matches("\\d{1,2} [a-zA-Z]{3} \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}"))
			dateformat = "dd MMM yyyy hh:mm:ss"; // 28 Aug 2004 19:53:19
		else if (strTime.matches("[a-zA-Z]{1,} [a-zA-Z]{1,} \\d{1,2}, \\d{4} \\d{1,2}:\\d{1,2} [a-zA-Z]{2}"))
			dateformat = "EEE MMM dd, yyyy hh:mm a"; // Sat Sep 09, 2006 3:39 pm
		else if (strTime.matches("[a-zA-Z]{1,} [a-zA-Z]{1,} \\d{1,2} \\d{1,2}:\\d{1,2}:\\d{1,2} [a-zA-Z]{2}  \\d{4}")) {
			dateformat = "EEE MMM dd hh:mm:ss yyyy"; // Sat Dec 03 12:54:26 CS 2005
			strb = new StringBuffer(strTime.replaceAll("[a-zA-Z]{2,}  ", " "));
		} else if (strTime.matches("[a-zA-Z]{1,} \\d{1,2}, \\d{4} \\d{1,2}:\\d{1,2}:\\d{1,2}"))
			dateformat = "MMM dd, yyyy hh:mm:ss"; // September 21, 2006 12:00:00
		/** 这里将试图使用上面解析出的格式来解析具体时间，如果失败则报出ParseException */
		return sdfa.parse(dateformat, strb.toString());
	}

	/**
	 * 获取传入时间所在的整周期点时间
	 * 
	 * @param time
	 *            传入时间
	 * @param interval
	 *            周期（要求类似5分钟、10分钟、1小时这样的整分时间）
	 * @example 12：07的整5分钟时间为12：05
	 */
	public synchronized long getIntervalPoint(long time, long interval) {
		time = time + 1000 * 60 * 60 * 8;
		time = time - time % interval - 1000 * 60 * 60 * 8;
		return time;
	}

	private class SplDateFormatAgent implements Serializable {
		private static final long serialVersionUID = 1L;
		private HashMap<String, SimpleDateFormat> map = new HashMap<>();

		private SimpleDateFormat tryCrt(String dateformat) {
			if (map.containsKey(dateformat) == false)
				map.put(dateformat, new SimpleDateFormat(dateformat));
			return map.get(dateformat);
		}

		private SimpleDateFormat tryCrt(String dateformat, Locale locale) {
			String key = dateformat + locale.toString();
			if (map.containsKey(key) == false)
				map.put(key, new SimpleDateFormat(dateformat, locale));
			return map.get(key);
		}

		public synchronized String format(String dateformat, Date date) {
			return tryCrt(dateformat).format(date);
		}

		public synchronized String format(String dateformat, Locale locale, Date date) {
			return tryCrt(dateformat, locale).format(date);
		}

		public synchronized Date parse(String dateformat, String date) throws ParseException {
			return tryCrt(dateformat).parse(date);
		}

		public synchronized Date parse(String dateformat, Locale locale, String date) throws ParseException {
			return tryCrt(dateformat, locale).parse(date);
		}

		public int size() {
			return map.size();
		}
	}

	public String dateExpr2Content(String str) {
		long nowTime = System.currentTimeMillis();
		return dateExpr(str, nowTime);
	}

	/**
	 * @param str
	 *            表达式，如/opt/ulfiles/pull_$yyyy$_$MM$_$dd$
	 * @param delay
	 *            时延，精确到毫秒，如86400000，表示1天
	 * 
	 *            将字符中的‘$yyyyMMdd$’转换为实际的时间并减去时延。允许存在多组时间标识。
	 *            例如：/opt/ulfiles/pull_$yyyy$_$MM$_$dd$、86400000
	 *            转换为：/opt/ulfiles/pull_2007_05_04 -1 ->/opt/ulfiles/pull_2007_05_03
	 *            delay为毫秒级别参数
	 */
	public String dateExpr2Content(String str, long delay) {
		long nowTime = System.currentTimeMillis() - delay;

		return dateExpr(str, nowTime);
	}

	public String dateExpr(String str, long nowTime) {
		String tag = "$";
		StringBuffer resultBuf = new StringBuffer();
		int strlen = str.length();
		int endCursor = strlen;

		boolean isfirst = true;
		int idx = 0;

		while (true) {
			// 获取表达式头位置
			idx = str.lastIndexOf(tag, endCursor);
			if (idx == -1) {
				if (endCursor != -1)
					resultBuf.insert(0, str.substring(0, (endCursor >= strlen ? endCursor : endCursor + 1)));
				break;
			}

			// 获取表达式尾位置
			int strWith = str.lastIndexOf(tag, idx - 1);
			if (strWith == -1) {
				resultBuf.insert(0, str.substring(0, endCursor - 1));
				break;
			}

			// 加入最后一个表达式后面的正文
			if (isfirst == true) {
				resultBuf.append(str.substring(idx + 1));
				isfirst = false;
			}
			// 加入两个表达式之间的正文
			else
				resultBuf.insert(0, str.substring(idx + 1, endCursor + 1));

			endCursor = strWith - 1;
			String expr = str.substring(strWith + 1, idx);
			String keys[] = expr.split("-");
			if (keys.length <= 0)
				resultBuf.insert(0, expr);
			else if (keys.length <= 1) {// 需要格式转换，但不需要计算
				if (keys[0].toLowerCase().startsWith("max(") == true) {
					keys[0] = keys[0].substring(4, keys[0].length() - 1);
					resultBuf.insert(0, "MAX".concat(keys[0]).concat("$"));
				} else {
					if ("m*".equals(keys[0])) {
						String value = randomDate(nowTime, 0, DAY, "mm");
						resultBuf.insert(0, value.substring(0, 1) + "*");
					} else {
						resultBuf.insert(0, randomDate(nowTime, 0, DAY, keys[0]));
					}
				}
			} else {
				if (keys[0].startsWith("Y") || keys[0].startsWith("y"))
					nowTime = randomDate(nowTime, 0 - Double.parseDouble(keys[1]), YEAR);
				else if (keys[0].startsWith("M"))
					nowTime = randomDate(nowTime, 0 - Double.parseDouble(keys[1]), MONTH);
				else if (keys[0].startsWith("d")) {
					nowTime = randomDate(nowTime, 0 - Double.parseDouble(keys[1]), DAY);
				} else if (keys[0].startsWith("H") || keys[0].startsWith("h"))
					nowTime = randomDate(nowTime, 0 - Double.parseDouble(keys[1]), HOUR);
				else if (keys[0].startsWith("m"))
					nowTime = randomDate(nowTime, 0 - Double.parseDouble(keys[1]), MINUTE);
				if ("m*".equals(keys[0])) {
					String value = dateFormat(nowTime, "mm");
					resultBuf.insert(0, value.substring(0, 1) + "*");
				} else {
					resultBuf.insert(0, dateFormat(nowTime, keys[0]));
				}
			}
		}

		// 实现MAX时间函数计算
		int maxIdx = 0, day = 0;
		while ((maxIdx = resultBuf.indexOf("MAX", maxIdx)) != -1) {
			int endIdx = resultBuf.indexOf("$", maxIdx + 3);
			String expr = resultBuf.substring(maxIdx + 3, endIdx);

			if (expr.toLowerCase().startsWith("d") == true) { // 求某月最大的天
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(nowTime);
				day = c.getActualMaximum(Calendar.DAY_OF_MONTH);

				resultBuf.delete(maxIdx, endIdx + 1);
				resultBuf.insert(maxIdx, day);
			}
			maxIdx = maxIdx + 3;
		}

		return resultBuf.toString();
	}

	// 取策略下发偏移量
	// 0 2 * * * ? 即取2
	public static int getDiff(String orderPeriod, int collectPeriod) {
		if (orderPeriod == null || orderPeriod.trim().length() == 0) {
			return 0;
		}

		String[] ar = orderPeriod.split("\\s+");
		if (ar == null || ar.length < 2) {
			return 0;
		}

		try {
			String d = ar[3];
			String h = ar[2];
			String m = ar[1];
			d = d.split("[,/]")[0];
			h = h.split("[,/]")[0];
			m = m.split("[,/]")[0];

			if (collectPeriod <= 60 * 60) {
				int mi = Integer.parseInt(m);
				return mi * 60;
			} else if (collectPeriod <= 24 * 60 * 60) {
				int hour = Integer.parseInt(h);
				int mi = Integer.parseInt(m);
				return hour * 60 * 60 + mi * 60;
			} else {
				int day = 0;
				if (!"?".equals(d.trim())) {
					day = Integer.parseInt(d);
				}

				int hour = Integer.parseInt(h);
				int mi = Integer.parseInt(m);
				return day * 24 * 60 * 60 + hour * 60 * 60 + mi * 60;
			}
		} catch (Exception e) {
			// log.error("处理字符串["+orderPeriod+"]时产生异常！collectPeriod=" +collectPeriod, e);
		}
		return 0;
	}

	/**
	 * 
	 * @param fireTime
	 *            支行时间
	 * @param collectPeriod
	 *            采集周期
	 * @param delay延迟时间
	 * @return 采集的开始和结束时间
	 */
	public static long[] getScanScope(Date fireTime, long collectPeriod, long delay) {
		Calendar fire = Calendar.getInstance();
		long start = 0L;
		long end = 0L;
		fire.setTime(fireTime);
		fire.set(Calendar.SECOND, 0);
		fire.set(Calendar.MILLISECOND, 0);// 取当前执行时间的整天为采集结束时间

		// 找结束时间
		if (collectPeriod < 3600) {// 粒度到分钟
			long minite = fire.get(Calendar.MINUTE);
			long collectMinite = (int) collectPeriod / 60;
			long s = minite % collectMinite; // 当前执行分钟数与采集粒度分钟数求余
			end = fire.getTimeInMillis() - s * 60 * 1000; // 当前执行时间减去余数时间,就是采集结束时间
			start = end - collectPeriod * 1000;// 采集结束时间减去采集周期为采集开始时间
		} else if (collectPeriod == 3600) { // 小时粒度
			fire.set(Calendar.MINUTE, 0);
			end = fire.getTimeInMillis();// 取当前执行时间的整小时为采集结束时间
			start = end - collectPeriod * 1000;// 采集结束时间减去采集周期为采集开始时间
		} else if (collectPeriod == 24 * 60 * 60) { // 一天粒度
			fire.set(Calendar.HOUR_OF_DAY, 0);
			fire.set(Calendar.MINUTE, 0);
			end = fire.getTimeInMillis();
			start = end - collectPeriod * 1000;// 采集结束时间减去采集周期为采集开始时间
		} else if (collectPeriod == 7 * 24 * 60 * 60) {
			int today = fire.get(Calendar.DAY_OF_WEEK);
			fire.set(Calendar.DAY_OF_WEEK, 2);
			fire.set(Calendar.HOUR_OF_DAY, 0);
			fire.set(Calendar.MINUTE, 0);
			end = fire.getTimeInMillis();
			if (today == 1) {
				end = end - collectPeriod * 1000; // 如果今天是周日的话，需要往前再退一周的时间
			}
			start = end - collectPeriod * 1000;// 采集结束时间减去采集周期为采集开始时间
		} else if (collectPeriod == 30 * 24 * 60 * 60) {// 月粒度
			String e = fire.get(Calendar.YEAR) + "-" + (fire.get(Calendar.MONTH) + 1) + "-01 00:00:00 000";
			fire.add(Calendar.MONTH, -1);
			String s = fire.get(Calendar.YEAR) + "-" + (fire.get(Calendar.MONTH) + 1) + "-01 00:00:00 000";
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
			try {
				start = format.parse(s).getTime();
				end = format.parse(e).getTime();
			} catch (ParseException e1) {
				e1.printStackTrace();
				start = 0;
				end = 0;
			}
		} else { // 其它的
					// 此操作可以包含上面情况,但如果采集周期为25分钟等,不是60的约数的时候,最终结果会不一样
			if (collectPeriod > 0) {
				end = fire.getTimeInMillis() - (fire.getTimeInMillis() + 8 * 60 * 60 * 1000) % (collectPeriod * 1000);
			} else {
				return null;
			}
			start = end - collectPeriod * 1000;// 采集结束时间减去采集周期为采集开始时间
		}

		if (delay > 0) { // 如果延迟,再减去延迟时间
			start -= delay * 1000;
			end -= delay * 1000;
		}

		return new long[] { start, end };
	}

	/**
	 * 工具方法，将Date类型时间转换成数据库对应的字符串格式
	 * 
	 * @param dateStr
	 *            时间字符串
	 * @param dateFormatStr
	 *            - 时间串格式
	 * @return
	 */
	public static Date stringToDate(String dateStr, String dateFormatStr) {
		if (dateStr == null || dateStr.trim().length() == 0 || dateFormatStr == null
				|| dateFormatStr.trim().length() == 0) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormatStr);
		return sdf.parse(dateStr, new ParsePosition(0));

	}

	public static Date stringToDate(String dateStr) {
		return stringToDate(dateStr, "yyyy-MM-dd HH:mm:ss");

	}

	/**
	 * 
	 * 将时间格式为"小时:分钟"的字符串，填充完整，并转换成时间类型
	 * 
	 */
	public static Date fillTime(String hourMinute) {
		Calendar cal = Calendar.getInstance();
		int months = cal.get(Calendar.MONTH) + 1;
		String month = "";
		if (months < 10) {
			month = "0" + months;
		} else {
			month = months + "";
		}
		return stringToDate(
				cal.get(Calendar.YEAR) + "" + month + "" + cal.get(Calendar.DAY_OF_MONTH) + " " + hourMinute,
				"yyyyMMdd HH:mm");
	}

	/**
	 * 工具方法，将Date类型时间转换成数据库对应的字符串格式
	 * 
	 * @param date
	 *            - java的Date类型时间
	 * @param dateFormatStr
	 *            - 时间串格式
	 */
	public static String dateToString(Date date, String dateFormatStr) {
		if (date == null || dateFormatStr == null || dateFormatStr.trim().length() == 0) {
			return null;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormatStr);
		return sdf.format(date);

	}

	public static String dateToString(Date date) {
		return dateToString(date, "yyyy-MM-dd HH:mm:ss");

	}

	/**
	 * 
	 * 判断传输返回数据周期的粒度(秒、分钟、小时、天)
	 * 
	 * @param TODO
	 *            填写参数
	 * @return TODO 填写返回值
	 * @exception TODO
	 *                填写异常对象
	 * @since GCP6.0
	 * @see TODO 填写参见
	 */
	public static char getGranularity(int interval) {
		if (interval < 60) {
			// 秒
			return 's';
		} else if (interval >= 60 && interval < 60 * 60) {
			// 分钟
			return 'm';
		} else if (interval >= 60 * 60 && interval < 60 * 60 * 24) {
			// 小时
			return 'h';
		} else if (interval >= 60 * 60 * 24) {
			// 天
			return 'd';
		}
		return ' ';
	}

	/**
	 * 
	 * 修改时间字段内容的
	 * 
	 * @param TODO
	 *            填写参数
	 * @return TODO 填写返回值
	 * @exception TODO
	 *                填写异常对象
	 * @since GCP6.0
	 * @see TODO 填写参见
	 */
	public static Date inreaseTime(Date time, int zone, int value) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(time);
		cal.add(zone, value);
		return cal.getTime();
	}

	public static Date inreaseTime(String time, int zone, int value) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(stringToDate(time));
		cal.add(zone, value);
		return cal.getTime();
	}

	/**
	 * 
	 * 整理时间，将秒、毫秒位忽略清零
	 * 
	 * @param TODO
	 *            填写参数
	 * @return TODO 填写返回值
	 * @exception TODO
	 *                填写异常对象
	 * @since GCP6.0
	 * @see TODO 填写参见
	 */
	public static Date formateDate(Date time) {
		if (time != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(time);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			return cal.getTime();
		} else {
			return time;
		}
	}

	public static Timestamp getTimestamp(Date date) {
		if (date != null) {
			Timestamp time = new Timestamp(date.getTime());
			return time;
		}
		return null;
	}

	public static Date get3TimesBeforeDate(Date scanStartTime, Date scanStopTime) {
		long offset = scanStopTime.getTime() - scanStartTime.getTime();

		Date threeTimesBefore = new Date(scanStartTime.getTime() - offset * 3);
		return threeTimesBefore;
	}

	/**
	 * 返回指定日期的disday日期
	 * 
	 * @param strDateLimit
	 * @param formatStr
	 * @param disday
	 * @return
	 */
	public static String getDateString(String strDateLimit, String formatStr, int disday) {
		if (strDateLimit == null || strDateLimit.equals("") || formatStr == null || formatStr.equals("")) {
			return "";
		}
		Calendar c = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr);
		try {
			Date thisdate = dateFormat.parse(strDateLimit);
			c.setTime(thisdate);
		} catch (Exception e) {
		}
		c.set(Calendar.DATE, c.get(Calendar.DATE) + disday);
		String ls_display = dateFormat.format(c.getTime()).toString();
		return ls_display;
	}

	/**
	 * 取当前时间
	 * 
	 * @param formatStr
	 * @return
	 */
	public static String getCurrentDateString(String formatStr) {
		if (formatStr == null || formatStr.equals("")) {
			return "";
		}
		Date currentDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatStr);
		String date = dateFormat.format(currentDate);
		return date;
	}

	/**
	 * 解析补采调度时间串，计算自动补采执行时间点
	 */
	public static LinkedList<Date> parseRecollDateList(Date startTime, String delayList) {
		LinkedList<Date> dateLink = new LinkedList<Date>();
		if (delayList != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startTime);
			String[] delayArray = delayList.split(",");
			for (String delay : delayArray) {
				Float delayTime = Float.parseFloat(delay) * 60;
				cal.add(Calendar.MINUTE, delayTime.intValue());
				dateLink.add(cal.getTime());
				cal = Calendar.getInstance();
				cal.setTime(startTime);
			}
		}
		return dateLink;
	}

	/**
	 * 将队头的时间获取出来，再计算新时间放入队尾
	 */
	public static Date pollTimeAndAddNew(LinkedList<Date> timeQueue) {

		Date time = timeQueue.poll();
		if (time != null) {
			timeQueue.add(inreaseTime(time, Calendar.HOUR_OF_DAY, 24));
		}
		return time;
	}

	/**
	 * 获取指定日期格式当前日期的字符型日期
	 * 
	 * @param p_format
	 *            日期格式 格式1:"yyyy-MM-dd" 格式2:"yyyy-MM-dd hh:mm:ss EE"
	 *            格式3:"yyyy年MM月dd日 hh:mm:ss EE" 说明: 年-月-日 时:分:秒 星期 注意MM/mm大小写
	 * @return String 当前时间字符串
	 */
	public static String getNowOfDateByFormat(String p_format) {
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(p_format);
		String dateStr = sdf.format(d);
		return dateStr;
	}

	/*
	 * 以下方法分别返回一个格式化的时间字符串
	 * 
	 */
	public static String getDateStr() {
		Date date = new Date();
		SimpleDateFormat time = new SimpleDateFormat("yyyyMMddHH");
		return time.format(date);
	}

	public static String getDateStrAll() {
		Date date = new Date();
		SimpleDateFormat timeall = new SimpleDateFormat("yyyyMMddHHmmss");
		return timeall.format(date);
	}

	public static String getFormat() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		String st = df.format(new Date());
		return st;
	}
}
