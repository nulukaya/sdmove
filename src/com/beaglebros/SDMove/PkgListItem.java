package com.beaglebros.SDMove;


import java.io.IOException;
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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class PkgListItem {
	public PackageInfo pkg;
	public String name;
	public int stored;
	public int storepref;
	public boolean hidden = false;
	
	// Experimentally determined
	public static final int PKG_STOREPREF_AUTO = 0; // auto
	public static final int PKG_STOREPREF_INT = 1;  // internalOnly
	public static final int PKG_STOREPREF_EXT = 2;  // preferExternal
	
	public static final int PKG_STORED_INTERNAL = 0;
	public static final int PKG_STORED_EXTERNAL = 1;
	
	// Should exist as ApplicationInfo.FLAG_FORWARD_LOCK
	// but FLAG_FORWARD_LOCK is marked as "@hide".  WHY?!?
	public static final int FLAG_FORWARD_LOCK = (1<<20);
	
	public PkgListItem(PackageInfo pkgin, String namein, int storedin, int storeprefin) {
		pkg = pkgin;
		name = namein;
		storepref = storeprefin;
		stored = storedin;
	}
	
	public PkgListItem(Context context, PackageInfo pkgin) throws IllegalArgumentException{
		PackageManager pm = context.getPackageManager();
		String packageName;
		packageName = pkgin.packageName;
		if ( packageName == null || packageName == "" ) {
			packageName = "android";
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
									pkg = pkgin;
									name = pkgin.applicationInfo.loadLabel(pm).toString();
									stored = ((pkgin.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0)?PkgListItem.PKG_STORED_INTERNAL:PkgListItem.PKG_STORED_EXTERNAL;
									if ( 
									     ( ( pkgin.applicationInfo.flags & FLAG_FORWARD_LOCK ) == 0 ) &&
									     ( ( pkgin.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM ) == 0 ) &&
									     ( ( pkgin.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP ) == 0 )
									   ) {
										storepref = Integer.parseInt(xml.getAttributeValue(j));
									} else {
										// TODO: set different flag?
										storepref = PKG_STOREPREF_INT;
									}
									break xmlloop;
								}
							}
						}
						break;
					}
					eventType = xml.nextToken();
				}
				if (eventType == XmlPullParser.END_DOCUMENT) {
					throw new IllegalArgumentException("package has no installLocation attribute");
				}
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
	
	public int getColor() {
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