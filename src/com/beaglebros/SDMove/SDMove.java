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

import android.app.AlertDialog;
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
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


public class SDMove extends ListActivity {
	
	private static final int ABOUT_DIALOG = 0;
	private static final int PROGRESS_DIALOG = 1;
	
	private static final String SETTINGS_SORTBY = "sortby";
	// if adding more SETTINGS_SORTBY, fix switch at DUMBASSBUG
	private static final int SETTINGS_SORTBY_NAME = 1;
	private static final int SETTINGS_SORTBY_STATUS = 2;
	private static final int SETTINGS_SORTBY_DEFAULT = SETTINGS_SORTBY_STATUS;
	private static final String SETTINGS_VIEWSIZE = "viewsize";
	private static final int SETTINGS_VIEWSIZE_LARGE = PkgListItemAdapter.TEXT_LARGE;
	private static final int SETTINGS_VIEWSIZE_MEDIUM = PkgListItemAdapter.TEXT_MEDIUM;
	private static final int SETTINGS_VIEWSIZE_SMALL = PkgListItemAdapter.TEXT_SMALL;
	private static final int SETTINGS_VIEWSIZE_DEFAULT = SETTINGS_VIEWSIZE_LARGE;
	private static final String IGNOREPREF = "ignore-";
	
	private PkgListItemAdapter plia;
	private PkgListItem controlledPkg = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		@SuppressWarnings("unchecked") // for "data" only
		ArrayList<PkgListItem> data = (ArrayList<PkgListItem>)getLastNonConfigurationInstance();
		
		if (data == null) {
			new GetPackagesInBackground().execute(new CreateHandler());
		} else {
			populateAdapter(data, getSortPref());
		}
		
	}

	class CreateHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			@SuppressWarnings("unchecked") // for "pl" only
			ArrayList<PkgListItem> pl = (ArrayList<PkgListItem>)msg.obj;
			populateAdapter(pl, getSortPref());
		}
	}
	
	@Override
	public void onStop() {
		Log.e("SDMove", "onStop");
		super.onStop();
	}

	@Override
	public void onRestart() {
		Log.e("SDMove", "onRestart");
		refreshPackage(controlledPkg);
		controlledPkg = null;
		super.onRestart();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
	    final ArrayList<PkgListItem> data = new ArrayList<PkgListItem>();
	    for (int i=0; i<plia.getCount(); i++) {
	    	data.add(plia.getItem(i));
	    }
	    return data;
	}
	
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case ABOUT_DIALOG:
			AlertDialog d = null;
			try {
				d = AboutDialogBuilder.create(this);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return d;
			//break;
		case PROGRESS_DIALOG:
			ProgressDialog pd = new ProgressDialog(SDMove.this);
			pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pd.setTitle("Please wait");
			pd.setMessage("Loading packages");
			pd.setCancelable(true);
			return pd;
			//break;
		default:
			return null;
		}		
	}

	public static class AboutDialogBuilder {
		public static AlertDialog create(Context context) throws NameNotFoundException {
			final RelativeLayout d = (RelativeLayout)View.inflate(context, R.layout.aboutdialog, null);
			RelativeLayout body = (RelativeLayout)d.findViewById(R.id.dialogbodyscroll).findViewById(R.id.dialogbody);
			TextView tv;
			SpannableString io;
			
			Resources r = context.getResources();
			
			io = new SpannableString(r.getString(R.string.intonly) + " " + r.getString(R.string.intonlydesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.intonly)), 0, r.getString(R.string.intonly).length(), 0);
			tv = (TextView)body.findViewById(R.id.intonlydesc);
			tv.setText(io);
			io = new SpannableString(r.getString(R.string.autoext) + " " + r.getString(R.string.autoextdesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.autoext)), 0, r.getString(R.string.autoext).length(), 0);
			tv = (TextView)body.findViewById(R.id.autoextdesc);
			tv.setText(io);
			io = new SpannableString(r.getString(R.string.autoint) + " " + r.getString(R.string.autointdesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.autoint)), 0, r.getString(R.string.autoint).length(), 0);
			tv = (TextView)body.findViewById(R.id.autointdesc);
			tv.setText(io);
			io = new SpannableString(r.getString(R.string.prefext) + " " + r.getString(R.string.prefextdesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.prefext)), 0, r.getString(R.string.prefext).length(), 0);
			tv = (TextView)body.findViewById(R.id.prefextdesc);
			tv.setText(io);
			io = new SpannableString(r.getString(R.string.prefint) + " " + r.getString(R.string.prefintdesc));
			io.setSpan(new ForegroundColorSpan(r.getColor(R.color.prefint)), 0, r.getString(R.string.prefint).length(), 0);
			tv = (TextView)body.findViewById(R.id.prefintdesc);
			tv.setText(io);
			String tmp = "";
			for (String s: r.getStringArray(R.array.thanks)) {
				tmp += s + "\n";
			}
			tv = (TextView)d.findViewById(R.id.thanks);
			tv.setMovementMethod(LinkMovementMethod.getInstance());
			tv.setText(Html.fromHtml(tmp));
	
			return new AlertDialog.Builder(context).setTitle(R.string.abouttitle).setCancelable(true).setIcon(android.R.drawable.ic_dialog_info).setPositiveButton(
				 context.getString(android.R.string.ok), null).setView(d).create();
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
		int vs = getPreferences(MODE_PRIVATE).getInt(SETTINGS_VIEWSIZE, SETTINGS_VIEWSIZE_DEFAULT);
		
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
		
		switch (vs) {
		case SETTINGS_VIEWSIZE_SMALL:
			vs = R.id.viewsmall;
			break;
		case SETTINGS_VIEWSIZE_MEDIUM:
			vs = R.id.viewmedium;
			break;
		case SETTINGS_VIEWSIZE_LARGE:
		default:
			vs = R.id.viewlarge;
			break;
		}
		menu.findItem(vs).setChecked(true);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		switch (item.getItemId()) {
		case R.id.aboutmenu:
			showDialog(ABOUT_DIALOG);
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
		case R.id.viewlarge:
			plia.setTextSize(PkgListItemAdapter.TEXT_LARGE);
			item.setChecked(true);
			settings.edit().putInt(SETTINGS_VIEWSIZE, SETTINGS_VIEWSIZE_LARGE).commit();
			return true;
			//break;
		case R.id.viewmedium:
			plia.setTextSize(PkgListItemAdapter.TEXT_MEDIUM);
			item.setChecked(true);
			settings.edit().putInt(SETTINGS_VIEWSIZE, SETTINGS_VIEWSIZE_MEDIUM).commit();
			return true;
			//break;
		case R.id.viewsmall:
			plia.setTextSize(PkgListItemAdapter.TEXT_SMALL);
			item.setChecked(true);
			settings.edit().putInt(SETTINGS_VIEWSIZE, SETTINGS_VIEWSIZE_SMALL).commit();
			return true;
			//break;
		case R.id.clearignoremenu:
			clearIgnores();
			return true;
			//break;
		case R.id.refreshmenu:
			refreshPackage();
			return true;
			//break;
		default:
			return super.onOptionsItemSelected(item);
			// break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.contextmenu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		PkgListItem pli = plia.getItem(info.position);
		addIgnore(pli.name);
		plia.remove(pli);
		return true;
	}

	private class GetPackagesInBackground extends AsyncTask<Handler, Void, Void> {
		
		ArrayList<PkgListItem> pat;
		
		@Override
		public void onPreExecute() {
			showDialog(PROGRESS_DIALOG);
		}
		
		@Override
		protected Void doInBackground(Handler... handlers) {
			if ( handlers.length != 1 ) {
				return null;
			}
			pat = new ArrayList<PkgListItem>();
			getPackages(pat);
			/*
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			Message m = handlers[0].obtainMessage(0);
			m.obj = pat;
			m.sendToTarget();
			return null;
		}
		
		@Override
		public void onPostExecute(Void v) {
			removeDialog(PROGRESS_DIALOG);
		}
		
	}
	
	private void getPackages(ArrayList<PkgListItem> p) {
		PackageManager pm = getPackageManager();
		
		for (PackageInfo pkg: pm.getInstalledPackages(0)) {
			try {
				p.add(new PkgListItem(this, pkg));
			} catch (IllegalArgumentException e) {
				// That's okay
			}
		}
	}

	private void refreshPackage(PkgListItem p) {
		if (p == null) {
			refreshPackage();
			return;
		}
		try {
			PackageInfo pi;
			pi = getPackageManager().getPackageInfo(p.pkg.packageName, 0);
			plia.add(new PkgListItem(this, pi));
			plia.sort();
		} catch (NameNotFoundException e1) {
			Toast.makeText(this, "I guess you removed it?", Toast.LENGTH_SHORT).show();
		} catch (IllegalArgumentException e) {
			// That's okay
		}
		plia.remove(p);
	}

	private void refreshPackage() {
		new GetPackagesInBackground().execute(new RefreshHandler());
	}

	class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			@SuppressWarnings("unchecked") // for "pl" only
			ArrayList<PkgListItem> pl = (ArrayList<PkgListItem>)msg.obj;
			plia.clear();
			for (PkgListItem pli: pl) {
				plia.insert(pli, 0);
			}
			removeIgnoredPackages(plia);
			plia.sort();
		}
	}
	
	private void populateAdapter(ArrayList<PkgListItem> pap, Comparator<PkgListItem> s) {
		Collections.sort(pap, s);
		plia = new PkgListItemAdapter(SDMove.this, R.layout.pkglistitemview, getPreferences(MODE_PRIVATE).getInt(SETTINGS_VIEWSIZE, SETTINGS_VIEWSIZE_DEFAULT), pap);
		plia.sorter = s;
		removeIgnoredPackages(plia);
		setListAdapter(plia);
		ListView lv = getListView();
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent it = new Intent(Intent.ACTION_VIEW);
				PkgListItem pli = plia.getItem(position);

				it.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
				it.putExtra("com.android.settings.ApplicationPkgName", pli.name);
				it.putExtra("pkg", pli.pkg.packageName);

				List<ResolveInfo> acts = getPackageManager().queryIntentActivities(it, 0);

				controlledPkg = pli;
				if (acts.size() > 0) {
					startActivity(it);
				}
			}
		});
		registerForContextMenu(lv);
	}

	private void addIgnore(String pkg) {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		settings.edit().putBoolean(IGNOREPREF + pkg, true).commit();
	}

	private void removeIgnoredPackages(PkgListItemAdapter plia) {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		for (int i = 0; i < plia.getCount(); i++) {
			if (settings.contains(IGNOREPREF + plia.getItem(i).name)) {
				plia.remove(plia.getItem(i));
				i--; // because the remove causes the positions of the following items to shift down
			}
		}
	}
	
	private void clearIgnores() {
		SharedPreferences settings = getPreferences(MODE_PRIVATE);
		int count = 0;
		Set<String> s = settings.getAll().keySet();
		for (String key: s) {
			if (key.startsWith(IGNOREPREF)) {
				settings.edit().remove(key).commit();
				count++;
			}
		}
		if (count > 0) {
			refreshPackage();
		} else {
			Toast.makeText(this, R.string.noignoredpackages, Toast.LENGTH_SHORT).show();
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