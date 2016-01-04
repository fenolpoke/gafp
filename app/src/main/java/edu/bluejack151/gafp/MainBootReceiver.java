package edu.bluejack151.gafp;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

/**
 * Created by COMPAQ on 01/01/2016.
 */
public class MainBootReceiver extends BroadcastReceiver {


    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;


    @Override
    public void onReceive(Context context, Intent intent) {

        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, MainBootReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, i, 0);

        if(intent.getAction() != null) {
            if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 8);

                // With setInexactRepeating(), you have to use one of the AlarmManager interval
                // constants--in this case, AlarmManager.INTERVAL_DAY.
                alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, alarmIntent);
            } else {
                PendingIntent notificationIntent = PendingIntent.getActivities(context, 0,
                        new Intent[]{new Intent(context, MainActivity.class)}, 0);

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setContentTitle("GAFP")
                        .setSmallIcon(R.drawable.titlelogo)
                        .setTicker("GAFP notification")
                        .setContentText("Do your task!");


                mBuilder.setContentIntent(notificationIntent);
                mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
                mBuilder.setAutoCancel(true);

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                mNotificationManager.notify(1, mBuilder.build());

            }
        }
    }
}
