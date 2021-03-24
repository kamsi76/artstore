package com.uni4989.artstore.preference;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceDialogFragmentCompat;

public class TimepickerFragment extends PreferenceDialogFragmentCompat implements TimePickerDialog.OnTimeSetListener {

    private final String TAG = this.getTag();

    private String mValue;

    public static TimepickerFragment newInstance(String key) {
        final TimepickerFragment fragment = new TimepickerFragment();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TimepickerPreference preference = (TimepickerPreference)getPreference();
        return new TimePickerDialog(getActivity(), this, preference.getHours(), preference.getMins(), preference.isDialog24Hours());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        TimepickerPreference preference = (TimepickerPreference) getPreference();
        if (preference.callChangeListener(TimepickerPreference.calculateValue(hourOfDay, minute))) {
            preference.setValue(hourOfDay, minute);
        }
    }
}
