package exe.phormapps.myscheduler;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Alarm for updating widgets
 *
 * Created by bakerwyatt19 on 7/7/2018.
 */

public class UpdateWidget extends BroadcastReceiver {

    /**
     * Update the widget that this alarm belongs to
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        //retrieve widget information from intent
        Bundle args = intent.getBundleExtra("DATA");
        final Task widgetTask = (Task) args.getSerializable("task");
        final int appWidgetId = (int) args.getSerializable("widgetid");
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scheduler_widget);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                long millisUntilFinished = 0;
                millisUntilFinished = widgetTask.getTaskDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();

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
                    if(count < 10) {
                        timerDisp.append("0");
                    }
                    timerDisp.append(count).append(":");
                    millisUntilFinished %= DateUtils.MINUTE_IN_MILLIS;
                } else {
                    timerDisp.append("00:");
                }
                if(millisUntilFinished > 1000) {
                    long count = millisUntilFinished / DateUtils.SECOND_IN_MILLIS;
                    if(count < 10) {
                        timerDisp.append("0");
                    }
                    timerDisp.append(count);
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

                Handler handler = new Handler();
                handler.postDelayed(this, 1000);
            }
        };
        new Handler().postDelayed(runnable, 1000);
    }
}
