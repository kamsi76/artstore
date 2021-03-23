package com.uni4989.artstore.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;

public class TimepickerPreference extends DialogPreference implements Preference.OnPreferenceChangeListener {

    private int mHour = 0;
    private int mMinute = 0;

    private String mValue = "";
    private String mDefaultValue = "";

    private final String DEFAULT_VALUE = "00:00";

    private final String TAG = this.getFragment();

    private String mText;

    public TimepickerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.dialogPreferenceStyle);
    }


    public TimepickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnPreferenceChangeListener(this);
    }

    public void setValue(String value) {
        final boolean wasBlocking = shouldDisableDependents();
        mValue = value;
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    public String getValue() {
        return mValue;
    }

    public String getDefaultValue() {
        return mDefaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        mDefaultValue = defaultValue;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
