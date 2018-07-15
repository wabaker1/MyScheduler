package exe.phormapps.myscheduler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Sets up how the user will be notified of a task coming up
 *
 * Created by bakerwyatt19 on 6/21/2018.
 */

public class TaskAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle args = intent.getBundleExtra("DATA");
        Task taskInfo = (Task) args.getSerializable("task");

        Intent notificationIntent = new Intent(context, NavigationActivity.class);

        NotificationManager notificationManager
                = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        String channelID = "alarm_channel";
        CharSequence channelName = "alarm";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        //Makes a local notification to send to the user
        NotificationChannel notificationChannel = new NotificationChannel(channelID, channelName, importance);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.enableVibration(true);
        notificationChannel.setVibrationPattern(new long[] {100, 200, 300, 400, 500});

        notificationManager.createNotificationChannel(notificationChannel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "alarm_channel");
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setContentTitle(taskInfo.getTaskName());
        builder.setContentText(taskInfo.getTaskDesc());
        builder.setContentIntent(pIntent);
        builder.setSound(uri);
        builder.setSmallIcon(android.R.drawable.ic_menu_my_calendar);
        builder.setAutoCancel(true);

        notificationManager.notify(0, builder.build());
    }
}