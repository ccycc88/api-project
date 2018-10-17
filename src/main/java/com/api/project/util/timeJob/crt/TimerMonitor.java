package com.api.project.util.timeJob.crt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.project.util.StringUtil;
import com.api.project.util.helper.SysHelper;
import com.api.project.util.thread.ControllThread;
import com.api.project.util.timeJob.Job;
import com.api.project.util.timeJob.JobGroup;

public class TimerMonitor extends ControllThread{

	private Logger log = LoggerFactory.getLogger(TimerMonitor.class);

	// 待执行任务列表
	private List<Job> wait2RunJobList = null;
	// 工人线程列表
	private List<TimerWorker> workerList = null;
	// 工人线程列表
	private Map<String, JobGroup> JobGroupMap = null;

	final static int TIMEOUT = 1000 * 60 * 30; // 60 minute;

	public TimerMonitor(List<Job> wait2RunJobList,
			List<TimerWorker> workerList, Map<String, JobGroup> JobGroupMap) {
		this.wait2RunJobList = wait2RunJobList;
		this.workerList = workerList;
		this.JobGroupMap = JobGroupMap;
	}

	public void dispose() {
		while (true) {
			doHeartBeat(this.getClass());
			SysHelper.waitIt(this, 1000 * 3); // 3 seconds
			try {
				int healthWorker = 0;
				// 检索超时任务并关闭任务组
				for (TimerWorker tw : workerList) {
					synchronized(tw){
						if (tw.getRunType() == TimerWorker.RUN
								&& System.currentTimeMillis()
										- tw.getHeartbeatTime() > TIMEOUT) {
							// 任务超时，需要关闭组
							JobGroup jobgroup = TimerService.getInstance()
									.getJobGroup(tw.getJob().getJobGroupName());
							tw.getJob().setIsTimeOut(true);
							if (jobgroup.getIsActive() == true) {
								JobGroupMap.get(tw.getJob().getJobGroupName())
										.setIsActive(false);
								log.debug("任务超时，关闭组["
										+ tw.getJob().getJobGroupName() + "]");
							}

						} else if (tw.getRunType() == TimerWorker.RUN) {
							++healthWorker;
						}
					}	
				}

				// 检查任务组
				synchronized (JobGroupMap) {
					for (JobGroup jobGroup : JobGroupMap.values()) {
						List<Job> l = jobGroup.getJobList();
						Job[] jobArray = l.toArray(new Job[l.size()]);
						long nowTime = System.currentTimeMillis();
						boolean groupActiveFlag = true;
						for (Job job : jobArray) {
							// 删除超时任务
							if (nowTime - job.getNewJobTime() > job
									.getTimeOut()) {
								jobGroup.removeJob(job);
								System.out.println(job + "超时退出");
								if (jobGroup.getGroupSize() == 0) {
									jobGroup.setIsActive(true);
								}
								log.debug(job + "超时退出");
							}
							// 判断任务组是否可恢复
							if (job.getIsTimeOut() == true) {
								groupActiveFlag = false;
							}
						}
						if (jobGroup.getIsActive() == false) {
							if (groupActiveFlag == true) {
								log.debug(jobGroup.getGroupName()
										+ "所有任务超时恢复，任务组设置为可执行");
								jobGroup.setIsActive(true);
							}
						}

					}
				}

				// 重置工人线程数量，实现动态调整
				int addWorkerNum = 0;
				if (healthWorker <= 100) {
					for (TimerWorker tw : workerList) {
						if (tw.getRunType() == TimerWorker.SUSPEND) {
							++addWorkerNum;
							tw.setRunType(TimerWorker.RUN);
							log.debug("所有工人线程堵塞，增加[" + addWorkerNum + "]，激活线程["
									+ tw.getName() + "]");
							if (addWorkerNum >= 50)
								break;
						}
					}
				} else if (healthWorker > 250) {
					for (TimerWorker tw : workerList) {
						if (tw.getRunType() == TimerWorker.RUN) {
							--healthWorker;
							++addWorkerNum;
							tw.setRunType(TimerWorker.SUSPEND);
							log.debug("活动工人线程上限，缩减[" + addWorkerNum + "]，暂停线程["
									+ tw.getName() + "]");
							if (healthWorker <= 250) {
								break;
							}
						}
					}
				}

				// 检索任务
				int activeWorker = 0;
				for (TimerWorker tw : workerList) {
					if (tw.getRunType() == TimerWorker.RUN) {
						++activeWorker;
					}
				}
				//log.debug("-目前活跃检测线程"+activeWorker+"个");
				// log.debug("-目前"+wait2RunJobList.size()+"个可执行任务");
				// log.debug("-目前JobGroupMap大小"+JobGroupMap.size()+"个");
				synchronized (JobGroupMap) {
					for (JobGroup jobGroup : JobGroupMap.values()) {
						if (jobGroup.getIsActive() == false) {
							log.debug(jobGroup.getGroupName() + "任务组状态"
									+ jobGroup.getIsActive());
							continue;
						}
						List<Job> jobs = jobGroup.find();
						if (jobs.size() == 0) {
							// log.debug(jobGroup.getGroupName()+"任务组没有找到可以执行的任务");
							continue;
						}
						for (int i = 0; i < jobs.size() && i < 10; i++) {
							Job job = jobs.get(i);
							job.setStatus(1);
							synchronized (wait2RunJobList) {
								wait2RunJobList.add(job);
							}
						}
						// log.debug("目前"+wait2RunJobList.size()+"个可执行任务");
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				log.debug("执行检索异常，消息:" + StringUtil.createStackTrace(ex));
			}
		}
	}
}
