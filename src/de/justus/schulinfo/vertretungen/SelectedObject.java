package de.justus.schulinfo.vertretungen;

import org.json.JSONObject;

public class SelectedObject {
	private static JSONObject selectedObj = null;

	public static JSONObject get() {
		return selectedObj;
	}

	public static void set(JSONObject selectedObj) {
		SelectedObject.selectedObj = selectedObj;
	}

	public static boolean exists() {
		if (selectedObj == null) {
			return false;
		} else {
			return true;
		}
	}

}
