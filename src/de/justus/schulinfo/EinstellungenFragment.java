package de.justus.schulinfo;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import de.justus.schulinfo.R;

public class EinstellungenFragment extends PreferenceFragment {

	public EinstellungenFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		MultiSelectListPreference multi = (MultiSelectListPreference) findPreference("class_select");
		if (Downloader.downloaded) {
			JSONArray klassenArray = Downloader.getKlassen();
			Log.d("JSON", "länge: " + klassenArray.length());
			int lenght = klassenArray.length();
			String[] entries = new String[lenght];
			for (int i = 0; i < lenght; i++) {
				try {
					entries[i] = klassenArray.getJSONObject(i).getString("name");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			multi.setEntries(entries);
			multi.setEntryValues(entries);
		} else {
			multi.setEnabled(false);
			multi.setSummary("Die Klassenliste konnte nicht herruntergeladen werden.");
		}
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.d("Prefs", preference.getKey());
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}
