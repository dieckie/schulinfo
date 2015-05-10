package de.justus.schulinfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import de.justus.schulinfo.R;

public class UrlPreference extends DialogPreference {

	String DEFAULT_VALUE = "";
	
	private EditText urlEdit;
	
	public UrlPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDialogLayoutResource(R.layout.url_preference);
		setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
	}
	
	@Override
	protected void onBindDialogView(View view) {

	    urlEdit = (EditText) view.findViewById(R.id.url_edit);

	    SharedPreferences pref = getSharedPreferences();
	    urlEdit.setText(pref.getString(getKey(), ""));
	    super.onBindDialogView(view);
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			persistString(urlEdit.getText().toString());
		}
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		String mCurrentValue = "";
		if (restorePersistedValue) {
			// Restore existing state
			mCurrentValue = this.getPersistedString(DEFAULT_VALUE);
		} else {
			// Set default state from the XML attribute
			mCurrentValue = (String) defaultValue;
			persistString(mCurrentValue);
		}
	}
}
