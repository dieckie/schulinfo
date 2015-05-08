package de.justus.schulinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.ViewFlipper;
import de.justus.gymboapp.R;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

	DrawView drawView;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mFragmentNames;
	public static Context context;
	boolean rightUrl = true;
	public static Downloader downloader;
	public static SharedPreferences prefs;
	public static ActionBar actionbar;
	String s = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTitle = mDrawerTitle = getTitle();
		mFragmentNames = getResources().getStringArray(R.array.fragment_names);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mFragmentNames));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(0);
		}
		context = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		downloader = new Downloader(this);
		try {
			downloader.download();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		actionbar = getActionBar();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	public static Downloader getDownloader() {
		return downloader;
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		Fragment fragment;
		switch (position) {
		case 0:
			fragment = new VertretungenFragment();
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
			break;
		case 1:
			fragment = new ArbeitenFragment();
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
			break;
		case 2:
			fragment = new EinstellungenFragment();
			fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
		default:
			/*
			 * fragment = new PlanetFragment(); Bundle args = new Bundle(); args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position); fragment.setArguments(args);
			 * fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
			 */
			break;
		}

		mDrawerList.setItemChecked(position, true);
		setTitle(mFragmentNames[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void downloadJSON(final String params) {
		Log.d("Method", "downloadJSON");

		Thread t2 = new Thread(new Runnable() {
			@Override
			public synchronized void run() {
				try {
					String sUrl = prefs.getString("url", "");
					Log.d("HTTP", sUrl + "");
					URL url = new URL("http://demo.schooljoomla.de/components/com_school_mobile/wserv/service.php?" + params);
					HttpURLConnection conn;
					conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					s = in.readLine();
					conn.disconnect();
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});
		t2.start();
		while (t2.isAlive())
			;
	}

	public static class VertretungenFragment extends Fragment {

		public VertretungenFragment() {
			// Empty constructor required for fragment subclasses
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			//TODO Wieder zurück ändern?
			ViewFlipper rootView = (ViewFlipper) inflater.inflate(R.layout.fragment_vertretung_flipper, container, false);
			getActivity().setTitle("Vertretungen");
			return rootView;
		}
	}
}