package de.justus.schulinfo.vertretungen;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import de.justus.schulinfo.Downloader;
import de.justus.schulinfo.MainActivity;

public class Errorpanel {

	public static String getError() {
		String error = "";
		boolean needRefresh = false;
		JSONObject getUpdates;
		if (MainActivity.prefs.getString("url", "").equals("")) {
			error = "Du hast die URL nicht angegeben, bitte trage sie in den Einstellungen ein.";
		} else {
			ConnectivityManager cm = (ConnectivityManager) MainActivity.context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			if (activeNetwork == null) {
				error = "Das Handy hat keine Internetverbindung. Bitte stelle das WLAN oder die mobile Datennutzung an.";
			} else if (!Downloader.downloaded) {
				error = "Laden";
			} else if (Downloader.noJSONString) {
				error = "Du hast wahrscheinlich die URL falsch eingegeben.";
			} else if (!Downloader.getUpdates().has("errors")) {
				error = "Entweder hast du die falsche URL angegeben, oder der Server funktioniert nicht richtig.";
			} else {
				getUpdates = Downloader.getUpdates();
				try {
					if (getUpdates.getInt("error") != 0) {
						switch (getUpdates.getInt("error")) {
						case 17:
							if (MainActivity.prefs.getString("password", "").equals("")) {
								error = "Du musst das Passwort in den Einstellungen eingeben, damit du die Daten ansehen kannst.";
							} else {
								error = "Du hast das falsche Passwort eingeben, bitte ändere es in den Einstellungen";
							}
							break;
						}
					}
				} catch (JSONException e) {
					Log.e("error", "Errorpanel:getError()", e);
				}
			}
		}
		return error;
	}
}
