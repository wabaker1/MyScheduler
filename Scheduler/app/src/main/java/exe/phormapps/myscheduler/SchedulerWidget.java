package exe.phormapps.myscheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SchedulerWidgetConfigureActivity SchedulerWidgetConfigureActivity}
 */
public class SchedulerWidget extends AppWidgetProvider {

    /**
     * Set up a new Widget
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager,
                                final int appWidgetId) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Gson gson = new Gson();
        String taskJson = SchedulerWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        if(!taskJson.equals("")) {
            final Task widgetTask = gson.fromJson(taskJson, new TypeToken<Task>() {
            }.getType());

            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scheduler_widget);

            Intent intent = new Intent(context, UpdateWidget.class);
            Bundle args = new Bundle();
            args.putSerializable("task", (Serializable)widgetTask);
            args.putSerializable("widgetid", appWidgetId);
            intent.putExtra("DATA", args);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Calendar currentDay = Calendar.getInstance();
            long millisUntilFinished = 0;
            millisUntilFinished = widgetTask.getTaskDate().getTimeInMillis() - currentDay.getTimeInMillis();

            StringBuilder timerDisp = new StringBuilder();
            timerDisp.setLength(0);

            if (millisUntilFinished > DateUtils.DAY_IN_MILLIS) {
                long count = millisUntilFinished / DateUtils.DAY_IN_MILLIS;
                if (count > 1) {
                    timerDisp.append(count).append(" days ");
                } else {
                    timerDisp.append(count).append(" day ");
                }
                millisUntilFinished %= DateUtils.DAY_IN_MILLIS;
            }
            if(millisUntilFinished > DateUtils.HOUR_IN_MILLIS) {
                long count = millisUntilFinished / DateUtils.HOUR_IN_MILLIS;
                timerDisp.append(count).append(":");
                millisUntilFinished %= DateUtils.HOUR_IN_MILLIS;
            }
            if(millisUntilFinished > DateUtils.MINUTE_IN_MILLIS) {
                long count = millisUntilFinished / DateUtils.MINUTE_IN_MILLIS;
                timerDisp.append(count + 1);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
            String date = sdf.format(widgetTask.getTaskDate().getTime());

            views.setTextViewText(R.id.appwidget_text, timerDisp.toString());
            views.setTextViewText(R.id.widgetTitle, widgetTask.getTaskName());
            views.setTextViewText(R.id.timerDate, date);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

            //set alarm that will update the UI every 30 minute
            alarmManager.setRepeating(AlarmManager.RTC, currentDay.getTimeInMillis() + DateUtils.MINUTE_IN_MILLIS, DateUtils.MINUTE_IN_MILLIS * 30, pendingIntent);
        } else {
            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scheduler_widget);

            views.setTextViewText(R.id.appwidget_text, "Task no longer exists");
            views.setTextViewText(R.id.widgetTitle, "");
            views.setTextViewText(R.id.timerDate, "");
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    /**
     * Delete items used only by deleted widget
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            SchedulerWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Task widgetTask = null;
            Gson gson = new Gson();
            String taskJson = SchedulerWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
            if(!taskJson.equals("")) {
                widgetTask = gson.fromJson(taskJson, new TypeToken<Task>() {
                }.getType());
            }

            Intent intent = new Intent(context, UpdateWidget.class);
            Bundle args = new Bundle();
            args.putSerializable("task", (Serializable)widgetTask);
            args.putSerializable("widgetid", appWidgetId);
            intent.putExtra("DATA", args);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * Update all widgets
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (final int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            Task widgetTask = null;
            Gson gson = new Gson();
            String taskJson = SchedulerWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
            if (!taskJson.equals("")) {
                widgetTask = gson.fromJson(taskJson, new TypeToken<Task>() {
                }.getType());

                Calendar currentDay = Calendar.getInstance();
                long millisUntilFinished = 0;
                millisUntilFinished = widgetTask.getTaskDate().getTimeInMillis() - currentDay.getTimeInMillis();

                final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scheduler_widget);

                //Build time display
                StringBuilder timerDisp = new StringBuilder();
                timerDisp.setLength(0);

                if (millisUntilFinished > DateUtils.DAY_IN_MILLIS) {
                    long count = millisUntilFinished / DateUtils.DAY_IN_MILLIS;
                    if (count > 1) {
                        timerDisp.append(count).append(" days ");
                    } else {
                        timerDisp.append(count).append(" day ");
                    }
                    millisUntilFinished %= DateUtils.DAY_IN_MILLIS;
                }
                if (millisUntilFinished > DateUtils.HOUR_IN_MILLIS) {
                    long count = millisUntilFinished / DateUtils.HOUR_IN_MILLIS;
                    timerDisp.append(count).append(":");
                    millisUntilFinished %= DateUtils.HOUR_IN_MILLIS;
                } else {
                    timerDisp.append("00:");
                }
                if (millisUntilFinished > DateUtils.MINUTE_IN_MILLIS) {
                    long count = millisUntilFinished / DateUtils.MINUTE_IN_MILLIS;
                    timerDisp.append(count + 1);
                } else {
                    timerDisp.append("00");
                }

                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
                String date = sdf.format(widgetTask.getTaskDate().getTime());

                views.setTextViewText(R.id.appwidget_text, timerDisp.toString());
                views.setTextViewText(R.id.widgetTitle, widgetTask.getTaskName());
                views.setTextViewText(R.id.timerDate, date);
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    /**
     * default enable effect
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        int[] widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context.getPackageName(), SchedulerWidget.class.getName()));

        for(int widgetId : widgetIds) {
            updateAppWidget(context, AppWidgetManager.getInstance(context), widgetId);
        }
    }

    /**
     * default disable effect
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

}