package com.uni4989.artstore.firebase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.uni4989.artstore.MainActivity;

public class FirebaseBackgroundService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getExtras() != null) {
            for (String key : intent.getExtras().keySet()) {
                Object value = intent.getExtras().get(key);
                Log.e("FirebaseDataReceiver", "Key: " + key + " Value: " + value);
                if (key.equalsIgnoreCase("gcm.notification.body") && value != null) {
                    Bundle bundle = new Bundle();
                    Intent backgroundIntent = new Intent(context, MainActivity.class);
                    bundle.putString("push_message", value + "");
                    backgroundIntent.putExtras(bundle);
                    context.startService(backgroundIntent);
                }
            }
        }
    }
}