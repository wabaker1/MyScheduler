package exe.phormapps.myscheduler;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Stack;

import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.vo.DateData;


public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    //Variables uses for repeating task
    String[] daysList;
    boolean[] checkedDays;
    ArrayList<Integer> selectedDays = new ArrayList<>();

    //Variables for going back pages
    Stack<String> backStack = new Stack();
    String currentPage = "tasks";

    //List of all tasks in the system
    ArrayList<Task> taskList = new ArrayList<>();
    ArrayList<Task> calendarTaskList = new ArrayList<>();

    //List of tasks for task page and calendar page
    ListView taskView;
    ListView calListView;

    //Used to fill the lists for task and calendar pages.
    ArrayAdapter<String> taskListAdapter;
    ArrayAdapter<String> calListAdapter;

    //Valid task entry verification
    boolean reqDate = false;
    boolean reqTime = false;
    boolean editing = false;
    boolean itemSelected = false;

    //Currently selected task in listview
    int pos = 0;

    //Current alarm request code to ensure a new one is used for each alarm
    int alarmReqCode;

    //CalendarView for calendar page
    MCalendarView calendarView;

    //newTaskDate for next task to be added
    Calendar newTaskDate = Calendar.getInstance();
    Calendar currentDate = Calendar.getInstance();

    /**
     * Sets initial page upon launch and initializes starting variables
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(R.id.nav_tasksViewLayout);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.contentFrame, new TaskFragment()).commit();

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        Gson gson = new Gson();
        String json = appSharedPrefs.getString("taskList", "");
        taskList = gson.fromJson(json, new TypeToken<ArrayList<Task>>() {}.getType());

        alarmReqCode = appSharedPrefs.getInt("requestCode", -1);

        if(taskList == null) {
            taskList = new ArrayList<>();
        }

        daysList = getResources().getStringArray(R.array.daysToRepeat);
        checkedDays = new boolean[daysList.length];
    }

    /**
     * Swaps to a new navigation fragment
     *
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fragmentManager = getFragmentManager();

        backStack.push(currentPage);
        if(id == R.id.nav_tasksViewLayout) {
            currentPage = "tasks";
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new TaskFragment()).commit();
        } else if (id == R.id.nav_calendarLayout) {
            currentPage = "calendar";
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new CalendarFragment()).commit();
            unMarkDates(calendarView, currentDate);
        } else if (id == R.id.nav_informationLayout) {
            currentPage = "information";
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new InformationFragment()).commit();
        }
        if(id == R.id.nav_tasksViewLayout) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new TaskFragment()).commit();
        } else if (id == R.id.nav_calendarLayout) {
            unMarkDates(calendarView, currentDate);
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new CalendarFragment()).commit();
        } else if (id == R.id.nav_informationLayout) {
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new InformationFragment()).commit();
        }
        itemSelected = false;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Switch to previous page user was on, or out of application if pressed on the first page.
     */
    @Override
    public void onBackPressed() {

        editing = false;
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!backStack.isEmpty()){
            FragmentManager fragmentManager = getFragmentManager();
            String backPage = backStack.pop();
            if(backPage.equals("tasks")) {
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new TaskFragment()).commit();
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(0).setChecked(true);
                currentPage = "tasks";
            } else if(backPage.equals("calendar")) {
                unMarkDates(calendarView, currentDate);
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(1).setChecked(true);
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new CalendarFragment()).commit();
                currentPage = "calendar";
            } else if(backPage.equals("information")) {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                navigationView.getMenu().getItem(2).setChecked(true);
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new InformationFragment()).commit();
                currentPage = "information";
            }
            itemSelected = false;

        } else{
            super.onBackPressed();
        }
    }

    /**
     * Pass-through for canceling adding a task
     *
     * @param v
     */
    public void onCancelPressed(View v) {

        selectedDays = new ArrayList<>();
        checkedDays = new boolean[daysList.length];
        onBackPressed();
    }

    /**
     * Brings up the options menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    /**
     * Goes to fragment for adding a new task.
     *
     * @param v
     */
    public void onAddTaskPressed(View v) {

        FragmentManager fragmentManager = getFragmentManager();

        backStack.push(currentPage);
        fragmentManager.beginTransaction().replace(R.id.contentFrame, new AddTaskFragment()).commit();
    }

    /**
     * User clicks on calendar to select day for the task, brings up datepicker
     *
     * @param v
     */
    public void onSelectDate(View v) {

        android.support.v4.app.DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), "date picker");
    }

    /**
     * User clicks on clock to select time for the task, brings up timepicker
     *
     * @param v
     */
    public void onSelectTime(View v) {

        DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(), "time picker");
    }

    /**
     * Saves a task to the system
     *
     * @param v
     */
    public void onSaveTaskPressed(View v) {

        //If editting, these will be filled out already
        if(editing) {
            taskList.remove(pos);
            updateListView(taskView);
            editing = false;
            reqTime = true;
            reqDate = true;
        }

        //Set up new task
        Calendar tempTaskDate = Calendar.getInstance();
        TextView titleView = (TextView) findViewById(R.id.taskTitleTV);
        TextView descView = (TextView) findViewById(R.id.taskDescription);
        Switch remindSwitch = (Switch) findViewById(R.id.taskRemindToggle);
        String title = titleView.getText().toString();
        String desc = descView.getText().toString();
        boolean remind = remindSwitch.isChecked();
        if(reqDate
                && reqTime
                && !titleView.getText().toString().equals("")
                && tempTaskDate.before(newTaskDate)) {
            reqDate = false;
            reqTime = false;

            tempTaskDate.set(newTaskDate.get(Calendar.YEAR)
                    , newTaskDate.get(Calendar.MONTH)
                    , newTaskDate.get(Calendar.DAY_OF_MONTH)
                    , newTaskDate.get(Calendar.HOUR_OF_DAY)
                    , newTaskDate.get(Calendar.MINUTE)
                    , 0);

            Task tempTask = new Task(tempTaskDate, title, desc, selectedDays, remind);
            tempTask.addRequestCode(alarmReqCode);
            if(tempTask.isReminder()) {
                setAlarm(tempTask);
                Toast.makeText(this, "Alarm Set", Toast.LENGTH_LONG).show();
            }
            System.out.println(tempTask.getDailyRepeat().size() + " " + tempTask.getDailyRepeat().toString());
            taskList.add(tempTask);
            SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
            SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();

            Gson gson = new Gson();
            String taskJson = gson.toJson(taskList);
            prefsEditor.putString("taskList", taskJson);

            prefsEditor.putInt("requestCode", alarmReqCode);

            prefsEditor.commit();

            selectedDays = new ArrayList<>();
            checkedDays = new boolean[daysList.length];

            onBackPressed();
        } else {
            //Make sure all necessary fields are entered correctly
            String missingFields = "Encountered the problems:";

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Cannot make task!");
            if(!reqDate) {
                missingFields = missingFields + " [date missing]";
            }
            if(titleView.getText().toString().equals("")) {
                missingFields = missingFields + " [title missing]";
            }
            if(!reqTime) {
                missingFields = missingFields + " [time missing]";
            }
            if(tempTaskDate.after(newTaskDate)) {
                missingFields = missingFields + " [invalid date]";
            }
            builder.setMessage(missingFields);
            builder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * Sets up editing a task
     *
     * @param view
     */
    public void onEditPress(View view) {

        if(itemSelected) {
            editing = true;
            backStack.push(currentPage);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new AddTaskFragment()).commit();
            itemSelected = false;
        }
    }

    /**
     * Fill all elements for editing a task
     *
     * @param titleView
     * @param descView
     * @param remindSwitch
     * @param dateDisplay
     * @param timeDisplay
     */
    public void editingFill(TextView titleView, TextView descView, Switch remindSwitch, TextView dateDisplay, TextView timeDisplay) {

        Task editTask;
        if(!taskList.isEmpty() && editing) {
            editTask = taskList.get(pos);
            if(editing) {
                titleView.setText(editTask.getTaskName());
                descView.setText(editTask.getTaskDesc());
                remindSwitch.setChecked(editTask.isReminder());
                newTaskDate = Calendar.getInstance();
                newTaskDate.set(editTask.getTaskDate().get(Calendar.YEAR)
                        , editTask.getTaskDate().get(Calendar.MONTH)
                        , editTask.getTaskDate().get(Calendar.DAY_OF_MONTH)
                        , editTask.getTaskDate().get(Calendar.HOUR_OF_DAY)
                        , editTask.getTaskDate().get(Calendar.MINUTE)
                        , 0);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, ''yy");
                dateDisplay.setText(dateFormat.format(newTaskDate.getTime()));
                dateFormat = new SimpleDateFormat("h:mm a");
                timeDisplay.setText(dateFormat.format(newTaskDate.getTime()));

                selectedDays = editTask.getDailyRepeat();
                for(int i = 0; i < selectedDays.size(); i++) {
                    checkedDays[selectedDays.get(i)] = true;
                }
            }
        }

    }

    /**
     * Sets an alarm for the task
     *
     * @param task
     */
    public void setAlarm(Task task) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, TaskAlarm.class);
        Bundle args = new Bundle();
        args.putSerializable("task", (Serializable)task);
        intent.putExtra("DATA", args);

        //If any days are selected for repeating, set them for repeat
        if(!task.getDailyRepeat().isEmpty()) {
            for (int repeatDay : task.getDailyRepeat()) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmReqCode, intent, 0);

                alarmReqCode++;
                Calendar alarmRepeat = Calendar.getInstance();
                alarmRepeat.set(task.getTaskDate().get(Calendar.YEAR)
                        , task.getTaskDate().get(Calendar.MONTH)
                        , task.getTaskDate().get(Calendar.DAY_OF_MONTH)
                        , task.getTaskDate().get(Calendar.HOUR_OF_DAY)
                        , task.getTaskDate().get(Calendar.MINUTE)
                        , 0);
                if (task.getTaskDate().get(Calendar.DAY_OF_WEEK) > repeatDay) {

                    alarmRepeat.add(Calendar.DATE, 7 - task.getTaskDate().get(Calendar.DAY_OF_WEEK + repeatDay));
                } else {

                    alarmRepeat.add(Calendar.DATE, repeatDay - task.getTaskDate().get(Calendar.DAY_OF_WEEK));
                }
                alarmManager.setRepeating(AlarmManager.RTC, alarmRepeat.getTimeInMillis(), 1000 * 60 * 60 * 24 * 7, pendingIntent);
            }
        }
        else {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmReqCode, intent, 0);

            alarmReqCode++;
            alarmManager.set(AlarmManager.RTC, task.getTaskDate().getTimeInMillis(), pendingIntent);
        }
    }

    /**
     * Clears all task for the selected calendar day
     *
     * @param cal
     */
    public void onClearDay(Calendar cal) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        for(int i = taskList.size() - 1; i >= 0; i--) {
            Calendar tempTaskDate = taskList.get(i).getTaskDate();
            if(tempTaskDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    && tempTaskDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                    && tempTaskDate.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)) {
                if(taskList.get(i).isReminder()) {
                    Intent intent = new Intent(this, TaskAlarm.class);
                    Bundle args = new Bundle();
                    args.putSerializable("task", (Serializable) taskList.get(i));
                    intent.putExtra("DATA", args);

                    ArrayList<Integer> reqCodes = taskList.get(i).getRequestCode();

                    for(int j = 0; j < reqCodes.size(); j++) {
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reqCodes.get(j), intent, 0);
                        alarmManager.cancel(pendingIntent);
                    }
                }
                taskList.remove(i);
            }
        }

        updateCalListView(calListView, cal);

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        prefsEditor.putString("taskList", json);
        prefsEditor.commit();
    }

    /**
     * Sets currently selected list view
     *
     * @param i
     */
    public void setListPos(int i) {

        pos = i;
        itemSelected = true;
    }

    /**
     * Deletes the selected item from list view
     *
     * @param view
     */
    public void onDeletePress(View view){

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        itemSelected = false;

        if(taskList.get(pos).isReminder()) {
            Intent intent = new Intent(this, TaskAlarm.class);
            Bundle args = new Bundle();
            args.putSerializable("task", (Serializable) taskList.get(pos));
            intent.putExtra("DATA", args);

            ArrayList<Integer> reqCodes = taskList.get(pos).getRequestCode();

            for(int i = 0; i < reqCodes.size(); i++) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reqCodes.get(i), intent, 0);
                alarmManager.cancel(pendingIntent);
            }
        }

        taskList.remove(pos);
        updateListView(taskView);

        SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(taskList);
        prefsEditor.putString("taskList", json);
        prefsEditor.commit();
    }

    /**
     * Sets up dialog for selecting repeating days
     *
     * @param v
     */
    public void onSelectRepeat(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.repeat_days_title);
        builder.setMultiChoiceItems(daysList, checkedDays, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int pos, boolean isChecked) {
                if(isChecked){
                    if(!selectedDays.contains(pos)) {
                        selectedDays.add(pos);

                    }
                } else {
                    selectedDays.remove(new Integer(pos));
                }
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Sets date of task
     *
     * @param view
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        TextView dateDisplay = (TextView) findViewById(R.id.dateSelectionTB);
        newTaskDate.set(year, month, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, ''yy");
        dateDisplay.setText(dateFormat.format(newTaskDate.getTime()));
        reqDate = true;
    }

    /**
     * Sets time of task
     *
     * @param view
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        TextView timeDisplay = (TextView) findViewById(R.id.timeSelectionTB);
        newTaskDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        newTaskDate.set(Calendar.MINUTE, minute);
        newTaskDate.set(Calendar.SECOND, 0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
        timeDisplay.setText(dateFormat.format(newTaskDate.getTime()));
        reqTime = true;
    }

    /**
     * Updates task listview after a task is added/deleted
     *
     * @param taskView
     */
    public void updateListView(ListView taskView) {

        this.taskView = taskView;
        ArrayList<String> taskNameList = new ArrayList<>();
        if(taskList != null || !taskList.isEmpty()) {
            for (int i = 0; i < taskList.size(); i++) {
                taskNameList.add(taskList.get(i).getTaskName()
                        + "\n" + taskList.get(i).getTaskDesc()
                        + "\n" + taskList.get(i).getTaskDate().getTime().toString());
            }
            taskListAdapter = new ArrayAdapter<String>(this, R.layout.listrowlayout, taskNameList);
            taskView.setAdapter(taskListAdapter);
        }
    }

    /**
     * Updates Calendar calendar listview after a task is added/deleted
     *
     * @param calList
     * @param cal
     */
    public void updateCalListView(ListView calList, Calendar cal) {

        calListView = calList;
        calendarTaskList.clear();
        ArrayList<String> taskNameList = new ArrayList<>();
        for(int i = 0; i < taskList.size(); i++) {
            Calendar tempDate = taskList.get(i).getTaskDate();
            if(tempDate.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                    && tempDate.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
                    && tempDate.get(Calendar.DAY_OF_MONTH) == cal.get(Calendar.DAY_OF_MONTH)) {

                taskNameList.add(taskList.get(i).getTaskName()
                        + "\n" + taskList.get(i).getTaskDesc()
                        + "\n" + taskList.get(i).getTaskDate().getTime().toString());
            }
        }
        calListAdapter = new ArrayAdapter<String>(this, R.layout.listrowlayout, taskNameList);
        calListView.setAdapter(calListAdapter);
    }

    /**
     * Unmarks dates on the calendar
     *
     * @param calView
     * @param prev
     */
    public void unMarkDates(MCalendarView calView, Calendar prev) {

        if(calView != null) {

            for (int i = 0; i < taskList.size(); i++) {
                int year = taskList.get(i).getTaskDate().get(Calendar.YEAR);
                int month = taskList.get(i).getTaskDate().get(Calendar.MONTH) + 1;
                int day = taskList.get(i).getTaskDate().get(Calendar.DAY_OF_MONTH);
                calView.unMarkDate(new DateData(year, month, day).setMarkStyle(new MarkStyle(MarkStyle.DOT, Color.RED)));
            }
            calView.unMarkDate(new DateData(prev.get(Calendar.YEAR),
                    prev.get(Calendar.MONTH) + 1,
                    prev.get(Calendar.DAY_OF_MONTH)));
        }
    }

    /**
     * Highlights dates on the calendar
     *
     * @param calView
     * @param cal
     * @param prev
     */
    public void highlightCalDay(MCalendarView calView, Calendar cal, Calendar prev) {

        currentDate = cal;
        calendarView = calView;
        unMarkDates(calView, prev);

        for(int i = 0; i < taskList.size(); i++) {
            int year = taskList.get(i).getTaskDate().get(Calendar.YEAR);
            int month = taskList.get(i).getTaskDate().get(Calendar.MONTH) + 1;
            int day = taskList.get(i).getTaskDate().get(Calendar.DAY_OF_MONTH);
            calView.markDate(new DateData(year, month, day).setMarkStyle(new MarkStyle(MarkStyle.DOT, Color.RED)));
        }
        calView.markDate(
                new DateData(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)).setMarkStyle(
                        new MarkStyle(MarkStyle.DEFAULT, Color.BLUE)
                ));

    }
}