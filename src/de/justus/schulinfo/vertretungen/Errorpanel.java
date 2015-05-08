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
						case 2:
						case 17:
							if (MainActivity.prefs.getString("password", "").equals("")) {
								error = "Du musst das Passwort in den Einstellungen eingeben, damit du die Daten ansehen kannst.";
							} else {
								error = "Du hast das falsche Passwort eingeben, bitte ändere es in den Einstellungen";
							}
							break;
						case 14:
						case 15:
							error = "Du darfst nicht vom Handy aus auf den Vertretungsplan zugreifen.";
							break;
						case 2001:
							error = "Die Vertretungsplankomponente ist nicht auf dem Server installiert.";
							break;
						case 3001:
							error = "Die Klassenarbeitskomponente ist nicht auf dem Server installiert.";
							break;
						case 3002:
							error = "Schüler dürfen nicht auf Klassenarbeiten zugreifen.";
							break;
						case 4001:
							error = "Die Klausurkomponente ist nicht auf dem Server installiert.";
							break;
						case 4002:
							error = "Schüler dürfen nicht auf Klausuren zugreifen.";
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
