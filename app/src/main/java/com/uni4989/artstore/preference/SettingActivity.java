package com.uni4989.artstore.preference;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.uni4989.artstore.R;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // below line is used to check if
        // frame layout is empty or not.
        if (findViewById(R.id.settingFrameLayout) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // below line is to inflate our fragment.
            getSupportFragmentManager().beginTransaction().add(R.id.settingFrameLayout, new SettingsFragment()).commit();
        }
    }
}