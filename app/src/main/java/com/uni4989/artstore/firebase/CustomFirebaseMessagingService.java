package com.uni4989.artstore.firebase;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.uni4989.artstore.MainActivity;
import com.uni4989.artstore.PopupWebActivity;
import com.uni4989.artstore.R;
import com.uni4989.artstore.preference.CustomPreferenceManager;
import com.uni4989.artstore.preference.TimepickerPreference;

import java.io.File;
import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
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

        Intent intent = null;
        String msgType = data.get("msgType");
        String targetUrl = data.get("targetUrl");

        /*
         * 백그라운드 또는 실행이 안되고 있는 경우는 Main을 띄우고
         * 그렇지 않은 경우는 자식 팝업을 띄운다.
         */
        if( isAppIsInBackground(getApplicationContext()) ) {

            intent = new Intent(this, MainActivity.class);
            intent
                    .setAction(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if( !targetUrl.isEmpty() ) {
                intent.putExtra("msgType", msgType);
                intent.putExtra("targetUrl", targetUrl);
            }

        } else {

            if(
                    msgType.isEmpty() || "chat".equals(msgType)
            ) {

                intent = new Intent(this, MainActivity.class);
                intent
                      //  .setAction(Intent.ACTION_MAIN)
                       // .addCategory(Intent.CATEGORY_LAUNCHER)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                if( !targetUrl.isEmpty() ) {
                    intent.putExtra("msgType", msgType);
                    intent.putExtra("targetUrl", targetUrl);
                }

            } else {

                if( "detail".equals(msgType) ) {
                    intent = new Intent(this, PopupWebActivity.class);
                    intent.putExtra("wepUrl", targetUrl);
                }
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent,
               PendingIntent.FLAG_UPDATE_CURRENT);

        String title = data.get("title");
        String message = data.get("message");

        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notifysnd);

        //설정에 진동여부 확인
        boolean prefVibration = CustomPreferenceManager.getBoolean(this, "pref_vibration");

        final String CHANNEL_ID = "UNIART";
        NotificationManager mManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_NAME = "UNIARTCHANNER";
            final String CHANNEL_DESCRIPTION = "UNIARTCHANNERDescription";
            final int importance = NotificationManager.IMPORTANCE_HIGH;

            // add in API level 26
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            if( !prefVibration ) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

                mChannel.setSound(sound, audioAttributes);
            } else {
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            }

            mManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent);

        if( !prefVibration ) {
            builder.setSound(sound);
        }

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setContentTitle(title);
            builder.setVibrate(new long[]{100, 200, 100, 200});
        }
        mManager.notify(0, builder.build());
    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if(remoteMessage == null ) return;

        boolean prefNotification = CustomPreferenceManager.getBoolean(this, "pref_notification");
        if( !prefNotification ) return;

        boolean abledNotifi = true;
        //방해 금지 모드 여부
        boolean prefDisturb = CustomPreferenceManager.getBoolean(this, "pref_disturb");
        if( prefDisturb ) {

            Calendar nowDatetime = Calendar.getInstance();

            int startTime = CustomPreferenceManager.getInt(this, "pref_start_disturb_time");
            int endTime = CustomPreferenceManager.getInt(this, "pref_end_disturb_time");

            int startHour = TimepickerPreference.calculateHours(startTime);
            int startMins = TimepickerPreference.calculateMins(startTime);

            Calendar sc = Calendar.getInstance();
            sc.setTime(new Date());
            sc.set(Calendar.HOUR_OF_DAY, startHour);
            sc.set(Calendar.MINUTE, startMins);
            sc.set(Calendar.SECOND, 0);

            int endHour = TimepickerPreference.calculateHours(endTime);
            int endMins = TimepickerPreference.calculateMins(endTime);
            Calendar ec = Calendar.getInstance();
            ec.setTime(new Date());
            ec.set(Calendar.HOUR_OF_DAY, endHour);
            ec.set(Calendar.MINUTE, endMins);
            ec.set(Calendar.SECOND, 0);

            if( startTime > endTime ) {
                ec.add(Calendar.DATE, 1);
            }

            abledNotifi = compareDate(nowDatetime, sc, ec);
        }

        if( abledNotifi )
            sendNotification(remoteMessage);
    }

    private boolean compareDate(Calendar now, Calendar start, Calendar end) {

        Log.d("CustomFirebaseMessaging", "compareDate: " + now.compareTo(start) );
        Log.d("CustomFirebaseMessaging", "compareDate: " + now.compareTo(end) );

        return now.compareTo(start) == 1 && now.compareTo(end) == -1;
    }

}
