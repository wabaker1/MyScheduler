package exe.phormapps.myscheduler;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * The configuration screen for the {@link SchedulerWidget SchedulerWidget} AppWidget.
 */
public class SchedulerWidgetConfigureActivity extends Activity {

    static ArrayList<Task> taskList = new ArrayList<>();
    ArrayList<String> taskNames = new ArrayList<>();
    Spinner spinner;
    static int selectedItemPos;

    private static final String PREFS_NAME = "exe.phormapps.myscheduler.SchedulerWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    /**
     * Set what happens when add widget button is pressed
     */
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            final Context context = SchedulerWidgetConfigureActivity.this;

            Task widgetTask = taskList.get(selectedItemPos);
            Gson gson = new Gson();
            String taskJson = gson.toJson(widgetTask);

            // When the button is clicked, store the string locally
            saveTitlePref(context, mAppWidgetId, taskJson);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            SchedulerWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public SchedulerWidgetConfigureActivity() {
        super();
    }

    /**
     * Saves information to sharedprefs
     *
      * @param context
     * @param appWidgetId
     * @param text
     */
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    /**
     * Retrieves information from shared prefs
     *
     * @param context
     * @param appWidgetId
     * @return
     */
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, "");
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    /**
     * Delete information from shared prefs
     * @param context
     * @param appWidgetId
     */
    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    /**
     * Set up configuration page
     * @param icicle
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.scheduler_widget_configure);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        ArrayList<Task> tempTaskList = new ArrayList<>();

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("taskList", "");
        tempTaskList = gson.fromJson(json, new TypeToken<ArrayList<Task>>() {}.getType());

        for(int i = 0; i < tempTaskList.size(); i++) {
            if(tempTaskList.get(i).getTaskDate().after(Calendar.getInstance())
                    && tempTaskList.get(i).getDailyRepeat().isEmpty()) {
                taskList.add(tempTaskList.get(i));
            }
        }

        spinner = (Spinner) findViewById(R.id.spinner);
        if(taskList == null) {
            taskList = new ArrayList<>();
            Toast.makeText(this,"Make tasks in the app to track them here!", Toast.LENGTH_LONG);
        } else {
            for(int i = 0; i < taskList.size(); i++) {
                taskNames.add(taskList.get(i).getTaskName());
            }
        }
        ArrayAdapter<String> arrAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, taskNames);
        arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar cal = Calendar.getInstance();
                selectedItemPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}

