package com.api.project.util.queue;

import java.util.LinkedList;

import com.api.project.util.helper.SysHelper;

public class BlockingQueue<E> {

	private LinkedList<E> queue = new LinkedList<>();
	private int maxSize = 0;

	private byte[] rlock = new byte[1];
	private byte[] wlock = new byte[1];
	
	private byte[] addFirstLock = new byte[1];
	private byte[] removeLastLock = new byte[1];

	public BlockingQueue() {
		this(1000);
	}
		
	public BlockingQueue(int maxSize) {
		this.maxSize = maxSize;
	}
	
	public void add(E o) {
		synchronized(addFirstLock) {
			if (queue.size() >= maxSize) 
				SysHelper.waitIt(wlock);

			synchronized(this) {
				queue.addFirst(o);
				SysHelper.notifyIt(rlock);
			}
		}
	}
	
	public void addFirst(E o) {
		synchronized(this) {
			queue.addLast(o);
			SysHelper.notifyIt(rlock);
		}
	}
	
	public E remove(int second) {
		synchronized(removeLastLock) {
			if (queue.size() <= 0) {
				if (second > 0)
					SysHelper.waitIt(rlock, second*1000);
				else
					SysHelper.waitIt(rlock);
			}
		
			synchronized(this) {
				if (queue.size() <= 0)
					return null;
				
				E o = queue.removeLast();
				SysHelper.notifyIt(wlock);
				return o;
			}
		}
	}
	
	public E remove() {
		return remove(-1);
	}
	
	public int size() {
		return queue.size();
	}
	
	public int maxSize() {
		return maxSize;
	}
}
