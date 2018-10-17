package com.api.project.util;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * 将输入写到光标处
 * @author Death
 *
 */
public class RobotKeyBoard {

	public final static Map<Character, Integer> KEY_BOARD = 
			new HashMap<Character, Integer>();
	
	static{
		
		KEY_BOARD.put('0', KeyEvent.VK_0);
		KEY_BOARD.put('1', KeyEvent.VK_1);
		KEY_BOARD.put('2', KeyEvent.VK_2);
		KEY_BOARD.put('3', KeyEvent.VK_3);
		KEY_BOARD.put('4', KeyEvent.VK_4);
		KEY_BOARD.put('5', KeyEvent.VK_5);
		KEY_BOARD.put('6', KeyEvent.VK_6);
		KEY_BOARD.put('7', KeyEvent.VK_7);
		KEY_BOARD.put('8', KeyEvent.VK_8);
		KEY_BOARD.put('9', KeyEvent.VK_9);
		KEY_BOARD.put('.', KeyEvent.VK_PERIOD);
		KEY_BOARD.put(',', KeyEvent.VK_COMMA);
		KEY_BOARD.put('-', KeyEvent.VK_MINUS);
	}
	
	private Robot robot = null;
	public RobotKeyBoard(){
		
		try {
			robot = new Robot();
			robot.setAutoDelay(20);
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void outPutWord(String input){
		
		long start = System.currentTimeMillis();
		robot.keyPress(KeyEvent.VK_CAPS_LOCK);
		robot.keyRelease(KeyEvent.VK_CAPS_LOCK);
		//robot.delay(50);
		char[] chs = input.toCharArray();
		for(int i=0; i<chs.length; i++){
			
			char c = chs[i];
			//robot.delay(50);
			robot.keyPress(KEY_BOARD.get(c));
			robot.keyRelease(KEY_BOARD.get(c));
		}
		//robot.delay(50);
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
	}
	public static void main(String[] args) {

		//RobotKeyBoard keyBoard = new RobotKeyBoard();
		//keyBoard.outPutWord("1256464");
	}
}
