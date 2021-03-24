package com.uni4989.artstore.preference;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.uni4989.artstore.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final String TAG = this.getTag();

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {

        if( preference instanceof TimepickerPreference ) {
            DialogFragment dialogFragment = TimepickerFragment.newInstance(preference.getKey());
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), null);
        } else
            super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey);

        Log.d(TAG, "onCreatePreferences: " + rootKey);

    }

}