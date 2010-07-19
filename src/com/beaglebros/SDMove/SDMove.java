package com.beaglebros.SDMove;

// Thanks to iSec Partners' available source code for Manifest Explorer
// for pointing me in the right direction for dealing with the apk's xml file
// <https://www.isecpartners.com/manifest_explorer.html>

// Thanks to Quick System Info's source code for showing me how to open the
// Application Info manager.

// Thanks to herriojr on #android-dev for 'splainin' to me how my dialog should work
// Thanks also to romainguy and tateitsu for answering my stupid Java questions
// Thanks also to kroot for making me understand how SpannableString worked
// Thanks to Leeds for pointing out my stupid mistake in interpreting a stack trace
// and to everyone in general on #android-dev for putting up with me

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


public class SDMove extends ListActivity {
	
	private static final int ABOUT_DIALOG = 0;
	private static final int PROGRESS_DIALOG = 1;
	
	private static final String SETTINGS_SORTBY = "sortby";
	// if adding more SETTINGS_SORTBY, fix switch at DUMBASSBUG
	private static final int SETTINGS_SORTBY_NAME = 1;
	private static final int SETTINGS_SORTBY_STATUS = 2;
	private static final int SETTINGS_SORTBY_DEFAULT = SETTINGS_SORTBY_STATUS;
	
	PkgListAdapter plia;
	ArrayList<PkgListItem> ee, ei, ae, ai, io;
	List<ArrayList<PkgListItem>> pll;
	
	/*
	class PkgChgReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context c, Intent i) {
			Toast.makeText(SDMove.this, "testing broadcast receive", Toast.LENGTH_LONG).show();
		}
	}
	*/
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    final ArrayList<PkgListItem> data = new ArrayList<PkgListItem>();
	    for (int i=0; i<plia.getCount(); i++) {
	    	data.add(plia.getItem(i));
	    }
	    return data;
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//registerReceiver(new PkgChgReceiver(), new IntentFilter(Intent.ACTION_PACKAGE_CHANGED));
		
		//@SuppressWarnings("unchecked") // for "data" only
		PkgList data = (PkgList)getLastNonConfigurationInstance();
		
		if (data != null) {
			populateAdapter(data, getSortPref());
		} else {
		
			new AsyncTask<Void,Void,Void>() {
				PkgList pat;
				@Override
				public void onPreExecute() {
					showDialog(PROGRESS_DIALOG);
				}
				@Override
				public void onPostExecute(Void p) {
					dismissDialog(PROGRESS_DIALOG);
			        pt.setState(ProgressThread.STATE_DONE);
					populateAdapter(pat, getSortPref());
				}
				@Override
				protected Void doInBackground(Void... params) {
					pat = new PkgList();
					getPackages(pat);
					return null;
				}
			}.execute();
			
		}
		
	}

	private Comparator<PkgListItem> getSortPref() {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		switch (settings.getInt(SETTINGS_SORTBY, SETTINGS_SORTBY_DEFAULT)) {
		case SETTINGS_SORTBY_NAME:
			return new byPkgName();
			//break;
		case SETTINGS_SORTBY_STATUS:
		default:
			return new byPkgStatus();
			//break;
		}
	}
	
	private void populateAdapter(PkgList pap, Comparator<PkgListItem> s) {
		Collections.sort(pap, s);
		plia = new PkgListAdapter(SDMove.this, android.R.layout.simple_list_item_1, pap);
		plia.sorter = s;
		setListAdapter(plia);
		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent it = new Intent(Intent.ACTION_VIEW);

				it.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
				it.putExtra("com.android.settings.ApplicationPkgName", plia.getItem(position).name);
				it.putExtra("pkg", plia.getItem(position).pkg.packageName);

				List<ResolveInfo> acts = getPackageManager().queryIntentActivities(it, 0);

				if (acts.size() > 0) {
					startActivity(it);
				}
			}
		});
	}
	
	ProgressDialog pd;
	ProgressThread pt;
	
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case ABOUT_DIALOG:
			TextView tv;
			SpannableString io;
			
			Resources r = getResources();
			Dialog d = new Dialog(this);
			
			d.setContentView(R.layout.aboutdialog);
			d.setTitle(r.getString(R.string.abouttitle));
			io = new SpannableString(r.getString(R.string.intonly) + " " + r.getString(R.string.intonlydesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.intonly)), 0, r.getString(R.string.intonly).length(), 0);
			tv = (TextView)d.findViewById(R.id.intonlydesc);
			tv.setText(io);
			io = new SpannableString(r.getString(R.string.autoext) + " " + r.getString(R.string.autoextdesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.autoext)), 0, r.getString(R.string.autoext).length(), 0);
			tv = (TextView)d.findViewById(R.id.autoextdesc);
			tv.setText(io);
			io = new SpannableString(r.getString(R.string.autoint) + " " + r.getString(R.string.autointdesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.autoint)), 0, r.getString(R.string.autoint).length(), 0);
			tv = (TextView)d.findViewById(R.id.autointdesc);
			tv.setText(io);
			io = new SpannableString(r.getString(R.string.prefext) + " " + r.getString(R.string.prefextdesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.prefext)), 0, r.getString(R.string.prefext).length(), 0);
			tv = (TextView)d.findViewById(R.id.prefextdesc);
			tv.setText(io);
			io = new SpannableString(r.getString(R.string.prefint) + " " + r.getString(R.string.prefintdesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.prefint)), 0, r.getString(R.string.prefint).length(), 0);
			tv = (TextView)d.findViewById(R.id.prefintdesc);
			tv.setText(io);
			String tmp = "";
			for (String s: r.getStringArray(R.array.thanks)) {
				tmp += s + "\n";
			}
			tv = (TextView)d.findViewById(R.id.thanks);
			tv.setMovementMethod(LinkMovementMethod.getInstance());
			tv.setText(Html.fromHtml(tmp));
			return d;
			//break;
		case PROGRESS_DIALOG:
			pd = new ProgressDialog(SDMove.this);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setTitle("Please wait");
			pd.setMessage("Loading packages");
			pd.setCancelable(true);
			pt = new ProgressThread(handler);
			pt.start();
			return pd;
			//break;
		default:
			return null;
		}		
	}
	
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };

	private class ProgressThread extends Thread {
	    @SuppressWarnings("unused")
		Handler mHandler;
	    final static int STATE_DONE = 0;
	    final static int STATE_RUNNING = 1;
	    int mState;
	
	    ProgressThread(Handler h) {
	        mHandler = h;
	    }
	
	    public void run() {
	        mState = STATE_RUNNING;   
	        while (mState == STATE_RUNNING) {
	            try {
	                Thread.sleep(100);
	            } catch (InterruptedException e) {
	                //Log.e("ERROR", "Thread Interrupted");
	            }
	        }
	    }
	    
	    public void setState(int state) {
	        mState = state;
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		int sb = getPreferences(MODE_PRIVATE).getInt(SETTINGS_SORTBY, SETTINGS_SORTBY_DEFAULT);
		
		/* 
		 * DUMBASSBUG
		 * 
		 * This seems insane, but I had an early bug that used R.id.sortby*
		 * as the SETTINGS_SORTBY value.  Unfortunately, those identifiers
		 * can and do change between builds.
		 * 
		 * So now I have to check the setting every time to make sure it's
		 * valid, as there could be a bogus setting from a prior version.
		 * 
		 */
		switch (sb) {
		case SETTINGS_SORTBY_NAME:
			sb = R.id.sortbyname;
			break;
		case SETTINGS_SORTBY_STATUS:
		default:
			sb = R.id.sortbystatus;
			break;
		}
		
		menu.findItem(sb).setChecked(true);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		switch (item.getItemId()) {
		case R.id.aboutmenu:
			showDialog(0);
			return true;
			// break;
		case R.id.sortbyname:
			plia.sorter = new byPkgName();
			plia.sort();
			item.setChecked(true);
			settings.edit().putInt(SETTINGS_SORTBY, SETTINGS_SORTBY_NAME).commit();
			return true;
			// break;
		case R.id.sortbystatus:
			plia.sorter = new byPkgStatus();
			plia.sort();
			item.setChecked(true);
			settings.edit().putInt(SETTINGS_SORTBY, SETTINGS_SORTBY_STATUS).commit();
			return true;
			// break;
		default:
			return super.onOptionsItemSelected(item);
			// break;
		}
	}

	private void getPackages(ArrayList<PkgListItem> p) {
		PackageManager pm = getPackageManager();
		
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
											p.add(new PkgListItem(
													pkg,
													pkg.applicationInfo.loadLabel(pm).toString(),
													((pkg.applicationInfo.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0)?PkgListItem.PKG_STORED_INTERNAL:PkgListItem.PKG_STORED_EXTERNAL,
													Integer.parseInt(xml.getAttributeValue(j))));
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

	private final class byPkgStatus implements Comparator<PkgListItem> {
		public int compare(PkgListItem a, PkgListItem b) {
			int ret;
			ret = new Integer(a.stored).compareTo(b.stored);
			if (ret != 0 ) {
				return ret;
			}
			ret = new Integer(a.storepref).compareTo(b.storepref);
			if (ret != 0 ) {
				if (a.storepref == PkgListItem.PKG_STOREPREF_INT) {
					ret = 1;
				} else if (b.storepref == PkgListItem.PKG_STOREPREF_INT) {
					ret = -1;
				}
				return ret;
			}
			return(a.toString().compareToIgnoreCase(b.toString()));
		}
	}

}