package com.ims.fees.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {

	/** First group of the first match of the regex, trimmed; an empty string when there is no match. */
	public static String find(String text, String regex) {
		Matcher m = Pattern.compile(regex).matcher(text);
		return m.find() ? m.group(1).trim() : "";
	}
}
