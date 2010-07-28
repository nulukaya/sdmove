package com.beaglebros.SDMove;


import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class PkgListItem {
	public PackageInfo pkg;
	public String name;
	public int stored;
	public int storepref;
	
	// Experimentally determined
	public static final int PKG_STOREPREF_AUTO = 0; // auto
	public static final int PKG_STOREPREF_INT = 1;  // internalOnly
	public static final int PKG_STOREPREF_EXT = 2;  // preferExternal
	
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

class PkgListItemAdapter extends ArrayAdapter<PkgListItem> {
	Context context;
	Comparator<PkgListItem> sorter;
	int size;

	public static final int TEXT_SMALL = 0;
	public static final int TEXT_MEDIUM = 1;
	public static final int TEXT_LARGE = 2;
	public static final int TEXT_DEFAULT = TEXT_LARGE;
	
	public PkgListItemAdapter(Context context, int layout, List<PkgListItem> p) {
		super(context, layout, p);
		this.context = context;
		size = TEXT_DEFAULT;
	}

	public PkgListItemAdapter(Context context, int layout, int s, List<PkgListItem> p) {
		super(context, layout, p);
		this.context = context;
		size = s;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView)super.getView(position, convertView, parent);
		PkgListItem p = (PkgListItem)this.getItem(position);
		switch (size) {
		case TEXT_SMALL:
			view.setTextAppearance(context, android.R.style.TextAppearance_Small);
			break;
		case TEXT_MEDIUM:
			view.setTextAppearance(context, android.R.style.TextAppearance_Medium);
			break;
		case TEXT_LARGE:
		default:
			view.setTextAppearance(context, android.R.style.TextAppearance_Large);
			break;
		}
		view.setMinHeight(Math.round(context.getResources().getDisplayMetrics().density * (view.getTextSize() * 3 - 2)));
		view.setTextColor(context.getResources().getColor((p.getColor())));
		return view;
	}
	
	public void setTextSize(int s) {
		size = s;
		this.notifyDataSetChanged();
	}
	
	public void sort() {
		super.sort(sorter);
		this.notifyDataSetChanged();
	}

}