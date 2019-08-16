package com.example.demo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	public static String fromTime(final long time) {
		return sdf.format(new Date(time));
	}
	
	public static String now() {
		return sdf.format(new Date());
	}
}
