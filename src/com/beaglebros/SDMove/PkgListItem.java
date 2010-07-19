package com.beaglebros.SDMove;

import java.util.ArrayList;
import java.util.Comparator;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

class PkgList extends ArrayList<PkgListItem> {
	private static final long serialVersionUID = 2531887428352785853L;
	public String title;
	
	public PkgList() {
		super();
	}
	
	public PkgList(final String tin) {
		super();
		title = tin;
	}
	
	public String toString() {
		return title;
	}
}

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

class PkgListAdapter extends ArrayAdapter<PkgListItem> {
	Context context;
	Comparator<PkgListItem> sorter;

	public PkgListAdapter(Context c, int layout, PkgList p) {
		super(c, layout, p);
		context = c;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView)super.getView(position, convertView, parent);
		PkgListItem p = (PkgListItem)this.getItem(position);
		view.setTextColor(context.getResources().getColor((p.getColor())));
		return view;
	}
	
	public void sort() {
		super.sort(sorter);
		this.notifyDataSetChanged();
	}

}

class ExpandablePkgListAdapter extends BaseExpandableListAdapter {
	ArrayList<PkgList> pll;
	Context context;
	
	public static final int MAX_GROUPS = 1000;
	
	public ExpandablePkgListAdapter(Context c) {
		pll = new ArrayList<PkgList>();
		context = c;
	}
	
	public ExpandablePkgListAdapter(Context c, ArrayList<PkgList> pllin) {
		pll = pllin;
		context = c;
	}
	
	public boolean addGroup(PkgList plin) {
		return pll.add(plin);
	}

	public Object getChild(int groupPosition, int childPosition) {
		return pll.get(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return ((childPosition + 1) * MAX_GROUPS) + (groupPosition + 1);
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		TextView view = new TextView(context);
		view.setText(pll.get(groupPosition).get(childPosition).toString());
		return view;
	}

	public int getChildrenCount(int groupPosition) {
		return pll.get(groupPosition).size();
	}

	public Object getGroup(int groupPosition) {
		return pll.get(groupPosition);
	}

	public int getGroupCount() {
		return pll.size();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition + 1;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		TextView view = new TextView(context);
		view.setText(pll.get(groupPosition).toString());
		return view;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
}