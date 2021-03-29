package com.uni4989.artstore.preference;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class CustomPreferenceManager {

    private static final String DEFAULT_VALUE_STRING = "";
    private static final int DEFAULT_VALUE_INT = -1;
    private static final long DEFAULT_VALUE_LONG = -1L;
    private static final float DEFAULT_VALUE_FLOAT = -1F;
    private static final boolean DEFAULT_VALUE_BOOLEAN = false;

    private static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getString(Context context, String key) {
        return getPreferences(context).getString(key, DEFAULT_VALUE_STRING);
    }

    public static int getInt(Context context, String key) {
        return getPreferences(context).getInt(key, DEFAULT_VALUE_INT);
    }

    public static long getLong(Context context, String key) {
        return getPreferences(context).getLong(key, DEFAULT_VALUE_LONG);
    }

    public static float getFloat(Context context, String key) {
        return getPreferences(context).getFloat(key, DEFAULT_VALUE_FLOAT);
    }

    public static boolean getBoolean(Context context, String key) {
        return getPreferences(context).getBoolean(key, DEFAULT_VALUE_BOOLEAN);
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
}
