package exe.phormapps.myscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import static exe.phormapps.myscheduler.SchedulerWidget.updateAppWidget;

/**
 * On boot start alarms of the tasks
 *
 * Created by bakerwyatt19 on 7/11/2018.
 */

public class bootStartAlarms extends BroadcastReceiver {

    int alarmReqCode = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            ArrayList<Task> taskList;
            SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
            Gson gson = new Gson();
            String json = appSharedPrefs.getString("taskList", "");
            taskList = gson.fromJson(json, new TypeToken<ArrayList<Task>>() {}.getType());

            alarmReqCode = appSharedPrefs.getInt("requestCode", -1);

            if(taskList == null) {
                taskList = new ArrayList<>();
            }

            for(int i = 0; i < taskList.size(); i++) {
                if(taskList.get(i).isReminder() && taskList.get(i).getTaskDate().after(Calendar.getInstance())) {
                    setAlarm(taskList.get(i), context);
                }
            }
        }
    }

    public void setAlarm(Task task, Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TaskAlarm.class);
        Bundle args = new Bundle();
        args.putSerializable("task", (Serializable)task);
        intent.putExtra("DATA", args);

        Toast.makeText(context, task.getTaskName(), Toast.LENGTH_LONG);

        //If any days are selected for repeating, set them for repeat
        if(!task.getDailyRepeat().isEmpty()) {
            for (int repeatDay : task.getDailyRepeat()) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmReqCode, intent, 0);

                alarmReqCode++;

                if (task.getTaskDate().get(Calendar.DAY_OF_WEEK) > repeatDay) {

                    task.getTaskDate().add(Calendar.DATE, 7 - task.getTaskDate().get(Calendar.DAY_OF_WEEK + repeatDay));
                } else {

                    task.getTaskDate().add(Calendar.DATE, repeatDay - task.getTaskDate().get(Calendar.DAY_OF_WEEK));
                }
                alarmManager.setRepeating(AlarmManager.RTC, task.getTaskDate().getTimeInMillis(), 1000 * 60 * 60 * 24 * 7, pendingIntent);
            }
        }
        else {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, alarmReqCode, intent, 0);

            alarmReqCode++;
            alarmManager.set(AlarmManager.RTC, task.getTaskDate().getTimeInMillis(), pendingIntent);
        }
    }
}
