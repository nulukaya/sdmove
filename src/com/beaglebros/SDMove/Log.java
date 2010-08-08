package com.beaglebros.SDMove;

public class Log {
	
	private final static String TAG = "SDMove";
	
	public static void d(String msg) {
		d(null, msg, null);
	}

	public static void d(String tag, String msg) {
		d(tag, msg, null);
	}

	public static void d(String tag, String msg, Throwable e) {
		if (android.util.Log.isLoggable(tag, android.util.Log.DEBUG)) {
			if (e == null) {
				if (tag != null) {
					android.util.Log.d(tag, msg);
				} else {
					android.util.Log.d(TAG, msg);
				}
			} else {
				if (tag != null) {
					android.util.Log.d(tag, msg, e);
				} else {
					android.util.Log.d(TAG, msg, e);
				}
			}
		}
	}
	
	public static void i(String msg) {
		i(null, msg, null);
	}

	public static void i(String tag, String msg) {
		i(tag, msg, null);
	}

	public static void i(String tag, String msg, Throwable e) {
		if (android.util.Log.isLoggable(tag, android.util.Log.INFO)) {
			if (e == null) {
				if (tag != null) {
					android.util.Log.i(tag, msg);
				} else {
					android.util.Log.i(TAG, msg);
				}
			} else {
				if (tag != null) {
					android.util.Log.i(tag, msg, e);
				} else {
					android.util.Log.i(TAG, msg, e);
				}
			}
		}
	}

	public static void e(String msg) {
		e(null, msg, null);
	}
	
	public static void e(String tag, String msg) {
		e(tag, msg, null);
	}

	public static void e(String tag, String msg, Throwable e) {
		if (android.util.Log.isLoggable(tag, android.util.Log.ERROR)) {
			if (e == null) {
				if (tag != null) {
					android.util.Log.e(tag, msg);
				} else {
					android.util.Log.e(TAG, msg);
				}
			} else {
				if (tag != null) {
					android.util.Log.e(tag, msg, e);
				} else {
					android.util.Log.e(TAG, msg, e);
				}
			}
		}
	}

	public static void v(String msg) {
		v(null, msg, null);
	}
	
	public static void v(String tag, String msg) {
		v(tag, msg, null);
	}

	public static void v(String tag, String msg, Throwable e) {
		if (android.util.Log.isLoggable(tag, android.util.Log.VERBOSE)) {
			if (e == null) {
				if (tag != null) {
					android.util.Log.v(tag, msg);
				} else {
					android.util.Log.v(TAG, msg);
				}
			} else {
				if (tag != null) {
					android.util.Log.v(tag, msg, e);
				} else {
					android.util.Log.v(TAG, msg, e);
				}
			}
		}
	}

	public static void w(String msg) {
		w(null, msg, null);
	}
	
	public static void w(String tag, String msg) {
		w(tag, msg, null);
	}

	public static void w(String tag, String msg, Throwable e) {
		if (android.util.Log.isLoggable(tag, android.util.Log.WARN)) {
			if (e == null) {
				if (tag != null) {
					android.util.Log.w(tag, msg);
				} else {
					android.util.Log.w(TAG, msg);
				}
			} else {
				if (tag != null) {
					android.util.Log.w(tag, msg, e);
				} else {
					android.util.Log.w(TAG, msg, e);
				}
			}
		}
	}

	public static void wtf(String msg) {
		wtf(null, msg, null);
	}
	
	public static void wtf(String tag, String msg) {
		wtf(tag, msg, null);
	}

	public static void wtf(String tag, String msg, Throwable e) {
		if (android.util.Log.isLoggable(tag, android.util.Log.ASSERT)) {
			if (e == null) {
				if (tag != null) {
					android.util.Log.wtf(tag, msg);
				} else {
					android.util.Log.wtf(TAG, msg);
				}
			} else {
				if (tag != null) {
					android.util.Log.wtf(tag, msg, e);
				} else {
					android.util.Log.wtf(TAG, msg, e);
				}
			}
		}
	}
}
