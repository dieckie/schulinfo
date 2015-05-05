package de.justus.schulinfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class Downloader {
	static JSONObject updates = null;
	static String sObj = "";
	public static boolean downloaded = false;
	public static boolean noJSONString = false;
	Context context;

	public Downloader(Context context) {
		this.context = context;
	}

	public boolean download() throws MalformedURLException {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			String sUrl = prefs.getString("url", "");
			String pw = prefs.getString("password", "");
			URL url = null;
			if (sUrl != "") {
				if (pw == "") {
					url = new URL("http://" + sUrl + "/components/com_school_mobile/wserv/service.php?task=getUpdates");
				} else {
					url = new URL("http://" + sUrl + "/components/com_school_mobile/wserv/service.php?task=getUpdates&pw=" + pw);
				}

				sObj = new DownloadFileFromURL().execute(url.toString()).get();
				if (sObj != null) {
					updates = new JSONObject(sObj);
					downloaded = true;
					return true;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			noJSONString = true;
		}
		return false;
	}

	public static JSONObject getVertretungsplan() {
		JSONObject vertretungsplan = null;
		if (downloaded) {
			try {
				vertretungsplan = updates.getJSONObject("vertretungsplan");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return vertretungsplan;
	}

	public static JSONObject getUpdates() {
		if (downloaded) {
			return updates;
		} else {
			return new JSONObject();
		}

	}

	public static String getJSONString() {
		return sObj;
	}

	public static JSONArray getKlassen() {
		JSONArray klassen = null;
		if (downloaded) {
			try {
				klassen = updates.getJSONArray("klassenjgst");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return klassen;
	}

	static class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Bar Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		/**
		 * Downloading file in background thread
		 * */
		@Override
		protected String doInBackground(String... f_url) {
			try {
				URL url = new URL(f_url[0]);
				URLConnection con = url.openConnection();
				con.connect();

				BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String sObj = input.readLine();
				input.close();
				return sObj;
			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
			}

			return null;
		}

		@Override
		protected void onPostExecute(String file_url) {

		}
	}
}
