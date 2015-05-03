package de.justus.schulinfo;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import de.justus.gymboapp.R;

public class EinstellungenFragment extends PreferenceFragment {

	public EinstellungenFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		JSONArray klassenArray = Downloader.getKlassen();
		Log.d("JSON", "länge: " + klassenArray.length());
		addPreferencesFromResource(R.xml.preferences);
		int lenght = klassenArray.length();
		String[] entries = new String[lenght];
		for (int i = 0; i < lenght; i++) {
			try {
				entries[i] = klassenArray.getJSONObject(i).getString("name");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		//TODO Hier zwingend notwendig, die IDs der Klassen zu benutzen, oder gehen auch einfach die Namen?
		MultiSelectListPreference multi = (MultiSelectListPreference) findPreference("class_select");
		multi.setEntries(entries);
		multi.setEntryValues(entries);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.d("Prefs", preference.getKey());
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
}
