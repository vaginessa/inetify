package net.luniks.android.inetify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.luniks.android.impl.ConnectivityManagerImpl;
import net.luniks.android.impl.WifiManagerImpl;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * Main activity of the app, providing a possibility to manually test internet connectivity,
 * go to the settings and display a help text.
 * 
 * @author dode@luniks.net
 */
public class Inetify extends Activity {
	
	/** Tag used for logging */
	public static final String LOG_TAG = "Inetify";
	
	/** Number of retries when manually testing internet connectivity */
	private static final int TEST_RETRIES = 1;
	
	/** Title key used for SimpleAdapter */
	private static final String KEY_TITLE = "title";
	
	/** Summary key used for SimpleAdapter */
	private static final String KEY_SUMMARY = "summary";
	
	/** Index of the list item to test internet connectivity */
	private static final int INDEX_TEST = 0;
	
	/** Index of the list item to show the settings */
	private static final int INDEX_SETTINGS = 1;
	
	/** Index of the list item to show the ignore list */
	private static final int INDEX_IGNORELIST = 2;
	
	/** Index of the list item to show the help */
	private static final int INDEX_HELP = 3;
	
	/** Shared preferences */
	private SharedPreferences sharedPreferences;
	
	/** Tester */
	private Tester tester;

	/** 
	 * Performs initialization, sets the default notification tone and populates the view.
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		tester = new TesterImpl(this,
				new ConnectivityManagerImpl((ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE)), 
				new WifiManagerImpl((WifiManager)getSystemService(WIFI_SERVICE)),
				new TitleVerifierImpl());
		
		setDefaultTone();
		
		this.setContentView(R.layout.main);
		
		List<Map<String, String>> listViewData = buildListViewData();
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, listViewData, android.R.layout.simple_list_item_2, 
				new String[] { KEY_TITLE, KEY_SUMMARY },
				new int[] { android.R.id.text1, android.R.id.text2 });
		
		ListView listViewMain = (ListView)findViewById(R.id.listview_main);
		listViewMain.setAdapter(simpleAdapter);
		listViewMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
				if(position == INDEX_TEST) {
					runTest();
				}
				if(position == INDEX_SETTINGS) {
					showSettings();
				}
				if(position == INDEX_IGNORELIST) {
					showIgnoreList();
				}
				if(position == INDEX_HELP) {
					showHelp();
				}
			}
		});
	}
	
	/**
	 * Sets the Tester implementation used by the activity - intended for unit tests.
	 * @param tester
	 */
	public void setTester(final Tester tester) {
		this.tester = tester;
	}
	
	/**
	 * Returns a list of maps used as data given to SimpleAdapter.
	 * @return List<Map<String, String>>
	 */
	private List<Map<String, String>> buildListViewData() {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		
		Map<String, String> mapTest = new HashMap<String, String>();
		mapTest.put(KEY_TITLE, getString(R.string.main_title_test));
		mapTest.put(KEY_SUMMARY, getString(R.string.main_summary_test));
		list.add(INDEX_TEST, mapTest);
		
		Map<String, String> mapSettings = new HashMap<String, String>();
		mapSettings.put(KEY_TITLE, getString(R.string.main_title_settings));
		mapSettings.put(KEY_SUMMARY, getString(R.string.main_summary_settings));
		list.add(INDEX_SETTINGS, mapSettings);
		
		Map<String, String> mapIgnorelist = new HashMap<String, String>();
		mapIgnorelist.put(KEY_TITLE, getString(R.string.main_title_ignorelist));
		mapIgnorelist.put(KEY_SUMMARY, getString(R.string.main_summary_ignorelist));
		list.add(INDEX_IGNORELIST, mapIgnorelist);
		
		Map<String, String> mapHelp = new HashMap<String, String>();
		mapHelp.put(KEY_TITLE, getString(R.string.main_title_help));
		mapHelp.put(KEY_SUMMARY, getString(R.string.main_summary_help));
		list.add(INDEX_HELP, mapHelp);
		
		return list;
	}
	
	/**
	 * Sets DEFAULT_NOTIFICATION_URI if the notification tone in in the preferences
	 * is null (first installation or data deleted).
	 */
	private void setDefaultTone() {
		// Is there really no other way to set the default tone, i.e. in XML?
		String tone = sharedPreferences.getString("settings_tone", null);
		if(tone == null) {
			sharedPreferences.edit().putString("settings_tone", android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString()).commit();
		}
	}
	
	/**
	 * Test internet connectivity using an AsyncTask.
	 */
	private void runTest() {
		new TestTask().execute(new Void[0]);
	}
	
	/**
	 * Shows the settings.
	 */
	private void showSettings() {
		Intent launchPreferencesIntent = new Intent().setClass(this, Settings.class);
		startActivity(launchPreferencesIntent);
	}
	
	/**
	 * Shows the ignore list.
	 */
	private void showIgnoreList() {
		Intent showIgnoreListIntent = new Intent().setClass(this, IgnoreList.class);
		startActivity(showIgnoreListIntent);
	}
	
	/**
	 * Shows the help.
	 */
	private void showHelp() {
		Intent launchHelpIntent = new Intent().setClass(this, Help.class);
		startActivity(launchHelpIntent);
	}
	
	/**
	 * Displays the given TestInfo in the InfoDetail view.
	 * @param info
	 */
	private void showInfoDetail(final TestInfo info) {
		Intent infoDetailIntent = new Intent().setClass(Inetify.this, InfoDetail.class);
		infoDetailIntent.putExtra(InfoDetail.EXTRA_TEST_INFO, info);
		startActivity(infoDetailIntent);
	}
	
	/**
	 * AsyncTask showing a progress dialog while it is testing internet connectivity,
	 * and then displaying the results.
	 * 
	 * @author dode@luniks.net
	 */
    private class TestTask extends AsyncTask<Void, Void, TestInfo> {
    	
    	private ProgressDialog dialog = ProgressDialog.show(Inetify.this, "", Inetify.this.getString(R.string.main_testing), true);

    	/**
    	 * Shows the progress dialog.
    	 */
		@Override
		protected void onPreExecute() {
			dialog.show();
		}

		/**
		 * Runs the internet connectivity test in the background.
		 */
		@Override
		protected TestInfo doInBackground(final Void... arg) {
			return tester.test(TEST_RETRIES, 0, false);
		}
		
		/**
		 * Cancels the progress dialog, and calls showInfoDetail(TestInfo) with
		 * the TestInfo returned by doInBackground().
		 */
		@Override
	    protected void onPostExecute(final TestInfo info) {
			dialog.cancel();
			showInfoDetail(info);
	    }
		
    }
	
}
