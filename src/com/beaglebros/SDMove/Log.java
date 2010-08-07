package com.beaglebros.SDMove;

public class Log {
	
	private final static String TAG = "SDMove";
	
	public static void d(String msg) {
		d(null, msg);
	}

	public static void d(String tag, String msg) {
		if (android.util.Log.isLoggable(tag, android.util.Log.DEBUG)) {
			if (tag != null) {
				android.util.Log.d(tag, msg);
			} else {
				android.util.Log.d(TAG, msg);
			}
		}
	}
	
	public static void i(String msg) {
		i(null, msg);
	}

	public static void i(String tag, String msg) {
		if (android.util.Log.isLoggable(tag, android.util.Log.INFO)) {
			if (tag != null) {
				android.util.Log.i(tag, msg);
			} else {
				android.util.Log.i(TAG, msg);
			}
		}
	}

	public static void e(String msg) {
		e(null, msg);
	}
	
	public static void e(String tag, String msg) {
		if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
			if (tag != null) {
				android.util.Log.e(tag, msg);
			} else {
				android.util.Log.e(TAG, msg);
			}
		}
	}

	public static void v(String msg) {
		v(null, msg);
	}
	
	public static void v(String tag, String msg) {
		if (android.util.Log.isLoggable(tag, android.util.Log.VERBOSE)) {
			if (tag != null) {
				android.util.Log.v(tag, msg);
			} else {
				android.util.Log.v(TAG, msg);
			}
		}
	}

	public static void w(String msg) {
		w(null, msg);
	}
	
	public static void w(String tag, String msg) {
		if (android.util.Log.isLoggable(tag, android.util.Log.WARN)) {
			if (tag != null) {
				android.util.Log.w(tag, msg);
			} else {
				android.util.Log.w(TAG, msg);
			}
		}
	}

	public static void wtf(String msg) {
		wtf(null, msg);
	}
	
	public static void wtf(String tag, String msg) {
		if (android.util.Log.isLoggable(tag, android.util.Log.ASSERT)) {
			if (tag != null) {
				android.util.Log.wtf(tag, msg);
			} else {
				android.util.Log.wtf(TAG, msg);
			}
		}
	}
}