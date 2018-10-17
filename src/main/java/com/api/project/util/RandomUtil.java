package com.api.project.util;

import java.util.Random;

public class RandomUtil {

	private static final int[] digits = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	private static final char[] chs = new char[] { '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public static String[] randomVal = new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
			"e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y",
			"z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };

	public static String getRandomDigitsNoRepeat(int length) {

		Random rand = new Random();
		for (int i = 10; i > 1; i--) {
			int index = rand.nextInt(i);
			int tmp = digits[index];
			digits[index] = digits[i - 1];
			digits[i - 1] = tmp;
		}
		int result = 0;
		for (int i = 0; i < length; i++)
			result = result * 10 + digits[i];

		if (result < 1000) {

			return ("0" + result);
		}
		return String.valueOf(result);
	}

	public static String getRandomCharacter(int length) {

		int len = chs.length;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {

			int index = (int) (Math.random() * len);
			sb.append(chs[index]);
		}

		return sb.toString();
	}

	public static String getRandom(int num) {

		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < num; i++) {

			sb.append(randomVal[random.nextInt(randomVal.length)]);
		}
		return sb.toString();
	}

	public static String getRandom() {
		return getRandom(4);
	}

	public static void main(String[] args) {

		System.out.println(getRandomCharacter(32));
	}
}
