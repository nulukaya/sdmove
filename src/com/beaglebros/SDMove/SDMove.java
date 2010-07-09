package com.beaglebros.SDMove;

// Thanks to iSec Partners' available source code for Manifest Explorer
// for pointing me in the right direction for dealing with the apk's xml file
// <https://www.isecpartners.com/manifest_explorer.html>

import android.app.Dialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.graphics.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


class PkgListItem {
	public PackageInfo pkg;
	public String name;
	public String meta;
	public int color;
	
	public PkgListItem(PackageInfo pkgin, String namein, String metain, int colorin) {
		pkg = pkgin;
		name = namein;
		meta = metain;
		color = colorin;
	}

	public String toString() {
		return name;
	}
	
}

@SuppressWarnings("unchecked")
class PkgListItemAdapter extends ArrayAdapter {
	Context context;

	public PkgListItemAdapter(Context context, int layout, List<PkgListItem> pkglist) {
		super(context, layout, pkglist.toArray());
		this.context = context;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = (TextView)super.getView(position, convertView, parent);
		PkgListItem p = (PkgListItem) this.getItem(position);
		view.setTextColor(p.color);
		return view;
	}

}

public class SDMove extends ListActivity {
	
	// Experimentally determined
	private static final int auto = 0;
	private static final int internalOnly = 1;
	private static final int preferExternal = 2;
	
	private static final int ABOUT_DIALOG = 0;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ArrayList<PkgListItem> pkglist = new ArrayList<PkgListItem>();        
		
		getPackages(pkglist);
		
		Collections.sort(pkglist, new byPkgName());
	
		setListAdapter(new PkgListItemAdapter(this, android.R.layout.simple_list_item_1, pkglist));
		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent it = new Intent(Intent.ACTION_VIEW);

				it.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
				it.putExtra("com.android.settings.ApplicationPkgName", pkglist.get(position).name);
				it.putExtra("pkg", pkglist.get(position).pkg.packageName);

				List<ResolveInfo> acts = getPackageManager().queryIntentActivities(it, 0);

				if (acts.size() > 0) {
					startActivity(it);
				}
			}
		});

	}
	
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog d = new Dialog(this);
		switch (id) {
		case ABOUT_DIALOG:
			d.setContentView(R.layout.aboutdialog);
			d.setTitle("About");
			break;
		default:
			return null;
		}		
		return d;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.aboutmenu:
			showDialog(0);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void getPackages(ArrayList<PkgListItem> pkglist) {
		PackageManager pm = getPackageManager();
		
		String tmp;
	
		for (PackageInfo pkg: pm.getInstalledPackages(0)) {
			String packageName;
			packageName = pkg.packageName;
			if ( packageName == null || packageName == "" ) {
				packageName = "android";
			}
			
			try {
				AssetManager am = createPackageContext(packageName, 0).getAssets();
				XmlResourceParser xml = am.openXmlResourceParser("AndroidManifest.xml");
				try {
					int eventType = xml.getEventType();
					xmlloop:
					while (eventType != XmlPullParser.END_DOCUMENT) {
						switch (eventType) {
							case XmlPullParser.START_TAG:
								if (! xml.getName().matches("manifest")) {
									break xmlloop;
								} else {
									attrloop:
									for (int j = 0; j < xml.getAttributeCount(); j++) {
										if (xml.getAttributeName(j).matches("installLocation")) {
											switch (Integer.parseInt(xml.getAttributeValue(j))) {
											case auto:
												tmp = "May Be Moved\n";
												if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
													tmp += "(already moved)";
													pkglist.add(new PkgListItem(pkg, pkg.applicationInfo.loadLabel(pm).toString(), tmp, Color.GREEN));
												} else {
													tmp += "(not already moved)";
													pkglist.add(new PkgListItem(pkg, pkg.applicationInfo.loadLabel(pm).toString(), tmp, Color.YELLOW));
												}
												break;
											case internalOnly:
												pkglist.add(new PkgListItem(pkg, pkg.applicationInfo.loadLabel(pm).toString(), "Cannot Be Moved", Color.RED));
												break;
											case preferExternal:
												tmp = "External Storage Preferred\n";
												if ((pkg.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
													tmp += "(already moved)";
													pkglist.add(new PkgListItem(pkg, pkg.applicationInfo.loadLabel(pm).toString(), tmp, Color.BLUE));
												} else {
													tmp += "(not already moved)";
													pkglist.add(new PkgListItem(pkg, pkg.applicationInfo.loadLabel(pm).toString(), tmp, Color.CYAN));
												}
												break;
											default:
												pkglist.add(new PkgListItem(pkg, pkg.applicationInfo.loadLabel(pm).toString(), "Crazy Wackiness!", Color.MAGENTA));
												break;
											}
											break attrloop;
										}
									}
								}
								break;
						}
						eventType = xml.nextToken();
					}
				} catch (IOException ioe) {
					// TODO showError("Reading XML", ioe);
				} catch (XmlPullParserException xppe) {
					// TODO showError("Parsing XML", xppe);
				}
	
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
	
		}
	}

	private final class byPkgName implements Comparator<PkgListItem> {
		public int compare(PkgListItem a, PkgListItem b) {
		   return(a.toString()).compareToIgnoreCase(b.toString());
	     }
	}

}