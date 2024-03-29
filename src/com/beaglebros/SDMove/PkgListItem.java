package com.beaglebros.SDMove;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

class PkgList extends ArrayList<PkgListItem>{
	private static final long serialVersionUID = -5384284065196705520L;
	
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
	public boolean noflag = true;
	public boolean forwlock = false;
	public boolean hidden = false;
	
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
	
	public PkgListItem(Context context, PackageInfo pkgin) throws IllegalArgumentException{
		PackageManager pm = context.getPackageManager();
		
		pkg = pkgin;
		String packageName = pkg.packageName;
		if ( packageName == null || packageName == "" ) {
			packageName = "android";
		}
		name = pkgin.applicationInfo.loadLabel(pm).toString();
		if (name == null) {
			name = packageName;
		}
		if ( ( pkgin.applicationInfo.flags & (1<<20) ) != 0 ) {
			// should be:
			//  if (pkgin.applicationInfo.flags & ApplicationInfo.FLAG_FORWARD_LOCK) {
			// but FLAG_FORWARD_LOCK is marked as "@hide".  WHY?!?
			forwlock = true;
		}
		try {
			AssetManager am = context.createPackageContext(packageName, 0).getAssets();
			XmlResourceParser xml = am.openXmlResourceParser("AndroidManifest.xml");
			try {
				int eventType = xml.getEventType();
				xmlloop:
				while (eventType != XmlPullParser.END_DOCUMENT) {
					switch (eventType) {
					case XmlPullParser.START_TAG:
						if (! xml.getName().matches("manifest")) {
							eventType = xml.nextToken();
							continue xmlloop;
						} else {
							for (int j = 0; j < xml.getAttributeCount(); j++) {
								if (xml.getAttributeName(j).matches("installLocation")) {
									noflag = false;
									stored = ((pkgin.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0)?PkgListItem.PKG_STORED_INTERNAL:PkgListItem.PKG_STORED_EXTERNAL;
									storepref = Integer.parseInt(xml.getAttributeValue(j));
									break xmlloop;
								}
							}
						}
						break;
					}
					eventType = xml.nextToken();
				}
				/*
				if (eventType == XmlPullParser.END_DOCUMENT) {
					throw new IllegalArgumentException("package has no installLocation attribute");
				}
				*/
			} catch (IOException ioe) {
				Log.e("PkgListItem", "Reading XML", ioe);
			} catch (XmlPullParserException xppe) {
				Log.e("PkgListItem", "Parsing XML", xppe);
			}

		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	public String toString() {
		return name;
	}
	
	public int getStyle() {
		if (stored == PKG_STORED_INTERNAL) {
			return Typeface.BOLD;
		} else {
			return -1;
		}
	}
	
	public int getColor() {
		if (noflag || forwlock) {
			return (R.color.intonly);
		}
		int color = R.color.impossible;
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


class PkgListArray extends ArrayList<PkgList>{
	private static final long serialVersionUID = 4805887563026094864L;
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
		PkgListItem p = (PkgListItem)this.getItem(position);
		if (p.hidden) {
			return new View(context);
		}
		TextView view = (TextView)super.getView(position, convertView, parent);
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
		//view.setMinHeight(Math.round(context.getResources().getDisplayMetrics().density * (view.getTextSize() * 3 - 2)));
		view.setMinHeight((int)(view.getTextSize() * 3 - 2));
		view.setTextColor(context.getResources().getColor((p.getColor())));
		int style = p.getStyle();
		if (style != -1) {
			view.setTypeface(Typeface.create(view.getTypeface(), style));
		}
		return view;
	}
	
	public int getItemViewType(int position) {
		PkgListItem p = (PkgListItem)this.getItem(position);
		if (p.hidden) {
			return 1;
		} else {
			return 0;
		}
	}
	
	public int getViewTypeCount() {
		return 2;
	}
	
	public void setTextSize(int s) {
		size = s;
		this.notifyDataSetChanged();
	}
	
	public void sort() {
		super.sort(sorter);
		this.notifyDataSetChanged();
	}
	
	public PkgListItem getItem(String name) {
		for (int i=0; i < this.getCount(); i++) {
			if (this.getItem(i).pkg.packageName.contentEquals(name)) {
				return this.getItem(i);
			}
		}
		return null;
	}

}


class ExpandablePkgListAdapter extends BaseExpandableListAdapter {
	private PkgListArray pll;
	private Context context;
	private int groupLayout;
	private int childLayout;
	
	public static final int MAX_GROUPS = 100;
	
	public ExpandablePkgListAdapter(Context c, int gl, int cl) {
		pll = new PkgListArray();
		context = c;
		groupLayout = gl;
		childLayout = cl;
	}
	
	public ExpandablePkgListAdapter(Context c, int gl, int cl, PkgListArray pllin) {
		pll = pllin;
		context = c;
		groupLayout = gl;
		childLayout = cl;
	}
	
	public boolean addGroup(PkgList plin) {
		if (pll.size() >= MAX_GROUPS) 
			return false;
		return pll.add(plin);
	}
	
	public boolean addGroup(String s) {
		if (pll.size() >= MAX_GROUPS) 
			return false;
		return pll.add(new PkgList(s));
	}
	
	public boolean addChild(PkgListItem pin, int groupPosition) {
		return pll.get(groupPosition).add(pin);
	}

	public Object getChild(int groupPosition, int childPosition) {
		return pll.get(groupPosition).get(childPosition);
	}

	public long getChildId(int groupPosition, int childPosition) {
		return ((childPosition + 1) * MAX_GROUPS) + (groupPosition + 1);
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView view = (TextView)inflater.inflate(childLayout, parent, false);
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
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TextView view = (TextView)inflater.inflate(groupLayout, parent, false);
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