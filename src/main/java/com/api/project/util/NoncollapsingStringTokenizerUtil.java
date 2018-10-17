package com.api.project.util;

public class NoncollapsingStringTokenizerUtil {

	private String str;
	private String[] delims;
	private int currentPosition;

	public NoncollapsingStringTokenizerUtil(String str) {   
        this(str, " \t\n\r\f");   
    }

	public NoncollapsingStringTokenizerUtil(String str, String delimStr) {   
        this.str = str;   
        delims = new String[delimStr.length()];   
        for (int i = 0; i < delimStr.length(); i++) {   
            delims[i] = delimStr.substring(i, i + 1);   
        }   
    }

	public String nextToken() {
		int nextDelimPosition = str.length();
		for (int i = 0; i < delims.length; i++) {
			int delimPosition = str.indexOf(delims[i], currentPosition);
			if (delimPosition >= 0 && delimPosition < nextDelimPosition) {
				nextDelimPosition = delimPosition;
			}
		}
		String token = str.substring(currentPosition, nextDelimPosition);
		currentPosition = nextDelimPosition + 1;
		return token;
	}

	public boolean hasMoreTokens() {
		return (currentPosition < str.length());
	}
}
