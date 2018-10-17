package com.api.project.util.thread.mutiTreadManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ExecuteThreadManager {

    private List<ExecuteThread> threadList = new ArrayList<>();
    private List<AbsTractTask> taskList = new ArrayList<>();
    private long createtime = System.currentTimeMillis();

    private ReentrantLock lock = new ReentrantLock();
    private Condition empty = lock.newCondition();

    public long getCreatetime() {
        return createtime;
    }

    private int MAX_THREAD_NUM = 5;

    public ExecuteThreadManager(int max_thread_num) {
        this.MAX_THREAD_NUM = max_thread_num;
    }

    public void executeTask(AbsTractTask task) {

        lock.lock();
        try {

            taskList.add(task);
            empty.signalAll();
        } finally {
            // TODO: handle finally clause
            lock.unlock();
        }

        synchronized (threadList) {
            if (taskList.size() > 0 && MAX_THREAD_NUM > threadList.size()) {
                ExecuteThread cet = new ExecuteThread(taskList, lock, empty);
                cet.start();
                threadList.add(cet);
            }
        }
    }

    public boolean taskIsFinish() {

        if (taskList.size() > 0) {

            System.out.println("----------------");
            return false;
        }

        for (ExecuteThread thread : threadList) {
            if (thread.isBusy()) {

                System.out.println("================");
                return false;
            }
        }
        return true;
    }

    public int getTaskSize() {

        return taskList.size();
    }

    public int getThreadSize() {
        return threadList.size();
    }

    public void clear() {
        synchronized (threadList) {
            if (threadList != null) {
                for (ExecuteThread thread : threadList)
                    thread.interrupt();
                threadList.clear();
            }
        }
        lock.lock();
        try {

            if (taskList != null)
                taskList.clear();
        } finally {
            // TODO: handle finally clause
            lock.unlock();
        }
    }

    public void halt() {
        synchronized (threadList) {
            for (ExecuteThread thread : threadList)
                thread.halt();
        }
    }

    public void go_on() {
        synchronized (threadList) {
            for (ExecuteThread thread : threadList)
                thread.go_on();
        }
    }
}
