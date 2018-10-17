package com.api.project.util.stream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class MyInputStreamReader {

	protected StringBuffer charCache = new StringBuffer(200);
	protected InputStreamReader reader = null;

	public MyInputStreamReader(InputStreamReader reader) {
		this.reader = reader;
	}

	public InputStreamReader getInputStreamReader() {
		return reader;
	}

	public int read() throws IOException {
		return reader.read();
	}

	public int read(char[] c, int off, int len) throws IOException {
		return reader.read(c, off, len);
	}

	/**
	 * 接收字节直到满足正则条件 方法将读取流中的数据并累加到缓存，且同时检索当前缓存是否满足正则条件数组中的任一元
	 * 素，如满足则返回并删除缓存中的数据，否则将继续读取并检索（易堵塞）直到该流中不存在任何 可读取的字节时（返回-1）将返回缓存中所有数据。
	 * 
	 * matches方法更适用于接收网络字节传输，它将通过用户给出的正则条件保证数据的完整性能 ，适用于telnet、ssh服务中接收数据。
	 * 
	 * 参数ignoreCase表示在检索时是否区分大小写，true/false 区分/不区分，当为 'true'时会消耗更多的资源
	 * 参数bufsize表示默认的缓存大小。
	 */
	public String matches(String expr[], int bufsize, boolean ignoreCase) throws IOException {
		char[] cbuf = new char[bufsize];

		boolean togo = false;
		Pattern p[] = getPatternInstance(expr, ignoreCase);
		for (int num = 0; !togo && (num = read(cbuf, 0, bufsize)) > 0;) {
			charCache.append(cbuf, 0, num);

			for (int i = 0; i < p.length; i++)
				if (p[i].matcher(charCache).find()) {
					togo = true;
					break;
				}
		}

		String msg = charCache.toString();
		charCache.delete(0, charCache.length());
		return msg;
	}

	/**
	 * 通过正则表达式获得正则实例 参数ignoreCase表示在检索时是否区分大小写，true/false 区分/不区分，当为 'true'时会消耗更多的资源
	 */
	protected Pattern[] getPatternInstance(String expr[], boolean ignoreCase) {
		int flag = ignoreCase ? Pattern.CASE_INSENSITIVE : 0;

		Pattern p[] = new Pattern[expr.length];
		for (int i = 0; i < expr.length; i++)
			p[i] = Pattern.compile(expr[i], flag);
		return p;
	}

	/**
	 * 根据关键字截取流中的数据，并分段返回。 方法将读取流中的数据并累加到缓存，且同时检索缓存是否包含关键字数组'endWith'
	 * 中的任一关键字，如包含则返回并删除缓存中此关键字之前的数据（包含此关键字）否则将继
	 * 续读取并检索（易堵塞）直到该流中不存在任何可读取的字节时（返回-1）将返回缓存中所有 数据
	 * 参数ignoreCase表示在检索时是否区分大小写，true/false 区分/不区分，当为 'true'时会消耗更多的资源
	 * 参数bufsize表示默认的缓存大小。
	 */
	public String truncate(String endWith[], int bufsize, boolean ignoreCase) throws IOException {
		int tagIdx = -1;
		char[] cbuf = new char[bufsize];

		for (int num = 0; (num = read(cbuf, 0, bufsize)) > 0;) {
			charCache.append(cbuf, 0, num);
			if ((tagIdx = tagIndex(endWith, ignoreCase)) > 0)
				break;
		}

		if (tagIdx == -1)
			tagIdx = charCache.length();

		String msg = charCache.substring(0, tagIdx);
		charCache.delete(0, tagIdx);
		return msg;
	}

	/**
	 * 检索字符缓冲是否包含特定的关键字 调用该方法需提供关键字数组'endWith’，方法将依次检索缓存是否包含此数组中任一关键字，
	 * 如包含则返回此关键字在缓存中的位置（此位置包含关键字的长度）。如数组中所有的关键字均不
	 * 存在于缓存中则返回'-1'。参数ignoreCase表示在比较时是否区分大小写，true/false 区分/ 不区分。
	 * 注意，参数ignoreCase为'true'会消耗更多资源。
	 */
	protected int tagIndex(String endWith[], boolean ignoreCase) {
		for (int i = 0, idx = -1; i < endWith.length; i++) {
			if (ignoreCase == true)
				idx = charCache.toString().toLowerCase().lastIndexOf(endWith[i].toLowerCase());
			else
				idx = charCache.lastIndexOf(endWith[i]);

			if (idx != -1)
				return idx + endWith[i].length();
		}
		return -1;
	}

	/**
	 * 释放资源 清空缓存并关闭输入流。
	 */
	public void close() {
		if (charCache != null)
			charCache.setLength(0);
		try {
			reader.close();
		} catch (IOException e) {
		}
	}
}
