package com.uni4989.artstore.preference;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.uni4989.artstore.PopupWebActivity;
import com.uni4989.artstore.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    private final String TAG = this.getTag();
    protected final static int SETTING_REQUEST_CODE = 900;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting_preferences, rootKey);

        SwitchPreference disturb = findPreference("pref_disturb");
        if( !disturb.isChecked() ) {
            TimepickerPreference startTimepicker = findPreference("pref_start_disturb_time");
            TimepickerPreference endTimepicker = findPreference("pref_end_disturb_time");

            startTimepicker.setVisible(false);
            endTimepicker.setVisible(false);
        }

        disturb.setOnPreferenceChangeListener((preference, newValue) -> {
            TimepickerPreference startTimepicker = findPreference("pref_start_disturb_time");
            TimepickerPreference endTimepicker = findPreference("pref_end_disturb_time");

            Boolean isCheck = (Boolean) newValue;

            startTimepicker.setVisible(isCheck);
            endTimepicker.setVisible(isCheck);

            return true;
        });

        Preference term = findPreference("pref_term");
        term.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), PopupWebActivity.class);
            intent.putExtra("wepUrl", "/content.php?t=member&s=term");
            startActivity(intent);
            return false;
        });

        Preference privacy = findPreference("pref_privacy");
        privacy.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), PopupWebActivity.class);
            intent.putExtra("wepUrl", "/content.php?t=member&s=privacy");
            startActivity(intent);
            return false;
        });

        Preference oppolicy = findPreference("pref_oppolicy");
        oppolicy.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), PopupWebActivity.class);
            intent.putExtra("wepUrl", "/content.php?t=member&s=oppolicy");
            startActivity(intent);
            return false;
        });

        Preference logout = findPreference("pref_logout");
        logout.setOnPreferenceClickListener(preference -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("로그아웃");
            builder.setMessage("로그아웃 하시겠습니까?");
            builder.setPositiveButton("예", (dialog, which) -> {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("actionType", "logout");
                getActivity().setResult(SETTING_REQUEST_CODE, resultIntent);
                getActivity().finish();
            });

            builder.setNegativeButton("아니오", (dialog, which) -> {});


            builder.show();

            return false;
        });

        Preference secession = findPreference("pref_secession");
        secession.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), PopupWebActivity.class);
            intent.putExtra("wepUrl", "/content.php?t=member&s=secession");
            startActivityForResult(intent, SETTING_REQUEST_CODE);
            getActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            return false;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        String actionType = data.getStringExtra("actionType");
        switch (actionType) {
            case "secession" :
                Intent resultIntent = new Intent();
                resultIntent.putExtra("actionType", "secession");
                getActivity().setResult(SETTING_REQUEST_CODE, resultIntent);
                getActivity().finish();
                break;
        }
    }
}