package com.uni4989.artstore.firebase;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.uni4989.artstore.MainActivity;
import com.uni4989.artstore.PopupWebActivity;
import com.uni4989.artstore.R;

import java.util.List;
import java.util.Map;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {

    private boolean isAppIsInBackground(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        //If your app is the process in foreground, then it's not in running in background
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");


        final String CHANNEL_ID = "ChannerID";
        NotificationManager mManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_NAME = "ChannerName";
            final String CHANNEL_DESCRIPTION = "ChannerDescription";
            final int importance = NotificationManager.IMPORTANCE_HIGH;

            // add in API level 26
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mManager.createNotificationChannel(mChannel);
        }

        Intent intent;
        String targetUrl = data.get("targetUrl");

        if( isAppIsInBackground(getApplicationContext()) ) {

            intent = new Intent(this, MainActivity.class);
            intent
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    );

            if( !targetUrl.isEmpty() ) {
                intent.putExtra("targetUrl", targetUrl);
            }

        } else {
            intent = new Intent(this, PopupWebActivity.class);
            intent.putExtra("wepUrl", targetUrl);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
               PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setContentIntent(pendingIntent);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setContentTitle(title);
            builder.setVibrate(new long[]{500, 500});
        }
        mManager.notify(0, builder.build());
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.i("### msg : ", remoteMessage.toString());
        if(remoteMessage != null ) {
            sendNotification(remoteMessage);
        }
    }

}
