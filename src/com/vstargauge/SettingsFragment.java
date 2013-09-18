/**
 * 
 */
package com.vstargauge;

import com.downrangeproductions.vstargauge.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author PyleC1
 * 
 */
public class SettingsFragment extends PreferenceFragment {
	// =============================================
	// Statics

	public SettingsFragment() {
	}

	// =============================================
	// Overrides

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
	}
}
