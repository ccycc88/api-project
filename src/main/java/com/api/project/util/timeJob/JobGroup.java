package com.api.project.util.timeJob;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobGroup {

	private Logger log = LoggerFactory.getLogger(JobGroup.class);
	private List<Job> jobList = new ArrayList<>();
	// 组的状态，true为可用，fasle为不可用
	private boolean isActive = true;
	private String groupName;

	public synchronized void putJob(Job job) {
		jobList.add(job);
	}

	public synchronized void removeJob(Job job) {
		jobList.remove(job);
	}

	public synchronized List<Job> find() {
		List<Job> reJobList = new ArrayList<>();
		Job[] jobArray = jobList.toArray(new Job[jobList.size()]);
		// log.debug("目前jobList大小" + jobArray.length);
		for (Job job : jobArray) {
			if (job.getIsActive() == false) {
				// log.debug("任务非active");
				jobList.remove(job);
				continue;
			}
			if (job.getStatus() == 1) {
				// log.debug("任务状态为待执行");
				continue;
			}
			if (job.isTimeOn()) {
				// log.debug("达到触发时间");
				reJobList.add(job);
			}
		}
		return reJobList;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean getIsActive() {
		return isActive;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName
	 *            the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public List<Job> getJobList() {
		return jobList;
	}

	public int getGroupSize() {
		return jobList.size();
	}
}
