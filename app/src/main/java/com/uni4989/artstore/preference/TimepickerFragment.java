package com.uni4989.artstore.preference;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import java.util.Calendar;

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
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            getTimepickerPreference().setValue(mValue);
        }
    }

    private TimepickerPreference getTimepickerPreference() {
        return (TimepickerPreference) getPreference();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d(TAG, "onTimeSet: " + hourOfDay);
        Log.d(TAG, "onTimeSet: " + minute);
        mValue = hourOfDay + ":" + minute;
        getTimepickerPreference().setValue(mValue);
    }
}
