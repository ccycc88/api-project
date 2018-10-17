package com.api.project.worker;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.OsInfoUtil;
import com.api.project.util.StringUtil;
import com.api.project.util.helper.SysHelper;
import com.api.project.util.thread.ControllThread;

public class MemMonitor extends ControllThread {

	private Logger log = LoggerFactory.getLogger(MemMonitor.class);

	private boolean isMemProtect = false;

	// 多长时间获取一下内存占用情况
	private long interval = 5 * 1000;
	// 内存占用超过多少达到阈值
	private long threthod = 70;
	private volatile boolean islimit = false;
	private static MemMonitor memtask = new MemMonitor();

	// 控制一次通过的任务总数,每次通过totalnum个任务数，就等待waittime时间
	// 防止内存某一时刻空闲时，通过过多任务，内存溢出。
	private int num = 0;
	private byte[] waitForLimitLock = new byte[1];
	private byte[] waitForTotalnumLock = new byte[1];
	private byte[] waitForNextLoopLock = new byte[1];
	private int totalnum = 3;
	private int waittime = 10 * 1000;

	private MemMonitor(){
	}

	public static MemMonitor getInstance() {
		return memtask;
	}

	// 过一段时间统计一下内存使用情况，对于solaris,linux,windows等jvm有old区的，统计old区占用
	// aix，hp等没有old区的，统计整个内存占用情况
	public void dispose() {
		boolean isGeneration = false;
		if (OsInfoUtil.isSolaris() || OsInfoUtil.isLinux() || OsInfoUtil.isFreeBSD() || OsInfoUtil.isSunOS()
				|| OsInfoUtil.isWindows()) {
			isGeneration = true;
		}
		while (true) {
			doHeartBeat(this.getClass());
			SysHelper.waitIt(waitForNextLoopLock, interval);
			try {
				float useradio = 0;
				if (isGeneration) {
					java.util.List<MemoryPoolMXBean> mpools = ManagementFactory.getMemoryPoolMXBeans();
					for (int i = 0; i < mpools.size(); i++) {
						MemoryPoolMXBean mp = mpools.get(i);
						String poolname = mp.getName();
						if (poolname.toLowerCase().startsWith("tenured") || poolname.toLowerCase().contains("old")) {
							float usedMemory = mp.getUsage().getUsed();
							float totalMemory = mp.getUsage().getMax();
							useradio = (float) ((usedMemory / totalMemory) * 100);
							break;
						}
					}
				} else {
					useradio = (float) ((((float) Runtime.getRuntime().totalMemory()
							- (float) Runtime.getRuntime().freeMemory()) / (float) Runtime.getRuntime().maxMemory())
							* 100);
				}
				if (useradio >= threthod) {
					log.debug("内存使用率超过阈值[" + threthod + "],当前使用率[" + useradio + "]");
					islimit = true;
				} else {
					islimit = false;
					synchronized (waitForLimitLock) {
						waitForLimitLock.notifyAll();
					}
				}
			} catch (Exception e) {
				log.error("获取内存使用情况异常" + StringUtil.createStackTrace(e), e);
			}

		}
	}

	public boolean islimit() {
		return this.islimit;
	}

	// 参数，毫秒
	public void waitForLimitFalse(long waittime) {
		if (!isMemProtect) {
			return;
		}
		if (islimit) {
			log.debug("等待内存回收开始");
			long last = System.currentTimeMillis();
			try {
				synchronized (waitForLimitLock) {
					waitForLimitLock.wait(waittime);
				}
			} catch (InterruptedException e) {
				log.error(StringUtil.createStackTrace(e));
			}
			float time = (float) (System.currentTimeMillis() - last) / (float) 1000;
			log.debug("等待内存回收时间[" + time + "]s");
		}
		this.increaceNum();
	}

	public synchronized void increaceNum() {
		if (num >= totalnum) {
			long last = System.currentTimeMillis();
			SysHelper.waitIt(waitForTotalnumLock, this.waittime);
			float time = (float) (System.currentTimeMillis() - last) / (float) 1000;
			log.debug("连续取走[" + totalnum + "]个任务，等待[" + time + "]s");
			num = 0;
		}
		num++;
	}

	public void waitForLimitFalse() {
		this.waitForLimitFalse(0);
	}
}
