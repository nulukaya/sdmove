package com.beaglebros.SDMove;


import android.content.pm.PackageInfo;

class PkgListItem {
	public PackageInfo pkg;
	public String name;
	public int stored;
	public int storepref;
	
	// Experimentally determined
	private static final int PKG_STOREPREF_AUTO = 0; // auto
	private static final int PKG_STOREPREF_INT = 1;  // internalOnly
	private static final int PKG_STOREPREF_EXT = 2;  // preferExternal
	
	public static final int PKG_STORED_INTERNAL = 0;
	public static final int PKG_STORED_EXTERNAL = 1;
	
	public PkgListItem(PackageInfo pkgin, String namein, int storedin, int storeprefin) {
		pkg = pkgin;
		name = namein;
		storepref = storeprefin;
		stored = storedin;
	}

	public String toString() {
		return name;
	}
	
	public int getColor() {
		int color = -1;
		//Context c = getApplicationContext();
		switch(storepref) {
		case PKG_STOREPREF_AUTO:
			if (stored == PKG_STORED_INTERNAL) {
				color = R.color.autoint;
			} else if (stored == PKG_STORED_EXTERNAL) {
				color = R.color.autoext;
			}
			break;
		case PKG_STOREPREF_INT:
			if (stored == PKG_STORED_INTERNAL) {
				color = R.color.intonly;
			}
			break;
		case PKG_STOREPREF_EXT:
			if (stored == PKG_STORED_INTERNAL) {
				color = R.color.prefint;
			} else if (stored == PKG_STORED_EXTERNAL) {
				color = R.color.prefext;
			}
			break;
		}
		return color;
	}
	
	public String getMeta() {
		String ret = "";
		if (storepref == PKG_STOREPREF_AUTO) {
			ret = "no storage preference, ";
		} else if (storepref == PKG_STOREPREF_INT) {
			ret = "requires internal storage, ";
		} else if (storepref == PKG_STOREPREF_EXT) {
			ret = "prefers external storage, ";
		} else {
			return null;
		}
		if (stored == PKG_STORED_EXTERNAL) {
			ret += "currently on external storage";
		} else if (stored == PKG_STORED_INTERNAL) {
			ret += "currently on internal storage";
		} else {
			return null;
		}
		return ret;
	}
	
}